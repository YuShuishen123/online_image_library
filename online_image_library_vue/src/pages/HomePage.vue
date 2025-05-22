<template>
  <a-layout-content class="home-page">
    <div class="search-bar">
      <a-input-search
        v-model:value="searchText"
        placeholder="输入关键词搜索图片"
        enter-button="搜索"
        size="large"
        @search="handleSearch"
      />
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
                <img :src="picture.url" :alt="picture.name" class="picture-image" />
              </div>
            </template>
            <template #actions>
              <a-button :href="picture.originalImageurl" target="_blank" @click.stop>
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
      <a-pagination
        v-model:current="currentPage"
        v-model:pageSize="pageSize"
        :total="total"
        :pageSizeOptions="['4', '8', '12', '24']"
        show-size-changer
        @change="handlePageChange"
      />
    </div>

    <!-- 图片预览模态框 -->
    <a-modal
      v-model:visible="previewVisible"
      :footer="null"
      :width="800"
      @cancel="handlePreviewCancel"
    >
      <img :src="previewImage" style="width: 100%" />
    </a-modal>
  </a-layout-content>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'

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

const showPreview = (picture: API.PictureVO) => {
  previewImage.value = picture.url || ''
  previewVisible.value = true
}

const handlePreviewCancel = () => {
  previewVisible.value = false
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
    const res = await listPictureVoByPageUsingPost(requestBody.value)

    if (res.data.code === 200 && res.data) {
      pictureList.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
      console.log('Pictures loaded:', pictureList.value.length)
    } else {
      pictureList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
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
</style>
