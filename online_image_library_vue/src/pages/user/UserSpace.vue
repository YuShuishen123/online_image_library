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

    <div v-if="loadingMore" class="loading-more">
      <a-spin tip="加载更多..."></a-spin>
    </div>

    <!-- 图片预览模态框 -->
    <a-modal
      v-model:visible="previewVisible"
      :footer="null"
      :width="1000"
      @cancel="handlePreviewCancel"
      class="preview-modal"
    >
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
            <div class="tags-container">
              <a-tag v-for="tag in currentPicture?.tags" :key="tag" color="blue">{{ tag }}</a-tag>
            </div>
          </div>
          <div class="info-content">
            <div class="info-item">
              <span class="label">上传时间：</span>
              <span class="value">{{ formatDate(currentPicture?.createTime || '') }}</span>
            </div>
            <div class="info-item">
              <span class="label">原图比例：</span>
              <span class="value">{{ currentPicture?.picScale || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">原图大小：</span>
              <span class="value">{{ formatFileSize(currentPicture?.picSize || 0) }}</span>
            </div>
            <div class="info-item">
              <span class="label">分辨率：</span>
              <span class="value"
                >{{ currentPicture?.picWidth }} x {{ currentPicture?.picHeight }}</span
              >
            </div>
            <div class="info-item description">
              <span class="label">图片描述：</span>
              <span class="value">{{ currentPicture?.introduction || '暂无描述' }}</span>
            </div>
            <a-button
              class="download-btn-modal"
              type="primary"
              @click.stop="downloadImage(previewImage || '', currentPicture?.name || '')"
            >
              <DownOutlined /> 下载
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, watch } from 'vue'
import { listSpacePicturePage } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'

// 定义搜索表单的类型接口
interface SearchFormType extends Omit<API.PictureQueryRequest, 'tags'> {
  tags: string
}

const pictureContainer = ref<HTMLElement | null>(null)
const searchForm = reactive<SearchFormType>({
  current: 1,
  pageSize: 20, // 初始加载20张图片
  name: '',
  category: '',
  tags: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})
const pictureList = ref<API.PictureVO[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const total = ref(0)

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<API.PictureVO | null>(null)

// 下载图片
const downloadImage = (url: string, name: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = `${name || 'image'}.jpg` // 默认扩展名可根据需要调整
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const fetchPictureList = async (append = false) => {
  if (loading.value || loadingMore.value) return

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

    const res = await listSpacePicturePage({
      ...searchForm,
      current: searchForm.current,
      pageSize: searchForm.pageSize,
      tags: tagsArray.length > 0 ? tagsArray : undefined,
    })
    if (res.data.code === 200 && res.data.data) {
      const records = (res.data.data.records || []).map((pic: API.Picture) => {
        const picVO: API.PictureVO = {
          ...pic,
          tags: pic.tags
            ? (pic.tags as string)
                .split(',')
                .map((tag) => tag.trim())
                .filter((tag) => tag !== '')
            : [],
        }
        return picVO
      }) as API.PictureVO[]

      total.value = res.data.data.total || 0
      if (append) {
        pictureList.value = [...pictureList.value, ...records]
      } else {
        pictureList.value = records
      }
      searchForm.current++
    } else {
      if (!append) {
        pictureList.value = []
      }
      message.error('获取图片失败')
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
    message.error('获取图片列表失败')
    if (!append) {
      pictureList.value = []
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const showPreview = (picture: API.PictureVO) => {
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true
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

const handleScroll = () => {
  if (!pictureContainer.value) return
  const { scrollTop, clientHeight, scrollHeight } = pictureContainer.value
  if (
    scrollHeight - scrollTop - clientHeight < 200 &&
    !loadingMore.value &&
    total.value > pictureList.value.length
  ) {
    fetchPictureList(true)
  }
}

watch(
  () => pictureContainer.value,
  (newVal) => {
    if (newVal) {
      newVal.addEventListener('scroll', handleScroll)
    }
  },
)

onMounted(() => {
  fetchPictureList(false)
})

onUnmounted(() => {
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
  column-gap: 20px; /* 图片之间的间隙 */
  width: 70%; /* 宽度达到画面的70% */
  margin: 0 auto;
}

.picture-item {
  display: inline-block;
  margin: 0 0 20px; /* 底部间隙 */
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  break-inside: avoid; /* 防止图片在列间断开 */
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
  font-weight: bold; /* 使用粗体 */
  font-size: 14px; /* 保持较小尺寸 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: Arial, sans-serif; /* 美化字体 */
  color: #ffffff;
}

.download-btn {
  background-color: #a217b4; /* 绿色背景，参考你的图片 */
  border: none;
  color: #fff;
  font-size: 12px;
  border-radius: 8px;
  padding: 4px 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.3);
  transition:
    background-color 0.2s,
    box-shadow 0.2s;
}

.download-btn:hover {
  background-color: #45a049; /* 悬停时变深 */
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.download-btn :deep(.anticon) {
  margin-right: 4px; /* 图标与文字间距 */
}

.tags {
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

.preview-modal :deep(.ant-modal-content) {
  background: rgba(20, 22, 24, 0.6); /* 增强毛玻璃效果，半透暗色调 */
  backdrop-filter: blur(15px); /* 加重毛玻璃效果 */
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  border: none; /* 移除任何默认边框 */
}

.preview-container {
  display: flex;
  gap: 0; /* 移除间隙，使图片与详细信息贴合 */
  height: 100%;
  align-items: stretch; /* 确保高度自适应 */
  background: rgba(20, 22, 24, 0.6); /* 统一毛玻璃效果 */
  backdrop-filter: blur(15px); /* 统一毛玻璃效果 */
}

.preview-image-container {
  flex: 1;
  min-width: 0; /* 防止溢出 */
  display: flex;
  flex-direction: column;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 100%; /* 与右侧信息框高度一致 */
  object-fit: contain; /* 保持宽高比，无空隙 */
  border-radius: 8px 0 0 8px; /* 左侧圆角 */
}

.image-info-bar {
  display: none; /* 移除分辨率和大小信息 */
}

.preview-info-panel {
  width: 300px;
  background: rgba(20, 22, 24, 0.6); /* 统一毛玻璃效果 */
  backdrop-filter: blur(15px); /* 统一毛玻璃效果 */
  border-radius: 0 8px 8px 0; /* 右侧圆角 */
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  border-left: 1px solid rgba(127, 90, 240, 0.2);
}

.info-header {
  border-bottom: 1px solid rgba(127, 90, 240, 0.2);
  padding-bottom: 16px;
}

.info-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #e0e0e0; /* 浅灰色，贴合暗色调 */
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  color: #a0a0a0; /* 浅灰色标签 */
  font-size: 14px;
}

.info-item .value {
  color: #d0d0d0; /* 浅灰色值 */
  font-size: 15px;
}

.info-item.description {
  margin-top: 8px;
}

.info-item.description .value {
  line-height: 1.6;
  color: #b0b0b0; /* 稍深灰色描述 */
}

.download-btn-modal {
  background-color: #4caf50; /* 绿色下载按钮 */
  border: none;
  color: #fff;
  font-size: 14px;
  border-radius: 8px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.3);
  transition:
    background-color 0.2s,
    box-shadow 0.2s;
}

.download-btn-modal:hover {
  background-color: #45a049; /* 悬停变深 */
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.download-btn-modal :deep(.anticon) {
  margin-right: 8px; /* 图标与文字间距 */
}

@media (min-width: 768px) {
  .picture-masonry {
    column-count: 3; /* 桌面端显示三列 */
  }
}

@media (max-width: 767px) {
  .picture-masonry {
    column-count: 1; /* 移动端显示一列 */
    width: 90%; /* 移动端宽度调整 */
  }
}
</style>
