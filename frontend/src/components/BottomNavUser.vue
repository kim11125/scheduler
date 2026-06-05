<template>
  <nav class="bottom-nav">
    <button v-for="item in navItems" :key="item.path"
      :class="['nav-btn', { active: isActive(item.path) }]"
      @click="router.push(item.path)">
      <AppIcon :name="item.icon" size="md" class="nav-btn-icon" />
      <span class="nav-btn-label">{{ item.label }}</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import AppIcon from './AppIcon.vue'

const router = useRouter()
const route = useRoute()

const navItems = [
  { path: '/', icon: 'home', label: '홈' },
  { path: '/calendar', icon: 'calendar', label: '캘린더' },
  { path: '/schedules', icon: 'list', label: '내 일정' },
  { path: '/profile', icon: 'user', label: '내 정보' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>
