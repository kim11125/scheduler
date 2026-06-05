<template>
  <div class="shell shell-nav">
    <!-- Header: white, no color dots -->
    <header class="topbar">
      <span class="topbar-title">관리자</span>
      <div style="display:flex;align-items:center;gap:4px;margin-left:auto">
        <button class="topbar-icon-btn" @click="router.push('/my-schedules')">
          <AppIcon name="calendar" size="md" />
        </button>
        <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
          <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
        </button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃">
          <AppIcon name="logout" size="md" />
        </button>
      </div>
    </header>

    <div style="overflow-y:auto;flex:1">
      <!-- Pending approvals alert bar -->
      <div v-if="usersStore.pendingUsers.length > 0" class="pending-alert-bar" @click="scrollTo('pending')">
        <AppIcon name="alert" size="sm" />
        <span>승인 대기 {{ usersStore.pendingUsers.length }}명</span>
        <AppIcon name="chevron-right" size="sm" style="margin-left:auto" />
      </div>

      <!-- Compact summary -->
      <div class="admin-summary">
        <div class="admin-sum-item" @click="router.push('/admin/users')">
          <span class="admin-sum-num">{{ activeCount }}</span>
          <span class="admin-sum-label">활성 사용자</span>
        </div>
        <div class="admin-sum-sep"></div>
        <div class="admin-sum-item" @click="router.push('/admin/schedules')">
          <span class="admin-sum-num">{{ totalScheduleCount }}</span>
          <span class="admin-sum-label">전체 일정</span>
        </div>
      </div>

      <!-- Quick shortcuts - compact horizontal strip -->
      <div class="quick-strip">
        <button class="quick-strip-item" @click="router.push('/admin/users')">
          <span class="quick-strip-icon"><AppIcon name="users" size="md" /></span>
          <span class="quick-strip-label">사용자</span>
        </button>
        <button class="quick-strip-item" @click="router.push('/admin/schedules')">
          <span class="quick-strip-icon"><AppIcon name="calendar" size="md" /></span>
          <span class="quick-strip-label">일정</span>
        </button>
        <button class="quick-strip-item" @click="router.push('/admin/companies')">
          <span class="quick-strip-icon"><AppIcon name="building" size="md" /></span>
          <span class="quick-strip-label">회사</span>
        </button>
        <button class="quick-strip-item" @click="router.push('/admin/teams')">
          <span class="quick-strip-icon"><AppIcon name="team" size="md" /></span>
          <span class="quick-strip-label">팀</span>
        </button>
        <button class="quick-strip-item" @click="logsExpanded = !logsExpanded">
          <span class="quick-strip-icon"><AppIcon name="log" size="md" /></span>
          <span class="quick-strip-label">로그</span>
        </button>
      </div>

      <!-- Pending approvals section -->
      <div class="admin-section" ref="pendingRef" v-if="usersStore.pendingUsers.length > 0">
        <p class="admin-section-title">승인 대기</p>
        <div class="row-list">
          <div v-for="u in usersStore.pendingUsers.slice(0,3)" :key="u.id" class="row-item">
            <div class="avatar avatar-sm" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
            <div class="row-body">
              <div class="row-title">{{ u.name }}</div>
              <div class="row-sub">@{{ u.username }} · {{ formatDate(u.createdAt) }}</div>
            </div>
            <div style="display:flex;gap:6px">
              <button class="btn btn-primary btn-sm" @click.stop="handleApprove(u.id)">승인</button>
              <button class="btn btn-ghost" @click.stop="handleReject(u.id)">거절</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent users -->
      <div class="admin-section">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
          <p class="admin-section-title" style="margin-bottom:0">최근 사용자</p>
          <button class="btn btn-ghost" style="height:26px;font-size:11px;padding:0 8px" @click="router.push('/admin/users')">전체 보기</button>
        </div>
        <div class="row-list">
          <div v-for="u in recentUsers" :key="u.id" class="row-item" @click="openUserModal(u)">
            <div class="avatar avatar-sm" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
            <div class="row-body">
              <div class="row-title">{{ u.name }}</div>
              <div class="row-sub">@{{ u.username }}</div>
            </div>
            <span :class="['badge', statusBadgeClass(u.status)]">{{ STATUS_LABELS[u.status] }}</span>
            <AppIcon name="chevron-right" size="sm" style="color:var(--color-text-3)" />
          </div>
          <div v-if="recentUsers.length === 0" class="empty" style="padding:24px">
            <p class="empty-text">사용자가 없습니다</p>
          </div>
        </div>
      </div>

      <!-- Login log - collapsed by default -->
      <div class="admin-section" v-if="authStore.user?.role === 'ADMIN'" ref="logsRef">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
          <p class="admin-section-title" style="margin-bottom:0">로그인 로그</p>
          <button class="btn btn-ghost" style="height:26px;font-size:11px;padding:0 8px" @click="logsExpanded = !logsExpanded">
            {{ logsExpanded ? '접기' : '펼치기' }}
          </button>
        </div>
        <div v-if="logsExpanded" class="row-list">
          <div v-if="logs.length === 0" class="empty" style="padding:24px"><p class="empty-text">로그가 없습니다</p></div>
          <div v-for="log in logs.slice(0,10)" :key="log.id" class="row-item" style="padding:10px 16px">
            <span :class="['badge', log.action==='LOGIN' ? 'badge-active' : 'badge-disabled']" style="width:52px;justify-content:center">
              {{ log.action==='LOGIN' ? '로그인' : '로그아웃' }}
            </span>
            <div class="row-body" style="margin-left:8px">
              <div class="row-title" style="font-size:13px">{{ log.username }}</div>
              <div class="row-sub">{{ log.ipAddress || '-' }}</div>
            </div>
            <div style="font-size:11px;color:var(--color-text-3)">{{ formatLogDate(log.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div style="height:16px"></div>
    </div>

    <!-- User detail sheet -->
    <Teleport to="body">
      <div class="overlay" v-if="selectedUser" @click.self="closeUserModal">
        <div class="sheet sheet-full">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">사용자 정보</span>
            <button class="sheet-close" @click="closeUserModal"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <div style="display:flex;align-items:center;gap:14px;margin-bottom:16px">
              <div class="avatar avatar-lg" :style="{ background: avatarColor(selectedUser.name) }">{{ selectedUser.name[0] }}</div>
              <div>
                <div style="font-size:17px;font-weight:700;color:var(--color-text-1)">{{ selectedUser.name }}</div>
                <div style="font-size:13px;color:var(--color-text-2)">@{{ selectedUser.username }}</div>
                <span :class="['badge', `badge-${selectedUser.status.toLowerCase()}`]" style="margin-top:4px">{{ STATUS_LABELS[selectedUser.status] }}</span>
              </div>
            </div>
            <div class="divider-sm"></div>
            <div class="detail-row-item"><span class="detail-key">등록일</span><span class="detail-val">{{ formatDate(selectedUser.createdAt) }}</span></div>
            <div class="detail-row-item">
              <span class="detail-key">역할</span>
              <select v-if="authStore.user?.role === 'ADMIN'" class="field-input field-select" style="height:34px;font-size:13px;flex:1;max-width:160px"
                :value="selectedUser.role"
                @change="handleRoleChange(selectedUser.id, ($event.target as HTMLSelectElement).value)">
                <option value="USER">일반 사용자</option>
                <option value="MANAGER">일반 관리자</option>
                <option value="ADMIN">최고 관리자</option>
              </select>
              <span v-else class="detail-val">{{ ROLE_LABELS[selectedUser.role] || selectedUser.role }}</span>
            </div>
            <div class="divider-sm"></div>
            <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px">
              <button v-if="selectedUser.status === 'PENDING'" class="btn btn-primary btn-sm" @click="handleApprove(selectedUser.id)">승인</button>
              <button v-if="selectedUser.status === 'PENDING'" class="btn btn-danger btn-sm" @click="handleReject(selectedUser.id)">거절</button>
              <button v-if="selectedUser.status === 'ACTIVE'" class="btn btn-secondary btn-sm" @click="usersStore.disable(selectedUser.id)">비활성화</button>
              <button v-if="selectedUser.status === 'DISABLED'" class="btn btn-primary btn-sm" @click="usersStore.activate(selectedUser.id)">재활성화</button>
              <button v-if="selectedUser.status === 'REJECTED'" class="btn btn-primary btn-sm" @click="usersStore.activate(selectedUser.id)">승인</button>
              <button v-if="selectedUser.status === 'ACTIVE'" class="btn btn-outline btn-sm" @click="goToUserSchedules(selectedUser.id, selectedUser.name)">일정 보기</button>
            </div>
            <div class="divider-sm"></div>

            <!-- 소속 회사 -->
            <div class="field" style="margin-bottom:12px">
              <span class="field-label">소속 회사</span>
              <div v-if="userCompanies.length > 0" style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                <span v-for="uc in userCompanies" :key="uc.companyId" class="chip active" style="gap:6px">
                  {{ uc.companyName }}
                  <button @click="removeCompany(uc.companyId)" style="display:flex;align-items:center;color:inherit;opacity:0.7"><AppIcon name="close" size="sm" /></button>
                </span>
              </div>
              <div v-else style="font-size:13px;color:var(--color-text-3);margin-top:4px">없음</div>
              <div v-if="availableCompanies.length > 0" style="display:flex;gap:8px;margin-top:8px">
                <select v-model="addCompanyId" class="field-input field-select" style="height:36px;font-size:13px;flex:1">
                  <option :value="null">회사 선택...</option>
                  <option v-for="c in availableCompanies" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
                <button class="btn btn-primary btn-sm" :disabled="!addCompanyId" @click="addCompany">등록</button>
              </div>
            </div>
            <div class="divider-sm"></div>

            <!-- 소속 팀 -->
            <div class="field" style="margin-bottom:12px">
              <span class="field-label">소속 팀</span>
              <div v-if="userTeams.length > 0" style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                <span v-for="ut in userTeams" :key="ut.teamId" class="chip" :class="{ active: ut.isPrimary }" style="gap:6px">
                  {{ ut.teamName }}
                  <span v-if="ut.isPrimary" style="font-size:10px">(대표)</span>
                  <button v-if="!ut.isPrimary" @click="setPrimaryTeam(ut.teamId)" style="font-size:10px;color:var(--color-primary)">대표</button>
                  <button @click="removeTeam(ut.teamId)" style="display:flex;align-items:center;opacity:0.7"><AppIcon name="close" size="sm" /></button>
                </span>
              </div>
              <div v-else style="font-size:13px;color:var(--color-text-3);margin-top:4px">없음</div>
              <div v-if="availableTeams.length > 0" style="display:flex;gap:8px;margin-top:8px">
                <select v-model="addTeamId" class="field-input field-select" style="height:36px;font-size:13px;flex:1">
                  <option :value="null">팀 선택...</option>
                  <option v-for="t in availableTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <button class="btn btn-primary btn-sm" :disabled="!addTeamId" @click="addTeam">추가</button>
              </div>
            </div>
            <div class="divider-sm"></div>

            <!-- 비밀번호 변경 -->
            <div class="field"><span class="field-label">비밀번호 변경</span></div>
            <div class="field" style="margin-top:8px"><input v-model="newPw" type="password" class="field-input" placeholder="새 비밀번호 입력" /></div>
            <div class="field" style="margin-top:8px"><input v-model="newPwConfirm" type="password" class="field-input" placeholder="새 비밀번호 확인" /></div>
            <p v-if="pwError" class="field-err" style="margin-top:4px">{{ pwError }}</p>
            <button class="btn btn-primary btn-sm" style="margin-top:10px" @click="handleAdminPwChange">변경</button>
          </div>
        </div>
      </div>
    </Teleport>

    <BottomNavAdmin />
    <ThemeSheet :open="themeSheetOpen" @close="themeSheetOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useUsersStore } from '@/stores/users'
import { adminApi } from '@/api/admin'
import { companyApi } from '@/api/company'
import { teamApi } from '@/api/team'
import type { ThemeKey, UserStatus } from '@/types'
import type { ManagedUser } from '@/stores/users'
import BottomNavAdmin from '@/components/BottomNavAdmin.vue'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const usersStore = useUsersStore()
const totalScheduleCount = ref(0)
const logsExpanded = ref(false)
const themeSheetOpen = ref(false)

const pendingRef = ref<HTMLElement | null>(null)
const logsRef   = ref<HTMLElement | null>(null)

function statusBadgeClass(status: string) {
  const map: Record<string, string> = { ACTIVE: 'badge-active', PENDING: 'badge-pending', REJECTED: 'badge-rejected', DISABLED: 'badge-disabled' }
  return map[status] || 'badge-disabled'
}

function scrollTo(target: 'pending' | 'logs') {
  const el = target === 'pending' ? pendingRef.value : logsRef.value
  el?.scrollIntoView({ behavior: 'smooth' })
}

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',  color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',     color: '#E8836A', label: '피치' },
  { key: 'mint',      color: '#4DB896', label: '민트' },
  { key: 'dark',      color: '#A695F0', label: '다크' },
  { key: 'rose-milk', color: '#D4789A', label: '로즈' },
]

const STATUS_FILTERS: { value: UserStatus | 'ALL'; label: string }[] = [
  { value: 'ALL',      label: '전체' },
  { value: 'ACTIVE',   label: '활성' },
  { value: 'PENDING',  label: '대기' },
  { value: 'REJECTED', label: '거절' },
  { value: 'DISABLED', label: '비활성' },
]

const STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE:   '활성',
  PENDING:  '대기',
  REJECTED: '거절',
  DISABLED: '비활성',
}

