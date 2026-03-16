export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export type IPage<T> = {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
