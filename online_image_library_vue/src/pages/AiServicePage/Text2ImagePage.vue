<template>
  <div class="text2image-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>AI 文生图</h1>
      <p class="subtitle">输入文字描述，让 AI 为你创造独特的图像</p>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧参数配置区域 -->
      <div class="config-panel">
        <div class="panel-section">
          <h3>基础设置</h3>
          <div class="form-item">
            <label>模型选择</label>
            <a-select v-model:value="formData.model" placeholder="请选择模型" class="custom-select">
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
              v-model:value="formData.parameters.n"
              :min="1"
              :max="4"
              :step="1"
              class="custom-slider"
            />
          </div>

          <div class="form-item">
            <label>分辨率</label>
            <a-select v-model:value="resolution" placeholder="请选择分辨率" class="custom-select">
              <a-select-option value="512x512">512 x 512</a-select-option>
              <a-select-option value="768x768">768 x 768</a-select-option>
              <a-select-option value="1024x1024">1024 x 1024</a-select-option>
              <a-select-option value="1024x768">1024 x 768</a-select-option>
              <a-select-option value="768x1024">768 x 1024</a-select-option>
            </a-select>
          </div>

          <div class="form-item">
            <label>智能改写提示词</label>
            <a-switch v-model:checked="formData.parameters.prompt_extend" class="custom-switch" />
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
              v-model:value="formData.input.prompt"
              placeholder="例如：一只可爱的猫咪，超现实主义，柔和的色彩"
              :rows="4"
              class="custom-textarea prompt-textarea"
            />
          </div>

          <div class="form-item">
            <label>负向提示词</label>
            <a-textarea
              v-model:value="formData.input.negativePrompt"
              placeholder="例如：模糊，低质量，扭曲，丑陋"
              :rows="4"
              class="custom-textarea prompt-textarea"
            />
          </div>
        </div>

        <div class="panel-section">
          <h3>高级设置</h3>
          <div class="form-item">
            <a-tooltip
              title="随机种子用于控制图片生成的可重复性，相同的种子和参数会生成相似的图片。"
            >
              <label>随机种子</label>
            </a-tooltip>
            <a-input-number
              v-model:value="formData.parameters.seed"
              :min="0"
              :max="999999"
              class="custom-input seed-input"
            />
          </div>

          <div class="form-item">
            <a-tooltip
              title="去噪推理步数决定了图像生成的精细程度。步数越大，图像质量通常越高，但生成时间也会更长。建议范围：20 ~ 80。"
            >
              <label>去噪推理步数</label>
            </a-tooltip>
            <a-slider
              v-model:value="formData.parameters.steps"
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
              v-model:value="formData.parameters.shift"
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
              v-model:value="formData.parameters.cfg"
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
            :loading="generating"
            @click="handleGenerate"
            class="generate-btn"
          >
            {{ generating ? '生成中...' : '开始生成' }}
          </a-button>
        </div>
      </div>

      <!-- 右侧结果展示区域 -->
      <div class="result-panel">
        <div class="result-header">
          <h3>生成结果</h3>
          <div class="result-actions">
            <a-button type="text" @click="clearResults">
              <template #icon><DeleteOutlined /></template>
              清空
            </a-button>
          </div>
        </div>

        <div class="result-content">
          <div v-if="!generating && !results.length" class="empty-result">
            <a-empty description="暂无生成结果" />
          </div>
          <div v-else class="result-grid">
            <div v-for="(result, index) in results" :key="'img-' + index" class="result-item">
              <div class="result-image" @click="showImagePreview(result.url)">
                <img :src="result.url" :alt="'生成结果 ' + (index + 1)" />
                <div class="image-actions">
                  <a-button type="text" @click.stop="downloadImage(result.url)">
                    <template #icon><DownloadOutlined /></template>
                  </a-button>
                  <a-button type="text" @click.stop="saveToGallery()">
                    <template #icon><SaveOutlined /></template>
                  </a-button>
                </div>
              </div>
            </div>
            <!-- 占位符格子 -->
            <template v-if="generating">
              <div
                v-for="i in formData.parameters.n"
                :key="'placeholder-' + i"
                class="result-item placeholder-item"
              >
                <a-spin size="large" />
              </div>
            </template>
          </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { DeleteOutlined, DownloadOutlined, SaveOutlined } from '@ant-design/icons-vue'