const statusFilter = ref<UserStatus | 'ALL'>('ALL')

const activeCount = computed(() =>
  usersStore.allUsers.filter(u => u.status === 'ACTIVE').length
)

const recentUsers = computed(() => usersStore.allUsers.slice(0, 5))

// 사용자 모달
const selectedUser = ref<ManagedUser | null>(null)
const newPw = ref('')
const newPwConfirm = ref('')

// 회사/팀 연결
const userCompanies = ref<any[]>([])
const userTeams = ref<any[]>([])
const allCompanies = ref<any[]>([])
const allTeams = ref<any[]>([])
const addCompanyId = ref<number | null>(null)
const addTeamId = ref<number | null>(null)

const availableCompanies = computed(() => {
  const linked = new Set(userCompanies.value.map((c: any) => c.companyId))
  return allCompanies.value.filter((c: any) => !linked.has(c.id) && c.isActive)
})
const availableTeams = computed(() => {
  const linked = new Set(userTeams.value.map((t: any) => t.teamId))
  return allTeams.value.filter((t: any) => !linked.has(t.id) && t.isActive)
})

async function loadUserRelations(userId: number) {
  try {
    const [compRes, teamRes, allCompRes, allTeamRes] = await Promise.all([
      adminApi.getUserCompanies(userId),
      adminApi.getUserTeams(userId),
      companyApi.getAll(),
      teamApi.getAll(),
    ])
    userCompanies.value = compRes.data
    userTeams.value = teamRes.data
    allCompanies.value = allCompRes.data
    allTeams.value = allTeamRes.data
  } catch {}
}

