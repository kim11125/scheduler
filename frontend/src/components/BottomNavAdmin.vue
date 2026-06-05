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
  { path: '/admin', icon: 'grid', label: '홈' },
  { path: '/admin/users', icon: 'users', label: '사용자' },
  { path: '/admin/schedules', icon: 'calendar', label: '일정' },
  { path: '/admin/management', icon: 'settings', label: '관리' },
]

function isActive(path: string) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}
</script>
