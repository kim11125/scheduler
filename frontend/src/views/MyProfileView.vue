<template>
  <div class="app-shell profile-view">
    <!-- 헤더 -->
    <header class="app-header">
      <button class="back-btn" @click="goBack">&#8249;</button>
      <span class="header-title">내 정보</span>
      <div style="width: 32px"></div>
    </header>

    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
    </div>

    <div v-else-if="profile" class="profile-body">

      <!-- 프로필 사진 섹션 -->
      <div class="avatar-section">
        <div class="avatar-wrap" @click="triggerImageUpload">
          <img v-if="profile.profileImageUrl" :src="profile.profileImageUrl" class="avatar-img" alt="프로필" />
          <div v-else class="avatar-placeholder" :style="{ background: avatarColor(profile.name) }">
            {{ profile.name?.[0] ?? '?' }}
          </div>
          <div class="avatar-overlay">
            <span class="avatar-camera">📷</span>
          </div>
        </div>
        <input ref="fileInputRef" type="file" accept="image/*" class="hidden-input" @change="handleImageChange" />
        <button v-if="profile.profileImageUrl" class="btn-img-delete" @click.stop="deleteImage">삭제</button>
      </div>

      <!-- 이름 -->
      <div class="info-card">
        <div class="info-row">
          <span class="info-label">이름</span>
          <div class="info-value-wrap">
            <template v-if="!editingName">
              <span class="info-value">{{ profile.name }}</span>
              <button class="btn-edit-inline" @click="startEditName">수정</button>
            </template>
            <template v-else>
              <input v-model="nameInput" class="inline-input" @keyup.enter="saveName" @keyup.escape="cancelEditName" />
              <button class="btn-save-inline" @click="saveName" :disabled="nameSaving">저장</button>
              <button class="btn-cancel-inline" @click="cancelEditName">취소</button>
            </template>
          </div>
        </div>

        <!-- 아이디 -->
        <div class="info-row">
          <span class="info-label">아이디</span>
          <div class="info-value-wrap">
            <span class="info-value">{{ profile.loginId }}</span>
            <button class="btn-edit-inline" @click="showChangeIdModal = true">변경</button>
          </div>
        </div>
      </div>

      <!-- 소속 회사 -->
      <div class="section-card" v-if="profile.companies.length > 0">
        <h3 class="section-title">소속 회사</h3>
        <div class="tag-list">
          <span
            v-for="c in profile.companies"
            :key="c.id"
            class="tag"
            :class="{ 'tag-primary': c.isPrimary }"
          >
            {{ c.name }}
            <span v-if="c.isPrimary" class="primary-mark">대표</span>
          </span>
        </div>
      </div>

      <!-- 활동 팀 -->
      <div class="section-card" v-if="profile.teams.length > 0">
        <h3 class="section-title">활동 팀</h3>
        <div class="tag-list">
          <span
            v-for="t in profile.teams"
            :key="t.id"
            class="tag"
            :class="{ 'tag-primary': t.isPrimary }"
          >
            <span class="tag-dot" :style="{ background: DOT_COLORS[t.category] }"></span>
            {{ t.name }}
            <span v-if="t.isPrimary" class="primary-mark">대표</span>
          </span>
        </div>
      </div>

    </div>

    <!-- 아이디 변경 모달 -->
    <ChangeLoginIdModal v-if="showChangeIdModal" @close="showChangeIdModal = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import type { UserProfile, Category } from '@/types'
import ChangeLoginIdModal from './ChangeLoginIdModal.vue'

const router = useRouter()
const authStore = useAuthStore()

const profile = ref<UserProfile | null>(null)
const loading = ref(true)
const showChangeIdModal = ref(false)

// 이름 편집
const editingName = ref(false)
const nameInput = ref('')
const nameSaving = ref(false)

// 이미지
const fileInputRef = ref<HTMLInputElement | null>(null)

const DOT_COLORS: Record<Category, string> = {
  BASEBALL:          'var(--color-dot-baseball)',
  BASKETBALL:        'var(--color-dot-basketball)',
  SOCCER:            'var(--color-dot-soccer)',
  WOMENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  MENS_VOLLEYBALL:   'var(--color-dot-volleyball)',
  ETC:               'var(--color-dot-etc)',
}

function avatarColor(name: string): string {
  const colors = ['#1976D2','#00897B','#F4511E','#9C27B0','#FF9800','#607D8B','#E91E63']
  return colors[(name?.charCodeAt(0) ?? 0) % colors.length]
}

function goBack() {
  const role = authStore.user?.role
  if (role === 'ADMIN' || role === 'MANAGER') router.push('/admin')
  else router.push('/')
}

