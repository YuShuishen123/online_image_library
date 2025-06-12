<template>
  <a-modal
    v-model:open="showModal"
    title="保存到图库"
    @ok="handleUploadConfirm"
    @cancel="handleUploadCancel"
    :confirmLoading="uploading"
    class="upload-modal"
    :maskStyle="{ backdropFilter: 'blur(10px)' }"
  >
    <a-form :model="uploadForm" layout="vertical">
      <a-form-item label="图片名称" required>
        <a-input v-model:value="uploadForm.name" placeholder="请输入图片名称" />
      </a-form-item>

      <a-form-item label="图片描述">
        <a-textarea v-model:value="uploadForm.description" placeholder="请输入图片描述" :rows="4" />
      </a-form-item>

      <a-form-item label="图片分类">
        <a-select
          v-model:value="uploadForm.categoryId"
          placeholder="请选择图片分类"
          :loading="loadingCategories"
          allowClear
        >
          <a-select-option v-for="category in categories" :key="category" :value="category">
            {{ category }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="图片标签">
        <a-select
          v-model:value="uploadForm.tags"
          mode="multiple"
          placeholder="请选择图片标签"
          :loading="loadingTags"
          allowClear
        >
          <a-select-option v-for="tag in tags" :key="tag" :value="tag">
            {{ tag }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="是否公开">
        <a-switch
          v-model:checked="uploadForm.isPublic"
          checked-children="公开"
          un-checked-children="私密"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByUrl, editPicture, listPictureTagCategory } from '@/api/pictureController'

const props = defineProps<{
  show: boolean
  imageUrl: string
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

// 计算属性，用于双向绑定show属性
const showModal = computed({
  get: () => props.show,
  set: (value: boolean) => emit('update:show', value),
})

// 上传相关的状态
const uploading = ref(false)
const loadingCategories = ref(false)
const loadingTags = ref(false)
const categories = ref<API.PictureTagCategory['categoryList']>([])
const tags = ref<API.PictureTagCategory['tagList']>([])

// 上传表单数据
const uploadForm = reactive({
  name: '',
  description: '',
  categoryId: undefined as string | undefined,
  tags: [] as string[],
  isPublic: false,
})

// 监听show属性变化，重置表单
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      resetForm()
      fetchCategoriesAndTags()
    }
  },
)

// 重置表单
const resetForm = () => {
  uploadForm.name = ''
  uploadForm.description = ''
  uploadForm.categoryId = undefined
  uploadForm.tags = []
  uploadForm.isPublic = false
}

// 获取分类和标签数据
const fetchCategoriesAndTags = async () => {
  loadingCategories.value = true
  loadingTags.value = true
  try {
    const res = await listPictureTagCategory()
    if (res.data.code === 200 && res.data.data) {
      categories.value = res.data.data.categoryList || []
      tags.value = res.data.data.tagList || []
    }
  } catch (error: unknown) {
    if (error instanceof Error) {
      message.error('获取分类和标签失败: ' + error.message)
    } else {
      message.error('获取分类和标签失败: 未知错误')
    }
  } finally {
    loadingCategories.value = false
    loadingTags.value = false
  }
}

// 处理上传确认
const handleUploadConfirm = async () => {
  if (!uploadForm.name) {
    message.warning('请填写图片名称')
    return
  }

  uploading.value = true
  try {
    // 第一步：上传图片
    const uploadRes = await uploadPictureByUrl({
      fileurl: props.imageUrl,
      pictureUploadRequest: {
        name: uploadForm.name,
      },
    })

    if (uploadRes.data.code === 200 && uploadRes.data.data) {
      const pictureId = uploadRes.data.data.id

      // 第二步：更新图片信息
      const editRes = await editPicture({
        id: pictureId,
        name: uploadForm.name,
        introduction: uploadForm.description,
        category: uploadForm.categoryId,
        tags: uploadForm.tags,
        spaceId: uploadForm.isPublic ? '0' : undefined,
      })

      if (editRes.data.code === 200) {
        message.success('保存到图库成功')
        showModal.value = false
        emit('success')
      } else {
        throw new Error(editRes.data.message || '更新图片信息失败')
      }
    } else {
      throw new Error(uploadRes.data.message || '上传图片失败')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('保存到图库失败')
    }
  } finally {
    uploading.value = false
  }
}

// 处理上传取消
const handleUploadCancel = () => {
  showModal.value = false
}
</script>

<style scoped>
.upload-modal :deep(.ant-modal-content) {
  background: rgba(30, 30, 30, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
}

.upload-modal :deep(.ant-modal-header) {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.upload-modal :deep(.ant-modal-title) {
  color: #fff;
}

.upload-modal :deep(.ant-modal-close) {
  color: rgba(255, 255, 255, 0.8);
}

.upload-modal :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.8);
}

.upload-modal :deep(.ant-input),
.upload-modal :deep(.ant-input-textarea),
.upload-modal :deep(.ant-select-selector) {
  background: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.upload-modal :deep(.ant-input::placeholder),
.upload-modal :deep(.ant-input-textarea::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.upload-modal :deep(.ant-select-selection-placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.upload-modal :deep(.ant-select-selection-item) {
  background: rgba(127, 90, 240, 0.2);
  border: 1px solid rgba(127, 90, 240, 0.3);
  color: #fff;
}

.upload-modal :deep(.ant-select-dropdown) {
  background: rgba(30, 30, 30, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.upload-modal :deep(.ant-select-item) {
  color: rgba(255, 255, 255, 0.8);
}

.upload-modal :deep(.ant-select-item-option-selected) {
  background: rgba(127, 90, 240, 0.2);
  color: #fff;
}

.upload-modal :deep(.ant-select-item-option-active) {
  background: rgba(127, 90, 240, 0.1);
}

.upload-modal :deep(.ant-switch) {
  background: rgba(0, 0, 0, 0.2);
}

.upload-modal :deep(.ant-switch-checked) {
  background: #7f5af0;
}

.upload-modal :deep(.ant-modal-footer) {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.upload-modal :deep(.ant-btn) {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.upload-modal :deep(.ant-btn-primary) {
  background: #7f5af0;
  border: none;
  color: #fff;
}

.upload-modal :deep(.ant-btn-primary:hover) {
  background: #6f4fd8;
}

.upload-modal :deep(.ant-btn-default:hover) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}
</style>
