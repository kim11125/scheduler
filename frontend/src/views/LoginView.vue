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
        <span class="logo-icon">📅</span>
        <h1 class="logo-title">스케줄 관리</h1>
        <p class="logo-sub">나만의 경기 일정을 기록하세요</p>
      </div>

      <!-- Form card -->
      <div class="login-card">
        <p v-if="loginIdChangedMsg" class="info-msg">{{ loginIdChangedMsg }}</p>
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
          <p v-if="authStore.error" class="error-msg">{{ authStore.error }}</p>
          <button type="submit" class="btn-primary">로그인</button>
        </form>

        <div class="login-footer">
          <p class="register-link">
            계정이 없으신가요?
            <RouterLink to="/register">회원가입 신청</RouterLink>
          </p>
          <p class="hint">승인된 사용자만 이용 가능한 서비스입니다</p>
        </div>
      </div>
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

const themeLabel = computed(() => ({ lavender: '라벤더', peach: '피치', mint: '민트', dark: '다크', 'rose-milk': '로즈' }[themeStore.current] ?? '라벤더'))
const themeColor = computed(() => ({ lavender: '#8B7FD4', peach: '#E8836A', mint: '#4DB896', dark: '#A695F0', 'rose-milk': '#D4789A' }[themeStore.current] ?? '#8B7FD4'))

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
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
}
.theme-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px 0;
}
.theme-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 12px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-soft);
}
.theme-btn:active { opacity: 0.8; }
.theme-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.login-body {
  flex: 1;
  padding: 16px 28px 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.logo-area {
  text-align: center;
  padding: 32px 0 4px;
}
.logo-icon { font-size: 60px; margin-bottom: 16px; display: block; }
.logo-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}
.logo-sub {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* Form card */
.login-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
  padding: 24px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.2px;
}
.field-input {
  padding: 13px 16px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--color-border);
  background: var(--color-surface-muted);
  color: var(--color-text-primary);
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  width: 100%;
}
.field-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  background: var(--color-surface);
}
.error-msg {
  font-size: 13px;
  color: var(--color-danger);
  background: var(--color-danger-soft);
  padding: 10px 14px;
  border-radius: var(--radius-md);
  text-align: center;
}
.info-msg {
  font-size: 13px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  padding: 10px 14px;
  border-radius: var(--radius-md);
  line-height: 1.5;
  text-align: center;
}
.btn-primary {
  margin-top: 4px;
  padding: 15px;
  border-radius: var(--radius-pill);
  background: var(--color-btn);
  color: var(--color-btn-text);
  font-size: 15px;
  font-weight: 700;
  width: 100%;
  transition: opacity 0.2s, transform 0.15s;
  box-shadow: var(--shadow-soft);
}
.btn-primary:active { opacity: 0.85; transform: scale(0.98); }

.login-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding-top: 4px;
}
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
.hint {
  text-align: center;
  font-size: 11px;
  color: var(--color-text-secondary);
  opacity: 0.7;
}
</style>
