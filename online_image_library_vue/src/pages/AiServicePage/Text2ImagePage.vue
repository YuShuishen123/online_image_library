<template>
  <div class="text2image-container">
    <div class="header-section">
      <div class="header-content">
        <h1 class="title">AI 文生图</h1>
        <p class="description">
          通过自然语言描述，让 AI 为您生成精美的图片。支持多种风格和参数调整，让创作更加自由。
        </p>
        <div class="feature-tags">
          <span class="feature-tag">
            <PictureOutlined />
            高质量图片
          </span>
          <span class="feature-tag">
            <SettingOutlined />
            参数可调
          </span>
          <span class="feature-tag">
            <ThunderboltOutlined />
            快速生成
          </span>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧参数配置区域 -->
      <div class="config-panel">
        <div class="panel-section">
          <h3>基础设置</h3>
          <div class="form-item">
            <label>模型选择</label>
            <a-select
              v-model:value="store.formData.model"
              placeholder="请选择模型"
              class="custom-select"
            >
              <a-select-option value="wanx2.1-t2i-turbo"
                >Wanx 2.1 T2I Turbo (快速生成)</a-select-option
              >
              <a-select-option value="wanx2.1-t2i-plus"
                >Wanx 2.1 T2I Plus (细节丰富)</a-select-option
              >
              <a-select-option value="wanx2.0-t2i-turbo"
                >Wanx 2.0 T2I Turbo (人像优化)</a-select-option
              >
              <a-select-option value="flux-dev">Flux Dev</a-select-option>
              <a-select-option value="flux-merged">Flux Merged</a-select-option>
              <a-select-option value="stable-diffusion-3.5-large"
                >Stable Diffusion 3.5 Large</a-select-option
              >
              <a-select-option value="stable-diffusion-3.5-large-turbo"
                >Stable Diffusion 3.5 Large Turbo</a-select-option
              >
            </a-select>
          </div>

          <div class="form-item">
            <label>生成数量</label>
            <a-slider
              v-model:value="store.formData.parameters.n"
              :min="1"
              :max="4"
              :step="1"
              class="custom-slider"
            />
          </div>

          <div class="form-item">
            <label>分辨率</label>
            <a-select
              v-model:value="store.resolution"
              placeholder="请选择分辨率"
              class="custom-select"
            >
              <a-select-option value="512x512">512 x 512</a-select-option>
              <a-select-option value="768x768">768 x 768</a-select-option>
              <a-select-option value="1024x1024">1024 x 1024</a-select-option>
              <a-select-option value="1024x768">1024 x 768</a-select-option>
              <a-select-option value="768x1024">768 x 1024</a-select-option>
            </a-select>
          </div>

          <div class="form-item">
            <label>智能改写提示词</label>
            <a-switch
              v-model:checked="store.formData.parameters.prompt_extend"
              class="custom-switch"
            />
            <span class="switch-tip">开启后会对提示词进行智能优化，增加3-4秒处理时间</span>
          </div>
        </div>

        <div class="panel-section">
          <h3>提示词设置</h3>
          <div class="form-item">
            <label>
              正向提示词
              <span style="color: red; margin-left: 4px">*</span>
            </label>
            <a-textarea
              v-model:value="store.formData.input.prompt"
              placeholder="例如：一只可爱的猫咪，超现实主义，柔和的色彩"
              :rows="4"
              class="custom-textarea prompt-textarea"
            />
          </div>

          <div class="form-item">
            <label>负向提示词</label>
            <a-textarea
              v-model:value="store.formData.input.negativePrompt"
              placeholder="例如：模糊，低质量，扭曲，丑陋"
              :rows="4"
              class="custom-textarea prompt-textarea"
            />
          </div>
        </div>

        <div class="panel-section">
          <h3>高级设置</h3>
          <div class="form-item">
            <span class="label">随机种子</span>
            <div class="seed-control">
              <a-input-number
                v-model:value="store.formData.parameters.seed"
                :min="0"
                :max="2147483647"
                class="seed-input"
              />
              <a-button type="primary" class="random-seed-btn" @click="generateRandomSeed">
                <template #icon><SyncOutlined /></template>
                随机种子
              </a-button>
            </div>
          </div>

          <div class="form-item">
            <a-tooltip
              title="去噪推理步数决定了图像生成的精细程度。步数越大，图像质量通常越高，但生成时间也会更长。建议范围：20 ~ 80。"
            >
              <label>去噪推理步数</label>
            </a-tooltip>
            <a-slider
              v-model:value="store.formData.parameters.steps"
              :min="20"
              :max="80"
              :step="1"
              class="custom-slider"
            />
          </div>

          <div class="form-item">
            <a-tooltip title="图像生成内容的偏移量，默认为3.0。">
              <label>图像生成内容偏移量 (Shift)</label>
            </a-tooltip>
            <a-slider
              v-model:value="store.formData.parameters.shift"
              :min="0"
              :max="10"
              :step="0.1"
              class="custom-slider"
            />
          </div>

          <div class="form-item">
            <a-tooltip
              title="引导系数（CFG）控制生成图像与提示词的贴合程度。值越高，AI越会严格遵循提示词，但可能牺牲创造性。建议范围：4 ~ 5，默认值为 4.5。"
            >
              <label>引导系数 (CFG)</label>
            </a-tooltip>
            <a-slider
              v-model:value="store.formData.parameters.cfg"
              :min="4"
              :max="5"
              :step="0.1"
              class="custom-slider"
            />
          </div>
        </div>

        <div class="action-buttons">
          <a-button
            type="primary"
            :loading="store.isGenerating"
            @click="handleGenerate"
            class="generate-btn"
          >
            {{ store.isGenerating ? '生成中...' : '开始生成' }}
          </a-button>
        </div>
      </div>

      <!-- 右侧结果展示区域 -->
      <div class="result-panel">
        <div class="result-header">
          <h3>生成结果</h3>
          <div class="result-actions">
            <a-button type="primary" class="clear-btn" @click="clearResults">
              <template #icon><DeleteOutlined /></template>
              清空结果
            </a-button>
          </div>
        </div>

        <div class="result-content">
          <div v-if="!store.isGenerating && !paginatedResults.length" class="empty-result">
            <a-empty description="暂无生成结果" />
          </div>
          <div v-else class="result-grid">
            <!-- 占位符格子 -->
            <template v-if="store.isGenerating">
              <div
                v-for="i in store.formData.parameters.n"
                :key="'placeholder-' + i"
                class="result-item placeholder-item"
              >
                <a-spin size="large" />
              </div>
            </template>
            <div
              v-for="(result, index) in paginatedResults"
              :key="'img-' + index"
              class="result-item"
            >
              <div class="result-image" @click="showImagePreview(result.url)">
                <img :src="result.url" :alt="'生成结果 ' + (index + 1)" />
                <div class="image-actions">
                  <a-button type="text" @click.stop="downloadImage(result.url)">
                    <template #icon><DownloadOutlined /></template>
                  </a-button>
                  <a-button type="text" @click.stop="saveToGallery(result.url)">
                    <template #icon><SaveOutlined /></template>
                  </a-button>
                  <a-button type="text" @click.stop="removeImage(index)" class="delete-btn">
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页组件 -->
        <div v-if="store.results.length > 0" class="pagination-container">
          <CustomPagination
            :current="pagination.current"
            :pageSize="pagination.pageSize"
            :total="store.results.length"
            :pageSizeOptions="['6', '12', '18', '24']"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 图片预览模态框 -->
    <a-modal
      :open="showPreviewModal"
      :footer="null"
      @cancel="handleCancelPreview"
      width="auto"
      wrapClassName="image-preview-modal"
      centered
    >
      <img
        :src="previewImageUrl"
        alt="预览图片"
        style="max-width: 100%; max-height: 80vh; display: block; margin: 0 auto"
      />
    </a-modal>

    <!-- 添加图片上传对话框 -->
    <a-modal
      v-model:open="showUploadModal"
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
          <a-textarea
            v-model:value="uploadForm.description"
            placeholder="请输入图片描述"
            :rows="4"
          />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted, onMounted, computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  DownloadOutlined,
  SaveOutlined,
  SyncOutlined,
  PictureOutlined,
  SettingOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { createTextToImageTask, queryOutPaintingTask } from '@/api/aiServiceController'
