<template>
  <a-layout-content class="home-page">
    <div class="search-area">
      <a-input-search
        v-model:value="searchForm.name"
        placeholder="输入图片名称关键词搜索"
        enter-button="搜索"
        size="large"
        @search="handleSearch"
        class="search-input"
      />
      <div class="filter-options">
        <a-input v-model:value="searchForm.category" placeholder="输入分类" class="filter-input" />
        <a-input
          v-model:value="searchForm.tags"
          placeholder="输入标签 (多个用逗号隔开)"
          class="filter-input"
        />
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>

    <div v-else-if="pictureList.length === 0" class="empty-container">
      <a-empty description="暂无图片" />
    </div>

    <div v-else class="picture-list">
      <a-row :gutter="[24, 24]">
        <a-col
          v-for="picture in pictureList"
          :key="picture.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
        >
          <a-card hoverable class="picture-card" @click="showPreview(picture)">
            <template #cover>
              <div class="image-container">
                <img
                  :src="picture.url"
                  :alt="picture.name"
                  class="picture-image"
                  loading="lazy"
                  @error="handleImageError"
                />
              </div>
            </template>
            <a-card-meta>
              <template #title>
                <div class="picture-title">{{ picture.name }}</div>
              </template>
              <template #description>
                <div class="picture-description">{{ picture.introduction || '暂无描述' }}</div>
                <div v-if="picture.tags && picture.tags.length > 0" class="tags">
                  <a-tag v-for="tag in picture.tags" :key="tag" color="blue">{{ tag }}</a-tag>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div v-if="total > 0" class="pagination">
      <CustomPagination
        :current="searchForm.current"
        :pageSize="searchForm.pageSize"
        :total="total"
        :pageSizeOptions="['4', '8', '12', '24']"
        @change="handlePageChange"
      />
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
                <!-- Removed "取消公开" / "设为公开" button as per request -->
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
  </a-layout-content>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { listPictureVoPage } from '@/api/pictureController'
import { getUserVobyId } from '@/api/userController'
import { message } from 'ant-design-vue'
import CustomPagination from '@/components/CustomPagination.vue' // 引入自定义分页组件
import { DownloadOutlined } from '@ant-design/icons-vue'

// 定义上传者信息接口
interface Uploader {
  avatar?: string
  nickname?: string
}

// 扩展 PictureVO 类型，添加 uploader 属性
interface ExtendedPictureVO extends API.PictureVO {
  uploader?: Uploader
}

// 搜索表单状态
const searchForm = reactive<Omit<API.PictureQueryRequest, 'tags'> & { tags: string }>({
  current: 1,
  pageSize: 12,
  name: '',
  category: '',
  tags: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})

const total = ref(0)
const pictureList = ref<API.PictureVO[]>([])
const loading = ref(false)

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<ExtendedPictureVO | null>(null)

