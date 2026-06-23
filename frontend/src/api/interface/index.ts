/**
 * 请求响应参数（不包含DATA）
 */
export interface Result {
  code: string
  timestamp: number,
  message: string
}

/**
 * 请求响应参数（包含DATA）
 */
export interface ResultData<T = any> extends Result {
  result: T
}
