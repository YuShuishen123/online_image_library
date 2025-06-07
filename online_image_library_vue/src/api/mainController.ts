/* eslint-disable */
import request from '@/request'

/** 健康检查 用于检测服务是否正常运行 GET /health */
export async function health(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 幂等测试 用于检测幂等切面是否生效 POST /idempotent */
export async function idempotentTest(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/idempotent', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 切换用户和管理员 用于切换用户和管理员 GET /switch */
export async function switchUser(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/switch', {
    method: 'GET',
    ...(options || {}),
  })
}
