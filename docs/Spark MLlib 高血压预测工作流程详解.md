# Spark MLlib 高血压预测工作流程详解

我将以**结构化数据集+分步操作流程**的形式，完整展示Spark MLlib在该系统中的核心工作流程（数据准备→训练→验证→预测），所有步骤均贴合课题的融合模型设计（随机森林+逻辑回归Stacking），并明确训练成功的判定标准。

### 一、基础准备：标准化数据集样例（贴合课题多源数据）

先定义系统中用于Spark MLlib训练/预测的**标准化数据集结构**（已整合电子健康档案、体检、问卷数据），以下是可直接导入Spark的结构化样例（CSV格式）：

|user_id (脱敏)|age|gender (0=女/1=男)|systolic_bp (mmHg)|diastolic_bp (mmHg)|bmi|cholesterol (mmol/L)|family_hypertension (0/1)|smoking (0/1/2)|diet_preference (0=低盐/1=正常/2=高盐)|hypertension (标签 0=无/1=有)|
|---|---|---|---|---|---|---|---|---|---|---|
|U1001|55|1|145|95|28.5|6.2|1|2|2|1|
|U1002|42|0|120|80|22.1|4.8|0|0|0|0|
|U1003|68|1|155|100|30.2|7.5|1|2|2|1|
|U1004|35|0|130|85|24.8|5.5|1|0|1|0|
|U1005|72|1|160|105|29.0|7.0|0|2|2|1|
### 二、Spark MLlib 核心工作流程（代码+数据集操作）

#### 步骤1：数据加载与预处理（解决课题“多源数据质量差”问题）

```Python

from pyspark.sql import SparkSession
from pyspark.ml.feature import *
from pyspark.ml.classification import LogisticRegression, RandomForestClassifier
from pyspark.ml.evaluation import BinaryClassificationEvaluator
from pyspark.ml.tuning import CrossValidator, ParamGridBuilder
from pyspark.ml import Pipeline, PipelineModel

# 1. 初始化Spark环境
spark = SparkSession.builder.appName("HypertensionPrediction").master("local[*]").getOrCreate()

# 2. 加载标准化数据集（已整合多源数据）
# 实际场景：从数据湖读取整合后的Parquet/CSV文件
df = spark.read.csv("hypertension_standard_data.csv", header=True, inferSchema=True)

# 3. 数据预处理（清洗+特征编码，贴合课题数据治理）
## 3.1 缺失值填充（用中位数填充数值特征，众数填充类别特征）
from pyspark.sql.functions import col, mean, median, mode
# 数值特征填充
num_features = ["age", "systolic_bp", "diastolic_bp", "bmi", "cholesterol"]
for feat in num_features:
    median_val = df.select(median(col(feat))).collect()[0][0]
    df = df.fillna({feat: median_val})
# 类别特征填充
cat_features = ["family_hypertension", "smoking", "diet_preference"]
for feat in cat_features:
    mode_val = df.select(mode(col(feat))).collect()[0][0][0]
    df = df.fillna({feat: mode_val})

## 3.2 特征编码（类别特征转数值，适配模型输入）
# 饮食偏好（多类别）：StringIndexer + OneHotEncoder
diet_indexer = StringIndexer(inputCol="diet_preference", outputCol="diet_index")
diet_encoder = OneHotEncoder(inputCol="diet_index", outputCol="diet_vec")

# 性别/家族史/吸烟（二值/三值）：直接转为数值（已标准化，无需额外编码）

## 3.3 数值特征标准化（消除量纲影响）
num_assembler = VectorAssembler(inputCols=num_features, outputCol="num_features")
scaler = StandardScaler(inputCol="num_features", outputCol="scaled_num_features", withMean=True, withStd=True)

## 3.4 整合所有特征（最终模型输入）
all_features = ["scaled_num_features", "diet_vec", "gender", "family_hypertension", "smoking"]
final_assembler = VectorAssembler(inputCols=all_features, outputCol="features")
```

#### 步骤2：融合模型训练（随机森林+逻辑回归Stacking，贴合课题算法创新）

