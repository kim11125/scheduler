<template>
  <div class="page">
    <header class="app-header">
      <span class="header-title">⚙️ 관리</span>
    </header>

    <div class="mgmt-body">
      <div class="card menu-list" style="border-radius: 14px; overflow: hidden; margin: 16px 0;">
        <button class="menu-item" @click="router.push('/admin/companies')">
          <span class="menu-icon">🏢</span>
          <span class="menu-label">회사 관리</span>
          <span class="menu-arrow">›</span>
        </button>
        <button class="menu-item" @click="router.push('/admin/teams')">
          <span class="menu-icon">⚽</span>
          <span class="menu-label">팀 관리</span>
          <span class="menu-arrow">›</span>
        </button>
        <button class="menu-item" @click="router.push('/admin/schedules')">
          <span class="menu-icon">📅</span>
          <span class="menu-label">일정 관리</span>
          <span class="menu-arrow">›</span>
        </button>
      </div>

      <!-- Login logs (ADMIN only) -->
      <div v-if="authStore.user?.role === 'ADMIN'" class="logs-section">
        <div class="logs-header">
          <span class="logs-title">로그인 로그</span>
          <button class="refresh-btn" @click="fetchLogs">새로고침</button>
        </div>
        <div class="log-list">
          <div v-if="logs.length === 0" class="empty-state">
            <span class="empty-icon">📋</span>
            <p class="empty-text">로그가 없어요</p>
          </div>
          <div v-for="log in logs" :key="log.id" class="log-item">
            <span class="log-action" :class="log.action === 'LOGIN' ? 'login' : 'logout'">
              {{ log.action === 'LOGIN' ? '로그인' : '로그아웃' }}
            </span>
            <span class="log-user">{{ log.username }}</span>
            <span class="log-ip">{{ log.ipAddress || '-' }}</span>
            <span class="log-time">{{ formatLogDate(log.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <BottomNavAdmin />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminApi } from '@/api/admin'
import BottomNavAdmin from '@/components/BottomNavAdmin.vue'

const router = useRouter()
const authStore = useAuthStore()
const logs = ref<any[]>([])

async function fetchLogs() {
  if (authStore.user?.role !== 'ADMIN') return
  try {
    const res = await adminApi.getLogs(0, 100)
    logs.value = res.data.content
  } catch {}
}

function formatLogDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => { fetchLogs() })
</script>

<style scoped>
.mgmt-body { padding: 0 16px 100px; }
.logs-section { margin-top: 8px; }
.logs-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.logs-title { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.5px; }
.refresh-btn { font-size: 12px; color: var(--color-primary); font-weight: 600; background: none; border: none; cursor: pointer; }
.log-list { display: flex; flex-direction: column; gap: 6px; }
.log-item { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: var(--color-surface); border-radius: 10px; border: 1px solid var(--color-border); font-size: 12px; }
.log-action { font-weight: 700; padding: 2px 9px; border-radius: 999px; font-size: 11px; flex-shrink: 0; }
.log-action.login { background: var(--color-success-soft); color: var(--color-success); }
.log-action.logout { background: var(--color-warning-soft); color: var(--color-warning); }
.log-user { font-weight: 600; color: var(--color-text-primary); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.log-ip { color: var(--color-text-secondary); font-size: 11px; }
.log-time { color: var(--color-text-secondary); font-size: 11px; flex-shrink: 0; }
</style>
