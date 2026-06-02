<template>
  <div class="app-shell admin-user-detail">
    <!-- 헤더 -->
    <header class="admin-header">
      <button class="back-btn" @click="router.push('/admin')">&#8249;</button>
      <span class="admin-title">사용자 상세</span>
      <div style="width: 32px"></div>
    </header>

    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
    </div>

    <div v-else-if="profile" class="detail-body">

      <!-- 프로필 사진 -->
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

      <!-- 기본 정보 카드 -->
      <div class="info-card">
        <!-- 이름 -->
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
            <template v-if="!editingLoginId">
              <span class="info-value">{{ profile.loginId }}</span>
              <button class="btn-edit-inline" @click="startEditLoginId">변경</button>
            </template>
            <template v-else>
              <div class="id-change-wrap">
                <div class="id-row">
                  <input
                    v-model="newLoginId"
                    class="inline-input"
                    placeholder="새 아이디"
                    @input="onIdInput"
                  />
                  <button
                    class="btn-check"
                    :disabled="!canCheck || checkLoading"
                    @click="checkDuplicate"
                  >{{ checkLoading ? '...' : '확인' }}</button>
                </div>
                <p v-if="idCheckResult === 'available'" class="msg-ok">✓ 사용 가능</p>
                <p v-if="idCheckResult === 'taken'" class="msg-err">✗ 이미 사용 중</p>
                <div class="id-actions">
                  <button class="btn-save-inline" @click="saveLoginId" :disabled="idSaving">저장</button>
                  <button class="btn-cancel-inline" @click="cancelEditLoginId">취소</button>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 소속 회사 관리 -->
      <div class="section-card">
        <h3 class="section-title">소속 회사</h3>
        <div v-if="userCompanies.length === 0" class="empty-sm">소속 회사가 없습니다.</div>
        <div class="affil-list">
          <div v-for="c in userCompanies" :key="c.id" class="affil-row">
            <span class="affil-name">{{ c.name }}</span>
            <div class="affil-actions">
              <button
                class="btn-badge"
                :class="c.isPrimary ? 'badge-primary' : 'badge-normal'"
                @click="setPrimaryCompany(c.id)"
                :disabled="c.isPrimary"
              >{{ c.isPrimary ? '대표' : '대표 지정' }}</button>
              <button class="btn-remove" @click="removeCompany(c.id)">해제</button>
            </div>
          </div>
        </div>
        <!-- 회사 추가 -->
        <div class="add-affil-row">
          <select v-model="selectedCompanyId" class="form-input select-sm">
            <option :value="null">회사 선택...</option>
            <option v-for="c in availableCompanies" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
          <button class="btn-sm btn-add-affil" :disabled="!selectedCompanyId" @click="addCompany">추가</button>
        </div>
      </div>

      <!-- 활동 팀 관리 -->
      <div class="section-card">
        <h3 class="section-title">활동 팀</h3>
        <div v-if="userTeams.length === 0" class="empty-sm">활동 팀이 없습니다.</div>
        <div class="affil-list">
          <div v-for="t in userTeams" :key="t.id" class="affil-row">
            <div class="affil-name-row">
              <span class="affil-dot" :style="{ background: DOT_COLORS[t.category] }"></span>
              <span class="affil-name">{{ t.name }}</span>
            </div>
            <div class="affil-actions">
              <button
                class="btn-badge"
                :class="t.isPrimary ? 'badge-primary' : 'badge-normal'"
                @click="setPrimaryTeam(t.id)"
                :disabled="t.isPrimary"
              >{{ t.isPrimary ? '대표' : '대표 지정' }}</button>
              <button class="btn-remove" @click="removeTeam(t.id)">해제</button>
            </div>
          </div>
        </div>
        <!-- 팀 추가 -->
        <div class="add-affil-row">
          <select v-model="selectedTeamId" class="form-input select-sm">
            <option :value="null">팀 선택...</option>
            <option v-for="t in availableTeams" :key="t.id" :value="t.id">{{ t.name }} ({{ CATEGORY_LABELS[t.category] }})</option>
          </select>
          <button class="btn-sm btn-add-affil" :disabled="!selectedTeamId" @click="addTeam">추가</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminApi } from '@/api/admin'
