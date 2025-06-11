<template>
  <div class="testPage">
    <HeaderSection />
    <RouterView />
    <FooterSection />
  </div>
</template>

<script setup lang="ts">
import HeaderSection from '@/components/HomepageComponent/HeaderSection.vue'
import FooterSection from '@/components/HomepageComponent/FooterSection.vue'

// 这里可以添加页面交互逻辑
</script>

<style>
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

.testPage {
  min-height: 100vh;
  width: 100%;
  background: var(--background-color);
  color: var(--text-color);
  font-family: var(--font-family);
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

a {
  text-decoration: none !important;
  color: var(--primary-color);
  transition: color 0.3s ease;
}

a:hover {
  color: var(--secondary-color);
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

/* 导航栏样式 */
.navbar {
  padding: 20px 0;
  background-color: rgba(22, 22, 26, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-color);
}

.logo span {
  color: var(--primary-color);
}

.nav-links {
  list-style: none;
  display: flex;
  gap: 25px;
}

.nav-links a {
  color: var(--text-color);
  font-size: 16px;
}

.cta-button {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  color: white;
  padding: 10px 20px;
  border-radius: 50px;
  font-weight: bold;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(127, 90, 240, 0.4);
  color: white;
}

/* Hero 区域样式 */
.hero {
  text-align: center;
  padding: 120px 0;
  position: relative;
  overflow: hidden;
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

/* 图片上传区域样式 */
.upload-section {
  background: var(--card-background);
  border-radius: 20px;
  padding: 40px;
  width: 80%;
  max-width: 700px;
  margin: 0 auto;
  border: 1px dashed var(--subtitle-color);
  text-align: center;
  transition: all 0.3s ease;
}

.upload-section:hover {
  border-color: var(--primary-color);
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.upload-area {
  cursor: pointer;
}

.upload-area-icon {
  font-size: 50px;
  color: var(--primary-color);
  margin-bottom: 20px;
}

.upload-area h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.upload-area p {
  color: var(--subtitle-color);
}

/* AI 功能区样式 */
.ai-features {
  padding: 80px 0;
  text-align: center;
}

.ai-features h2 {
  font-size: 36px;
  margin-bottom: 15px;
}

.ai-features > p {
  color: var(--subtitle-color);
  margin-bottom: 50px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  background: var(--card-background);
  padding: 30px;
  border-radius: 15px;
  text-align: left;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.feature-card:hover {
  transform: translateY(-10px);
  border-color: var(--primary-color);
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 15px;
  color: var(--secondary-color);
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.feature-card p {
  color: var(--subtitle-color);
  font-size: 14px;
}

/* 灵感画廊样式 */
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
  border-radius: 10px;
  width: 100%;
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
}

.gallery-item img:hover {
  transform: scale(1.03);
  opacity: 0.8;
}

/* 页脚样式 */
.footer {
  background-color: var(--background-color);
  padding: 40px 0;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer p {
  color: var(--subtitle-color);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .hero h1 {
    font-size: 40px;
  }

  .gallery-grid {
    column-count: 2;
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .hero h1 {
    font-size: 32px;
  }

  .hero p {
    font-size: 16px;
  }

  .upload-section {
    width: 95%;
    padding: 30px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .gallery-grid {
    column-count: 1;
  }
}
</style>
