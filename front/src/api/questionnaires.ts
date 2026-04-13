import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type QuestionnaireDto = {
  id: number
  patientId: number
  questionnaireTime: string
  smoking: number
  drinking: number
  dietPreference: number
  exerciseFrequency: number
  workRest?: number
  stressLevel?: number
  habitRemark?: string
  createdAt?: string
  updatedAt?: string
}

export type QuestionnaireCreateRequest = {
  patientId: number
  questionnaireTime: string
  smoking: number
  drinking: number
  dietPreference: number
  exerciseFrequency: number
  workRest?: number
  stressLevel?: number
  habitRemark?: string
}

export type QuestionnaireUpdateRequest = {
  questionnaireTime: string
  smoking: number
  drinking: number
  dietPreference: number
  exerciseFrequency: number
  workRest?: number
  stressLevel?: number
  habitRemark?: string
}

export function questionnairePageApi(params: {
  page: number
  size: number
  patientId?: number | null
  startTime?: string | null
  endTime?: string | null
}) {
  return request<IPage<QuestionnaireDto>>(
    http.get<ApiResponse<IPage<QuestionnaireDto>>>('/api/questionnaires', {
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

export function questionnaireDetailApi(id: number) {
  return request<QuestionnaireDto>(http.get<ApiResponse<QuestionnaireDto>>(`/api/questionnaires/${id}`))
}

export function questionnaireCreateApi(req: QuestionnaireCreateRequest) {
  return request<QuestionnaireDto>(http.post<ApiResponse<QuestionnaireDto>>('/api/questionnaires', req))
}

export function questionnaireUpdateApi(id: number, req: QuestionnaireUpdateRequest) {
  return request<QuestionnaireDto>(http.put<ApiResponse<QuestionnaireDto>>(`/api/questionnaires/${id}`, req))
}

export function questionnaireDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/questionnaires/${id}`))
}
