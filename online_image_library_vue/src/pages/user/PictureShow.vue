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
        :current="currentPage"
        :pageSize="pageSize"
        :total="total"
        :pageSizeOptions="['4', '8', '12', '24']"
        @change="handlePageChange"
      />
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
              <span class="value"
                >{{ currentPicture?.picWidth }} x {{ currentPicture?.picHeight }}</span
              >
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
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { listPictureVoPage } from '@/api/pictureController'
// import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import CustomPagination from '@/components/CustomPagination.vue' // 引入自定义分页组件

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

const currentPage = ref(searchForm.current)
const pageSize = ref(searchForm.pageSize)
const total = ref(0)
const pictureList = ref<API.PictureVO[]>([])
const loading = ref(false)

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
      current: currentPage.value,
      pageSize: pageSize.value,
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
  currentPage.value = 1
  fetchPictureList()
}

// 分页组件change事件
const handlePageChange = (current: number, size: number) => {
  currentPage.value = current
  pageSize.value = size
  searchForm.current = current
  searchForm.pageSize = size
  fetchPictureList()
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
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  color: var(--subtitle-color); /* 文字颜色 */
  font-size: 14px;
}

.info-item .value {
  color: var(--text-color); /* 文字颜色 */
  font-size: 15px;
}

.info-item.description {
  margin-top: 8px;
}

.info-item.description .value {
  line-height: 1.6;
  color: var(--subtitle-color); /* 文字颜色 */
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
