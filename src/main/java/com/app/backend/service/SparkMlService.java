package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.SparkTrainRequest;
import com.app.backend.dto.SparkTrainResultDto;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.spark.sql.functions.col;

@Service
public class SparkMlService {

    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;
    private final String jdbcDriver;

    public SparkMlService(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String jdbcUsername,
            @Value("${spring.datasource.password}") String jdbcPassword,
            @Value("${spring.datasource.driver-class-name}") String jdbcDriver
    ) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
        this.jdbcDriver = jdbcDriver;
    }

    public SparkTrainResultDto trainTest(SparkTrainRequest req) {
        double testFraction = req != null && req.getTestFraction() != null ? req.getTestFraction() : 0.2d;
        if (testFraction <= 0 || testFraction >= 1) {
            throw new BizException(400, "testFraction必须在(0,1)之间");
        }
        long seed = req != null && req.getSeed() != null ? req.getSeed() : 1234L;
        int maxIter = req != null && req.getMaxIter() != null ? req.getMaxIter() : 100;

        SparkSession spark = SparkSession.builder()
                .appName("HypertensionTrainTest")
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .config("spark.sql.shuffle.partitions", "4")
                .getOrCreate();

        try {
            Dataset<Row> raw = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "hypertension_fusion")
                    .option("user", jdbcUsername)
                    .option("password", jdbcPassword)
                    .option("driver", jdbcDriver)
                    .load();

            Dataset<Row> df = raw
                    .select(
                            col("age").cast("double").alias("age"),
                            col("gender").cast("double").alias("gender"),
                            col("systolic_bp").cast("double").alias("systolic_bp"),
                            col("diastolic_bp").cast("double").alias("diastolic_bp"),
                            col("bmi").cast("double").alias("bmi"),
                            col("cholesterol").cast("double").alias("cholesterol"),
                            col("family_hypertension").cast("double").alias("family_hypertension"),
                            col("smoking").cast("double").alias("smoking"),
                            col("diet_preference").cast("double").alias("diet_preference"),
                            col("hypertension_label").cast("double").alias("label")
                    )
                    .na().drop();

            long total = df.count();
            if (total < 5) {
                throw new BizException(400, "融合表训练数据过少，至少需要5条");
            }

            Dataset<Row>[] splits = df.randomSplit(new double[]{1.0d - testFraction, testFraction}, seed);
            Dataset<Row> train = splits[0];
            Dataset<Row> test = splits[1];

            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{
                            "age",
                            "gender",
                            "systolic_bp",
                            "diastolic_bp",
                            "bmi",
                            "cholesterol",
                            "family_hypertension",
                            "smoking",
                            "diet_preference"
                    })
                    .setOutputCol("features");

            LogisticRegression lr = new LogisticRegression()
                    .setLabelCol("label")
                    .setFeaturesCol("features")
                    .setMaxIter(maxIter);

            Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{assembler, lr});
            PipelineModel model = pipeline.fit(train);

            Dataset<Row> pred = model.transform(test);

            BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
                    .setLabelCol("label")
                    .setRawPredictionCol("rawPrediction")
                    .setMetricName("areaUnderROC");

            double auc = evaluator.evaluate(pred);
            long testCount = pred.count();
            double accuracy = 0.0d;
            if (testCount > 0) {
                long correct = pred.filter(col("prediction").equalTo(col("label"))).count();
                accuracy = (double) correct / (double) testCount;
            }

            SparkTrainResultDto result = new SparkTrainResultDto();
            result.setTotal(total);
            result.setTrainCount(train.count());
            result.setTestCount(testCount);
            result.setAuc(auc);
            result.setAccuracy(accuracy);
            return result;
        } finally {
            spark.stop();
        }
    }

    public double predictProbabilityByPatientId(Long patientId) {
        if (patientId == null) {
            throw new BizException(400, "patientId不能为空");
        }

        SparkSession spark = SparkSession.builder()
                .appName("HypertensionPredict")
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .config("spark.sql.shuffle.partitions", "4")
                .getOrCreate();

        try {
            Dataset<Row> raw = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "hypertension_fusion")
                    .option("user", jdbcUsername)
                    .option("password", jdbcPassword)
                    .option("driver", jdbcDriver)
                    .load();

            Dataset<Row> df = raw
                    .select(
                            col("patient_id").cast("long").alias("patient_id"),
                            col("age").cast("double").alias("age"),
                            col("gender").cast("double").alias("gender"),
                            col("systolic_bp").cast("double").alias("systolic_bp"),
                            col("diastolic_bp").cast("double").alias("diastolic_bp"),
                            col("bmi").cast("double").alias("bmi"),
                            col("cholesterol").cast("double").alias("cholesterol"),
                            col("family_hypertension").cast("double").alias("family_hypertension"),
                            col("smoking").cast("double").alias("smoking"),
                            col("diet_preference").cast("double").alias("diet_preference"),
                            col("hypertension_label").cast("double").alias("label")
                    )
                    .na().drop();

            long total = df.count();
            if (total < 5) {
                throw new BizException(400, "融合表训练数据过少，至少需要5条");
            }

            Dataset<Row> patientDf = df.filter(col("patient_id").equalTo(patientId)).limit(1);
            if (patientDf.count() <= 0) {
                throw new BizException(400, "缺少融合数据，请先同步融合数据后再预测");
            }

            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{
                            "age",
                            "gender",
                            "systolic_bp",
                            "diastolic_bp",
                            "bmi",
                            "cholesterol",
                            "family_hypertension",
                            "smoking",
                            "diet_preference"
                    })
                    .setOutputCol("features");

            LogisticRegression lr = new LogisticRegression()
                    .setLabelCol("label")
                    .setFeaturesCol("features")
                    .setMaxIter(100);

            Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{assembler, lr});
            PipelineModel model = pipeline.fit(df);

            Row r = model.transform(patientDf).select(col("probability")).first();
            if (r == null) {
                throw new BizException(500, "Spark预测失败");
            }
            Vector prob = r.getAs(0);
            if (prob == null || prob.size() < 2) {
                throw new BizException(500, "Spark预测概率无效");
            }
            return prob.apply(1);
        } finally {
            spark.stop();
        }
    }
}
