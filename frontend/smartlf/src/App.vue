<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'

</script>

<template>
  <router-view />
</template>

<style>
/* 全局基础样式重置 */
html, body {
  margin: 0;
  padding: 0;
  background-color: #f0f2f5; /* 论坛极简浅灰背景 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
#app {
  height: 100vh;
}
</style>