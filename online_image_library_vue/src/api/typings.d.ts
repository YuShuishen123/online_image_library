declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    message?: string
    data?: boolean
  }

  type BaseResponseCreateTaskResponse = {
    code?: number
    message?: string
    data?: CreateTaskResponse
  }

  type BaseResponseListPictureVO = {
    code?: number
    message?: string
    data?: PictureVO[]
  }

  type BaseResponseListSpaceLevel = {
    code?: number
    message?: string
    data?: SpaceLevel[]
  }

  type BaseResponseLoginUserVO = {
    code?: number
    message?: string
    data?: LoginUserVO
  }

  type BaseResponseLong = {
    code?: number
    message?: string
    data?: string
  }

  type BaseResponseObject = {
    code?: number
    message?: string
    data?: Record<string, unknown>
  }

  type BaseResponsePagePicture = {
    code?: number
    message?: string
    data?: PagePicture
  }

  type BaseResponsePagePictureVO = {
    code?: number
    message?: string
    data?: PagePictureVO
  }

  type BaseResponsePageUserVO = {
    code?: number
    message?: string
    data?: PageUserVO
  }

  type BaseResponsePicture = {
    code?: number
    message?: string
    data?: Picture
  }

  type BaseResponsePictureTagCategory = {
    code?: number
    message?: string
    data?: PictureTagCategory
  }

  type BaseResponsePictureVO = {
    code?: number
    message?: string
    data?: PictureVO
  }

  type BaseResponseSpace = {
    code?: number
    message?: string
    data?: Space
  }

  type BaseResponseString = {
    code?: number
    message?: string
    data?: string
  }

  type BaseResponseUser = {
    code?: number
    message?: string
    data?: User
  }

  type CheckTaskStatusRequest = {
    taskId?: string
    taskType?: number
  }

  type CreateTaskResponse = {
    output?: Output
    code?: string
    message?: string
    requestId?: string
  }

  type DeleteRequest = {
    id?: string
  }

  type ExpansionTaskRequestFromTheFrontend = {
    pictureId?: string
    parameters?: Parameters
  }

  type getPictureByIdParams = {
    id: string
  }

  type getPictureVoByIdParams = {
    id: string
  }

  type getUserByIdParams = {
    id?: string
  }

  type Input = {
    prompt?: string
    function?: string
    baseImageUrl?: string
    maskImageUrl?: string
  }

  type LoginUserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type Output = {
    taskId?: string
    taskStatus?: string
  }

  type PagePicture = {
    records?: Picture[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePicture
    searchCount?: PagePicture
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PagePictureVO = {
    records?: PictureVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePictureVO
    searchCount?: PagePictureVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type Parameters = {
    watermark?: boolean
    n?: number
    seed?: number
    strength?: number
    topScale?: number
    bottomScale?: number
    leftScale?: number
    rightScale?: number
    isSketch?: boolean
    upscaleFactor?: number
  }

  type Picture = {
    id?: string
    url?: string
    thumbnailUrl?: string
    name?: string
    spaceId?: string
    introduction?: string
    category?: string
    tags?: string
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    userId?: string
    createTime?: string
    editTime?: string
    updateTime?: string
    isDelete?: number
    reviewStatus?: number
    reviewMessage?: string
    reviewerId?: string
    reviewTime?: string
    originalImageurl?: string
  }

  type PictureEditRequest = {
    id?: string
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    spaceId?: string
  }

  type PictureQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    searchText?: string
    userId?: string
    reviewStatus?: number
    reviewMessage?: string
    reviewerId?: string
  }

  type PictureReviewRequest = {
    id?: string
    reviewStatus?: number
    reviewMessage?: string
  }

  type PictureTagCategory = {
    tagList?: string[]
    categoryList?: string[]
  }

  type PictureUpdateRequest = {
    id?: string
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
  }

  type PictureUploadByBatchRequest = {
    searchText?: string
    count?: number
  }

  type PictureUploadRequest = {
    id?: string
    name?: string
    spaceId?: string
  }

  type PictureVO = {
    id?: string
    url?: string
    thumbnailUrl?: string
    originalImageurl?: string
    name?: string
    introduction?: string
    tags?: string[]
    category?: string
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    userId?: string
    createTime?: string
    editTime?: string
    user?: UserVO
    spaceId?: string
  }

  type queryOutPaintingTaskParams = {
    checkTaskStatusRequest: CheckTaskStatusRequest
  }

  type Space = {
    id?: string
    spaceName?: string
    spaceLevel?: number
    maxSize?: number
    maxCount?: number
    totalSize?: number
    totalCount?: number
    userId?: string
    createTime?: string
    editTime?: string
    updateTime?: string
    isDelete?: number
  }

  type SpaceAddRequest = {
    spaceName?: string
    spaceLevel?: number
  }

  type SpaceLevel = {
    value?: number
    text?: string
    maxCount?: number
    maxSize?: number
  }

  type SpaceUpdateRequest = {
    id?: string
    spaceName?: string
    spaceLevel?: number
    maxSize?: number
    maxCount?: number
  }

  type testDownloadFileParams = {
    filepath: string
  }

  type Text2ImageRequest = {
    model?: string
    input?: Input
    parameters?: Parameters
  }

  type UniversalImageEditingRequestBody = {
    model?: string
    input?: Input
    parameters?: Parameters
  }

  type uploadPictureByUrlParams = {
    pictureUploadRequest: PictureUploadRequest
    fileurl: string
  }

  type uploadPictureParams = {
    pictureUploadRequest: PictureUploadRequest
  }

  type User = {
    id?: string
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegistRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }
}