async function loadProfile() {
  loading.value = true
  try {
    const res = await userApi.getMyProfile()
    profile.value = res.data
  } catch {
    goBack()
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)

function startEditName() {
  nameInput.value = profile.value?.name ?? ''
  editingName.value = true
}

function cancelEditName() {
  editingName.value = false
}

async function saveName() {
  if (!nameInput.value.trim()) return
  nameSaving.value = true
  try {
    await userApi.updateMyProfile({ name: nameInput.value.trim() })
    if (profile.value) profile.value.name = nameInput.value.trim()
    // auth store의 name도 업데이트
    if (authStore.user) {
      authStore.user = { ...authStore.user, name: nameInput.value.trim() }
      localStorage.setItem('auth_user', JSON.stringify(authStore.user))
    }
    editingName.value = false
  } catch (e: any) {
    alert(e.response?.data?.message || '이름 변경에 실패했습니다.')
  } finally {
    nameSaving.value = false
  }
}

function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function handleImageChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    let res
    if (profile.value?.profileImageUrl) {
      res = await userApi.updateMyProfileImage(file)
    } else {
      res = await userApi.uploadMyProfileImage(file)
    }
    if (profile.value) profile.value.profileImageUrl = res.data.profileImageUrl ?? res.data.url ?? null
    await loadProfile()
  } catch (e: any) {
    alert(e.response?.data?.message || '이미지 업로드에 실패했습니다.')
  }
  // 파일 input 초기화
  if (fileInputRef.value) fileInputRef.value.value = ''
}

async function deleteImage() {
  if (!confirm('프로필 사진을 삭제하시겠습니까?')) return
  try {
    await userApi.deleteMyProfileImage()
    if (profile.value) profile.value.profileImageUrl = null
  } catch (e: any) {
    alert(e.response?.data?.message || '이미지 삭제에 실패했습니다.')
  }
}
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background: var(--color-background);
  overflow-y: auto;
}

.app-header {
  position: sticky; top: 0; z-index: 50;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 52px;
  background: var(--color-status-bar); color: #fff;
}
.back-btn { font-size: 26px; color: #fff; width: 32px; display: flex; align-items: center; }
.header-title { font-size: 17px; font-weight: 700; }

.loading-wrap {
  display: flex; align-items: center; justify-content: center;
  padding: 80px 0;
}
.loading-spinner {
  width: 36px; height: 36px; border-radius: 50%;
  border: 3px solid var(--color-separator);
  border-top-color: var(--color-primary);
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.profile-body {
  padding: 20px 16px;
  display: flex; flex-direction: column; gap: 16px;
}

/* 아바타 */
.avatar-section {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 24px 0 8px;
}
.avatar-wrap {
  position: relative; width: 90px; height: 90px;
  border-radius: 50%; cursor: pointer; overflow: hidden;
}
.avatar-img {
  width: 100%; height: 100%; object-fit: cover;
}
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 36px; font-weight: 700;
}
.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.avatar-camera { font-size: 22px; }
.hidden-input { display: none; }
.btn-img-delete {
  font-size: 12px; color: #C62828;
  background: #FFEBEE; padding: 5px 12px;
  border-radius: 8px; font-weight: 600;
}

/* 정보 카드 */
.info-card {
  background: var(--color-card);
  border: 1px solid var(--color-separator);
  border-radius: 14px;
  padding: 4px 0;
  box-shadow: var(--shadow-card);
}
.info-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-separator);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  font-size: 13px; font-weight: 600; color: var(--color-text-secondary);
  min-width: 52px;
}
.info-value-wrap {
  display: flex; align-items: center; gap: 10px; flex: 1; justify-content: flex-end;
}
.info-value { font-size: 15px; font-weight: 500; color: var(--color-text); }

.inline-input {
  padding: 7px 10px; border-radius: 8px;
  border: 1.5px solid var(--color-primary);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none; flex: 1;
}
.btn-edit-inline {
  font-size: 12px; color: var(--color-primary);
  font-weight: 600; padding: 4px 10px; border-radius: 8px;
  background: var(--color-primary-light);
}
.btn-save-inline {
  font-size: 12px; color: var(--color-on-primary);
  font-weight: 700; padding: 4px 12px; border-radius: 8px;
  background: var(--color-primary);
  transition: opacity 0.2s;
}
.btn-save-inline:disabled { opacity: 0.5; }
.btn-cancel-inline {
  font-size: 12px; color: var(--color-text-secondary);
  padding: 4px 10px; border-radius: 8px;
  background: var(--color-surface);
  border: 1px solid var(--color-separator);
}

/* 섹션 카드 */
.section-card {
  background: var(--color-card);
  border: 1px solid var(--color-separator);
  border-radius: 14px; padding: 16px;
  box-shadow: var(--shadow-card);
}
.section-title {
  font-size: 14px; font-weight: 700; color: var(--color-text);
  margin-bottom: 12px;
}
.tag-list { display: flex; flex-wrap: wrap; gap: 8px; }
.tag {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 12px; border-radius: 20px;
  background: var(--color-surface); color: var(--color-text);
  font-size: 13px; font-weight: 500;
  border: 1px solid var(--color-separator);
}
.tag-primary {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.tag-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.primary-mark {
  font-size: 10px; font-weight: 700;
  background: var(--color-primary); color: var(--color-on-primary);
  padding: 1px 5px; border-radius: 6px; margin-left: 2px;
}
</style>
