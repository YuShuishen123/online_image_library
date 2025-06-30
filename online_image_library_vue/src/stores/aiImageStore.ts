import {defineStore} from 'pinia'

// 定义任务历史记录项的类型
interface TaskHistoryItem {
  taskId: string
  status: 'RUNNING' | 'SUCCEEDED' | 'FAILED'
  createTime: number
  results?: { url: string }[]
  error?: string
}

// 定义 store 的状态类型
interface AiImageState {
  formData: {
    model: string
    input: {
      prompt: string
      negativePrompt: string
    }
    parameters: {
      n: number
      width: number
      height: number
      prompt_extend: boolean
      seed: number
      steps: number
      shift: number
      cfg: number
    }
  }
  results: { url: string }[]
  currentTaskId: string | null
  isGenerating: boolean
  pollingIntervalId: number | null
  resolution: string
  taskHistory: TaskHistoryItem[]
}

export const useAiImageStore = defineStore('aiImage', {
  state: (): AiImageState => ({
    formData: {
      model: 'wanx2.1-t2i-turbo',
      input: {
        prompt: '',
        negativePrompt: '',
      },
      parameters: {
        n: 1,
        width: 512,
        height: 512,
        prompt_extend: false,
        seed: 0,
        steps: 20,
        shift: 3.0,
        cfg: 4.5,
      },
    },
    results: [],
    currentTaskId: null,
    isGenerating: false,
    pollingIntervalId: null,
    resolution: '512x512',
    taskHistory: [],
  }),

  actions: {
    // 设置表单数据
    setFormData(data: Partial<AiImageState['formData']>) {
      this.formData = { ...this.formData, ...data }
    },

    // 添加结果
    addResults(newResults: { url: string }[]) {
      this.results.push(...newResults)
    },

    // 清空结果
    clearResults() {
      this.results = []
    },

    // 设置当前任务ID
    setCurrentTaskId(taskId: string | null) {
      this.currentTaskId = taskId
    },

    // 设置生成状态
    setIsGenerating(status: boolean) {
      this.isGenerating = status
    },

    // 设置轮询间隔ID
    setPollingIntervalId(id: number | null) {
      this.pollingIntervalId = id
    },

    // 添加任务到历史记录
    addTaskToHistory(task: TaskHistoryItem) {
      this.taskHistory.push(task)
    },

    // 更新任务状态
    updateTaskStatus(
      taskId: string,
      status: TaskHistoryItem['status'],
      results?: { url: string }[],
      error?: string,
    ) {
      const task = this.taskHistory.find((t) => t.taskId === taskId)
      if (task) {
        task.status = status
        if (results) {
          task.results = results
        }
        if (error) {
          task.error = error
        }
      }
    },

    // 清理过期的任务历史记录（24小时前的记录）
    cleanupTaskHistory() {
      const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000
      this.taskHistory = this.taskHistory.filter((task) => task.createTime > oneDayAgo)
    },
  },

  persist: {
    key: 'ai-image-state',
    paths: ['formData', 'results', 'currentTaskId', 'isGenerating', 'resolution', 'taskHistory'],
  },
})
