// @ts-exepect-error
/* eslint-disable */
import request from '@/request'

/** 管理员添加用户 用于添加用户功能 POST /api/user/add */
export async function addUserUsingPost(body: API.UserAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员删除用户 用于删除用户功能 POST /api/user/delete */
export async function deleteUserUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员获取用户信息(未脱敏) 用于获取用户信息 GET /api/user/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取当前登陆用户信息 用于获取当前登陆用户信息 GET /api/user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/get/login', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 查询其他用户信息(列表) 用于获取用户信息,返回的是一个列表 POST /api/user/get/vo */
export async function getUserVobyIdUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/get/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户登陆 用于用户登陆功能 POST /api/user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户登出 用于用户登出功能 POST /api/user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 用户注册 用于用户注册功能 POST /api/user/register */
export async function userRegisterUsingPost(
  body: API.UserRegistRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员更新用户 用于更新用户功能 POST /api/user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
