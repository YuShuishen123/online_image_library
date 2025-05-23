<template>
  <div class="picture-uploader">
    <a-tabs v-model:activeKey="uploadTab">
      <a-tab-pane key="file" tab="本地上传">
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :max-count="1"
          :show-upload-list="false"
          :custom-request="handleCustomFileUpload"
        >
          <a-button :loading="uploading">选择图片</a-button>
        </a-upload>
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL上传">
        <a-input v-model:value="uploadUrl" placeholder="请输入图片URL" style="margin-bottom: 8px" />
        <a-button type="primary" :loading="uploading" @click="handleUrlUpload">上传</a-button>
      </a-tab-pane>
    </a-tabs>
    <div v-if="uploadedImage" style="margin-top: 16px">
      <a-image :src="uploadedImage.url" :width="200" style="margin-bottom: 16px" />
      <a-form :model="uploadInfoForm" layout="vertical">
        <a-form-item label="图片名称" required>
          <a-input v-model:value="uploadInfoForm.name" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select
            v-model:value="uploadInfoForm.category"
            :options="categories.map((c) => ({ label: c, value: c }))"
          />
        </a-form-item>
        <a-form-item label="标签">
          <a-select
            v-model:value="uploadInfoForm.tags"
            mode="multiple"
            :options="tags.map((t) => ({ label: t, value: t }))"
          />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="uploadInfoForm.introduction" />
        </a-form-item>
      </a-form>
      <div style="text-align: right">
        <a-button @click="handleCancel">取消</a-button>
        <a-button
          type="primary"
          style="margin-left: 8px"
          :disabled="!uploadedImage"
          @click="handleSubmit"
          >确认上传</a-button
        >
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  uploadPictureUsingPost,
  uploadPictureByUrlUsingPost,
  updatePictureInfoUsingPost,
  deletePictureUsingPost,
} from '@/api/pictureController'

const { categories, tags } = defineProps<{
  categories: string[]
  tags: string[]
}>()

const emit = defineEmits<{
  (e: 'upload-success'): void
  (e: 'cancel'): void
}>()

const uploadTab = ref('file')
const uploadUrl = ref('')
const fileList = ref<UploadProps['fileList']>([])
const uploadedImage = ref<API.PictureVO | null>(null)
const uploading = ref(false)

const uploadInfoForm = ref<{
  name: string
  category: string
  tags: string[]
  introduction: string
}>({
  name: '',
  category: '',
  tags: [],
  introduction: '',
})

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const handleCustomFileUpload = async (options: any) => {
  uploading.value = true
  try {
    const res = await uploadPictureUsingPost({}, {}, options.file as File)
    if (res.data?.code === 200 && res.data?.data) {
      uploadedImage.value = res.data.data
      uploadInfoForm.value.name = typeof res.data.data.name === 'string' ? res.data.data.name : ''
    }
    if (options.onSuccess) options.onSuccess(undefined, options.file as File)
  } catch {
    if (options.onError) options.onError(new Error('上传失败'))
    message.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const handleUrlUpload = async () => {
  if (!uploadUrl.value) {
    message.warning('请输入图片URL')
    return
  }
  uploading.value = true
  try {
    const res = await uploadPictureByUrlUsingPost({ fileurl: uploadUrl.value })
    if (res.data?.code === 200 && res.data?.data) {
      uploadedImage.value = res.data.data
      uploadInfoForm.value.name = typeof res.data.data.name === 'string' ? res.data.data.name : ''
    }
  } catch {
    message.error('URL上传失败')
  } finally {
    uploading.value = false
  }
}

const handleSubmit = async () => {
  if (!uploadedImage.value) return
  try {
    const res = await updatePictureInfoUsingPost({
      id: uploadedImage.value.id,
      ...uploadInfoForm.value,
    })
    if (res.data?.code === 200) {
      message.success('图片信息已保存')
      emit('upload-success')
      resetForm()
    }
  } catch {
    message.error('图片信息保存失败')
  }
}

const handleCancel = async () => {
  if (uploadedImage.value?.id) {
    await deletePictureUsingPost({ id: uploadedImage.value.id })
  }
  emit('cancel')
  resetForm()
}

const resetForm = () => {
  uploadedImage.value = null
  uploadInfoForm.value = { name: '', category: '', tags: [], introduction: '' }
  fileList.value = []
  uploadUrl.value = ''
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt8M = file.size / 1024 / 1024 < 8
  if (!isLt8M) {
    message.error('图片大小不能超过8MB！')
    return false
  }
  return true
}
</script>

<style scoped>
.picture-uploader {
  width: 100%;
}
</style>
