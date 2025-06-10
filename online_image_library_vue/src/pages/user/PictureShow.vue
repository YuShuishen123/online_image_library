<template>
  <a-layout-content class="home-page">
    <div class="search-bar">
      <a-input-search v-model:value="searchText" placeholder="输入关键词搜索图片" enter-button="搜索" size="large"
        @search="handleSearch" />
    </div>

    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>

    <div v-else-if="pictureList.length === 0" class="empty-container">
      <a-empty description="暂无图片" />
    </div>

    <div v-else class="picture-list">
      <a-row :gutter="[24, 24]">
        <a-col v-for="picture in pictureList" :key="picture.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card hoverable class="picture-card" @click="showPreview(picture)">
            <template #cover>
              <div class="image-container">
                <img :src="picture.url" :alt="picture.name" class="picture-image" />
              </div>
            </template>
            <template #actions>
              <a-button @click.stop="handleDownload(picture.originalImageurl || '')">
                下载原图
              </a-button>
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

    <div v-if="pictureList.length > 0" class="pagination">
      <a-pagination v-model:current="currentPage" v-model:pageSize="pageSize" :total="total"
        :pageSizeOptions="['4', '8', '12', '24']" show-size-changer @change="handlePageChange" />
    </div>

    <!-- 图片预览模态框 -->
    <a-modal v-model:visible="previewVisible" :footer="null" :width="1000" @cancel="handlePreviewCancel"
      class="preview-modal">
      <div class="preview-container">
        <div class="preview-image-container">
          <img :src="previewImage" class="preview-image" />
          <div class="image-info-bar">
            <span>{{ currentPicture?.picWidth }} x {{ currentPicture?.picHeight }}</span>
            <span>{{ formatFileSize(currentPicture?.picSize || 0) }}</span>
          </div>
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
              <span class="label">上传者：</span>
              <span class="value">{{ currentPicture?.user?.userName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">原图比例：</span>
              <span class="value">{{ currentPicture?.picScale || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">原图大小: </span>
              <span class="value">{{ formatFileSize(currentPicture?.picSize || 0) }}</span>
            </div>
            <div class="info-item">
              <span class="label">分辨率：</span>
              <span class="value">{{ currentPicture?.picWidth }} x {{ currentPicture?.picHeight }}</span>
            </div>
            <div class="info-item description">
              <span class="label">图片描述：</span>
              <span class="value">{{ currentPicture?.introduction || '暂无描述' }}</span>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </a-layout-content>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { listPictureVoPage } from '@/api/pictureController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'

const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const pictureList = ref<API.PictureVO[]>([])
const loading = ref(false)
const requestBody = ref<API.PictureQueryRequest>()

// 图片预览相关
const previewVisible = ref(false)
const previewImage = ref<string>('')
const currentPicture = ref<API.PictureVO | null>(null)

const showPreview = (picture: API.PictureVO) => {
  previewImage.value = picture.url || ''
  currentPicture.value = picture
  previewVisible.value = true
}

const handlePreviewCancel = () => {
  previewVisible.value = false
  currentPicture.value = null
}

const fetchPictureList = async () => {
  requestBody.value = {
    current: currentPage.value,
    pageSize: pageSize.value,
  }
  if (searchText.value) {
    requestBody.value.name = searchText.value
  }

  loading.value = true
  try {
    const res = await listPictureVoPage(requestBody.value)

    if (res.data.code === 200 && res.data) {
      pictureList.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
      console.log('Pictures loaded:', pictureList.value.length)
    } else {
      pictureList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取图片列表失败啦:', error)
    pictureList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPictureList()
}

const handlePageChange = () => {
  fetchPictureList()
}

onMounted(() => {
  fetchPictureList()
})

const handleDownload = (url: string) => {
  const loginUserStore = useLoginUserStore()
  if (!loginUserStore.loginUser || loginUserStore.loginUser.id === 0) {
    message.warning('请先登录后再下载')
    return
  }

  if (!url) {
    message.error('下载链接不存在')
    return
  }

  try {
    window.open(url, '_blank')
  } catch (error) {
    message.error('下载失败，请稍后重试')
    console.error('下载失败:', error)
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '未知'
  const mb = size / (1024 * 1024)
  return `${mb.toFixed(2)} MB`
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
</script>

<style scoped>
.home-page {
  padding: 24px;
  max-width: 90%;
  margin: 0 auto;
}

.search-bar {
  width: 50%;
  margin: 0 auto;
  margin-bottom: 32px;
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

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.picture-card {
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.picture-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.picture-description {
  color: #666;
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
}

:deep(.ant-card-actions) {
  background: #fafafa;
}

:deep(.ant-card-actions > li) {
  margin: 0;
}

:deep(.ant-card-actions > li > span) {
  color: #1890ff;
}

:deep(.ant-modal-content) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-modal-body) {
  padding: 0;
}

.preview-modal :deep(.ant-modal-content) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
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
  color: #666;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 0 0 8px 8px;
}

.preview-info-panel {
  width: 300px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.info-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 16px;
}

.info-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #333;
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
  color: #666;
  font-size: 14px;
}

.info-item .value {
  color: #333;
  font-size: 15px;
}

.info-item.description {
  margin-top: 8px;
}

.info-item.description .value {
  line-height: 1.6;
  color: #666;
}

:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-tag) {
  margin: 0;
  padding: 2px 8px;
  border-radius: 4px;
}
</style>
