<template>
  <div class="ai-intro-root">
    <AIStyleIntro />
    <AIHDIntro />
    <AIBgIntro />
    <AITxt2ImgIntro />
  </div>
</template>
<script setup lang="ts">
import AIStyleIntro from '@/pages/AI/AIStyleIntro.vue'
import AIHDIntro from '@/pages/AI/AIHDIntro.vue'
import AIBgIntro from '@/pages/AI/AIBgIntro.vue'
import AITxt2ImgIntro from '@/pages/AI/AITxt2ImgIntro.vue'
</script>

<style scoped>
.ai-intro-root {
  width: 100%;
  padding: 40px 0 60px 0;
  background: var(--background-color);
}
</style>
