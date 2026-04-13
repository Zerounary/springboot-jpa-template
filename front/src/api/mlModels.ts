import { http, request } from './http'
import type { ApiResponse } from './types'

export type MlModelDto = {
  id: number
  modelName: string
  versionTag: string
  algorithm: string
  featureColumns: string
  modelPath: string
  paramJson?: string
  metricJson?: string
  featureImportanceJson?: string
  totalCount?: number
  trainCount?: number
  testCount?: number
  auc?: number
  accuracy?: number
  trainedAt?: string
  isActive?: number
  createdAt?: string
  updatedAt?: string
}

export function mlModelListApi(params?: { activeOnly?: boolean | null }) {
  return request<MlModelDto[]>(
    http.get<ApiResponse<MlModelDto[]>>('/api/ml-models', {
      params: {
        activeOnly: params?.activeOnly ?? undefined,
      },
    }),
  )
}

export function mlModelDetailApi(id: number) {
  return request<MlModelDto>(http.get<ApiResponse<MlModelDto>>(`/api/ml-models/${id}`))
}
