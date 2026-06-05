<template>
  <div class="login-page">
    <!-- Theme button top-right -->
    <div style="display:flex;justify-content:flex-end;padding:12px 0 0">
      <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
        <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
      </button>
    </div>

    <!-- Brand header -->
    <div class="login-brand">
      <div class="login-brand-icon">
        <AppIcon name="calendar" size="lg" style="color: var(--color-primary)" />
      </div>
      <h1 class="login-brand-title">스케줄 관리</h1>
      <p class="login-brand-sub">나의 일정을 간편하게 관리하세요</p>
    </div>

    <!-- Form -->
    <div class="login-form-area">
      <p v-if="loginIdChangedMsg" class="login-info-msg">
        <AppIcon name="alert" size="sm" /> {{ loginIdChangedMsg }}
      </p>
      <form @submit.prevent="handleLogin">
        <div class="field">
          <label class="field-label">아이디</label>
          <input v-model="form.username" type="text" class="field-input" placeholder="아이디" autocomplete="username" />
        </div>
        <div class="field" style="margin-top:12px">
          <label class="field-label">비밀번호</label>
          <input v-model="form.password" type="password" class="field-input" placeholder="비밀번호" autocomplete="current-password" />
        </div>
        <p v-if="authStore.error" class="field-err" style="margin-top:6px">{{ authStore.error }}</p>
        <button type="submit" class="btn btn-primary btn-full" style="margin-top:20px">로그인</button>
      </form>

      <div class="login-footer">
        <RouterLink to="/register" class="login-register-link">회원가입 신청</RouterLink>
        <span class="login-hint">관리자 승인 후 이용 가능합니다</span>
      </div>
    </div>

    <ThemeSheet :open="themeSheetOpen" @close="themeSheetOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const themeSheetOpen = ref(false)

const form = reactive({ username: '', password: '' })

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
.login-page {
  min-height: 100svh; background: var(--color-bg);
  display: flex; flex-direction: column;
  padding: 0 24px;
  max-width: 430px; margin: 0 auto; width: 100%;
}
.login-brand {
  padding-top: max(60px, 15vh);
  padding-bottom: 40px;
  text-align: center;
}
.login-brand-icon {
  width: 56px; height: 56px; border-radius: 14px;
  background: var(--color-primary-soft, var(--color-primary-light));
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
}
.login-brand-title { font-size: 22px; font-weight: 800; color: var(--color-text-1); margin-bottom: 6px; }
.login-brand-sub { font-size: 14px; color: var(--color-text-2); }
.login-form-area { flex: 1; }
.login-info-msg {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: var(--color-primary);
  background: var(--color-primary-soft, var(--color-primary-light));
  padding: 10px 14px; border-radius: 8px; margin-bottom: 16px;
}
.login-footer {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  margin-top: 24px; padding-bottom: max(32px, env(safe-area-inset-bottom, 16px));
}
.login-register-link { font-size: 14px; font-weight: 600; color: var(--color-primary); }
.login-hint { font-size: 12px; color: var(--color-text-3, var(--color-text-secondary)); }
</style>
