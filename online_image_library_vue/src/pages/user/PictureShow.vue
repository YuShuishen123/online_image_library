<template>
  <a-layout-content class="home-page">
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>

    <!-- 无图片状态 -->
    <div v-else-if="pictureList.length === 0" class="empty-container">
      <a-empty description="暂无图片" />
    </div>

    <!-- 图片列表 -->
    <div v-else class="picture-masonry">
      <div
        v-for="picture in pictureList"
        :key="picture.id"
        class="picture-item"
        @click="showPreview(picture)"
      >
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
            <div class="picture-description">{{ picture.introduction || '暂无描述' }}</div>
          </div>
          <div v-if="picture.tags && picture.tags.length > 0" class="tags">
            <a-tag v-for="tag in picture.tags" :key="tag" color="blue">{{ tag }}</a-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div v-if="total > 0" class="pagination-container">
      <CustomPagination
        :current="pagination.current"
        :pageSize="pagination.pageSize"
        :total="Number(total)"
        :pageSizeOptions="['4', '8', '12', '24']"
        @change="handlePageChange"
      />
    </div>

    <!-- 图片预览弹窗 -->
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
import { useUploaderStore } from '@/stores/useUploaderStore'
import { message } from 'ant-design-vue'
import CustomPagination from '@/components/CustomPagination.vue'
import { DownloadOutlined } from '@ant-design/icons-vue'

interface Uploader {
  avatar?: string
  nickname?: string
}

interface ExtendedPictureVO extends API.PictureVO {
  uploader?: Uploader
}

// 分页状态
const pagination = reactive({
  current: 1,
  pageSize: 12,
})

const total = ref<number>(0)
const pictureList = ref<API.PictureVO[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<ExtendedPictureVO | null>(null)

const uploaderStore = useUploaderStore()

// 获取图片列表
const fetchPictureList = async () => {
  if (loading.value) return

  loading.value = true
  try {
    const res = await listPictureVoPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
    })

    if (res.data.code === 200 && res.data.data) {
      pictureList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch {
    message.error('获取图片列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = (current: number, size: number) => {
  pagination.current = current
  pagination.pageSize = size
  fetchPictureList()
}

// 显示图片预览
const showPreview = async (picture: ExtendedPictureVO) => {
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true

  if (picture.userId) {
    const uploaderInfo = await uploaderStore.getUploaderInfo(picture.userId)
    currentPicture.value = {
      ...currentPicture.value,
      uploader: uploaderInfo,
    }
  }
}

// 关闭预览弹窗
const handlePreviewCancel = () => {
  previewVisible.value = false
  currentPicture.value = null
  previewImage.value = ''
}

// 处理图片加载错误
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.src.includes('/placeholder.png')) return
  img.src = '/public/placeholder.png'
}

// 处理预览图片加载错误
const handlePreviewImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.dataset.error === 'true') return
  img.dataset.error = 'true'

  const originalUrl = currentPicture.value?.originalImageurl
  if (originalUrl && img.src !== originalUrl) {
    img.src = originalUrl
    return
  }
}

// 处理头像加载错误
const handleAvatarError = (e: Event) => {
  const img = e.target as HTMLImageElement
  if (img.src.includes('/default-avatar.png')) return
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

// 格式化日期
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

// 组件挂载时获取图片列表
onMounted(() => {
  fetchPictureList()
})

// 组件卸载时清理数据
onUnmounted(() => {
  pictureList.value = []
  currentPicture.value = null
  previewImage.value = ''
  previewVisible.value = false
})
</script>

<style scoped>
.home-page {
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.picture-masonry {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  width: 70%;
}

.picture-item {
  display: flex;
  flex-direction: column;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  cursor: pointer;
  position: relative;
  background: transparent;
}

.picture-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.picture-image {
  width: 100%;
  height: 100%;
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
  padding: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.picture-item:hover .picture-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.picture-title {
  font-weight: bold;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #ffffff;
}

.picture-description {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tags {
  margin-top: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .picture-masonry {
    column-count: 3;
  }
}

@media (max-width: 767px) {
  .picture-masonry {
    column-count: 2;
  }
}

@media (max-width: 480px) {
  .picture-masonry {
    column-count: 1;
  }
}
</style>
