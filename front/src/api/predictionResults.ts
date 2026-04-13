import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type PredictionResultDto = {
  id: number
  patientId: number
  predictionTime: string
  predictionProb: number
  predictionLabel: number
  coreRiskFactors: string
  warningStatus: number
  createdAt?: string
  updatedAt?: string
}

export type PredictionResultCreateRequest = {
  patientId: number
  predictionTime: string
  predictionProb: number
  predictionLabel: number
  coreRiskFactors: string
  warningStatus: number
}

export type PredictionResultUpdateRequest = {
  predictionTime: string
  predictionProb: number
  predictionLabel: number
  coreRiskFactors: string
  warningStatus: number
}

export type PredictionGenerateRequest = {
  patientId: number
}

export function predictionResultPageApi(params: {
  page: number
  size: number
  patientId?: number | null
  startTime?: string | null
  endTime?: string | null
}) {
  return request<IPage<PredictionResultDto>>(
    http.get<ApiResponse<IPage<PredictionResultDto>>>('/api/prediction-results', {
      params: {
        page: params.page,
        size: params.size,
        patientId: params.patientId ?? undefined,
        startTime: params.startTime ?? undefined,
        endTime: params.endTime ?? undefined,
      },
    }),
  )
}

export function predictionResultMineApi(params: { page: number; size: number; startTime?: string | null; endTime?: string | null }) {
  return request<IPage<PredictionResultDto>>(
    http.get<ApiResponse<IPage<PredictionResultDto>>>('/api/prediction-results/mine', {
      params: {
        page: params.page,
        size: params.size,
        startTime: params.startTime ?? undefined,
        endTime: params.endTime ?? undefined,
      },
    }),
  )
}

export function predictionResultDetailApi(id: number) {
  return request<PredictionResultDto>(http.get<ApiResponse<PredictionResultDto>>(`/api/prediction-results/${id}`))
}

export function predictionResultGenerateApi(req: PredictionGenerateRequest) {
  return request<PredictionResultDto>(http.post<ApiResponse<PredictionResultDto>>('/api/prediction-results/generate', req))
}

export function predictionResultCreateApi(req: PredictionResultCreateRequest) {
  return request<PredictionResultDto>(http.post<ApiResponse<PredictionResultDto>>('/api/prediction-results', req))
}

export function predictionResultUpdateApi(id: number, req: PredictionResultUpdateRequest) {
  return request<PredictionResultDto>(http.put<ApiResponse<PredictionResultDto>>(`/api/prediction-results/${id}`, req))
}

export function predictionResultDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/prediction-results/${id}`))
}
