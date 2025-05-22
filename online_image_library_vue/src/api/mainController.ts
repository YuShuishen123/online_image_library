// @ts-exepect-error

/* eslint-disable */
import request from '@/request'

/** 健康检查 用于检测服务是否正常运行 GET /api/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 切换用户和管理员 用于切换用户和管理员 GET /api/switch */
export async function switchUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/switch', {
    method: 'GET',
    ...(options || {}),
  })
}