async function addCompany() {
  if (!addCompanyId.value || !selectedUser.value) return
  try {
    await adminApi.addUserCompany(selectedUser.value.id, addCompanyId.value)
    await loadUserRelations(selectedUser.value.id)
    addCompanyId.value = null
  } catch (e: any) { alert(e.response?.data?.message || '추가 실패') }
}
async function removeCompany(companyId: number) {
  if (!selectedUser.value) return
  try {
    await adminApi.removeUserCompany(selectedUser.value.id, companyId)
    await loadUserRelations(selectedUser.value.id)
  } catch (e: any) { alert(e.response?.data?.message || '해제 실패') }
}
async function addTeam() {
  if (!addTeamId.value || !selectedUser.value) return
  try {
    await adminApi.addUserTeam(selectedUser.value.id, addTeamId.value)
    await loadUserRelations(selectedUser.value.id)
    addTeamId.value = null
  } catch (e: any) { alert(e.response?.data?.message || '추가 실패') }
}
async function removeTeam(teamId: number) {
  if (!selectedUser.value) return
  try {
    await adminApi.removeUserTeam(selectedUser.value.id, teamId)
    await loadUserRelations(selectedUser.value.id)
  } catch (e: any) { alert(e.response?.data?.message || '해제 실패') }
}
async function setPrimaryTeam(teamId: number) {
  if (!selectedUser.value) return
  try {
    await adminApi.setPrimaryUserTeam(selectedUser.value.id, teamId)
    await loadUserRelations(selectedUser.value.id)
  } catch (e: any) { alert(e.response?.data?.message || '설정 실패') }
}
const pwError = ref('')

