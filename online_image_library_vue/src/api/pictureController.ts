/* eslint-disable */
import request from '@/request'

/** 根据id获取图片信息(管理员) 用于管理员获取图片信息功能 GET /picture/admin/get */
export async function getPictureById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePicture>('/picture/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 更新图片信息(管理员端) 用于更新图片功能 POST /picture/admin/update */
export async function updatePictureInfo(
  body: API.PictureUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/picture/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除图片 用于删除图片功能 POST /picture/delete */
export async function deletePicture(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/picture/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新图片信息(用户端) 用于更新图片功能 POST /picture/edit */
export async function editPicture(body: API.PictureEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据id获取图片信息(用户) 用于用户获取图片信息功能 GET /picture/get */
export async function getPictureVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVoByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePictureVO>('/picture/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页获取图片列表(管理员) 用于管理员获取图片列表功能 POST /picture/list/admin/page */
export async function listPicturePage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePicture>('/picture/list/admin/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页获取图片列表(用户) 用于用户获取图片列表功能,普通用户只能查看审核状态为已通过的图片(强制) POST /picture/list/page */
export async function listPictureVoPage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePictureVO>('/picture/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 图片审核接口(仅管理员) 用于图片审核功能 POST /picture/review */
export async function doPictureReview(
  body: API.PictureReviewRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/picture/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取图片标签和分类 用于获取图片标签和分类 GET /picture/tag_category */
export async function listPictureTagCategory(options?: { [key: string]: any }) {
  return request<API.BaseResponsePictureTagCategory>('/picture/tag_category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 从本地上传或更新图片 用于从本地更新或上传图片,限制图片最大为8MB POST /picture/upload */
export async function uploadPicture(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureParams,
  body: {},
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePictureVO>('/picture/upload', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
      pictureUploadRequest: undefined,
      ...params['pictureUploadRequest'],
    },
    data: body,
    ...(options || {}),
  })
}

/** 批量抓取图片 用于批量抓取图片功能 POST /picture/upload/batch */
export async function uploadPictureByBatch(
  body: API.PictureUploadByBatchRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListPictureVO>('/picture/upload/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据url上传图片 用于图片上传功能,限制图片最大为8MB POST /picture/upload/url */
export async function uploadPictureByUrl(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureByUrlParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePictureVO>('/picture/upload/url', {
    method: 'POST',
    params: {
      ...params,
      pictureUploadRequest: undefined,
      ...params['pictureUploadRequest'],
    },
    ...(options || {}),
  })
}

/** 查询空间内所有图片(分页) 用于分页查询当前用户个人空间内的所有图片(仅限自己获取) POST /picture/user/picture */
export async function listSpacePicturePage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePicture>('/picture/user/picture', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 查询当前登陆用户上传的所有图片 */
export async function listUserUploadPicturePage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePicture>('/picture/user/upload/picture', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