import { authApi } from '@/api/auth'
import type { UserProfile, Company, Team, Category, CompanyRef, TeamRef } from '@/types'
import { CATEGORY_LABELS } from '@/types'

const router = useRouter()
const route = useRoute()
const userId = Number(route.params.id)

const profile = ref<UserProfile | null>(null)
const loading = ref(true)
const userCompanies = ref<CompanyRef[]>([])
const userTeams = ref<TeamRef[]>([])
const allCompanies = ref<Company[]>([])
const allTeams = ref<Team[]>([])

// 이름 편집
const editingName = ref(false)
const nameInput = ref('')
const nameSaving = ref(false)

// 아이디 편집
const editingLoginId = ref(false)
const newLoginId = ref('')
const idCheckResult = ref<'none' | 'available' | 'taken'>('none')
const checkLoading = ref(false)
const idSaving = ref(false)

// 소속 선택
const selectedCompanyId = ref<number | null>(null)
const selectedTeamId = ref<number | null>(null)

// 이미지
const fileInputRef = ref<HTMLInputElement | null>(null)

const DOT_COLORS: Record<Category, string> = {
  BASEBALL:          '#F44336',
  BASKETBALL:        '#FF9800',
  SOCCER:            '#4CAF50',
  WOMENS_VOLLEYBALL: '#9C27B0',
  MENS_VOLLEYBALL:   '#9C27B0',
  ETC:               '#607D8B',
}

const canCheck = computed(() => /^[a-z][a-z0-9]{3,19}$/.test(newLoginId.value))

const availableCompanies = computed(() => {
  const linked = new Set(userCompanies.value.map(c => c.id))
  return allCompanies.value.filter(c => !linked.has(c.id) && c.isActive)
})

const availableTeams = computed(() => {
  const linked = new Set(userTeams.value.map(t => t.id))
  return allTeams.value.filter(t => !linked.has(t.id) && t.isActive)
})

function avatarColor(name: string): string {
  const colors = ['#1976D2','#00897B','#F4511E','#9C27B0','#FF9800','#607D8B','#E91E63']
  return colors[(name?.charCodeAt(0) ?? 0) % colors.length]
}

async function loadData() {
  loading.value = true
  try {
    const [profileRes, companiesRes, userCompRes, userTeamRes, teamsRes] = await Promise.all([
      adminApi.getUserProfile(userId),
      adminApi.getCompanies(),
      adminApi.getUserCompanies(userId),
      adminApi.getUserTeams(userId),
      adminApi.getTeams(),
    ])
    profile.value = profileRes.data
    allCompanies.value = companiesRes.data
    allTeams.value = teamsRes.data
    userCompanies.value = userCompRes.data ?? profileRes.data.companies ?? []
    userTeams.value = userTeamRes.data ?? profileRes.data.teams ?? []
  } catch {
    router.push('/admin')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

// 이름
function startEditName() { nameInput.value = profile.value?.name ?? ''; editingName.value = true }
function cancelEditName() { editingName.value = false }
async function saveName() {
  if (!nameInput.value.trim()) return
  nameSaving.value = true
  try {
    await adminApi.updateUserProfile(userId, { name: nameInput.value.trim() })
    if (profile.value) profile.value.name = nameInput.value.trim()
    editingName.value = false
  } catch (e: any) {
    alert(e.response?.data?.message || '이름 변경에 실패했습니다.')
  } finally { nameSaving.value = false }
}

// 아이디
function startEditLoginId() {
  newLoginId.value = ''
  idCheckResult.value = 'none'
  editingLoginId.value = true
}
function cancelEditLoginId() { editingLoginId.value = false }
function onIdInput() { idCheckResult.value = 'none' }

async function checkDuplicate() {
  if (!canCheck.value) return
  checkLoading.value = true
  try {
    const res = await authApi.checkLoginId(newLoginId.value)
    idCheckResult.value = res.data.available ? 'available' : 'taken'
  } catch { idCheckResult.value = 'none' }
  finally { checkLoading.value = false }
}

async function saveLoginId() {
  if (!canCheck.value) { alert('아이디 형식이 올바르지 않습니다.'); return }
  if (idCheckResult.value === 'none') { alert('아이디 중복 확인을 해주세요.'); return }
  if (idCheckResult.value === 'taken') { alert('이미 사용 중인 아이디입니다.'); return }
  idSaving.value = true
  try {
    await adminApi.changeUserLoginId(userId, { newLoginId: newLoginId.value })
    if (profile.value) profile.value.loginId = newLoginId.value
    editingLoginId.value = false
  } catch (e: any) {
    alert(e.response?.data?.message || '아이디 변경에 실패했습니다.')
  } finally { idSaving.value = false }
}

// 이미지
function triggerImageUpload() { fileInputRef.value?.click() }
async function handleImageChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    let res
    if (profile.value?.profileImageUrl) {
      res = await adminApi.updateUserProfileImage(userId, file)
    } else {
      res = await adminApi.uploadUserProfileImage(userId, file)
    }
    if (profile.value) profile.value.profileImageUrl = res.data.profileImageUrl ?? res.data.url ?? null
    await loadData()
  } catch (e: any) {
    alert(e.response?.data?.message || '이미지 업로드에 실패했습니다.')
  }
  if (fileInputRef.value) fileInputRef.value.value = ''
}
async function deleteImage() {
  if (!confirm('프로필 사진을 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteUserProfileImage(userId)
    if (profile.value) profile.value.profileImageUrl = null
  } catch (e: any) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  }
}

