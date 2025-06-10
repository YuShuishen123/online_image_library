<template>
  <section id="gallery" class="gallery-section">
    <div class="container">
      <h2>灵感画廊</h2>
      <div class="gallery-grid" ref="galleryGridRef">
        <div v-for="(pic, idx) in pictureList" :key="pic.id || idx" class="gallery-item">
          <img :src="pic.url" alt="灵感图片" loading="lazy" />
        </div>
        <div v-if="loading" class="gallery-loading">加载中...</div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { listPictureVoPage } from '@/api/pictureController'
const pictureList = ref<any[]>([])
const page = ref(1)
const pageSize = 20
const loading = ref(false)
const galleryGridRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

// 获取图片（只展示url字段webp图片）
async function fetchPictures() {
  if (loading.value) return
  loading.value = true
  const res = await listPictureVoPage({ current: page.value, pageSize, name: '壁纸' })
  if (res.data && res.data.code === 200) {
    const newPics = (res.data.data?.records || []).filter(
      (pic) => pic.url && pic.url.endsWith('.webp'),
    )
    pictureList.value.push(...newPics)
    page.value++
  }
  loading.value = false
}

// 懒加载：滚动到画廊区域时加载
function setupObserver() {
  if (!galleryGridRef.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        fetchPictures()
      }
    },
    { threshold: 0.1 },
  )
  observer.observe(galleryGridRef.value)
}

// 无限加载：滚动到底部继续请求
function onScroll() {
  if (!galleryGridRef.value || loading.value) return
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchPictures()
  }
}

onMounted(() => {
  setupObserver()
  window.addEventListener('scroll', onScroll)
})
onUnmounted(() => {
  if (observer && galleryGridRef.value) observer.unobserve(galleryGridRef.value)
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.gallery-section {
  padding: 80px 0;
  background-color: #1a1a1f;
}

.gallery-section h2 {
  text-align: center;
  font-size: 36px;
  margin-bottom: 50px;
}

.gallery-grid {
  column-count: 3;
  column-gap: 15px;
}

.gallery-item {
  margin-bottom: 15px;
  break-inside: avoid;
}

.gallery-item img {
  width: 100%;
  display: block;
  /* 无圆角 */
  border-radius: 0;
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
}

.gallery-item img:hover {
  transform: scale(1.03);
  opacity: 0.8;
}

.gallery-loading {
  text-align: center;
  color: var(--subtitle-color);
  padding: 20px 0;
  font-size: 16px;
}

@media (max-width: 992px) {
  .gallery-grid {
    column-count: 2;
  }
}

@media (max-width: 576px) {
  .gallery-grid {
    column-count: 1;
  }
}
</style>
