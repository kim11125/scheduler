<template>
  <div class="app-shell login-view">
    <!-- Theme toggle -->
    <div class="theme-bar">
      <button class="theme-btn" @click="themeStore.cycle()" :title="themeLabel">
        <span class="theme-dot" :style="{ background: themeColor }"></span>
        <span>{{ themeLabel }}</span>
      </button>
    </div>

    <div class="login-body">
      <!-- Logo area -->
      <div class="logo-area">
        <div class="logo-icon">📅</div>
        <h1 class="logo-title">스케줄 관리</h1>
        <p class="logo-sub">나만의 경기 일정을 기록하세요</p>
      </div>

      <!-- Form -->
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="field">
          <label class="field-label">아이디</label>
          <input
            v-model="form.username"
            class="field-input"
            type="text"
            placeholder="아이디를 입력하세요"
            autocomplete="username"
          />
        </div>
        <div class="field">
          <label class="field-label">비밀번호</label>
          <input
            v-model="form.password"
            class="field-input"
            type="password"
            placeholder="비밀번호를 입력하세요"
            autocomplete="current-password"
          />
        </div>
        <p v-if="loginIdChangedMsg" class="info-msg">{{ loginIdChangedMsg }}</p>
        <p v-if="authStore.error" class="error-msg">{{ authStore.error }}</p>
        <button type="submit" class="btn-primary">로그인</button>
      </form>

      <p class="register-link">
        계정이 없으신가요?
        <RouterLink to="/register">회원가입 신청</RouterLink>
      </p>

      <p class="register-hint">회원가입 신청 후 관리자 승인이 필요합니다.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const form = reactive({ username: '', password: '' })

const themeLabel = computed(() => ({ light: '라이트', dark: '다크', orange: '오렌지' }[themeStore.current]))
const themeColor = computed(() => ({ light: '#1976D2', dark: '#00CBA8', orange: '#F4511E' }[themeStore.current]))

const loginIdChangedMsg = computed(() =>
  route.query.reason === 'login-id-changed'
    ? '로그인 아이디가 변경되었습니다. 변경된 아이디로 다시 로그인해 주세요.'
    : null
)

async function handleLogin() {
  const ok = await authStore.login(form.username, form.password)
  if (!ok) return
  const { role, status } = authStore.user!
  const isAdmin = role === 'ADMIN' || role === 'MANAGER'
  if (isAdmin && status === 'ACTIVE') router.push('/admin')
  else if (status === 'ACTIVE') router.push('/')
  else router.push('/pending')
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  background: var(--color-background);
}
.theme-bar {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px 0;
}
.theme-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
  border: 1px solid var(--color-separator);
  cursor: pointer;
  transition: all 0.2s;
}
.theme-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.login-body {
  padding: 24px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.logo-area {
  text-align: center;
  padding: 32px 0 8px;
}
.logo-icon { font-size: 52px; margin-bottom: 12px; }
.logo-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 6px;
}
.logo-sub {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.field-input {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text);
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}
.field-input:focus {
  border-color: var(--color-primary);
}
.error-msg {
  font-size: 13px;
  color: #F44336;
  text-align: center;
}
.info-msg {
  font-size: 13px;
  color: #1976D2;
  text-align: center;
  background: #E3F2FD;
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.5;
}
.btn-primary {
  margin-top: 6px;
  padding: 14px;
  border-radius: 12px;
  background: var(--color-btn);
  color: var(--color-btn-text);
  font-size: 16px;
  font-weight: 700;
  width: 100%;
  transition: opacity 0.2s;
}
.btn-primary:active { opacity: 0.8; }
.register-link {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
}
.register-link a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
}
.register-hint {
  text-align: center;
  font-size: 13px;
  color: var(--color-text-secondary);
}
</style>
