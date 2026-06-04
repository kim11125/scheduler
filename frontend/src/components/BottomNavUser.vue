<template>
  <nav class="bottom-nav">
    <button v-for="item in navItems" :key="item.path"
      :class="['nav-btn', { active: isActive(item.path) }]"
      @click="router.push(item.path)">
      <span class="nav-btn-icon">{{ item.icon }}</span>
      <span>{{ item.label }}</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const navItems = [
  { path: '/', icon: '🏠', label: '홈' },
  { path: '/calendar', icon: '📅', label: '캘린더' },
  { path: '/schedules', icon: '📋', label: '내 일정' },
  { path: '/profile', icon: '👤', label: '내 정보' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>
