<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from './layouts/BasciLayout.vue'
import { healthUsingGet } from './api/system'
import { useLoginUserStore } from './stores/useLoginUserStore'
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()


healthUsingGet().then((res) => {
  console.log(res)
})

</script>
