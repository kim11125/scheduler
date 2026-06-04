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
  { path: '/admin', icon: '📊', label: '홈' },
  { path: '/admin/users', icon: '👥', label: '사용자' },
  { path: '/admin/schedules', icon: '📅', label: '일정' },
  { path: '/admin/management', icon: '⚙️', label: '관리' },
]

function isActive(path: string) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}
</script>
