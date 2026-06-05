<template>
  <div class="shell shell-nav">
    <header class="topbar">
      <span class="topbar-title">내 정보</span>
      <div style="display:flex;align-items:center;gap:4px;margin-left:auto">
        <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
          <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
        </button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃">
          <AppIcon name="logout" size="md" />
        </button>
      </div>
    </header>

    <div class="profile-body">
      <!-- Avatar section -->
      <div class="avatar-section">
        <div class="profile-avatar-wrap" @click="triggerImageUpload">
          <img v-if="profileImageUrl" :src="profileImageUrl" class="profile-avatar-img" alt="프로필" />
          <div v-else class="profile-avatar-placeholder">{{ authStore.user?.name?.[0] }}</div>
          <div class="profile-avatar-overlay"><AppIcon name="camera" size="md" style="color:#fff" /></div>
        </div>
        <div class="profile-info">
          <span class="profile-name">{{ authStore.user?.name }}</span>
          <span class="profile-id">@{{ authStore.user?.username }}</span>
          <span class="profile-hint">탭해서 사진 변경</span>
        </div>
        <input ref="imageInputRef" type="file" accept="image/jpeg,image/png,image/webp" class="hidden-input" @change="handleImageUpload" />
      </div>

      <!-- Theme picker -->
      <div style="padding: 16px 16px 0;">
        <p style="font-size:13px;font-weight:600;color:var(--color-text-2);margin-bottom:12px">테마</p>
        <div class="theme-picker-grid">
          <div v-for="t in themes" :key="t.key"
            :class="['theme-option', { selected: themeStore.current === t.key }]"
            @click="themeStore.setTheme(t.key)">
            <div class="theme-swatch" :style="{background: t.color}"></div>
            <span class="theme-name">{{ t.label }}</span>
          </div>
        </div>
      </div>

      <!-- Menu items -->
      <div class="card menu-list" style="margin: 16px; border-radius: 14px; overflow: hidden;">
        <button class="menu-item" @click="showPwChange = !showPwChange">
          <span class="menu-icon"><AppIcon name="key" size="sm" style="color:var(--color-primary)" /></span>
          <span class="menu-label">비밀번호 변경</span>
          <AppIcon :name="showPwChange ? 'chevron-down' : 'chevron-right'" size="sm" class="menu-arrow" />
        </button>
        <div v-if="showPwChange" class="pw-expand">
          <div class="pw-fields">
            <input v-model="currentPw" type="password" class="pw-input" placeholder="현재 비밀번호" />
            <input v-model="newPw" type="password" class="pw-input" placeholder="새 비밀번호 (6자 이상)" />
            <input v-model="newPwConfirm" type="password" class="pw-input" placeholder="새 비밀번호 확인" />
          </div>
          <p v-if="pwError" class="form-err">{{ pwError }}</p>
          <p v-if="pwSuccess" class="pw-success">비밀번호가 변경됐습니다.</p>
          <button class="btn-pw-save" @click="handlePwChange">변경하기</button>
        </div>
      </div>

      <!-- Logout -->
      <div style="padding: 0 16px;">
        <button class="btn-logout" @click="handleLogout">
          <AppIcon name="logout" size="sm" />
          로그아웃
        </button>
      </div>
    </div>

    <BottomNavUser />
    <ThemeSheet :open="themeSheetOpen" @close="themeSheetOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { userApi } from '@/api/user'
import type { ThemeKey } from '@/types'
import BottomNavUser from '@/components/BottomNavUser.vue'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',  color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',     color: '#E8836A', label: '피치' },
  { key: 'mint',      color: '#4DB896', label: '민트' },
  { key: 'dark',      color: '#A695F0', label: '다크' },
  { key: 'rose-milk', color: '#D4789A', label: '로즈' },
]

const profileImageUrl = ref<string | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)
const themeSheetOpen = ref(false)
const showPwChange = ref(false)
const currentPw = ref('')
const newPw = ref('')
const newPwConfirm = ref('')
const pwError = ref('')
const pwSuccess = ref(false)

async function loadProfileImage() {
  try {
    const res = await userApi.getMe()
    profileImageUrl.value = res.data.profileImageUrl || null
  } catch {}
}

function triggerImageUpload() { imageInputRef.value?.click() }

async function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const res = await userApi.uploadMyProfileImage(file)
    profileImageUrl.value = res.data.profileImageUrl
  } catch (err: any) {
    alert(err.response?.data?.message || '이미지 업로드에 실패했습니다.')
  }
  if (imageInputRef.value) imageInputRef.value.value = ''
}

async function handlePwChange() {
  pwError.value = ''
  pwSuccess.value = false
  if (!currentPw.value) { pwError.value = '현재 비밀번호를 입력하세요.'; return }
  if (newPw.value.length < 6) { pwError.value = '6자 이상 입력하세요.'; return }
  if (newPw.value !== newPwConfirm.value) { pwError.value = '비밀번호가 일치하지 않습니다.'; return }
  try {
    await userApi.changePassword(currentPw.value, newPw.value)
    currentPw.value = ''; newPw.value = ''; newPwConfirm.value = ''
    pwSuccess.value = true
  } catch (e: any) {
    pwError.value = e.response?.data?.message || '변경에 실패했습니다.'
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

onMounted(() => { loadProfileImage() })
</script>

<style scoped>
.profile-body { padding-top: 16px; }
.avatar-section { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 24px 16px; }
.profile-avatar-wrap { position: relative; width: 80px; height: 80px; cursor: pointer; }
.profile-avatar-img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
.profile-avatar-placeholder { width: 80px; height: 80px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 30px; font-weight: 700; }
.profile-avatar-overlay { position: absolute; inset: 0; border-radius: 50%; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; font-size: 20px; opacity: 0; transition: opacity 0.2s; }
.profile-avatar-wrap:hover .profile-avatar-overlay { opacity: 1; }
.profile-info { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.profile-name { font-size: 20px; font-weight: 700; color: var(--color-text-1, var(--color-text-primary)); }
.profile-id { font-size: 14px; color: var(--color-text-2, var(--color-text-secondary)); }
.profile-hint { font-size: 11px; color: var(--color-text-2, var(--color-text-secondary)); opacity: 0.7; }
.hidden-input { display: none; }
.pw-expand { padding: 0 16px 16px; display: flex; flex-direction: column; gap: 10px; border-top: 1px solid var(--color-border); }
.pw-fields { display: flex; flex-direction: column; gap: 8px; padding-top: 12px; }
.pw-input { padding: 12px 14px; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: var(--color-surface-2, var(--color-surface-muted)); color: var(--color-text-1, var(--color-text-primary)); font-size: 14px; outline: none; width: 100%; }
.pw-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-soft); background: var(--color-surface); }
.form-err { font-size: 12px; color: var(--color-danger); }
.pw-success { font-size: 12px; color: var(--color-success); }
.btn-pw-save { padding: 12px; border-radius: 999px; background: var(--color-btn); color: var(--color-btn-text); font-size: 14px; font-weight: 700; width: 100%; box-shadow: var(--shadow-soft); }
.btn-logout { width: 100%; padding: 14px; border-radius: 999px; background: var(--color-danger-soft); color: var(--color-danger); font-size: 15px; font-weight: 700; border: 1.5px solid var(--color-danger-soft); display: flex; align-items: center; justify-content: center; gap: 8px; }
</style>
