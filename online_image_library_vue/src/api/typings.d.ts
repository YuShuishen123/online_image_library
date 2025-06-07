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
    data?: number
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
    id?: number
  }

  type ExpansionTaskRequestFromTheFrontend = {
    pictureId?: number
    parameters?: Parameters
  }

  type getPictureByIdParams = {
    id: number
  }

  type getPictureVoByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id?: number
  }

  type Input = {
    prompt?: string
    function?: string
    baseImageUrl?: string
    maskImageUrl?: string
  }

  type LoginUserVO = {
    id?: number
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
    id?: number
    url?: string
    thumbnailUrl?: string
    name?: string
    spaceId?: number
    introduction?: string
    category?: string
    tags?: string
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    userId?: number
    createTime?: string
    editTime?: string
    updateTime?: string
    isDelete?: number
    reviewStatus?: number
    reviewMessage?: string
    reviewerId?: number
    reviewTime?: string
    originalImageurl?: string
  }

  type PictureEditRequest = {
    id?: number
    name?: string
    introduction?: string
    category?: string
    tags?: string[]
    spaceId?: number
  }

  type PictureQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
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
    userId?: number
    reviewStatus?: number
    reviewMessage?: string
    reviewerId?: number
  }

  type PictureReviewRequest = {
    id?: number
    reviewStatus?: number
    reviewMessage?: string
  }

  type PictureTagCategory = {
    tagList?: string[]
    categoryList?: string[]
  }

  type PictureUpdateRequest = {
    id?: number
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
    id?: number
    name?: string
    spaceId?: number
  }

  type PictureVO = {
    id?: number
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
    userId?: number
    createTime?: string
    editTime?: string
    user?: UserVO
    spaceId?: number
  }

  type queryOutPaintingTaskParams = {
    checkTaskStatusRequest: CheckTaskStatusRequest
  }

  type Space = {
    id?: number
    spaceName?: string
    spaceLevel?: number
    maxSize?: number
    maxCount?: number
    totalSize?: number
    totalCount?: number
    userId?: number
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
    id?: number
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
    id?: number
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
    id?: number
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
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }
}
