<template>
  <div class="picture-container" ref="pictureContainer">
    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>

    <div v-else-if="pictureList.length === 0" class="empty-container">
      <a-empty description="暂无图片" />
    </div>

    <div v-else class="picture-masonry">
      <div
        v-for="picture in pictureList"
        :key="picture.id"
        class="picture-item"
        @click="showPreview(picture)"
      >
        <div v-if="picture.spaceId === '0'" class="public-badge">已公开</div>
        <img
          :src="picture.url"
          :alt="picture.name"
          class="picture-image"
          loading="lazy"
          @error="handleImageError"
        />
        <div class="picture-overlay">
          <div class="overlay-content">
            <span class="picture-title">{{ picture.name }}</span>
            <a-button
              v-if="picture.url"
              class="download-btn"
              type="primary"
              @click.stop="downloadImage(picture.url || '', picture.name || '')"
            >
              <DownOutlined /> 下载
            </a-button>
          </div>
          <div v-if="picture.tags && picture.tags.length > 0" class="tags">
            <a-tag v-for="tag in picture.tags" :key="tag" color="blue">{{ tag }}</a-tag>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showLoadMoreButton" class="load-more-button">
      <a-button type="primary" @click="handleLoadMore">加载更多</a-button>
    </div>

    <div v-else-if="loadingMore" class="loading-more">
      <a-spin tip="加载更多..."></a-spin>
    </div>

    <!-- 自定义图片预览弹窗 -->
    <div v-if="previewVisible" class="custom-modal" :class="{ active: previewVisible }">
      <div class="modal-mask" @click="handlePreviewCancel"></div>
      <div class="modal-content">
        <div class="preview-container">
          <div class="preview-image-container">
            <img
              :src="previewImage"
              class="preview-image"
              loading="lazy"
              @error="handlePreviewImageError"
            />
          </div>
          <div class="preview-info-panel">
            <div class="info-header">
              <h2>{{ currentPicture?.name }}</h2>
              <div class="action-buttons" v-if="currentPicture">
                <a-button
                  type="primary"
                  class="action-btn download-btn"
                  @click="downloadImage(previewImage || '', currentPicture?.name || '')"
                >
                  <template #icon><DownloadOutlined /></template>
                  下载
                </a-button>
                <a-button
                  type="primary"
                  class="action-btn public-btn"
                  :class="{ 'is-public': currentPicture.isPublic }"
                  @click="togglePublicStatus"
                >
                  <template #icon><GlobalOutlined /></template>
                  {{ currentPicture.isPublic ? '取消公开' : '设为公开' }}
                </a-button>
                <a-button type="primary" danger class="action-btn delete-btn" @click="handleDelete">
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </div>
            </div>
            <div class="info-content">
              <div class="info-section uploader-section">
                <h3>上传者</h3>
                <div class="uploader-info">
                  <img
                    :src="currentPicture?.uploader?.avatar || '/public/default-avatar.png'"
                    class="uploader-avatar"
                    alt="Uploader Avatar"
                    @error="handleAvatarError"
                  />
                  <span class="uploader-name">{{
                    currentPicture?.uploader?.nickname || '未知用户'
                  }}</span>
                </div>
              </div>
              <div class="info-section">
                <h3>基本信息</h3>
                <div class="info-item">
                  <span class="label">上传时间</span>
                  <span class="value">{{ formatDate(currentPicture?.createTime || '') }}</span>
                </div>
                <div class="info-item">
                  <span class="label">原图比例</span>
                  <span class="value">{{ currentPicture?.picScale || '未知' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">原图大小</span>
                  <span class="value">{{ formatFileSize(currentPicture?.picSize || 0) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">分辨率</span>
                  <span class="value"
                    >{{ currentPicture?.picWidth }} x {{ currentPicture?.picHeight }}</span
                  >
                </div>
              </div>
              <div class="info-section">
                <h3>图片描述</h3>
                <div class="description-content">
                  {{ currentPicture?.introduction || '暂无描述' }}
                </div>
              </div>
              <div class="info-section">
                <h3>标签</h3>
                <div class="tags-container">
                  <a-tag v-for="tag in currentPicture?.tags" :key="tag" color="blue">{{
                    tag
                  }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
        <button class="modal-close" @click="handlePreviewCancel">
          <span class="close-icon">×</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { listUserUploadPicturePage } from '@/api/pictureController'
import { getUserVobyId } from '@/api/userController'
import { getSpace } from '@/api/spaceController'
import { message, Modal } from 'ant-design-vue'
import {
  DownOutlined,
  DownloadOutlined,
  GlobalOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { editPicture, deletePicture } from '@/api/pictureController'

// 定义上传者信息接口
interface Uploader {
  avatar?: string
  nickname?: string
}

// 定义搜索表单的类型接口
interface SearchFormType extends Omit<API.PictureQueryRequest, 'tags'> {
  tags: string
}

// 扩展 PictureVO 类型，添加 isPublic 和 uploader 属性
interface ExtendedPictureVO extends API.PictureVO {
  isPublic: boolean
  uploader?: Uploader
  uploaderAvatar?: string
  uploaderNickname?: string
}

const pictureContainer = ref<HTMLElement | null>(null)
const searchForm = reactive<SearchFormType>({
  current: 1,
  pageSize: 20,
  name: '',
  category: '',
  tags: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})
const pictureList = ref<ExtendedPictureVO[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const total = ref(0)
let observer: IntersectionObserver | null = null

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<ExtendedPictureVO | null>(null)

// 添加新的响应式变量
const userSpaceId = ref<string>('')
const showLoadMoreButton = ref(false)
const hasMore = ref(true)

// 设置 IntersectionObserver
const setupObserver = () => {
  if (!pictureContainer.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && hasMore.value && !loading.value && !loadingMore.value) {
        fetchPictureList(true)
      }
    },
    { threshold: 0.1 },
  )
  observer.observe(pictureContainer.value)
}

// 滚动监听
const handleScroll = () => {
  if (!pictureContainer.value || loading.value || loadingMore.value || !hasMore.value) return
  const { scrollTop, scrollHeight, clientHeight } = pictureContainer.value
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchPictureList(true)
  }
}

// 获取用户空间ID
const fetchUserSpaceId = async () => {
  try {
    const res = await getSpace()
    if (res.data.code === 200 && res.data.data) {
      userSpaceId.value = res.data.data.id || ''
    }
  } catch (error) {
    console.error('获取用户空间ID失败:', error)
    message.error('获取用户空间信息失败')
  }
}

// 下载图片
const downloadImage = (url: string, name: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = `${name || 'image'}.jpg`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const fetchPictureList = async (append = false) => {
  if (loading.value || loadingMore.value || !hasMore.value) return

  if (!append) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const tagsArray = searchForm.tags
      .split(',')
      .map((tag: string) => tag.trim())
      .filter((tag: string) => tag !== '')

    const res = await listUserUploadPicturePage({
      ...searchForm,
      current: searchForm.current,
      pageSize: searchForm.pageSize,
      tags: tagsArray.length > 0 ? tagsArray : undefined,
    })

    if (res.data.code === 200 && res.data.data) {
      const records = (res.data.data.records || []).map((pic: API.Picture) => {
        const picVO: ExtendedPictureVO = {
          ...pic,
          userId: pic.userId?.toString(),
          tags: pic.tags
            ? (pic.tags as string)
                .split(',')
                .map((tag) => tag.trim())
                .filter((tag: string) => tag !== '')
            : [],
          isPublic: pic.spaceId === '0',
          uploader: {
            avatar: '/public/default-avatar.png',
            nickname: '未知用户',
          },
        }
        return picVO
      }) as ExtendedPictureVO[]

      total.value = res.data.data.total || 0
      if (append) {
        pictureList.value = [...pictureList.value, ...records]
      } else {
        pictureList.value = records
      }

      const loadedCount = pictureList.value.length
      const totalCount = res.data.data.total || 0

      // 判断是否还有更多数据
      hasMore.value = loadedCount < totalCount

      // 如果还有更多数据，增加页码
      if (hasMore.value) {
        searchForm.current = (searchForm.current || 0) + 1
      }
    } else {
      if (!append) {
        pictureList.value = []
      }
      message.error('获取图片失败')
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
    message.error('获取图片列表失败')
    if (!append) {
      pictureList.value = []
    }
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const showPreview = async (picture: ExtendedPictureVO) => {
  console.log('showPreview called with picture:', picture)
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true

  // 获取用户信息
  if (picture.userId) {
    console.log('Fetching user info for userId:', picture.userId)
    try {
      const res = await getUserVobyId({
        id: picture.userId,
      })
      console.log('getUserVobyId response:', res)
      if (res.data?.code === 200 && res.data.data?.records?.[0]) {
        const userInfo = res.data.data.records[0]
        console.log('Fetched userInfo:', userInfo)
        currentPicture.value = {
          ...currentPicture.value,
          uploader: {
            avatar: userInfo.userAvatar || '/public/default-avatar.png',
            nickname: userInfo.userName || '未知用户',
          },
        }
        console.log('Updated currentPicture.value with uploader:', currentPicture.value)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  } else {
    console.log('picture.userId is undefined or null. Cannot fetch user info.')
  }
}

const handlePreviewCancel = () => {
  previewVisible.value = false
  currentPicture.value = null
  previewImage.value = ''
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.src.includes('/placeholder.png')) {
    return
  }
  img.src = '/public/placeholder.png'
}

const handlePreviewImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  const pictureData = currentPicture.value
  if (img.dataset.error === 'true') {
    return
  }
  img.dataset.error = 'true'
  const originalUrl = pictureData?.originalImageurl
  if (originalUrl && img.src !== originalUrl) {
    img.src = originalUrl
    return
  }
  img.src = '/public/placeholder.png'
}

const handleAvatarError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.src.includes('/default-avatar.png')) {
    return
  }
  img.src = '/public/default-avatar.png'
}

const handleLoadMore = () => {
  showLoadMoreButton.value = false
  hasMore.value = true // 重新启用自动加载，以便加载第6页及以后
  // searchForm.current 已经在 fetchPictureList(current === 5) 中被设置为 6
  fetchPictureList(true)
}

const togglePublicStatus = async () => {
  if (!currentPicture.value || !userSpaceId.value) return

  Modal.confirm({
    title: currentPicture.value.isPublic ? '取消公开' : '设为公开',
    content: `确定要${currentPicture.value.isPublic ? '取消公开' : '设为公开'}这张图片吗？`,
    okText: '确定',
    cancelText: '取消',
    async onOk() {
      try {
        // 如果当前是公开的，则设置为私有（使用用户空间ID）
        // 如果当前是私有的，则设置为公开（spaceId为0）
        const newSpaceId = currentPicture.value!.isPublic ? userSpaceId.value : '0'

        const res = await editPicture({
          id: currentPicture.value!.id,
          spaceId: newSpaceId,
        })
        if (res.data.code === 200) {
          // 更新本地状态
          currentPicture.value!.isPublic = !currentPicture.value!.isPublic
          currentPicture.value!.spaceId = newSpaceId

          // 更新列表中的图片状态
          const index = pictureList.value.findIndex((pic) => pic.id === currentPicture.value!.id)
          if (index !== -1) {
            pictureList.value[index].isPublic = currentPicture.value!.isPublic
            pictureList.value[index].spaceId = newSpaceId
          }

          message.success(currentPicture.value!.isPublic ? '已设为公开' : '已取消公开')
        } else {
          message.error(res.data.message || '操作失败')
        }
      } catch (error) {
        console.error('更新公开状态失败:', error)
        message.error('操作失败')
      }
    },
  })
}

const handleDelete = async () => {
  if (!currentPicture.value) return

  Modal.confirm({
    title: '删除图片',
    content: '确定要删除这张图片吗？此操作不可恢复。',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deletePicture({
          id: currentPicture.value!.id,
        })
        if (res.data.code === 200) {
          message.success('删除成功')
          handlePreviewCancel()
          fetchPictureList(false)
        } else {
          message.error(res.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除图片失败:', error)
        message.error('删除失败')
      }
    },
  })
}

onMounted(async () => {
  await fetchUserSpaceId()
  fetchPictureList(false)
  setupObserver()
  if (pictureContainer.value) {
    pictureContainer.value.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  if (observer && pictureContainer.value) {
    observer.unobserve(pictureContainer.value)
  }
  if (pictureContainer.value) {
    pictureContainer.value.removeEventListener('scroll', handleScroll)
  }
  pictureList.value = []
  currentPicture.value = null
  previewImage.value = ''
  previewVisible.value = false
})

const formatFileSize = (size: number) => {
  if (!size) return '未知'
  const mb = size / (1024 * 1024)
  return `${mb.toFixed(2)} MB`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
.picture-container {
  width: 100%;
  padding: 20px;
  min-height: 100vh;
  overflow-y: auto;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.picture-masonry {
  column-count: 2;
  column-gap: 20px;
  width: 70%;
  margin: 0 auto;
}

.picture-item {
  display: inline-block;
  margin: 0 0 20px;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  break-inside: avoid;
  cursor: pointer;
  position: relative;
}

.picture-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.picture-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

.picture-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.picture-item:hover .picture-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.picture-title {
  font-weight: bold;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: Arial, sans-serif;
  color: #ffffff;
}

.download-btn {
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  border: none;
  color: #fff;
  font-size: 12px;
  border-radius: 6px;
  padding: 4px 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition:
    box-shadow 0.3s ease,
    transform 0.3s ease;
}

.download-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 0 15px 3px rgba(127, 90, 240, 0.6);
}

.download-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 70%);
  opacity: 0;
  border-radius: 6px;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.download-btn:hover::after {
  opacity: 1;
}

.download-btn :deep(.anticon) {
  margin-right: 4px;
}

.tags-container {
  display: flex;
  margin-top: 8px;
}

.tag-masonry {
  margin-top: 8px;
}

:deep(.ant-tag) {
  margin-right: 4px;
  margin-bottom: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(127, 90, 240, 0.15) !important;
  border: none !important;
  color: var(--primary-color) !important;
  font-size: 12px;
}

/* 自定义弹窗样式 */
.custom-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition:
    opacity 0.3s ease,
    visibility 0.3s ease;
}

.custom-modal.active {
  opacity: 1;
  visibility: visible;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.85);
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.custom-modal.active .modal-mask {
  opacity: 1;
}

.modal-content {
  position: relative;
  z-index: 2;
  width: 90%;
  max-width: 1200px;
  height: 80vh;
  background: rgba(17, 19, 23, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  transform: scale(0.9);
  transition: transform 0.3s ease;
}

.custom-modal.active .modal-content {
  transform: scale(1);
}

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  color: rgba(120, 120, 120, 0.8);
  font-size: 24px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.modal-close:hover {
  color: #fff;
}

.close-icon {
  display: block;
  line-height: 1;
}

.preview-container {
  display: flex;
  height: 100%;
  background: transparent;
  border-radius: 16px;
  overflow: hidden;
}

.preview-image-container {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: transparent;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.preview-info-panel {
  width: 380px;
  background: rgba(25, 27, 31, 0.7);
  backdrop-filter: blur(10px);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.info-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 20px;
}

.info-header h2 {
  color: #fff;
  font-size: 24px;
  margin-bottom: 16px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 100px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.download-btn {
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  border: none;
}

.public-btn {
  background: linear-gradient(135deg, #2cb67d 0%, #1a8f5c 100%);
  border: none;
}

.public-btn.is-public {
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
}

.delete-btn {
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  border: none;
}

.info-section {
  margin-top: 6px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  padding: 16px;
}

.uploader-section {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.uploader-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.uploader-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.uploader-name {
  color: #fff;
  font-size: 16px;
  font-weight: 500;
}

.info-section h3 {
  color: #fff;
  font-size: 16px;
  margin-bottom: 12px;
  font-weight: 500;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-item .label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.info-item .value {
  color: #fff;
  font-size: 14px;
}

.description-content {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  line-height: 1.6;
  padding: 8px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.ant-tag) {
  margin: 0;
  padding: 4px 8px;
  border-radius: 6px;
  background: rgba(127, 90, 240, 0.4) !important;
  border: none !important;
  color: #7f5af0 !important;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .preview-container {
    flex-direction: column;
    height: auto;
  }

  .preview-info-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }
}

@media (min-width: 768px) {
  .picture-masonry {
    column-count: 3;
  }
}

@media (max-width: 767px) {
  .picture-masonry {
    column-count: 1;
    width: 90%;
  }
}

.public-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: #52c41a;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 1;
}

.load-more-button {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  margin-top: 20px;
}
</style>
