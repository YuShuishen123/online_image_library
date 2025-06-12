import { defineStore } from 'pinia'
import { getUserVobyId } from '@/api/userController'

interface UploaderInfo {
  avatar?: string
  nickname?: string
}

export const useUploaderStore = defineStore('uploader', {
  state: () => ({
    // 使用Map存储上传者信息，key是用户ID
    uploaders: new Map<string, UploaderInfo>(),
  }),
  actions: {
    // 获取上传者信息，优先从缓存获取
    async getUploaderInfo(userId: string): Promise<UploaderInfo> {
      // 如果缓存中已有，直接返回
      if (this.uploaders.has(userId)) {
        return this.uploaders.get(userId)!
      }

      // 否则从API获取
      try {
        const res = await getUserVobyId({ id: userId })
        if (res.data?.code === 200 && res.data.data?.records?.[0]) {
          const userInfo = res.data.data.records[0]
          const uploaderInfo = {
            avatar: userInfo.userAvatar || '/public/default-avatar.png',
            nickname: userInfo.userName || '未知用户',
          }

          // 存入缓存
          this.uploaders.set(userId, uploaderInfo)
          return uploaderInfo
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }

      // 如果API请求失败，返回默认信息
      const defaultInfo = {
        avatar: '/public/default-avatar.png',
        nickname: '未知用户',
      }
      this.uploaders.set(userId, defaultInfo)
      return defaultInfo
    },

    // 清除缓存
    clearCache() {
      this.uploaders.clear()
    },
  },
})
