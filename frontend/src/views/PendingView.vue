<template>
  <div class="app-shell pending-view">
    <div class="content">
      <div class="icon">{{ icon }}</div>
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
      <button class="btn-logout" @click="handleLogout">로그아웃</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const status = computed(() => authStore.user?.status ?? 'PENDING')

const ICONS: Record<string, string> = { PENDING: '⏳', REJECTED: '❌', DISABLED: '🔒' }
const TITLES: Record<string, string> = { PENDING: '승인 대기 중', REJECTED: '가입이 거절되었습니다', DISABLED: '계정 비활성화' }
const MESSAGES: Record<string, string> = {
  PENDING:  '관리자 승인을 기다리고 있습니다.',
  REJECTED: '가입이 거절되었습니다. 관리자에게 문의하세요.',
  DISABLED: '계정이 비활성화되었습니다. 관리자에게 문의하세요.',
}
const icon    = computed(() => ICONS[status.value]    ?? '⏳')
const title   = computed(() => TITLES[status.value]   ?? '')
const message = computed(() => MESSAGES[status.value] ?? '')

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.pending-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 32px;
  text-align: center;
}
.icon { font-size: 60px; }
h2 { font-size: 20px; font-weight: 700; color: var(--color-text); }
p { font-size: 15px; color: var(--color-text-secondary); line-height: 1.7; }
.btn-logout {
  margin-top: 16px;
  padding: 12px 32px;
  border-radius: 10px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 600;
  border: 1px solid var(--color-separator);
}
</style>
