import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type HypertensionFusionDto = {
  id: number
  patientId: number
  userId: string
  age: number
  gender: number
  systolicBp: number
  diastolicBp: number
  bmi: number
  cholesterol: number
  familyHypertension: number
  smoking: number
  dietPreference: number
  hypertensionLabel: number
  lastExamTime?: string
  lastQuestionnaireTime?: string
  createdAt?: string
  updatedAt?: string
}

export type HypertensionFusionSyncRequest = {
  patientId: number
  hypertensionLabel?: number | null
}

export type HypertensionFusionSyncAllResultDto = {
  total: number
  success: number
  failed: number
}

export function fusionPageApi(params: { page: number; size: number; keyword?: string | null }) {
  return request<IPage<HypertensionFusionDto>>(
    http.get<ApiResponse<IPage<HypertensionFusionDto>>>('/api/hypertension-fusion', {
      params: {
        page: params.page,
        size: params.size,
        keyword: params.keyword || undefined,
      },
    }),
  )
}

export function fusionDetailApi(patientId: number) {
  return request<HypertensionFusionDto>(
    http.get<ApiResponse<HypertensionFusionDto>>('/api/hypertension-fusion/detail', {
      params: { patientId },
    }),
  )
}

export function fusionSyncApi(req: HypertensionFusionSyncRequest) {
  return request<HypertensionFusionDto>(http.post<ApiResponse<HypertensionFusionDto>>('/api/hypertension-fusion/sync', req))
}

export function fusionSyncAllApi() {
  return request<HypertensionFusionSyncAllResultDto>(
    http.post<ApiResponse<HypertensionFusionSyncAllResultDto>>('/api/hypertension-fusion/sync-all'),
  )
}
