<template>
  <div class="user-space-root">
    <div class="user-info-card">
      <a-avatar :src="userInfo.userAvatar" :size="80" shape="circle" />
      <div class="user-info-meta">
        <div class="user-name">{{ userInfo.userName || '未命名用户' }}</div>
        <div class="user-account">账号：{{ userInfo.userAccount }}</div>
        <div class="user-profile">{{ userInfo.userProfile || '这个人很神秘~' }}</div>
      </div>
      <a-button class="edit-btn" type="primary" @click="editProfile">编辑资料</a-button>
    </div>
    <div class="user-pictures-section">
      <div class="section-title">我的图片</div>
      <a-row :gutter="[24, 24]">
        <a-col v-for="pic in pictureList" :key="pic.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="pic-card" hoverable @click="previewPicture(pic)">
            <template #cover>
              <img :src="pic.url" :alt="pic.name" class="pic-img" />
            </template>
            <a-card-meta :title="pic.name" :description="pic.introduction || '暂无描述'" />
          </a-card>
        </a-col>
      </a-row>
      <div class="pagination-bar">
        <a-pagination
          v-model:current="currentPage"
          :pageSize="pageSize"
          :total="total"
          show-size-changer
          :pageSizeOptions="['8', '16', '24', '32']"
          @change="fetchPictureList"
        />
      </div>
    </div>
    <a-modal
      v-model:visible="previewVisible"
      :footer="null"
      :width="900"
      @cancel="previewVisible = false"
    >
      <img :src="previewImage" class="preview-modal-img" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLoginUser } from '@/api/userController'
import { listSpacePicturePage } from '@/api/pictureController'
import { message } from 'ant-design-vue'

const userInfo = ref<API.LoginUserVO>({})
const pictureList = ref<API.PictureVO[]>([])
const currentPage = ref(1)
const pageSize = ref(16)
const total = ref(0)
const previewVisible = ref(false)
const previewImage = ref('')

const fetchUserInfo = async () => {
  const res = await getLoginUser()
  if (res.data.code === 200 && res.data.data) {
    userInfo.value = res.data.data
  } else {
    message.error('获取用户信息失败')
  }
}

const fetchPictureList = async () => {
  const res = await listSpacePicturePage({
    current: currentPage.value,
    pageSize: pageSize.value,
  })
  if (res.data.code === 200 && res.data.data) {
    pictureList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } else {
    pictureList.value = []
    total.value = 0
    message.error('获取图片失败')
  }
}

const previewPicture = (pic: API.PictureVO) => {
  previewImage.value = pic.url || ''
  previewVisible.value = true
}

const editProfile = () => {
  message.info('编辑资料功能开发中...')
}

onMounted(() => {
  fetchUserInfo()
  fetchPictureList()
})
</script>

<style scoped>
.user-space-root {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 48px 0;
  color: var(--text-color);
}
.user-info-card {
  display: flex;
  align-items: center;
  background: rgba(36, 38, 41, 0.98);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(127, 90, 240, 0.1);
  padding: 32px 36px;
  margin-bottom: 36px;
  gap: 32px;
}
.user-info-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}
.user-account {
  color: var(--subtitle-color);
  font-size: 15px;
}
.user-profile {
  color: #bdbdbd;
  font-size: 15px;
}
.edit-btn {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  border: none;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  border-radius: 32px;
  padding: 8px 32px;
  box-shadow: 0 2px 12px rgba(127, 90, 240, 0.1);
  transition:
    box-shadow 0.2s,
    background 0.2s;
}
.edit-btn:hover {
  background: linear-gradient(90deg, var(--secondary-color), var(--primary-color));
  box-shadow: 0 4px 24px rgba(127, 90, 240, 0.18);
}
.user-pictures-section {
  background: rgba(36, 38, 41, 0.92);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(127, 90, 240, 0.1);
  padding: 32px 24px 24px 24px;
}
.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 24px;
}
.pic-card {
  background: #23232b !important;
  border-radius: 14px !important;
  box-shadow: 0 2px 12px rgba(127, 90, 240, 0.08);
  color: #fff !important;
  border: none !important;
}
.pic-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 10px 10px 0 0;
}
.pagination-bar {
  margin-top: 32px;
  text-align: center;
}
.preview-modal-img {
  width: 100%;
  max-height: 600px;
  object-fit: contain;
  border-radius: 12px;
  background: #23232b;
}
@media (max-width: 900px) {
  .user-info-card,
  .user-pictures-section {
    padding: 16px 8px;
    border-radius: 12px;
  }
  .pic-img {
    height: 120px;
  }
}
</style>
