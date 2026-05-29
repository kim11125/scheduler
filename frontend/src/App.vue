<template>
  <RouterView />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
themeStore.init()
</script>