import { useAiImageStore } from '@/stores/aiImageStore'
import { storeToRefs } from 'pinia'
import CustomPagination from '@/components/CustomPagination.vue'
import { uploadPictureByUrl, editPicture, listPictureTagCategory } from '@/api/pictureController'

const store = useAiImageStore()

// 从 store 中解构状态，保持响应性
const { formData, currentTaskId, isGenerating, pollingIntervalId, resolution } = storeToRefs(store)

const showPreviewModal = ref(false) // 控制预览模态框的显示
const previewImageUrl = ref('') // 预览图片的URL

// 分页状态
const pagination = reactive({
  current: 1,
  pageSize: 6,
})

// 计算当前页的结果
const paginatedResults = computed(() => {
  const start = (pagination.current - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return store.results.slice(start, end)
})

// 处理分页变化
const handlePageChange = (current: number, size: number) => {
  pagination.current = current
  pagination.pageSize = size
}

// 类型守卫函数，判断是否是 Text2ImageTaskResponse 类型
function isText2ImageTaskResponse(data: unknown): data is API.Text2ImageTaskResponse {
  if (typeof data !== 'object' || data === null) {
    return false
  }
  const typedData = data as API.Text2ImageTaskResponse
  return (
    typedData &&
    typeof typedData.output === 'object' &&
    typedData.output !== null &&
    (typedData.output.task_status === 'RUNNING' ||
      typedData.output.task_status === 'SUCCEEDED' ||
      typedData.output.task_status === 'FAILED')
  )
}

// 处理生成请求
const handleGenerate = async () => {
  if (!formData.value.input.prompt) {
    message.warning('请输入正向提示词')
    return
  }

  store.setIsGenerating(true) // 更新 store 中的生成状态
  try {
    // 设置分辨率
    const [width, height] = resolution.value.split('x').map(Number)
    formData.value.parameters.width = width
    formData.value.parameters.height = height

    // 创建任务
    const res = await createTextToImageTask(formData.value) // 使用 store 中的 formData
    if (res.data.code === 200 && res.data.data?.output?.taskId) {
      const taskId = res.data.data.output.taskId
      store.setCurrentTaskId(taskId) // 保存 taskId 到 store

      // 添加任务到历史记录
      store.addTaskToHistory({
        taskId,
        status: 'RUNNING',
        createTime: Date.now(),
      })

      message.success('任务创建成功，开始生成图片')
      // 开始轮询任务状态
      startTaskPolling()
    } else {
      throw new Error(res.data.message || '创建任务失败')
    }
  } catch (error: unknown) {
    // 确保 error 是 Error 实例或具有 message 属性的对象
    if (error instanceof Error) {
      message.error(error.message || '生成失败')
    } else if (typeof error === 'object' && error !== null && 'message' in error) {
      message.error((error as { message: string }).message || '生成失败')
    } else {
      message.error('生成失败：未知错误')
    }
    store.setIsGenerating(false) // 更新 store 中的生成状态
  }
}

// 开始轮询任务状态
const startTaskPolling = () => {
  if (pollingIntervalId.value) {
    clearInterval(pollingIntervalId.value)
  }

  const intervalId = window.setInterval(async () => {
    try {
      if (!currentTaskId.value) {
        // 没有任务ID，停止轮询
        clearInterval(intervalId)
        store.setPollingIntervalId(null)
        store.setIsGenerating(false)
        return
      }
      const res = await queryOutPaintingTask(
        {
          taskId: currentTaskId.value,
          taskType: 2, // 文生图任务类型
        },
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )

      if (res.data.code === 200 && res.data.data) {
        if (isText2ImageTaskResponse(res.data.data)) {
          const taskData = res.data.data
          // 检查 output 和 output.task_status 是否存在
          if (taskData.output && taskData.output.task_status === 'SUCCEEDED') {
            // 任务完成
            clearInterval(intervalId)
            store.setPollingIntervalId(null)
            store.setIsGenerating(false)
            message.success('图片生成完成') // 成功提示

            // 更新任务状态
            const resultsFromTask =
              taskData.output.results?.map((item: { url: string }) => ({ url: item.url })) || []
            store.updateTaskStatus(currentTaskId.value, 'SUCCEEDED', resultsFromTask)

            // 处理结果，追加到现有results前面
            if (resultsFromTask.length > 0) {
              store.results = resultsFromTask.concat(store.results)
            }
            store.setCurrentTaskId(null) // 清除任务ID
          } else if (taskData.output && taskData.output.task_status === 'FAILED') {
            // 任务失败
            clearInterval(intervalId)
            store.setPollingIntervalId(null)
            store.setIsGenerating(false)
            const errorMsg = taskData.output.error || '未知错误'
            message.error('生成失败：' + errorMsg)

            // 更新任务状态
            store.updateTaskStatus(currentTaskId.value, 'FAILED', undefined, errorMsg)

            store.setCurrentTaskId(null) // 清除任务ID
          }
          // 其他状态继续等待
        } else {
          // 返回的数据结构不符合预期
          clearInterval(intervalId)
          store.setPollingIntervalId(null)
          store.setIsGenerating(false)
          message.error('查询任务状态返回数据结构异常')
          store.setCurrentTaskId(null) // 清除任务ID
        }
      } else if (res.data.code !== 200) {
        // 接口返回错误，停止轮询
        clearInterval(intervalId)
        store.setPollingIntervalId(null)
        store.setIsGenerating(false)
        message.error(res.data.message || '查询任务状态失败')
        store.setCurrentTaskId(null) // 清除任务ID
      }
    } catch (error: unknown) {
      clearInterval(intervalId)
      store.setPollingIntervalId(null)
      store.setIsGenerating(false)
      // 确保 error 是 Error 实例或具有 message 属性的对象
      if (error instanceof Error) {
        message.error('查询任务状态失败：' + error.message)
      } else if (typeof error === 'object' && error !== null && 'message' in error) {
        message.error('查询任务状态失败：' + (error as { message: string }).message)
      } else {
        message.error('查询任务状态失败：未知错误')
      }
      store.setCurrentTaskId(null) // 清除任务ID
    }
  }, 3000) // 每3秒查询一次

  store.setPollingIntervalId(intervalId)
}

// 组件挂载时恢复状态
onMounted(() => {
  // 清理过期的任务历史记录
  store.cleanupTaskHistory()

  // 如果有正在进行中的任务，则恢复轮询
  if (isGenerating.value && currentTaskId.value) {
    message.info('检测到有未完成的图片生成任务，正在恢复...')
    startTaskPolling()
  }

  // 如果有已完成的任务但结果未显示，则显示结果
  const completedTasks = store.taskHistory.filter(
    (task) => task.status === 'SUCCEEDED' && task.results && task.results.length > 0,
  )

  if (completedTasks.length > 0) {
    // 将所有已完成任务的结果合并，并去重
    const allResults = completedTasks.flatMap((task) => task.results || [])
    const uniqueResults = allResults.filter(
      (result, index, self) => index === self.findIndex((r) => r.url === result.url),
    )

    // 检查当前 results 中是否已存在这些结果
    const existingUrls = new Set(store.results.map((r) => r.url))
    const newResults = uniqueResults.filter((result) => !existingUrls.has(result.url))

    if (newResults.length > 0) {
      store.addResults(newResults)
    }
  }
})

// 组件卸载时清理定时器 (只清除非持久化的，持久化任务的定时器不清除)
onUnmounted(() => {
  // 如果任务仍在进行中，不清除定时器，让其在后台继续
  // 否则，如果定时器存在且任务已完成/失败，则清除
  if (pollingIntervalId.value && !isGenerating.value) {
    clearInterval(pollingIntervalId.value)
    store.setPollingIntervalId(null)
  }
})

// 清空结果
const clearResults = () => {
  // 清空 results 数组
  store.results = []

  // 清空 taskHistory 中的所有结果
  store.taskHistory = store.taskHistory.map((task) => ({
    ...task,
    results: [],
  }))

  message.success('已清空所有生成结果')
}

// 下载图片
const downloadImage = (url: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = `ai-generated-${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  message.success('开始下载图片')
}

// 上传相关的状态
const showUploadModal = ref(false)
const uploading = ref(false)
const loadingCategories = ref(false)
const loadingTags = ref(false)
const categories = ref<API.PictureTagCategory['categoryList']>([])
const tags = ref<API.PictureTagCategory['tagList']>([])
const currentUploadImageUrl = ref('')

// 上传表单数据
const uploadForm = reactive({
  name: '',
  description: '',
  categoryId: undefined as string | undefined,
  tags: [] as string[],
  isPublic: false,
})

// 打开上传对话框
const saveToGallery = (url?: string) => {
  if (url) {
    currentUploadImageUrl.value = url
  }
  showUploadModal.value = true
  // 重置表单
  uploadForm.name = ''
  uploadForm.description = ''
  uploadForm.categoryId = undefined
  uploadForm.tags = []
  uploadForm.isPublic = false
  // 获取分类和标签
  fetchCategoriesAndTags()
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
      fileurl: currentUploadImageUrl.value,
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
        introduction: uploadForm.description, // 将 description 改为 introduction
        categoryId: uploadForm.categoryId,
        tags: uploadForm.tags,
        spaceId: uploadForm.isPublic ? '0' : undefined, // 如果是公开，设置spaceId为0
      })

      if (editRes.data.code === 200) {
        message.success('保存到图库成功')
        showUploadModal.value = false
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
  showUploadModal.value = false
  currentUploadImageUrl.value = ''
}

// 显示图片预览
const showImagePreview = (url: string) => {
  previewImageUrl.value = url
  showPreviewModal.value = true
}

// 关闭图片预览模态框
const handleCancelPreview = () => {
  showPreviewModal.value = false
  previewImageUrl.value = ''
}

// 删除单张图片
const removeImage = (index: number) => {
  const imageToRemove = store.results[index]
  if (imageToRemove) {
    // 从 store 的 results 数组中删除
    store.results = store.results.filter((_, i) => i !== index)

    // 从 taskHistory 中删除相关结果
    store.taskHistory = store.taskHistory.map((task) => {
      if (task.results) {
        task.results = task.results.filter((result) => result.url !== imageToRemove.url)
      }
      return task
    })

    message.success('已删除图片')
  }
}

// 生成随机种子
const generateRandomSeed = () => {
  store.formData.parameters.seed = Math.floor(Math.random() * 2147483647)
}
</script>

<style scoped>
.text2image-container {
  min-height: 100vh;
  padding: 40px;
  color: #fff;
  overflow: hidden; /* 确保背景动画不会超出容器 */
  position: relative; /* 为伪元素背景定位 */
}

.text2image-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(circle at 50% 50%, rgba(127, 90, 240, 0.1) 0%, transparent 70%),
    radial-gradient(circle at 20% 80%, rgba(162, 23, 180, 0.1) 0%, transparent 70%);
  background-size: 100% 100%;
  animation: backgroundPan 30s linear infinite;
  z-index: -1;
}

@keyframes backgroundPan {
  0% {
    background-position: 0% 0%;
  }
  100% {
    background-position: -200% -200%;
  }
}

.header-section {
  margin-bottom: 30px;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
}

.title {
  font-size: 36px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: fadeInDown 0.8s ease;
}

.description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin-bottom: 24px;
  animation: fadeInUp 0.8s ease;
}

.feature-tags {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  animation: fadeIn 1s ease;
}

.feature-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(127, 90, 240, 0.1);
  border: 1px solid rgba(127, 90, 240, 0.2);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  transition: all 0.3s ease;
}

.feature-tag:hover {
  transform: translateY(-2px);
  background: rgba(127, 90, 240, 0.2);
  box-shadow: 0 4px 12px rgba(127, 90, 240, 0.2);
}

.feature-tag .anticon {
  font-size: 16px;
  color: #7f5af0;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.main-content {
  display: flex;
  gap: 30px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.config-panel,
.result-panel {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37); /* 增加更深的阴影 */
}

.panel-section {
  margin-bottom: 30px;
}

.panel-section h3 {
  font-size: 18px;
  margin-bottom: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.7);
}

.custom-select,
.custom-input,
.custom-textarea {
  width: 100%;
  background: rgba(55, 54, 54, 0.2) !important; /* 根据用户之前的修改 */
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
  border-radius: 8px !important;
}

.custom-select:hover,
.custom-input:hover,
.custom-textarea:hover {
  border-color: #7f5af0 !important;
}

.custom-textarea {
  resize: vertical;
}

.custom-slider {
  width: 100%;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}

.generate-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  border: none;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(127, 90, 240, 0.4);
}

.result-panel {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37); /* 增加更深的阴影 */
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-header h3 {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
}

.result-content {
  min-height: 400px;
}

.empty-result {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-bottom: 20px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.result-item {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  aspect-ratio: 1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.result-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.result-image {
  position: relative;
  width: 100%;
  height: 100%;
}

.result-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-item {
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.3);
  aspect-ratio: 1;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.result-item:hover .image-actions {
  opacity: 1;
}

.image-actions .ant-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.image-actions .ant-btn:hover {
  transform: scale(1.1);
  background: #fff;
}

.image-actions .ant-btn.delete-btn {
  background: rgba(255, 77, 79, 0.9);
  color: #fff;
}

.image-actions .ant-btn.delete-btn:hover {
  background: #ff4d4f;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    flex-direction: column;
  }

  .config-panel,
  .result-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .text2image-container {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .result-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

.switch-tip {
  margin-left: 8px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.custom-switch {
  margin-right: 8px;
}

/* 预览模态框样式 */
.image-preview-modal :deep(.ant-modal-content) {
  background: transparent; /* 透明背景 */
  box-shadow: none; /* 无阴影 */
}

.image-preview-modal :deep(.ant-modal-body) {
  padding: 0; /* 移除内边距 */
}

.image-preview-modal :deep(.ant-modal-close) {
  color: #fff; /* 关闭按钮颜色 */
  font-size: 24px;
}

.prompt-textarea :deep(textarea.ant-input) {
  color: #b0e0e6; /* 正向/负向提示词输入文字颜色：浅蓝色 */
  background-color: rgba(55, 54, 54, 0.2) !important; /* 输入框背景色：半透明深灰 */
}

.prompt-textarea :deep(textarea.ant-input::placeholder) {
  color: #d8f0f0; /* 占位文字颜色：更浅的青色，增加清晰度 */
}

.custom-input :deep(.ant-input-number-input) {
  background-color: rgba(0, 0, 0, 0.4) !important; /* 输入框背景色：深黑色 */
  color: #b0e0e6 !important; /* 输入文字颜色：浅蓝色 */
}

.custom-input :deep(.ant-input-number-handler-wrap) {
  background-color: rgba(0, 0, 0, 0.4) !important; /* 增减按钮区域背景色 */
  border-left: 1px solid rgba(255, 255, 255, 0.1) !important; /* 左侧边框 */
}

.custom-input :deep(.ant-input-number-handler) {
  color: #fff !important; /* 增减按钮文字颜色：白色 */
}

.custom-input :deep(.ant-input-number-handler:hover) {
  background-color: rgba(0, 0, 0, 0.6) !important;
}

/* 随机种子输入框美化 */
.seed-input :deep(.ant-input-number) {
  border: 2px solid transparent !important; /* 默认透明边框 */
  background:
    linear-gradient(to right, rgba(55, 54, 54, 0.2), rgba(55, 54, 54, 0.2)),
    linear-gradient(to right, #7f5af0, #a217b4) !important; /* 渐变边框效果 */
  background-clip: padding-box, border-box !important; /* 分割背景和边框 */
  background-origin: border-box !important; /* 背景从边框开始 */
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease !important; /* 动画效果 */
}

.seed-input :deep(.ant-input-number:hover) {
  transform: scale(1.02); /* 悬停时轻微放大 */
  box-shadow: 0 0 8px rgba(127, 90, 240, 0.5) !important; /* 发光效果 */
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.seed-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seed-input {
  width: 180px;
}

.random-seed-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 20px;
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.random-seed-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(127, 90, 240, 0.3);
}

.random-seed-btn:active {
  transform: translateY(0);
}

.random-seed-btn .anticon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.random-seed-btn:hover .anticon {
  transform: rotate(180deg);
}

.result-actions {
  display: flex;
  gap: 12px;
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 20px;
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.clear-btn:active {
  transform: translateY(0);
}

.clear-btn .anticon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.clear-btn:hover .anticon {
  transform: rotate(180deg);
}

/* 上传对话框样式 */
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
