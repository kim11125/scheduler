<template>
  <div class="app-shell pending-view">
    <div class="pending-content">
      <div class="pending-icon" :class="iconClass">{{ icon }}</div>
      <h2 class="pending-title">{{ title }}</h2>
      <p class="pending-message">{{ message }}</p>
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

const ICONS: Record<string, string> = { PENDING: '⏳', REJECTED: '✕', DISABLED: '🔒' }
const TITLES: Record<string, string> = { PENDING: '승인 대기 중', REJECTED: '가입이 거절되었습니다', DISABLED: '계정이 비활성화되었습니다' }
const MESSAGES: Record<string, string> = {
  PENDING:  '관리자가 가입 신청을 검토 중이에요.\n승인되면 바로 이용하실 수 있습니다.',
  REJECTED: '안타깝게도 가입이 거절되었습니다.\n자세한 사항은 관리자에게 문의해 주세요.',
  DISABLED: '계정 접근이 제한되었습니다.\n자세한 사항은 관리자에게 문의해 주세요.',
}
const ICON_CLASSES: Record<string, string> = { PENDING: 'icon-pending', REJECTED: 'icon-rejected', DISABLED: 'icon-disabled' }
const icon    = computed(() => ICONS[status.value]    ?? '⏳')
const title   = computed(() => TITLES[status.value]   ?? '')
const message = computed(() => MESSAGES[status.value] ?? '')
const iconClass = computed(() => ICON_CLASSES[status.value] ?? 'icon-pending')

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
  background: var(--color-bg);
}
.pending-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 32px;
  text-align: center;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
  margin: 24px;
  max-width: 320px;
  width: 100%;
}
.pending-icon {
  font-size: 56px;
  line-height: 1;
  margin-bottom: 4px;
}
.icon-rejected {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: var(--color-danger-soft);
  color: var(--color-danger);
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-bottom: 4px;
}
.pending-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text-primary);
}
.pending-message {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  white-space: pre-line;
}
.btn-logout {
  margin-top: 8px;
  padding: 13px 36px;
  border-radius: var(--radius-pill);
  background: var(--color-surface-muted);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 600;
  border: 1.5px solid var(--color-border);
  cursor: pointer;
  transition: all 0.2s;
}
.btn-logout:active { opacity: 0.8; transform: scale(0.98); }
</style>