function openUserModal(u: ManagedUser) {
  selectedUser.value = u
  newPw.value = ''
  newPwConfirm.value = ''
  pwError.value = ''
  userCompanies.value = []
  userTeams.value = []
  addCompanyId.value = null
  addTeamId.value = null
  loadUserRelations(u.id)
}
function closeUserModal() { selectedUser.value = null }

async function handleAdminPwChange() {
  pwError.value = ''
  if (newPw.value.length < 6) { pwError.value = '6자 이상 입력하세요.'; return }
  if (newPw.value !== newPwConfirm.value) { pwError.value = '비밀번호가 일치하지 않습니다.'; return }
  try {
    await adminApi.changeUserPassword(selectedUser.value!.id, newPw.value)
    newPw.value = ''
    newPwConfirm.value = ''
    alert('비밀번호가 변경됐습니다.')
  } catch {
    pwError.value = '변경에 실패했습니다.'
  }
}

const ROLE_LABELS: Record<string, string> = {
  ADMIN: '최고 관리자', MANAGER: '일반 관리자', USER: '일반 사용자'
}

async function handleRoleChange(id: number, role: string) {
  try {
    await adminApi.changeUserRole(id, role)
    const u = usersStore.users.find(u => u.id === id)
    if (u) u.role = role as any
    if (selectedUser.value?.id === id) selectedUser.value = { ...selectedUser.value, role: role as any }
  } catch {
    alert('역할 변경에 실패했습니다.')
  }
}