const showPreview = async (picture: ExtendedPictureVO) => {
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true

  // 获取用户信息
  if (picture.userId) {
    try {
      const res = await getUserVobyId({
        id: picture.userId,
      })
      if (res.data?.code === 200 && res.data.data?.records?.[0]) {
        const userInfo = res.data.data.records[0]
        currentPicture.value = {
          ...currentPicture.value,
          uploader: {
            avatar: userInfo.userAvatar || '/public/default-avatar.png',
            nickname: userInfo.userName || '未知用户',
          },
        }
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
  // 增加同样的判断，防止无限循环
  if (img.src.includes('/placeholder.png')) {
    return
  }
  img.src = '/public/placeholder.png' // 设置一个默认图片
}

const handlePreviewImageError = (e: Event) => {
  const img = e.target as HTMLImageElement

  // 1. 获取当前图片的数据
  const pictureData = currentPicture.value

  // 2. 检查此图片是否已经尝试过备用方案，防止死循环
  //    我们用一个自定义的 data-error 属性来标记
  if (img.dataset.error === 'true') {
    // 如果连备用方案都失败了，就直接返回，不再处理
    return
  }

  // 3. 标记此图片已经处理过一次错误
  img.dataset.error = 'true'

  // 4. 尝试加载更高质量的"原图"作为备用方案
  const originalUrl = pictureData?.originalImageurl
  if (originalUrl && img.src !== originalUrl) {
    // 如果存在原图链接，并且当前失败的不是原图链接，就尝试加载原图
    // 注意：这一次我们先不弹出 message.error，给原图一次加载的机会
    img.src = originalUrl
    return // 结束本次函数执行
  }
}

// 新增 handleAvatarError 函数，用于处理头像加载失败
const handleAvatarError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.src.includes('/default-avatar.png')) {
    return
  }
  img.src = '/public/default-avatar.png'
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

const fetchPictureList = async () => {
  if (loading.value) return

  loading.value = true
  try {
    // 处理 tags 参数：将逗号分隔的字符串转为数组，并过滤空字符串
    const tagsArray = searchForm.tags
      .split(',')
      .map((tag: string) => tag.trim()) // 明确 tag 类型
      .filter((tag: string) => tag !== '')

    const res = await listPictureVoPage({
      ...searchForm,
      tags: tagsArray.length > 0 ? tagsArray : undefined, // 如果没有标签则发送 undefined
    })

    if (res.data.code === 200 && res.data) {
      pictureList.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
    } else {
      pictureList.value = []
      total.value = 0
      message.error('获取图片失败')
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
    message.error('获取图片列表失败')
    pictureList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索按钮点击事件
const handleSearch = () => {
  searchForm.current = 1
  fetchPictureList()
}

// 分页组件change事件
const handlePageChange = (current: number, size: number) => {
  searchForm.current = current
  searchForm.pageSize = size
  fetchPictureList()
}

// 格式化文件大小
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

// 格式化时间
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

onMounted(() => {
  fetchPictureList()
})

onUnmounted(() => {
  // 清理资源
  pictureList.value = []
  currentPicture.value = null
  previewImage.value = ''
  previewVisible.value = false
})
</script>

<style scoped>
.home-page {
  padding: 24px;
  max-width: 90%;
  margin: 0 auto;
  color: var(--text-color); /* 统一文字颜色 */
}

.search-area {
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 50%;
  max-width: 600px;
}

.filter-options {
  display: flex;
  gap: 16px;
  width: 50%;
  max-width: 600px;
}

/* 搜索框美化 */
:deep(.ant-input-search .ant-input),
:deep(.ant-input-affix-wrapper .ant-input) {
  background: #ffffff !important; /* 使用纯白色作为背景 */
  border-color: #000000 !important; /* 调整边框颜色为纯黑色 */
  color: #000000 !important; /* 确保文字颜色为纯黑色 */
}

:deep(.ant-input-search .ant-input-group-addon .ant-btn) {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color)) !important;
  border-color: transparent !important;
  color: #fff !important;
}

/* 滤镜输入框样式 */
.filter-input :deep(.ant-input) {
  background: #ffffff !important; /* 统一使用纯白色作为背景 */
  border-color: #000000 !important; /* 统一边框颜色为纯黑色 */
  color: #000000 !important; /* 确保文字颜色为纯黑色 */
}

.picture-list {
  margin-bottom: 32px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px 0;
}

/* 分页器美化 */
/* :deep(.ant-pagination .ant-pagination-item-link),
:deep(.ant-pagination .ant-pagination-item) {
  background: rgba(36, 38, 41, 0.7) !important;
  border-color: rgba(127, 90, 240, 0.3) !important;
  color: var(--text-color) !important;
}

:deep(.ant-pagination .ant-pagination-item-active) {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color)) !important;
  border-color: transparent !important;
  color: #fff !important;
}

:deep(.ant-pagination-options .ant-select-selector) {
  background: rgba(36, 38, 41, 0.7) !important;
  border-color: rgba(127, 90, 240, 0.3) !important;
  color: var(--text-color) !important;
}

:deep(.ant-pagination-total-text) {
  color: var(--text-color);
} */

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  background: rgba(36, 38, 41, 0.7);
  backdrop-filter: blur(10px); /* 毛玻璃 */
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(127, 90, 240, 0.1);
  color: var(--text-color);
}

.picture-card {
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(36, 38, 41, 0.7) !important; /* 半透明深色背景 */
  border: 1px solid rgba(127, 90, 240, 0.2) !important;
  backdrop-filter: blur(10px); /* 毛玻璃 */
}

.picture-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 32px rgba(127, 90, 240, 0.25) !important;
}

.image-container {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.picture-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.picture-card:hover .picture-image {
  transform: scale(1.05);
}

.picture-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color); /* 文字颜色 */
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.picture-description {
  color: var(--subtitle-color); /* 文字颜色 */
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  margin-top: 12px;
}

:deep(.ant-tag) {
  margin-right: 8px;
  margin-bottom: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(127, 90, 240, 0.15) !important; /* 标签背景 */
  border: none !important;
  color: var(--primary-color) !important; /* 标签文字颜色 */
}

/* 移除卡片底部操作区 */
:deep(.ant-card-actions) {
  display: none; /* 隐藏操作区 */
}

:deep(.ant-modal-content) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-modal-body) {
  padding: 0;
}

.preview-modal :deep(.ant-modal-content) {
  background: rgba(36, 38, 41, 0.9) !important; /* 半透明深色背景 */
  backdrop-filter: blur(15px); /* 更强的毛玻璃 */
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 48px rgba(127, 90, 240, 0.35) !important; /* 更强的阴影 */
}

.preview-container {
  display: flex;
  gap: 24px;
  height: 600px;
}

.preview-image-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.preview-image {
  width: 100%;
  height: calc(100% - 40px);
  object-fit: contain;
  border-radius: 8px;
}

.image-info-bar {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--subtitle-color); /* 文字颜色 */
  font-size: 14px;
  background: rgba(36, 38, 41, 0.8); /* 深色背景 */
  border-radius: 0 0 8px 8px;
}

.preview-info-panel {
  width: 300px;
  background: rgba(36, 38, 41, 0.8) !important; /* 半透明深色背景 */
  backdrop-filter: blur(10px); /* 毛玻璃 */
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  border-left: 1px solid rgba(127, 90, 240, 0.2); /* 左侧边框 */
}

.info-header {
  border-bottom: 1px solid rgba(127, 90, 240, 0.2); /* 边框颜色 */
  padding-bottom: 16px;
}

.info-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: var(--text-color); /* 文字颜色 */
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 24px; /* 增加间距 */
}

.info-item {
  gap: 8px; /* 增加间距 */
}

.info-item .label {
  color: var(--subtitle-color);
  font-size: 14px;
  font-weight: 500;
}

.info-item .value {
  color: var(--text-color);
  font-size: 15px;
  line-height: 1.5;
}

.info-item.description {
  margin-top: 12px; /* 增加描述部分的间距 */
}

.info-item.description .value {
  line-height: 1.6;
  color: var(--subtitle-color);
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-tag) {
  margin: 0;
  padding: 2px 8px;
  border-radius: 4px;
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
