declare namespace API {
  type BaseResponse = {
    code?: number
    data?: Record<string, unknown>
    message?: string
  }

  type BaseResponseString_ = {
    code?: number
    data?: string
    message?: string
  }
}
