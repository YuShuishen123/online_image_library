<template>
  <div class="picture-container" ref="pictureContainer">
    <!-- 添加返回顶部按钮 -->
    <div class="back-to-top" :class="{ visible: showBackToTop }" @click="scrollToTop">
      <UpOutlined />
    </div>
    <!-- 添加空间信息展示区域 -->
    <div class="space-info-container">
      <div class="space-info-card">
        <div class="space-header">
          <h2 class="space-name">{{ spaceInfo.spaceName || '我的空间' }}</h2>
          <div class="space-level">
            <span class="level-badge">Lv.{{ spaceInfo.spaceLevel || 1 }}</span>
          </div>
        </div>
        <div class="space-stats">
          <div class="stat-item">
            <span class="stat-label"> <i class="icon-image"></i> 图片数量 </span>
            <div class="stat-value">
              {{ spaceInfo.totalCount || 0 }}
              <span class="stat-unit">/{{ spaceInfo.maxCount || 0 }}</span>
            </div>
            <div class="stat-progress">
              <div
                class="stat-progress-bar"
                :style="{
                  width: `${Math.min((Number(spaceInfo.totalCount) / Number(spaceInfo.maxCount)) * 100, 100)}%`,
                }"
              ></div>
            </div>
            <div class="stat-details">
              <span
                >已使用
                {{
                  ((Number(spaceInfo.totalCount) / Number(spaceInfo.maxCount)) * 100).toFixed(1)
                }}%</span
              >
              >
              <span>剩余 {{ (spaceInfo.maxCount || 0) - (spaceInfo.totalCount || 0) }}</span>
            </div>
          </div>
          <div class="stat-item">
            <span class="stat-label"> <i class="icon-storage"></i> 存储空间 </span>
            <div class="stat-value">
              {{ formatFileSize(Number(spaceInfo.totalSize) || 0) }}
              <span class="stat-unit">/{{ formatFileSize(Number(spaceInfo.maxSize) || 0) }}</span>
            </div>
            <div class="stat-progress">
              <div
                class="stat-progress-bar"
                :style="{
                  width: `${Math.min((Number(spaceInfo.totalSize) / Number(spaceInfo.maxSize)) * 100, 100)}%`,
                }"
              ></div>
            </div>
            <div class="stat-details">
              <span
                >已使用
                {{
                  ((Number(spaceInfo.totalSize) / Number(spaceInfo.maxSize)) * 100).toFixed(1)
                }}%</span
              >
              <span
                >剩余
                {{ formatFileSize(Number(spaceInfo.maxSize) - Number(spaceInfo.totalSize)) }}</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>

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
import { getSpace } from '@/api/spaceController'
import { useUploaderStore } from '@/stores/useUploaderStore'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  DownOutlined,
  DownloadOutlined,
  GlobalOutlined,
  DeleteOutlined,
  UpOutlined,
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
const isRequesting = ref(false)

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<ExtendedPictureVO | null>(null)

// 添加新的响应式变量
const userSpaceId = ref<string>('')
const showLoadMoreButton = ref(false)
const hasMore = ref(true)

// 添加空间信息相关的响应式变量
interface SpaceInfo {
  id: string
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

const spaceInfo = ref<SpaceInfo>({
  id: '',
  spaceName: '我的空间',
  spaceLevel: 1,
  maxSize: 1024 * 1024 * 100, // 默认100MB
  maxCount: 100,
  totalSize: 0,
  totalCount: 0,
  userId: '',
  createTime: '',
  editTime: '',
  updateTime: '',
  isDelete: 0,
})

// 添加返回顶部相关变量
const showBackToTop = ref(false)

const router = useRouter()

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 在路由切换前滚动到顶部
  window.scrollTo(0, 0)
  next()
})

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
  // 检查是否需要显示返回顶部按钮
  showBackToTop.value = window.scrollY > 300

  if (!pictureContainer.value || loading.value || loadingMore.value || !hasMore.value) return
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchPictureList(true)
  }
}

// 获取用户空间ID
const fetchUserSpaceId = async () => {
  try {
    const res = await getSpace()
    if (res.data.code === 200 && res.data.data) {
      userSpaceId.value = res.data.data?.id || ''
      spaceInfo.value = {
        ...spaceInfo.value,
        ...res.data.data,
        id: res.data.data?.id || '',
        spaceName: res.data.data?.spaceName || '我的空间',
        spaceLevel: res.data.data?.spaceLevel || 1,
        maxSize: res.data.data?.maxSize || 1024 * 1024 * 100,
        maxCount: res.data.data?.maxCount || 100,
        totalSize: res.data.data?.totalSize || 0,
        totalCount: res.data.data?.totalCount || 0,
      }
    }
  } catch (error) {
    console.error('获取用户空间信息失败:', error)
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
  if (loading.value || loadingMore.value || !hasMore.value || isRequesting.value) return

  if (!append) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  isRequesting.value = true

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

      hasMore.value = loadedCount < totalCount && records.length === searchForm.pageSize

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
    isRequesting.value = false
  }
}

const uploaderStore = useUploaderStore()

const showPreview = async (picture: ExtendedPictureVO) => {
  console.log('showPreview called with picture:', picture)
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true

  // 获取用户信息
  if (picture.userId) {
    const uploaderInfo = await uploaderStore.getUploaderInfo(picture.userId)
    currentPicture.value = {
      ...currentPicture.value,
      uploader: uploaderInfo,
    }
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
  hasMore.value = true
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
        const newSpaceId = currentPicture.value!.isPublic ? userSpaceId.value : '0'

        const res = await editPicture({
          id: currentPicture.value!.id,
          spaceId: newSpaceId,
        })
        if (res.data.code === 200) {
          currentPicture.value!.isPublic = !currentPicture.value!.isPublic
          currentPicture.value!.spaceId = newSpaceId

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

// 添加返回顶部函数
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}

onMounted(async () => {
  // 确保页面加载时滚动到顶部
  window.scrollTo(0, 0)
  await fetchUserSpaceId()
  fetchPictureList(false)
  setupObserver()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  if (observer && pictureContainer.value) {
    observer.unobserve(pictureContainer.value)
  }
  window.removeEventListener('scroll', handleScroll)
  // 确保组件卸载时滚动到顶部
  window.scrollTo(0, 0)
  pictureList.value = []
  currentPicture.value = null
  previewImage.value = ''
  previewVisible.value = false
})

const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let fileSize = size

  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }

  return `${fileSize.toFixed(2)} ${units[index]}`
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

/* 添加空间信息相关样式 */
.space-info-container {
  width: 70%;
  margin: 0 auto 30px;
  padding: 20px;
}

.space-info-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.space-info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
}

.space-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.space-name {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.space-level {
  display: flex;
  align-items: center;
}

.level-badge {
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(127, 90, 240, 0.3);
}

.space-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.stat-item {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(0, 0, 0, 0.3);
  transform: translateY(-3px);
}

.stat-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 8px 0;
}

.stat-progress {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  margin-top: 12px;
  overflow: hidden;
}

.stat-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #7f5af0 0%, #a217b4 100%);
  border-radius: 2px;
  transition: width 0.6s ease;
}

.stat-details {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

/* 添加返回顶部按钮样式 */
.back-to-top {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 1000;
}

.back-to-top.visible {
  opacity: 1;
  visibility: visible;
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.back-to-top :deep(.anticon) {
  color: white;
  font-size: 20px;
}
</style>
