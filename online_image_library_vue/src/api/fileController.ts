// @ts-exepect-error
/* eslint-disable */
import request from '@/request'

/** 测试文件下载 用于测试文件下载 GET /api/file/test/download/ */
export async function testDownloadFileUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/file/test/download/', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 测试文件上传 用于测试文件上传 POST /api/file/test/Upload */
export async function testUploadFileUsingPost(
  body: {},
  file?: File,
  options?: { [key: string]: any },
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponse>('/api/file/test/Upload', {
    method: 'POST',
    data: formData,
    ...(options || {}),
  })
}
