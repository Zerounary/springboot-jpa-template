import { http, request } from './http'
import type { ApiResponse } from './types'

const SPARK_TRAIN_TIMEOUT = 30 * 60 * 1000

export type SparkTrainRequest = {
  modelName?: string
  testFraction?: number
  seed?: number
  maxIter?: number
  numTrees?: number
  maxDepth?: number
  numFolds?: number
}

export type SparkTrainResultDto = {
  total: number
  trainCount: number
  testCount: number
  auc: number
  accuracy: number
  modelId?: number
  versionTag?: string
  algorithm?: string
  modelPath?: string
  featureImportanceJson?: string
}

export function sparkTrainTestApi(req?: SparkTrainRequest | null) {
  return request<SparkTrainResultDto>(http.post<ApiResponse<SparkTrainResultDto>>('/api/spark/train-test', req ?? null, { timeout: SPARK_TRAIN_TIMEOUT }))
}

export function sparkTrainApi(req?: SparkTrainRequest | null) {
  return request<SparkTrainResultDto>(http.post<ApiResponse<SparkTrainResultDto>>('/api/spark/train', req ?? null, { timeout: SPARK_TRAIN_TIMEOUT }))
}
