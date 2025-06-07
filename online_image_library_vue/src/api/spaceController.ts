/* eslint-disable */
import request from '@/request'

/** 添加空间 用于添加空间功能 POST /space/add */
export async function addSpace(body: API.SpaceAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseSpace>('/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除空间 用于删除空间功能 POST /space/delete */
export async function deleteSpace(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取当前用户空间信息 用于获取当前用户空间信息功能 POST /space/get */
export async function getSpace(options?: { [key: string]: any }) {
  return request<API.BaseResponseSpace>('/space/get', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 获取各级空间信息 用于获取各级空间信息 GET /space/space */
export async function listSpaceLevel(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevel>('/space/space', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 更新空间参数 用于更新空间参数功能 POST /space/update */
export async function updateSpace(body: API.SpaceUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