```Python

# 1. 划分训练集（80%）和测试集（20%）
train_df, test_df = df.randomSplit([0.8, 0.2], seed=1234)

# 2. 阶段1：训练基学习器（随机森林）
rf = RandomForestClassifier(
    featuresCol="features", 
    labelCol="hypertension",
    numTrees=10,  # 初始超参数
    maxDepth=5
)

# 3. 提取随机森林的预测概率作为元特征
## 先训练临时RF模型，生成元特征
rf_pipeline = Pipeline(stages=[diet_indexer, diet_encoder, num_assembler, scaler, final_assembler, rf])
rf_temp_model = rf_pipeline.fit(train_df)

## 对训练集/测试集生成RF预测概率（元特征）
def extract_prob(col):
    return col[1]  # 提取正类（高血压）的预测概率

from pyspark.sql.functions import udf
from pyspark.ml.linalg import Vector
prob_udf = udf(extract_prob, "double")

# 训练集元特征
train_rf_pred = rf_temp_model.transform(train_df)
train_meta = train_rf_pred.withColumn("rf_prob", prob_udf(col("probability"))).select("rf_prob", "hypertension")

# 测试集元特征
test_rf_pred = rf_temp_model.transform(test_df)
test_meta = test_rf_pred.withColumn("rf_prob", prob_udf(col("probability"))).select("rf_prob", "hypertension")

# 4. 阶段2：训练元学习器（逻辑回归，贴合课题可解释性需求）
## 整合元特征为逻辑回归输入
meta_assembler = VectorAssembler(inputCols=["rf_prob"], outputCol="meta_features")
lr = LogisticRegression(
    featuresCol="meta_features", 
    labelCol="hypertension",
    maxIter=100,
    regParam=0.1  # 正则化，防止过拟合
)

## 元学习器流水线
lr_pipeline = Pipeline(stages=[meta_assembler, lr])
lr_model = lr_pipeline.fit(train_meta)

# 5. 超参数调优（可选，提升模型性能）
## 随机森林超参数网格
rf_param_grid = ParamGridBuilder()\
    .addGrid(rf.numTrees, [10, 20])\
    .addGrid(rf.maxDepth, [3, 5])\
    .build()

## 逻辑回归超参数网格
lr_param_grid = ParamGridBuilder()\
    .addGrid(lr.regParam, [0.01, 0.1, 1.0])\
    .addGrid(lr.elasticNetParam, [0.0, 0.5, 1.0])\
    .build()

# （实际场景：用CrossValidator遍历网格，选择最优参数，此处简化）
```

#### 步骤3：训练成功的判定标准（贴合课题高精度+可解释性需求）

训练成功需同时满足**性能指标**和**业务指标**，以下是核心判定标准：

|判定维度|具体指标|合格标准（贴合医疗场景）|说明|
|---|---|---|---|
|模型性能|AUC（ROC曲线下面积）|≥0.85|医疗预测模型核心指标，越高说明区分能力越强|
|模型性能|召回率（Recall）|≥0.80|尽可能不漏诊高风险人群（贴合基层防控）|
|模型性能|精确率（Precision）|≥0.75|降低误判率，减少医疗资源浪费|
|可解释性|特征重要性符合临床常识|收缩压/家族史/高盐饮食为TOP3特征|确保模型结果可被医生理解（课题核心需求）|
|稳定性|测试集/验证集性能偏差|≤5%|避免过拟合，适配基层数据噪声|
**代码验证训练结果**：