function handleApprove(id: number) {
  usersStore.approve(id)
  if (selectedUser.value?.id === id) selectedUser.value = null
}
function handleReject(id: number) {
  if (confirm('가입을 거절하시겠습니까?')) {
    usersStore.reject(id)
    if (selectedUser.value?.id === id) selectedUser.value = null
  }
}
function goToUserSchedules(userId: number, name: string) {
  router.push({ path: '/admin/schedules', query: { userId: String(userId), name } })
}

function avatarColor(name: string): string {
  const colors = ['#1976D2','#00897B','#F4511E','#9C27B0','#FF9800','#607D8B','#E91E63']
  return colors[name.charCodeAt(0) % colors.length]
}

function formatDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
}

const logs = ref<any[]>([])

async function fetchLogs() {
  if (authStore.user?.role !== 'ADMIN') return
  const res = await adminApi.getLogs(0, 100)
  logs.value = res.data.content
}

function formatLogDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
}

onMounted(async () => {
  await usersStore.fetchAll()
  const res = await adminApi.getAllSchedules()
  totalScheduleCount.value = res.data.length
  await fetchLogs()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.pending-alert-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px; background: var(--color-warning-soft);
  color: var(--color-warning); font-size: 13px; font-weight: 600;
  cursor: pointer; border-bottom: 1px solid var(--color-border);
}
.admin-summary {
  display: flex; padding: 16px;
  background: var(--color-surface); border-bottom: 1px solid var(--color-border);
}
.admin-sum-item { flex: 1; text-align: center; cursor: pointer; padding: 8px; }
.admin-sum-num { display: block; font-size: 24px; font-weight: 800; color: var(--color-primary); }
.admin-sum-label { font-size: 11px; color: var(--color-text-2); }
.admin-sum-sep { width: 1px; background: var(--color-border); margin: 8px 0; }
.admin-section { padding: 16px 16px 0; }
.admin-section-title { font-size: 13px; font-weight: 600; color: var(--color-text-2); margin-bottom: 8px; }
.quick-strip { display: flex; background: var(--color-surface); border-top: 1px solid var(--color-border); border-bottom: 1px solid var(--color-border); }
.quick-strip-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 14px 4px; cursor: pointer; border-right: 1px solid var(--color-border); background: none; border-top: none; border-bottom: none; border-left: none; transition: background 0.1s; }
.quick-strip-item:last-child { border-right: none; }
.quick-strip-item:active { background: var(--color-surface-2); }
.quick-strip-icon { width: 36px; height: 36px; border-radius: 8px; background: var(--color-surface-2); display: flex; align-items: center; justify-content: center; color: var(--color-text-1); }
.quick-strip-label { font-size: 10px; font-weight: 500; color: var(--color-text-2); }
.detail-row-item { display:flex; align-items:center; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--color-border); }
.detail-key { font-size:13px; color:var(--color-text-2); }
.detail-val { font-size:13px; color:var(--color-text-1); font-weight:500; }
</style>
