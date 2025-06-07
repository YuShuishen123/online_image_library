/* eslint-disable */
import request from '@/request'

/** 创建扩图任务 创建扩图任务,参数填写说明文档https://bailian.console.aliyun.com/?utm_content=m_1000400275&tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2796845.html&renderType=iframe: POST /pictureAiService/pictureAiService/createOutPaintingTask */
export async function createOutPaintingTask(
  body: API.ExpansionTaskRequestFromTheFrontend,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCreateTaskResponse>(
    '/pictureAiService/pictureAiService/createOutPaintingTask',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  )
}

/** 创建文生图任务 创建文生图任务,参数填写说明文档https://bailian.console.aliyun.com/console?tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2862677.html POST /pictureAiService/pictureAiService/createTextToImageTask */
export async function createTextToImageTask(
  body: API.Text2ImageRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCreateTaskResponse>(
    '/pictureAiService/pictureAiService/createTextToImageTask',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  )
}

/** 创建通用图片编辑任务 创建通用图片编辑任务,参数填写说明文档https://bailian.console.aliyun.com/?utm_content=m_1000400275&tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2796845.html&renderType=iframe: POST /pictureAiService/pictureAiService/createUniversalImageTask */
export async function createUniversalImageTask(
  body: API.UniversalImageEditingRequestBody,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCreateTaskResponse>(
    '/pictureAiService/pictureAiService/createUniversalImageTask',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  )
}

/** 查询任务 查询任务 GET /pictureAiService/pictureAiService/queryOutPaintingTask */
export async function queryOutPaintingTask(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryOutPaintingTaskParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseObject>(
    '/pictureAiService/pictureAiService/queryOutPaintingTask',
    {
      method: 'GET',
      params: {
        ...params,
        checkTaskStatusRequest: undefined,
        ...params['checkTaskStatusRequest'],
      },
      ...(options || {}),
    },
  )
}