```Python

# 1. 验证模型性能
evaluator = BinaryClassificationEvaluator(
    labelCol="hypertension",
    rawPredictionCol="rawPrediction",
    metricName="areaUnderROC"  # AUC
)

# 测试集预测（元学习器）
test_lr_pred = lr_model.transform(test_meta)
auc = evaluator.evaluate(test_lr_pred)
recall = test_lr_pred.filter((col("hypertension") == 1) & (col("prediction") == 1)).count() / test_lr_pred.filter(col("hypertension") == 1).count()
precision = test_lr_pred.filter((col("hypertension") == 1) & (col("prediction") == 1)).count() / test_lr_pred.filter(col("prediction") == 1).count()

# 输出验证结果
print(f"AUC值：{auc:.4f}")
print(f"召回率：{recall:.4f}")
print(f"精确率：{precision:.4f}")

# 2. 验证特征重要性（随机森林）
rf_final_model = rf_temp_model.stages[-1]
feature_names = ["age", "systolic_bp", "diastolic_bp", "bmi", "cholesterol", "diet", "gender", "family_hypertension", "smoking"]
feature_importance = rf_final_model.featureImportances.toArray()
# 按重要性排序，验证TOP3是否符合临床常识
importance_ranking = sorted(zip(feature_names, feature_importance), key=lambda x: x[1], reverse=True)
print("特征重要性排名：", importance_ranking[:3])

# 3. 训练成功后保存模型（供SpringBoot调用）
## 保存随机森林模型
rf_temp_model.write.overwrite().save("./spark_model/rf_base_model")
## 保存逻辑回归模型
lr_model.write.overwrite().save("./spark_model/lr_meta_model")
```

#### 步骤4：新数据计算高血压发病概率（贴合系统实时预测需求）

```Python

# 1. 加载新数据（SpringBoot传入的用户新参数，已标准化）
new_data = spark.createDataFrame([
    # (user_id, age, gender, systolic_bp, diastolic_bp, bmi, cholesterol, family_hypertension, smoking, diet_preference)
    ("U2001", 60, 1, 150, 98, 29.5, 7.2, 1, 2, 2)  # 新用户数据
], schema=df.drop("hypertension").schema)  # 新数据无标签

# 2. 加载训练好的模型
rf_model = PipelineModel.load("./spark_model/rf_base_model")
lr_model = PipelineModel.load("./spark_model/lr_meta_model")

# 3. 步骤1：新数据经随机森林生成元特征（预测概率）
new_rf_pred = rf_model.transform(new_data)
new_meta = new_rf_pred.withColumn("rf_prob", prob_udf(col("probability"))).select("rf_prob")

# 4. 步骤2：逻辑回归计算最终发病概率
new_lr_pred = lr_model.transform(new_meta)

# 5. 提取最终结果（发病概率+风险标签）
result = new_lr_pred.select(
    col("rf_prob").alias("rf预测概率"),
    prob_udf(col("probability")).alias("最终发病概率"),
    col("prediction").alias("风险标签（0=低/1=高）")
).collect()[0]

# 输出结果
print(f"随机森林预测概率：{result['rf预测概率']:.4f}")
print(f"最终高血压发病概率：{result['最终发病概率']:.4f}")
print(f"风险标签：{'高风险' if result['风险标签（0=低/1=高）'] == 1 else '低风险'}")
```

### 三、核心流程总结

|阶段|Spark MLlib核心操作|输入数据|输出结果|
|---|---|---|---|
|数据预处理|缺失值填充、类别编码、数值标准化、特征整合|多源异构原始数据|标准化特征向量数据集|
|模型训练|随机森林（基学习器）+ 逻辑回归（元学习器）Stacking|带标签的训练集|训练好的融合模型+超参数|
|训练验证|AUC/召回率/精确率计算、特征重要性验证|测试集|模型性能指标（判定是否训练成功）|
|新数据预测|加载模型→生成元特征→逻辑回归计算最终概率|无标签的新用户数据|高血压发病概率+风险标签|
### 关键点回顾

1. Spark MLlib的核心工作是**将整合后的多源医疗数据转为标准化特征**，通过两阶段Stacking模型（随机森林+逻辑回归）实现“高精度+可解释性”的预测；

2. 训练成功的核心是**AUC≥0.85、召回率≥0.80**，且特征重要性符合临床常识；

3. 新数据预测需先经随机森林生成元特征，再通过逻辑回归校准，最终输出可直接对接SpringBoot系统的发病概率。

如果需要将这套流程封装为**Spark MLlib预测接口**（供SpringBoot调用），我可以补充对应的代码示例。
> （注：文档部分内容可能由 AI 生成）