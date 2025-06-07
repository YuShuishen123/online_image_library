import request from '@/request'

/** 测试文件下载 用于测试文件下载 GET /file/test/download/ */
export async function testDownloadFile(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileParams,
  options?: { [key: string]: unknown },
) {
  return request<unknown>('/file/test/download/', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 测试文件上传 用于测试文件上传 POST /file/test/Upload */
export async function testUploadFile(body: object, options?: { [key: string]: unknown }) {
  return request<API.BaseResponseString>('/file/test/Upload', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
