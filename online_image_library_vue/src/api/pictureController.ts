// @ts-exepect-error

/* eslint-disable */
import request from '@/request'

/** 根据id获取图片信息(管理员) 用于管理员获取图片信息功能 GET /api/picture/admin/get */
export async function getPictureByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 更新图片信息(管理员端) 用于更新图片功能 POST /api/picture/admin/update */
export async function updatePictureInfoUsingPost(
  body: API.PictureUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除图片(管理员) 用于删除图片功能 POST /api/picture/delete */
export async function deletePictureUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新图片信息(用户端) 用于更新图片功能 POST /api/picture/edit */
export async function editPictureUsingPost(
  body: API.PictureEditRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据id获取图片信息(用户) 用于用户获取图片信息功能 GET /api/picture/get */
export async function getPictureVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVoByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页获取图片列表(管理员) 用于管理员获取图片列表功能 POST /api/picture/list/admin/page */
export async function listPictureByPageUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/list/admin/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页获取图片列表(用户) 用于用户获取图片列表功能,普通用户只能查看审核状态为已通过的图片(强制) POST /api/picture/list/page */
export async function listPictureVoByPageUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePictureVO_>('/api/picture/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 图片审核接口(仅管理员) 用于图片审核功能 POST /api/picture/review */
export async function doPictureReviewUsingPost(
  body: API.PictureReviewRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取图片标签和分类 用于获取图片标签和分类 GET /api/picture/tag_category */
export async function listPictureTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponse>('/api/picture/tag_category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 图片更新或上传 用于图片更新或上传,限制图片最大为8MB POST /api/picture/upload */
export async function uploadPictureUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureUsingPOSTParams,
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

  return request<API.BaseResponse>('/api/picture/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    ...(options || {}),
  })
}

/** 批量抓取图片 用于批量抓取图片功能 POST /api/picture/upload/batch */
export async function uploadPictureByBatchUsingPost(
  body: API.PictureUploadByBatchRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/upload/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据url上传图片 用于图片上传功能,限制图片最大为8MB POST /api/picture/upload/url */
export async function uploadPictureByUrlUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureByUrlUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse>('/api/picture/upload/url', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
