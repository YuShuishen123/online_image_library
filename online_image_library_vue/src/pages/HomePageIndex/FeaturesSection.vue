<template>
  <section id="features" class="ai-features">
    <div class="container">
      <h2>探索通义万象的魔力</h2>
      <p>我们集成了业界领先的 AI 模型，为你提供无尽的创作可能。</p>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🎨</div>
          <h3>AI 艺术风格化</h3>
          <p>将你的照片转换为梵高、莫奈等大师风格的艺术画作，或尝试赛博朋克、卡通等现代风格。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <svg
              width="32"
              height="32"
              viewBox="0 0 32 32"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <rect
                x="20.07"
                y="7.757"
                width="3"
                height="18"
                rx="1.5"
                transform="rotate(45 20.07 7.757)"
                fill="#2cb67d"
              />
              <path
                d="M8 4v3M8 2v2m0 0V2m0 3V2m0 3H6m2 0h2m-2 0v2m0-2v-2"
                stroke="#2cb67d"
                stroke-width="2"
                stroke-linecap="round"
              />
              <path
                d="M26 20v2m0 2v-2m0 2h-2m2 0h2"
                stroke="#2cb67d"
                stroke-width="2"
                stroke-linecap="round"
              />
              <path
                d="M24 8h2m2 8v-2m0 2h-2m2 0v2"
                stroke="#2cb67d"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </div>
          <h3>智能修复与增强</h3>
          <p>自动去除图片中的噪点、划痕，提升模糊照片的清晰度和分辨率，让老照片焕发新生。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">✂️</div>
          <h3>智能抠图与替换背景</h3>
          <p>精准识别主体，一键移除或替换背景。无论是证件照还是创意海报，都能轻松搞定。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">✨</div>
          <h3>文生图 (Text-to-Image)</h3>
          <p>输入一段描述性文字，通义万象将为你创造出独一无二的精美图像，让你的想象力成为现实。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
// 这里可以添加交互逻辑
</script>

<style scoped>
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
</style>
