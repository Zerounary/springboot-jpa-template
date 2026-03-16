<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { sparkTrainTestApi, type SparkTrainResultDto } from '../api/spark'

const loading = ref(false)
const result = ref<SparkTrainResultDto | null>(null)

const form = reactive({
  testFraction: 0.2 as number | null,
  seed: 1234 as number | null,
  maxIter: 100 as number | null,
})

const canRun = computed(() => !loading.value)

function reset() {
  form.testFraction = 0.2
  form.seed = 1234
  form.maxIter = 100
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
      testFraction: form.testFraction ?? undefined,
      seed: form.seed ?? undefined,
      maxIter: form.maxIter ?? undefined,
    }

    const hasAny = Object.values(req).some((v) => v != null)
    result.value = await sparkTrainTestApi(hasAny ? req : null)
    message.success('训练/测试完成')
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
      <a-form-item label="testFraction">
        <a-input-number v-model:value="form.testFraction" :min="0.01" :max="0.99" :step="0.01" style="width: 160px" />
      </a-form-item>
      <a-form-item label="seed">
        <a-input-number v-model:value="form.seed" style="width: 160px" />
      </a-form-item>
      <a-form-item label="maxIter">
        <a-input-number v-model:value="form.maxIter" :min="1" :max="10000" style="width: 160px" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" :disabled="!canRun" @click="run">开始训练/测试</a-button>
      </a-form-item>
      <a-form-item>
        <a-button :disabled="loading" @click="reset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="接口：POST /api/spark/train-test"
      description="可不传参数；testFraction 为测试集比例（0~1），seed 为随机种子，maxIter 为迭代次数。"
    />

    <a-empty v-if="!result" description="暂无结果，请点击开始训练/测试" />

    <a-descriptions v-else bordered :column="2">
      <a-descriptions-item label="总样本数">{{ result.total }}</a-descriptions-item>
      <a-descriptions-item label="训练集样本数">{{ result.trainCount }}</a-descriptions-item>
      <a-descriptions-item label="测试集样本数">{{ result.testCount }}</a-descriptions-item>
      <a-descriptions-item label="AUC">{{ fmt(result.auc) }}</a-descriptions-item>
      <a-descriptions-item label="Accuracy">{{ fmt(result.accuracy) }}</a-descriptions-item>
    </a-descriptions>
  </a-card>
</template>
