<template>
  <section class="ai-feature-section hd-bg right-img">
    <div class="container">
      <div class="feature-text">
        <h2>智能修复与增强</h2>
        <p>一键提升图片清晰度，修复模糊、低分辨率照片，让每一张图片都焕然一新。</p>
        <a class="try-btn" @click="goTo('/ai/hd')">去试试</a>
      </div>
      <div class="feature-img">
        <img
          src="https://image-1329241986.cos.ap-chengdu.myqcloud.com/public/1920415149873795074/73b34257-e663-48b4-ad17-c136dc62c9ea.webp"
          alt="智能修复与增强示例"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()

// 路由跳转函数
function goTo(path: string) {
  router.push(path)
}
</script>

<style scoped>
/* 整体区域样式 */
.ai-feature-section {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 340px;
  padding: 48px 0;
  border-bottom: 2px solid #23232b;
  gap: 60px;
}

/* 右图布局样式 */
.right-img {
  flex-direction: row-reverse;
}

/* 容器样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;
  padding: 0 24px;
}

/* 文本区域样式 */
.feature-text {
  flex: 1.2;
  max-width: 480px;
  color: #fff;
}

/* 标题样式 */
.feature-text h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 18px;
  color: #3a8dde;
}

/* 描述文本样式 */
.feature-text p {
  font-size: 1.1rem;
  margin-bottom: 28px;
  color: #bdbdbd;
}

/* 按钮样式 */
.try-btn {
  display: inline-block;
  background: linear-gradient(90deg, #3a8dde, #2cb67d);
  color: #fff;
  font-weight: bold;
  border-radius: 24px;
  padding: 10px 32px;
  font-size: 1.1rem;
  text-decoration: none;
  box-shadow: 0 2px 12px rgba(58, 141, 222, 0.2);
  transition:
    background 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
  cursor: pointer;
}

/* 按钮悬停效果 */
.try-btn:hover {
  background: linear-gradient(90deg, #2cb67d, #3a8dde);
  box-shadow: 0 4px 24px rgba(58, 141, 222, 0.2);
  transform: translateY(-2px) scale(1.04);
}

/* 图片区域样式 */
.feature-img {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 图片样式，减弱光晕效果，优化阴影 */
.feature-img img {
  width: 340px;
  max-width: 90vw;
  border-radius: 18px;
  background: #23232b;
  /* 调整光晕效果，降低强度和透明度 */
  box-shadow:
    0 0 8px 4px rgba(58, 141, 222, 0.5),
    /* 更柔和的蓝色光晕 */ 0 2px 8px rgba(44, 182, 125, 0.3); /* 更轻的绿色阴影 */
  filter: drop-shadow(0 8px 12px rgba(58, 141, 222, 0.4));
  transition:
    transform 0.3s ease,
    /* 平滑缩放过渡 */ filter 0.3s ease,
    /* 亮度过渡 */ box-shadow 0.3s ease; /* 阴影过渡 */
}

/* 鼠标悬停时图片的反馈效果 */
.feature-img img:hover {
  transform: scale(1.05); /* 轻微放大 */
  filter: brightness(1.1) drop-shadow(0 10px 14px rgba(58, 141, 222, 0.6)); /* 增强亮度和光晕 */
  box-shadow:
    0 0 10px 6px rgba(58, 141, 222, 0.6),
    /* 光晕略微增强 */ 0 4px 12px rgba(44, 182, 125, 0.4); /* 阴影略微加深 */
}

/* 响应式布局 */
@media (max-width: 900px) {
  .ai-feature-section {
    flex-direction: column !important;
    gap: 32px;
    padding: 32px 0;
  }
  .feature-img img {
    width: 90vw;
    max-width: 400px;
  }
}
</style>