import { createTextToImageTask, queryOutPaintingTask } from '@/api/aiServiceController'

// 表单数据
const formData = ref<Required<API.Text2ImageRequest>>({
  model: 'wanx2.1-t2i-turbo',
  input: {
    prompt: '',
    negativePrompt: '',
  },
  parameters: {
    n: 1,
    width: 512,
    height: 512,
    prompt_extend: true,
    watermark: false,
    seed: Math.floor(Math.random() * 1000000),
    steps: 40,
    shift: 3.0,
    cfg: 4.5,
  },
})

// 其他状态
const resolution = ref('1024x1024')
const generating = ref(false)
const results = ref<Array<{ url: string }>>([])
const taskId = ref<string>('')
const taskTimer = ref<number | null>(null)
const showPreviewModal = ref(false) // 控制预览模态框的显示
const previewImageUrl = ref('') // 预览图片的URL

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

  generating.value = true
  try {
    // 设置分辨率
    const [width, height] = resolution.value.split('x').map(Number)
    formData.value.parameters.width = width
    formData.value.parameters.height = height

    // 创建任务
    const res = await createTextToImageTask(formData.value)
    if (res.data.code === 200 && res.data.data?.output?.taskId) {
      taskId.value = res.data.data.output.taskId
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
    generating.value = false
  }
}

// 开始轮询任务状态
const startTaskPolling = () => {
  if (taskTimer.value) {
    clearInterval(taskTimer.value)
  }

  taskTimer.value = window.setInterval(async () => {
    try {
      const res = await queryOutPaintingTask(
        {
          taskId: taskId.value,
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
            clearInterval(taskTimer.value!)
            generating.value = false
            message.success('图片生成完成') // 成功提示
            // 处理结果，追加到现有results后面
            if (taskData.output.results) {
              results.value.push(
                ...taskData.output.results.map((item: { url: string }) => ({ url: item.url })),
              ) // 追加图片
            }
          } else if (taskData.output && taskData.output.task_status === 'FAILED') {
            // 任务失败
            clearInterval(taskTimer.value!)
            generating.value = false
            message.error('生成失败：' + (taskData.output.error || '未知错误'))
          }
          // 其他状态继续等待
        } else {
          // 返回的数据结构不符合预期
          clearInterval(taskTimer.value!)
          generating.value = false
          message.error('查询任务状态返回数据结构异常')
        }
      } else if (res.data.code !== 200) {
        // 接口返回错误，停止轮询
        clearInterval(taskTimer.value!)
        generating.value = false
        message.error(res.data.message || '查询任务状态失败')
      }
    } catch (error: unknown) {
      clearInterval(taskTimer.value!)
      generating.value = false
      // 确保 error 是 Error 实例或具有 message 属性的对象
      if (error instanceof Error) {
        message.error('查询任务状态失败：' + error.message)
      } else if (typeof error === 'object' && error !== null && 'message' in error) {
        message.error('查询任务状态失败：' + (error as { message: string }).message)
      } else {
        message.error('查询任务状态失败：未知错误')
      }
    }
  }, 3000) // 每3秒查询一次
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (taskTimer.value) {
    clearInterval(taskTimer.value)
  }
})

// 清空结果
const clearResults = () => {
  results.value = []
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

// 保存到图库
const saveToGallery = () => {
  // TODO: 实现保存到图库的逻辑
  message.success('已保存到图库')
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

.page-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.page-header h1 {
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #7f5af0 0%, #a217b4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.result-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.result-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.result-image {
  position: relative;
  padding-top: 100%;
}

.result-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(73, 71, 71, 0.5); /* 根据用户之前的修改 */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.result-item:hover .image-actions {
  opacity: 1;
}

.image-actions .ant-btn {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.image-actions .ant-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 占位符样式 */
.placeholder-item {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px; /* 确保占位符有高度 */
  background: rgba(0, 0, 0, 0.3);
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
</style>