// 소속 회사
async function addCompany() {
  if (!selectedCompanyId.value) return
  try {
    await adminApi.addUserCompany(userId, selectedCompanyId.value)
    const c = allCompanies.value.find(x => x.id === selectedCompanyId.value)
    if (c) userCompanies.value.push({ id: c.id, name: c.name, isPrimary: userCompanies.value.length === 0 })
    selectedCompanyId.value = null
  } catch (e: any) { alert(e.response?.data?.message || '추가에 실패했습니다.') }
}
async function setPrimaryCompany(companyId: number) {
  try {
    await adminApi.setPrimaryUserCompany(userId, companyId)
    userCompanies.value.forEach(c => c.isPrimary = c.id === companyId)
  } catch (e: any) { alert(e.response?.data?.message || '변경에 실패했습니다.') }
}
async function removeCompany(companyId: number) {
  if (!confirm('소속 회사를 해제하시겠습니까?')) return
  try {
    await adminApi.removeUserCompany(userId, companyId)
    userCompanies.value = userCompanies.value.filter(c => c.id !== companyId)
  } catch (e: any) { alert(e.response?.data?.message || '해제에 실패했습니다.') }
}

// 소속 팀
async function addTeam() {
  if (!selectedTeamId.value) return
  try {
    await adminApi.addUserTeam(userId, selectedTeamId.value)
    const t = allTeams.value.find(x => x.id === selectedTeamId.value)
    if (t) userTeams.value.push({ id: t.id, name: t.name, category: t.category, isPrimary: userTeams.value.length === 0 })
    selectedTeamId.value = null
  } catch (e: any) { alert(e.response?.data?.message || '추가에 실패했습니다.') }
}
async function setPrimaryTeam(teamId: number) {
  try {
    await adminApi.setPrimaryUserTeam(userId, teamId)
    userTeams.value.forEach(t => t.isPrimary = t.id === teamId)
  } catch (e: any) { alert(e.response?.data?.message || '변경에 실패했습니다.') }
}
async function removeTeam(teamId: number) {
  if (!confirm('소속 팀을 해제하시겠습니까?')) return
  try {
    await adminApi.removeUserTeam(userId, teamId)
    userTeams.value = userTeams.value.filter(t => t.id !== teamId)
  } catch (e: any) { alert(e.response?.data?.message || '해제에 실패했습니다.') }
}
</script>

