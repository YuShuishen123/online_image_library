<template>
  <section class="hero">
    <div class="container">
      <h1>释放你的创造力，用 AI 重塑视觉</h1>
      <p>体验通义万象大模型带来的强大 AI 编辑能力，壁纸画廊自动滚动展示。</p>
    </div>
    <div
      class="scroll-gallery"
      ref="galleryRef"
      @mouseenter="pauseScroll"
      @mouseleave="resumeScroll"
    >
      <div class="scroll-gallery-track" ref="trackRef">
        <img
          v-for="(pic, idx) in pictureList"
          :key="pic.id || idx"
          :src="pic.url"
          :alt="pic.name"
          :height="galleryHeight"
          :style="{ width: getAutoWidth(pic) }"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from 'vue'
import {listPictureVoPage} from '@/api/pictureController'
// 类型直接用API.PictureVO

const pictureList = ref<API.PictureVO[]>([])
const galleryHeight = 260 // px
const scrollSpeed = 0.5 // px/帧
const galleryRef = ref<HTMLElement | null>(null)
const trackRef = ref<HTMLElement | null>(null)
let animationId: number | null = null
let isPaused = false

// 计算等高自适应宽度
function getAutoWidth(pic: API.PictureVO) {
  if (pic.picWidth && pic.picHeight) {
    return galleryHeight * (pic.picWidth / pic.picHeight) + 'px'
  }
  return 'auto'
}

// 自动无缝循环滚动动画
function autoScroll() {
  if (!trackRef.value || !galleryRef.value) return
  if (!isPaused) {
    trackRef.value.scrollLeft += scrollSpeed
    // 到达一半时重置scrollLeft，实现无缝循环
    const totalWidth = trackRef.value.scrollWidth / 2
    if (trackRef.value.scrollLeft >= totalWidth) {
      trackRef.value.scrollLeft = 0
    }
  }
  animationId = requestAnimationFrame(autoScroll)
}

function pauseScroll() {
  isPaused = true
}
function resumeScroll() {
  isPaused = false
}

// 获取图片数据，严格参照PictureShow.vue
async function fetchPictures() {
  const res = await listPictureVoPage({current: 1, pageSize: 15, name: '壁纸'})
  if (res.data && res.data.code === 200) {
    // 只保留横图
    const filtered = (res.data.data?.records || []).filter((pic) => {
      if (pic.picWidth && pic.picHeight) {
        return pic.picWidth / pic.picHeight >= 1
      }
      return false
    })
    // 拼接自身一份用于无缝滚动
    pictureList.value = [...filtered, ...filtered]
    await nextTick()
    if (animationId) cancelAnimationFrame(animationId)
    autoScroll()
  }
}

onMounted(() => {
  fetchPictures()
})
</script>

<style scoped>
/* --- 全局样式 & 变量 --- */
:root {
  --primary-color: #7f5af0;
  --secondary-color: #2cb67d;
  --background-color: #16161a;
  --card-background: #242629;
  --text-color: #fffffe;
  --subtitle-color: #94a1b2;
  --font-family:
    'Helvetica Neue', Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h1,
h2,
h3 {
  font-weight: 600;
}

/* --- 导航栏 --- */
.hero {
  text-align: center;
  padding: 120px 0 40px 0;
  position: relative;
  overflow: visible;
}

.hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 50% 100%, rgba(127, 90, 240, 0.2), transparent 70%),
    linear-gradient(160deg, #16161a 20%, #7f5af030 50%, #2cb67d20 80%, #16161a 100%);
  animation: background-pan 15s linear infinite;
  z-index: -1;
}

@keyframes background-pan {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.hero h1 {
  font-size: 48px;
  margin-bottom: 15px;
  line-height: 1.2;
  background: -webkit-linear-gradient(45deg, var(--primary-color), var(--secondary-color), #fffffe);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero p {
  font-size: 18px;
  color: var(--subtitle-color);
  margin-bottom: 30px;
}

.scroll-gallery {
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background: transparent;
  padding: 0;
  overflow: hidden;
  height: 280px;
  display: flex;
  align-items: center;
}

.scroll-gallery-track {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  height: 100%;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.scroll-gallery-track::-webkit-scrollbar {
  display: none;
}

.scroll-gallery-track img {
  height: 260px;
  width: auto;
  object-fit: cover;
  margin: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  user-select: none;
  pointer-events: auto;
  transition: transform 0.3s;
}
.scroll-gallery-track img:hover {
  transform: scale(1.05);
  z-index: 2;
}

@media (max-width: 768px) {
  .scroll-gallery {
    height: 160px;
  }
  .scroll-gallery-track img {
    height: 120px;
  }
}
</style>
