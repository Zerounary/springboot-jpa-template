<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { sparkTrainApi, type SparkTrainResultDto } from '../api/spark'

const loading = ref(false)
const result = ref<SparkTrainResultDto | null>(null)

const form = reactive({
  modelName: 'Hypertension RF',
  testFraction: 0.2 as number | null,
  seed: 1234 as number | null,
  numTrees: 20 as number | null,
  maxDepth: 5 as number | null,
  numFolds: 3 as number | null,
})

const canRun = computed(() => !loading.value)

function reset() {
  form.modelName = 'Hypertension RF'
  form.testFraction = 0.2
  form.seed = 1234
  form.numTrees = 20
  form.maxDepth = 5
  form.numFolds = 3
  result.value = null
}

async function run() {
  if (form.testFraction != null && (form.testFraction <= 0 || form.testFraction >= 1)) {
    message.warning('testFraction 需在 0~1 之间（且不含 0/1）')
    return
  }

  loading.value = true
  try {
    const req = {
      modelName: form.modelName || undefined,
      testFraction: form.testFraction ?? undefined,
      seed: form.seed ?? undefined,
      numTrees: form.numTrees ?? undefined,
      maxDepth: form.maxDepth ?? undefined,
      numFolds: form.numFolds ?? undefined,
    }

    const hasAny = Object.values(req).some((v) => v != null)
    result.value = await sparkTrainApi(hasAny ? req : null)
    message.success('训练并保存模型成功')
  } finally {
    loading.value = false
  }
}

function fmt(n: number | null | undefined, digits = 4) {
  if (n == null) return '-'
  const v = Number(n)
  if (Number.isNaN(v)) return '-'
  return v.toFixed(digits)
}
</script>

<template>
  <a-card>
    <template #title>Spark 训练/测试</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="modelName">
        <a-input v-model:value="form.modelName" style="width: 180px" />
      </a-form-item>
      <a-form-item label="testFraction">
        <a-input-number v-model:value="form.testFraction" :min="0.01" :max="0.99" :step="0.01" style="width: 160px" />
      </a-form-item>
      <a-form-item label="seed">
        <a-input-number v-model:value="form.seed" style="width: 160px" />
      </a-form-item>
      <a-form-item label="numTrees">
        <a-input-number v-model:value="form.numTrees" :min="1" :max="1000" style="width: 140px" />
      </a-form-item>
      <a-form-item label="maxDepth">
        <a-input-number v-model:value="form.maxDepth" :min="1" :max="50" style="width: 140px" />
      </a-form-item>
      <a-form-item label="numFolds">
        <a-input-number v-model:value="form.numFolds" :min="2" :max="10" style="width: 140px" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" :disabled="!canRun" @click="run">训练并保存模型</a-button>
      </a-form-item>
      <a-form-item>
        <a-button :disabled="loading" @click="reset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="接口：POST /api/spark/train"
      description="训练并保存新的模型版本；会在后端生成 ml_models 记录，并持久化 Spark PipelineModel。"
    />

    <a-empty v-if="!result" description="暂无结果，请点击开始训练/测试" />

    <a-descriptions v-else bordered :column="2">
      <a-descriptions-item label="模型ID">{{ result.modelId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="版本号">{{ result.versionTag || '-' }}</a-descriptions-item>
      <a-descriptions-item label="算法">{{ result.algorithm || '-' }}</a-descriptions-item>
      <a-descriptions-item label="模型路径">{{ result.modelPath || '-' }}</a-descriptions-item>
      <a-descriptions-item label="总样本数">{{ result.total }}</a-descriptions-item>
      <a-descriptions-item label="训练集样本数">{{ result.trainCount }}</a-descriptions-item>
      <a-descriptions-item label="测试集样本数">{{ result.testCount }}</a-descriptions-item>
      <a-descriptions-item label="AUC">{{ fmt(result.auc) }}</a-descriptions-item>
      <a-descriptions-item label="Accuracy">{{ fmt(result.accuracy) }}</a-descriptions-item>
      <a-descriptions-item label="特征重要性" :span="2">{{ result.featureImportanceJson || '-' }}</a-descriptions-item>
    </a-descriptions>
  </a-card>
</template>
