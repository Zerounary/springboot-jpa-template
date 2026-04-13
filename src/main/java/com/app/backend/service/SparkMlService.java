package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.SparkTrainRequest;
import com.app.backend.dto.SparkTrainResultDto;
import com.app.backend.entity.MlModel;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.OneHotEncoder;
import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.spark.sql.functions.col;

@Service
public class SparkMlService {

    private static final String[] NUMERIC_FEATURES = new String[]{"age", "bmi", "systolic_bp", "diastolic_bp", "cholesterol"};
    private static final String[] FINAL_FEATURES = new String[]{"scaled_num_features", "diet_vec", "smoking", "family_hypertension"};
    private static final long MIN_TOTAL_ROWS = 5L;
    private static final long MIN_ROWS_FOR_CV = 12L;

    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;
    private final String jdbcDriver;
    private final String modelRootPath;
    private final String hadoopHome;
    private final MlModelService mlModelService;
    private final Map<Long, PipelineModel> modelCache = new ConcurrentHashMap<>();
    private volatile boolean windowsNativeLibInitialized = false;

    public SparkMlService(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String jdbcUsername,
            @Value("${spring.datasource.password}") String jdbcPassword,
            @Value("${spring.datasource.driver-class-name}") String jdbcDriver,
            @Value("${app.spark.model-root-path:./data/spark-models}") String modelRootPath,
            @Value("${app.spark.hadoop-home:}") String hadoopHome,
            MlModelService mlModelService
    ) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
        this.jdbcDriver = jdbcDriver;
        this.modelRootPath = modelRootPath;
        this.hadoopHome = hadoopHome;
        this.mlModelService = mlModelService;
    }

    public SparkTrainResultDto trainTest(SparkTrainRequest req) {
        return trainInternal(req, false);
    }

    public SparkTrainResultDto trainAndSave(SparkTrainRequest req) {
        return trainInternal(req, true);
    }

    public MlModel resolveModel(Long modelId) {
        return modelId != null ? mlModelService.getById(modelId) : mlModelService.getLatestActive();
    }

    public double predictProbabilityByPatientId(Long patientId, Long modelId) {
        if (patientId == null) {
            throw new BizException(400, "patientId不能为空");
        }
        MlModel modelMeta = resolveModel(modelId);
        PipelineModel model = getOrLoadModel(modelMeta);
        SparkSession spark = createSpark("HypertensionPredict");
        try {
            Dataset<Row> patientDf = loadTrainingData(spark).filter(col("patient_id").equalTo(patientId)).limit(1);
            if (patientDf.count() <= 0) {
                throw new BizException(400, "缺少融合数据，请先同步融合数据后再预测");
            }
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

    private SparkTrainResultDto trainInternal(SparkTrainRequest req, boolean saveModel) {
        double testFraction = req != null && req.getTestFraction() != null ? req.getTestFraction() : 0.3d;
        if (testFraction <= 0 || testFraction >= 1) {
            throw new BizException(400, "testFraction必须在(0,1)之间");
        }
        long seed = req != null && req.getSeed() != null ? req.getSeed() : 1234L;
        int numTrees = req != null && req.getNumTrees() != null ? req.getNumTrees() : 20;
        int maxDepth = req != null && req.getMaxDepth() != null ? req.getMaxDepth() : 5;
        int numFolds = req != null && req.getNumFolds() != null ? req.getNumFolds() : 3;
        String modelName = req != null && req.getModelName() != null ? req.getModelName() : "Hypertension RF";

        SparkSession spark = createSpark(saveModel ? "HypertensionTrainSave" : "HypertensionTrainTest");
        try {
            Dataset<Row> df = loadTrainingData(spark);
            long total = df.count();
            if (total < MIN_TOTAL_ROWS) {
                throw new BizException(400, "融合表训练数据过少，至少需要5条");
            }

            boolean useCrossValidation = total >= Math.max(MIN_ROWS_FOR_CV, (long) numFolds * 4L);
            Dataset<Row> train;
            Dataset<Row> test;
            if (useCrossValidation) {
                Dataset<Row>[] splits = df.randomSplit(new double[]{1.0d - testFraction, testFraction}, seed);
                train = splits[0];
                test = splits[1];
                if (train.count() < 4 || test.count() < 1) {
                    useCrossValidation = false;
                    train = df;
                    test = df;
                }
            } else {
                train = df;
                test = df;
            }

            RandomForestClassifier rf = new RandomForestClassifier()
                    .setLabelCol("label")
                    .setFeaturesCol("features")
                    .setNumTrees(numTrees)
                    .setMaxDepth(maxDepth)
                    .setSeed(seed);

            Pipeline pipeline = createPipeline(rf);
            PipelineModel bestModel;
            int effectiveNumFolds = useCrossValidation ? numFolds : 1;
            if (useCrossValidation) {
                CrossValidator cv = new CrossValidator()
                        .setEstimator(pipeline)
                        .setEvaluator(createEvaluator())
                        .setEstimatorParamMaps(new ParamGridBuilder()
                                .addGrid(rf.numTrees(), new int[]{Math.max(10, numTrees), Math.max(20, numTrees * 2)})
                                .addGrid(rf.maxDepth(), new int[]{Math.max(3, maxDepth - 1), Math.max(5, maxDepth)})
                                .build())
                        .setNumFolds(numFolds)
                        .setParallelism(2);
                CrossValidatorModel cvModel = cv.fit(train);
                bestModel = (PipelineModel) cvModel.bestModel();
            } else {
                bestModel = pipeline.fit(train);
            }
            Dataset<Row> predictions = bestModel.transform(test);

            double auc = createEvaluator().evaluate(predictions);
            long testCount = predictions.count();
            double accuracy = 0.0d;
            if (testCount > 0) {
                long correct = predictions.filter(col("prediction").equalTo(col("label"))).count();
                accuracy = (double) correct / (double) testCount;
            }

            SparkTrainResultDto result = new SparkTrainResultDto();
            result.setTotal(total);
            result.setTrainCount(train.count());
            result.setTestCount(testCount);
            result.setAuc(auc);
            result.setAccuracy(accuracy);

            if (saveModel) {
                MlModel saved = persistModel(bestModel, modelName, seed, numTrees, maxDepth, effectiveNumFolds, result);
                result.setModelId(saved.getId());
                result.setVersionTag(saved.getVersionTag());
                result.setAlgorithm(saved.getAlgorithm());
                result.setModelPath(saved.getModelPath());
                result.setFeatureImportanceJson(saved.getFeatureImportanceJson());
            }
            return result;
        } finally {
            spark.stop();
        }
    }

    private SparkSession createSpark(String appName) {
        ensureWindowsHadoopEnv();
        return SparkSession.builder()
                .appName(appName)
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .config("spark.sql.shuffle.partitions", "4")
                .getOrCreate();
    }

    private void ensureWindowsHadoopEnv() {
        String os = System.getProperty("os.name", "");
        if (os == null || !os.toLowerCase().contains("win")) {
            return;
        }

        String home = trimToNull(System.getProperty("hadoop.home.dir"));
        if (home == null) {
            home = trimToNull(System.getenv("HADOOP_HOME"));
        }
        if (home == null) {
            home = trimToNull(hadoopHome);
        }
        if (home == null) {
            throw new BizException(500, "Windows运行Spark需要配置HADOOP_HOME或app.spark.hadoop-home，并提供bin/winutils.exe");
        }

        Path winutils = Paths.get(home, "bin", "winutils.exe");
        if (!Files.exists(winutils)) {
            throw new BizException(500, "未找到winutils.exe，请确认目录存在: " + winutils.toString());
        }

        Path hadoopDll = Paths.get(home, "bin", "hadoop.dll");
        if (!Files.exists(hadoopDll)) {
            throw new BizException(500, "未找到hadoop.dll，请使用与项目 Hadoop 3.3.x 兼容的 Windows native binaries: " + hadoopDll.toString());
        }

        System.setProperty("hadoop.home.dir", home);
        if (!windowsNativeLibInitialized) {
            try {
                System.load(hadoopDll.toAbsolutePath().toString());
                windowsNativeLibInitialized = true;
            } catch (UnsatisfiedLinkError e) {
                String message = e.getMessage();
                if (message != null && message.contains("already loaded")) {
                    windowsNativeLibInitialized = true;
                } else {
                    throw new BizException(500, "加载hadoop.dll失败，请确认本地 Hadoop 原生库版本与项目依赖兼容（建议 3.3.x）: " + e.getMessage());
                }
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Dataset<Row> loadTrainingData(SparkSession spark) {
        Dataset<Row> raw = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "hypertension_fusion")
                .option("user", jdbcUsername)
                .option("password", jdbcPassword)
                .option("driver", jdbcDriver)
                .load();

        return raw
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
                        col("diet_preference").cast("string").alias("diet_preference_str"),
                        col("hypertension_label").cast("double").alias("label")
                )
                .na().drop();
    }

    private Pipeline createPipeline(RandomForestClassifier rf) {
        StringIndexer dietIndexer = new StringIndexer()
                .setInputCol("diet_preference_str")
                .setOutputCol("diet_index")
                .setHandleInvalid("keep");

        OneHotEncoder dietEncoder = new OneHotEncoder()
                .setInputCol("diet_index")
                .setOutputCol("diet_vec")
                .setDropLast(false);

        VectorAssembler assemblerForScaler = new VectorAssembler()
                .setInputCols(NUMERIC_FEATURES)
                .setOutputCol("num_features");

        StandardScaler scaler = new StandardScaler()
                .setInputCol("num_features")
                .setOutputCol("scaled_num_features")
                .setWithMean(true)
                .setWithStd(true);

        VectorAssembler finalAssembler = new VectorAssembler()
                .setInputCols(FINAL_FEATURES)
                .setOutputCol("features");

        return new Pipeline().setStages(new PipelineStage[]{dietIndexer, dietEncoder, assemblerForScaler, scaler, finalAssembler, rf});
    }

    private BinaryClassificationEvaluator createEvaluator() {
        return new BinaryClassificationEvaluator()
                .setLabelCol("label")
                .setRawPredictionCol("rawPrediction")
                .setMetricName("areaUnderROC");
    }

    private MlModel persistModel(
            PipelineModel bestModel,
            String modelName,
            long seed,
            int numTrees,
            int maxDepth,
            int numFolds,
            SparkTrainResultDto result
    ) {
        String versionTag = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        Path modelPath = Paths.get(modelRootPath, versionTag);
        try {
            Files.createDirectories(modelPath.getParent());
            bestModel.write().overwrite().save(modelPath.toString());
        } catch (IOException e) {
            throw new BizException(500, "保存Spark模型失败: " + e.getMessage());
        }

        RandomForestClassificationModel rfModel = (RandomForestClassificationModel) bestModel.stages()[bestModel.stages().length - 1];
        double[] importance = rfModel.featureImportances().toArray();
        String[] names = new String[]{
                "scaled_age", "scaled_bmi", "scaled_systolic_bp", "scaled_diastolic_bp", "scaled_cholesterol",
                "diet_preference_0", "diet_preference_1", "diet_preference_2", "smoking", "family_hypertension"
        };
        StringBuilder featureImportanceJson = new StringBuilder("{");
        for (int i = 0; i < importance.length; i++) {
            if (i > 0) {
                featureImportanceJson.append(',');
            }
            String name = i < names.length ? names[i] : ("feature_" + i);
            featureImportanceJson.append('"').append(name).append('"').append(':')
                    .append(BigDecimal.valueOf(importance[i]).setScale(6, RoundingMode.HALF_UP).toPlainString());
        }
        featureImportanceJson.append('}');

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("seed", seed);
        params.put("numTrees", numTrees);
        params.put("maxDepth", maxDepth);
        params.put("numFolds", numFolds);
        params.put("pipeline", "StringIndexer+OneHotEncoder+StandardScaler+VectorAssembler+RandomForest+CrossValidator");

        Map<String, Object> metrics = new LinkedHashMap<String, Object>();
        metrics.put("auc", result.getAuc());
        metrics.put("accuracy", result.getAccuracy());

        MlModel model = new MlModel();
        model.setModelName(modelName);
        model.setVersionTag(versionTag);
        model.setAlgorithm("RandomForestClassifier");
        model.setFeatureColumns("age,bmi,systolic_bp,diastolic_bp,cholesterol,diet_preference,smoking,family_hypertension");
        model.setModelPath(modelPath.toString());
        model.setParamJson(toJson(params));
        model.setMetricJson(toJson(metrics));
        model.setFeatureImportanceJson(featureImportanceJson.toString());
        model.setTotalCount(result.getTotal());
        model.setTrainCount(result.getTrainCount());
        model.setTestCount(result.getTestCount());
        model.setAuc(BigDecimal.valueOf(result.getAuc()).setScale(6, RoundingMode.HALF_UP));
        model.setAccuracy(BigDecimal.valueOf(result.getAccuracy()).setScale(6, RoundingMode.HALF_UP));
        model.setTrainedAt(LocalDateTime.now());
        MlModel saved = mlModelService.save(model);
        modelCache.put(saved.getId(), bestModel);
        return saved;
    }

    private PipelineModel getOrLoadModel(MlModel meta) {
        PipelineModel cached = modelCache.get(meta.getId());
        if (cached != null) {
            return cached;
        }
        try {
            PipelineModel loaded = PipelineModel.load(meta.getModelPath());
            modelCache.put(meta.getId(), loaded);
            return loaded;
        } catch (Exception e) {
            throw new BizException(500, "加载模型失败: " + e.getMessage());
        }
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(entry.getKey()).append('"').append(':');
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean) {
                sb.append(String.valueOf(value));
            } else {
                sb.append('"').append(String.valueOf(value)).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
