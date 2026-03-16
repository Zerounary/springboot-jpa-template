import { http, request } from './http'
import type { ApiResponse } from './types'

export type SparkTrainRequest = {
  testFraction?: number
  seed?: number
  maxIter?: number
}

export type SparkTrainResultDto = {
  total: number
  trainCount: number
  testCount: number
  auc: number
  accuracy: number
}

export function sparkTrainTestApi(req?: SparkTrainRequest | null) {
  return request<SparkTrainResultDto>(http.post<ApiResponse<SparkTrainResultDto>>('/api/spark/train-test', req ?? null))
}
