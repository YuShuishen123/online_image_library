<template>
  <section id="introduction" class="introduction-section">
    <div class="container">
      <h2>关于我们</h2>
      <div class="introduction-content">
        <div class="introduction-text">
          <p>欢迎来到我们的在线图片库，这是一个专为创意工作者打造的图片资源平台。</p>
          <p>我们提供：</p>
          <ul>
            <li>高质量的图片资源</li>
            <li>智能AI图片处理功能</li>
            <li>灵感画廊展示</li>
            <li>个人空间管理</li>
          </ul>
          <p>无论您是设计师、摄影师还是创意爱好者，这里都能满足您的需求。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
// 组件逻辑
</script>

<style scoped>
.introduction-section {
  padding: 80px 0;
  background: var(--background-color);
  color: var(--text-color);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h2 {
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 40px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.introduction-content {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.introduction-text {
  max-width: 800px;
  font-size: 1.1rem;
  line-height: 1.8;
}

.introduction-text p {
  margin-bottom: 20px;
}

.introduction-text ul {
  list-style: none;
  padding-left: 20px;
  margin-bottom: 20px;
}

.introduction-text li {
  position: relative;
  padding-left: 25px;
  margin-bottom: 10px;
}

.introduction-text li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--primary-color);
  font-size: 1.5em;
}

@media (max-width: 768px) {
  .introduction-section {
    padding: 40px 0;
  }

  h2 {
    font-size: 2rem;
  }

  .introduction-text {
    font-size: 1rem;
  }
}
</style>
