<template>
  <div class="picture-manage-page">
    <a-card title="图片管理" :bordered="false">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="图片名称" name="name">
          <a-input v-model:value="searchForm.name" placeholder="请输入图片名称" />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-select v-model:value="searchForm.category" placeholder="请选择分类" style="width: 200px" allowClear>
            <a-select-option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审核状态" name="reviewStatus">
          <a-select v-model:value="searchForm.reviewStatus" placeholder="请选择审核状态" style="width: 200px" allowClear>
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上传时间排序" name="sortOrder">
          <a-select v-model:value="searchForm.sortOrder" style="width: 150px">
            <a-select-option value="descend">最新上传</a-select-option>
            <a-select-option value="ascend">最早上传</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- 图片列表 -->
      <a-table :columns="columns" :data-source="pictureList" :loading="loading" :pagination="pagination"
        @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'thumbnailUrl'">
            <a-image :width="100" :src="record.thumbnailUrl" :preview="{ src: record.url }" />
          </template>
          <template v-else-if="column.key === 'tags'">
            <template v-for="tag in parseTags(record.tags)" :key="tag">
              <a-tag>{{ tag }}</a-tag>
            </template>
          </template>
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag :color="getReviewStatusColor(record.reviewStatus)">
              {{ getReviewStatusText(record.reviewStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatToBeijingTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleEdit(record)">编辑</a-button>
              <a-button v-if="record.reviewStatus === 0" type="link" @click="handleReview(record)">
                审核
              </a-button>
              <a-popconfirm title="确定要删除这张图片吗？" @confirm="handleDelete(record)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 上传弹窗 -->
    <a-modal v-model:visible="uploadModalVisible" title="上传图片" :footer="null" @cancel="handleUploadCancel">
      <picture-uploader :categories="categories" :tags="tags" @upload-success="handleUploadSuccess"
        @cancel="handleUploadCancel" />
    </a-modal>

    <!-- 其余弹窗等内容保持不变 -->
    <a-modal v-model:visible="editModalVisible" title="编辑图片信息" @ok="handleEditSubmit"
      @cancel="editModalVisible = false">
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="图片名称" required>
          <a-input v-model:value="editForm.name" />
        </a-form-item>
        <a-form-item label="分类" required>
          <a-select v-model:value="editForm.category">
            <a-select-option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="标签">
          <a-select v-model:value="editForm.tags" mode="multiple" placeholder="请选择标签"
            :options="tags.map((tag) => ({ label: tag, value: tag }))" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="editForm.introduction" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:visible="reviewModalVisible" title="图片审核" @ok="handleReviewSubmit"
      @cancel="reviewModalVisible = false">
      <a-form :model="reviewForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="审核结果" required>
          <a-radio-group v-model:value="reviewForm.reviewStatus">
            <a-radio :value="1">通过</a-radio>
            <a-radio :value="2">拒绝</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审核意见">
          <a-textarea v-model:value="reviewForm.reviewMessage" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import {
  listPicturePage,
  updatePictureInfo,
  deletePicture,
  doPictureReview,
  listPictureTagCategory,
} from '@/api/pictureController'

const columns = [
  {
    title: '缩略图',
    dataIndex: 'thumbnailUrl',
    key: 'thumbnailUrl',
    width: 120,
  },
  {
    title: '图片ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '图片名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags',
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
  },
  {
    title: '上传时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
]

const loading = ref(false)
const pictureList = ref<API.PictureVO[]>([])
const categories = ref<string[]>([])
const tags = ref<string[]>([])
const pagination = ref<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
})

const searchForm = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const uploadModalVisible = ref(false)


const handleUploadSuccess = () => {
  uploadModalVisible.value = false
  fetchPictureList()
}

const handleUploadCancel = () => {
  uploadModalVisible.value = false
}

const editModalVisible = ref(false)
const editForm = ref<API.PictureUpdateRequest>({
  id: undefined,
  name: '',
  category: '',
  tags: [],
  introduction: '',
})

const reviewModalVisible = ref(false)
const reviewForm = ref<API.PictureReviewRequest>({
  id: undefined,
  reviewStatus: 1,
  reviewMessage: '',
})

const fetchPictureList = async () => {
  loading.value = true
  try {
    const res = await listPicturePage({
      ...searchForm.value,
      current: pagination.value.current,
      pageSize: pagination.value.pageSize,
      sortField: 'createTime',
      sortOrder: searchForm.value.sortOrder,
    })
    if (res.data?.code === 200 && res.data?.data) {
      const responseData = res.data.data as API.PagePictureVO
      pictureList.value = responseData.records || []
      pagination.value.total = responseData.total || 0
    }
  } catch (error: unknown) {
    console.error('获取图片列表失败:', error)
    message.error('获取图片列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCategoriesAndTags = async () => {
  try {
    const res = await listPictureTagCategory()
    if (res.data?.code === 200 && res.data?.data) {
      const data = res.data.data as API.PictureTagCategory
      categories.value = data.categoryList || []
      tags.value = data.tagList || []
    }
  } catch (error: unknown) {
    console.error('获取分类和标签失败:', error)
    message.error('获取分类和标签失败')
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  fetchPictureList()
}

const resetSearch = () => {
  searchForm.value = {
    current: 1,
    pageSize: 10,
    sortField: 'createTime',
    sortOrder: 'descend',
  }
  handleSearch()
}

const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value.current = pag.current ?? 1
  pagination.value.pageSize = pag.pageSize ?? 10
  fetchPictureList()
}

const handleEdit = (record: API.PictureVO) => {
  editForm.value = {
    id: record.id,
    name: record.name,
    category: record.category,
    tags: parseTags(record.tags),
    introduction: record.introduction,
  }
  editModalVisible.value = true
}

const handleEditSubmit = async () => {
  try {
    const res = await updatePictureInfo(editForm.value)
    if (res.data?.code === 200) {
      message.success('更新成功')
      editModalVisible.value = false
      fetchPictureList()
    } else if (res.data?.code === 40101) {
      message.error('您没有权限进行此操作')
    }
  } catch (error: unknown) {
    console.error('更新失败:', error)
    message.error('更新失败')
  }
}

const handleReview = (record: API.PictureVO) => {
  reviewForm.value = {
    id: record.id,
    reviewStatus: 1,
    reviewMessage: '',
  }
  reviewModalVisible.value = true
}

const handleReviewSubmit = async () => {
  try {
    const res = await doPictureReview(reviewForm.value)
    if (res.data?.code === 200) {
      message.success('审核成功')
      reviewModalVisible.value = false
      fetchPictureList()
    }
  } catch (error: unknown) {
    console.error('审核失败:', error)
    message.error('审核失败')
  }
}

const handleDelete = async (record: API.PictureVO) => {
  try {
    const res = await deletePicture({ id: record.id })
    if (res.data?.code === 200) {
      message.success('删除成功')
      fetchPictureList()
    }
  } catch (error: unknown) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const getReviewStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
  }
  return statusMap[status] || '未知状态'
}

const getReviewStatusColor = (status: number): string => {
  const colorMap: Record<number, string> = {
    0: 'orange',
    1: 'green',
    2: 'red',
  }
  return colorMap[status] || 'default'
}

// 工具函数：将ISO时间转为北京时间（+8），格式化为"YYYY-MM-DD HH:mm:ss"
function formatToBeijingTime(isoString: string): string {
  if (!isoString) return ''
  const date = new Date(isoString)
  // 转为北京时间
  const beijingDate = new Date(date.getTime() + 8 * 60 * 60 * 1000)
  const y = beijingDate.getUTCFullYear()
  const m = String(beijingDate.getUTCMonth() + 1).padStart(2, '0')
  const d = String(beijingDate.getUTCDate()).padStart(2, '0')
  const h = String(beijingDate.getUTCHours()).padStart(2, '0')
  const min = String(beijingDate.getUTCMinutes()).padStart(2, '0')
  const s = String(beijingDate.getUTCSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}
// 工具函数：兼容tags为字符串或数组
function parseTags(tags: string[] | string | null | undefined): string[] {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  try {
    const arr = JSON.parse(tags)
    if (Array.isArray(arr)) return arr
    return []
  } catch {
    return []
  }
}

onMounted(() => {
  fetchPictureList()
  fetchCategoriesAndTags()
})
</script>

<style scoped>
.picture-manage-page {
  min-height: calc(100vh - 120px);
  /* 120px可根据你的header/footer高度调整 */
  padding: 24px;
  box-sizing: border-box;
  background: #fff;
  /* 保证内容区背景为白色 */
}
</style>