<style scoped>
.admin-user-detail {
  min-height: 100vh;
  background: var(--color-background);
  overflow-y: auto;
}
.admin-header {
  position: sticky; top: 0; z-index: 50;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 52px;
  background: var(--color-status-bar); color: #fff;
}
.back-btn { font-size: 26px; color: #fff; width: 32px; display: flex; align-items: center; }
.admin-title { font-size: 17px; font-weight: 700; }

.loading-wrap {
  display: flex; align-items: center; justify-content: center; padding: 80px 0;
}
.loading-spinner {
  width: 36px; height: 36px; border-radius: 50%;
  border: 3px solid var(--color-separator);
  border-top-color: var(--color-primary);
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.detail-body {
  padding: 20px 16px;
  display: flex; flex-direction: column; gap: 16px;
}

.avatar-section {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 16px 0 4px;
}
.avatar-wrap {
  position: relative; width: 80px; height: 80px;
  border-radius: 50%; cursor: pointer; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 32px; font-weight: 700;
}
.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.avatar-camera { font-size: 20px; }
.hidden-input { display: none; }
.btn-img-delete {
  font-size: 12px; color: #C62828;
  background: #FFEBEE; padding: 4px 10px;
  border-radius: 8px; font-weight: 600;
}

.info-card {
  background: var(--color-card);
  border: 1px solid var(--color-separator);
  border-radius: 14px; padding: 4px 0;
  box-shadow: var(--shadow-card);
}
.info-row {
  display: flex; align-items: flex-start; justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-separator);
  gap: 10px;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); min-width: 48px; padding-top: 2px; }
.info-value-wrap { display: flex; align-items: flex-start; gap: 10px; flex: 1; justify-content: flex-end; flex-wrap: wrap; }
.info-value { font-size: 15px; font-weight: 500; color: var(--color-text); }

.inline-input {
  padding: 7px 10px; border-radius: 8px;
  border: 1.5px solid var(--color-primary);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none;
}
.btn-edit-inline {
  font-size: 12px; color: var(--color-primary); font-weight: 600;
  padding: 4px 10px; border-radius: 8px; background: var(--color-primary-light);
}
.btn-save-inline {
  font-size: 12px; color: var(--color-on-primary); font-weight: 700;
  padding: 4px 12px; border-radius: 8px; background: var(--color-primary);
  transition: opacity 0.2s;
}
.btn-save-inline:disabled { opacity: 0.5; }
.btn-cancel-inline {
  font-size: 12px; color: var(--color-text-secondary);
  padding: 4px 10px; border-radius: 8px; background: var(--color-surface);
  border: 1px solid var(--color-separator);
}

.id-change-wrap { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.id-row { display: flex; gap: 6px; }
.btn-check {
  padding: 7px 10px; border-radius: 8px;
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 12px; font-weight: 700; white-space: nowrap;
  transition: opacity 0.2s;
}
.btn-check:disabled { opacity: 0.45; cursor: not-allowed; }
.id-actions { display: flex; gap: 6px; justify-content: flex-end; }
.msg-ok  { font-size: 12px; color: #2E7D32; }
.msg-err { font-size: 12px; color: #F44336; }

.section-card {
  background: var(--color-card);
  border: 1px solid var(--color-separator);
  border-radius: 14px; padding: 16px;
  box-shadow: var(--shadow-card);
}
.section-title { font-size: 14px; font-weight: 700; color: var(--color-text); margin-bottom: 12px; }
.empty-sm { font-size: 13px; color: var(--color-text-secondary); padding: 4px 0 8px; }

.affil-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.affil-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 10px; border-radius: 10px;
  background: var(--color-surface); gap: 8px;
}
.affil-name-row { display: flex; align-items: center; gap: 6px; }
.affil-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.affil-name { font-size: 13px; font-weight: 500; color: var(--color-text); }
.affil-actions { display: flex; gap: 6px; align-items: center; }
.btn-badge {
  font-size: 11px; font-weight: 600;
  padding: 3px 8px; border-radius: 8px; cursor: pointer;
}
.badge-primary { background: var(--color-primary); color: var(--color-on-primary); }
.badge-normal  { background: var(--color-primary-light); color: var(--color-primary); }
.badge-normal:disabled { opacity: 1; }
.btn-remove {
  font-size: 11px; color: #C62828; font-weight: 600;
  padding: 3px 8px; border-radius: 8px; background: #FFEBEE;
}

.add-affil-row { display: flex; gap: 8px; align-items: center; margin-top: 4px; }
.form-input {
  padding: 9px 11px; border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 13px; outline: none;
}
.form-input:focus { border-color: var(--color-primary); }
.select-sm { flex: 1; }
.btn-sm {
  padding: 8px 14px; border-radius: 10px;
  font-size: 13px; font-weight: 700; cursor: pointer;
}
.btn-add-affil {
  background: var(--color-primary); color: var(--color-on-primary);
  transition: opacity 0.2s;
}
.btn-add-affil:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
