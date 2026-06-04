<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <span class="topbar-title">⚙️ 관리자</span>
      <div style="display:flex;align-items:center;gap:8px">
        <div class="theme-row">
          <button v-for="t in themes" :key="t.key"
            :class="['theme-dot-btn', { 'is-active': themeStore.current === t.key }]"
            :style="{ '--dot-c': t.color }"
            @click="themeStore.setTheme(t.key)" />
        </div>
        <button class="topbar-action" @click="router.push('/my-schedules')">📅 내 일정</button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃" style="font-size:15px">↩</button>
      </div>
    </header>

    <!-- Pending alert strip -->
    <div v-if="usersStore.pendingUsers.length > 0" class="pending-alert" @click="scrollTo('pending')">
      <span>🔔 승인 대기 {{ usersStore.pendingUsers.length }}명</span>
      <span>›</span>
    </div>

    <div class="dash-scroll">
      <!-- Summary panel -->
      <div class="section" style="margin-top:16px">
        <div class="summary-panel">
          <div class="summary-row">
            <div class="summary-item" @click="scrollTo('pending')">
              <span class="summary-num" :class="{ 'summary-num-alert': usersStore.pendingUsers.length > 0 }">{{ usersStore.pendingUsers.length }}</span>
              <div class="summary-label">승인 대기</div>
            </div>
            <div class="summary-item" @click="router.push('/admin/users')">
              <span class="summary-num">{{ activeCount }}</span>
              <div class="summary-label">활성 사용자</div>
            </div>
            <div class="summary-item" @click="router.push('/admin/schedules')">
              <span class="summary-num">{{ totalScheduleCount }}</span>
              <div class="summary-label">전체 일정</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick menu -->
      <div class="section" style="margin-top:20px">
        <div class="section-title">빠른 메뉴</div>
        <div class="row-list">
          <div class="row-item" @click="router.push('/admin/users')">
            <div class="row-icon">👥</div>
            <div class="row-body"><div class="row-title">사용자 관리</div><div class="row-sub">가입 승인 · 계정 관리</div></div>
            <span class="row-arrow">›</span>
          </div>
          <div class="row-item" @click="router.push('/admin/schedules')">
            <div class="row-icon">📅</div>
            <div class="row-body"><div class="row-title">전체 일정</div><div class="row-sub">모든 사용자 일정 조회</div></div>
            <span class="row-arrow">›</span>
          </div>
          <div class="row-item" @click="router.push('/admin/companies')">
            <div class="row-icon">🏢</div>
            <div class="row-body"><div class="row-title">회사 관리</div><div class="row-sub">회사 추가 · 팀 연결</div></div>
            <span class="row-arrow">›</span>
          </div>
          <div class="row-item" @click="router.push('/admin/teams')">
            <div class="row-icon">⚽</div>
            <div class="row-body"><div class="row-title">팀 관리</div><div class="row-sub">팀 추가 · 카테고리 설정</div></div>
            <span class="row-arrow">›</span>
          </div>
          <div v-if="authStore.user?.role === 'ADMIN'" class="row-item" @click="scrollTo('logs')">
            <div class="row-icon">📋</div>
            <div class="row-body"><div class="row-title">로그인 로그</div><div class="row-sub">접속 기록 확인</div></div>
            <span class="row-arrow">›</span>
          </div>
        </div>
      </div>

      <!-- Pending approvals -->
      <div class="section" style="margin-top:20px" ref="pendingRef">
        <div class="section-header">
          <span class="section-title">승인 대기</span>
          <span v-if="usersStore.pendingUsers.length" class="badge badge-pending">{{ usersStore.pendingUsers.length }}</span>
        </div>
        <div v-if="usersStore.pendingUsers.length === 0" class="empty" style="padding:24px">
          <span class="empty-icon">✅</span>
          <p class="empty-text">대기 중인 가입 신청이 없습니다</p>
        </div>
        <div v-else class="row-list">
          <div v-for="u in usersStore.pendingUsers" :key="u.id" class="row-item" style="cursor:default">
            <div class="avatar avatar-sm" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
            <div class="row-body">
              <div class="row-title">{{ u.name }}</div>
              <div class="row-sub">@{{ u.username }} · {{ formatDate(u.createdAt) }}</div>
            </div>
            <div class="row-right">
              <button class="btn btn-xs btn-primary" @click="handleApprove(u.id)">승인</button>
              <button class="btn btn-xs btn-danger" @click="handleReject(u.id)">거절</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent users -->
      <div class="section" style="margin-top:20px">
        <div class="section-header">
          <span class="section-title">최근 사용자</span>
          <button class="see-all" @click="router.push('/admin/users')">전체 보기</button>
        </div>
        <div class="row-list">
          <div v-for="u in recentUsers" :key="u.id" class="row-item" @click="openUserModal(u)">
            <div class="avatar avatar-sm" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
            <div class="row-body">
              <div class="row-title">{{ u.name }}</div>
              <div class="row-sub">@{{ u.username }}</div>
            </div>
            <span :class="['badge', `badge-${u.status.toLowerCase()}`]">{{ STATUS_LABELS[u.status] }}</span>
          </div>
          <div v-if="recentUsers.length === 0" class="empty" style="padding:24px">
            <p class="empty-text">사용자가 없습니다</p>
          </div>
        </div>
      </div>

      <!-- Login logs (ADMIN only) -->
      <div class="section" style="margin-top:20px" ref="logsRef" v-if="authStore.user?.role === 'ADMIN'">
        <div class="section-header">
          <span class="section-title">로그인 로그</span>
          <button class="see-all" @click="fetchLogs">새로고침</button>
        </div>
        <div class="row-list">
          <div v-if="logs.length === 0" class="empty" style="padding:24px"><p class="empty-text">로그가 없습니다</p></div>
          <div v-for="log in logs" :key="log.id" class="log-row">
            <span class="log-action" :class="log.action === 'LOGIN' ? 'log-in' : 'log-out'">{{ log.action === 'LOGIN' ? '로그인' : '로그아웃' }}</span>
            <span class="log-user">{{ log.username }}</span>
            <span class="log-meta">{{ log.ipAddress || '-' }}</span>
            <span class="log-meta">{{ formatLogDate(log.createdAt) }}</span>
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
            <button class="sheet-close" @click="closeUserModal">✕</button>
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
                  <button @click="removeCompany(uc.companyId)" style="font-size:10px;color:inherit;opacity:0.7">✕</button>
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
                  <button @click="removeTeam(ut.teamId)" style="font-size:10px;opacity:0.7">✕</button>
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

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const usersStore = useUsersStore()
const totalScheduleCount = ref(0)

const pendingRef = ref<HTMLElement | null>(null)
const logsRef   = ref<HTMLElement | null>(null)

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
.dash-scroll { flex:1; overflow-y:auto; }
.section-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; }
.see-all { font-size:12px; font-weight:600; color:var(--color-primary); background:none; border:none; cursor:pointer; }
.pending-alert { background:var(--color-danger-bg); color:var(--color-danger); font-size:13px; font-weight:600; padding:10px 16px; display:flex; justify-content:space-between; cursor:pointer; }
.detail-row-item { display:flex; align-items:center; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--color-border); }
.detail-key { font-size:13px; color:var(--color-text-2); }
.detail-val { font-size:13px; color:var(--color-text-1); font-weight:500; }
.log-row { display:flex; align-items:center; gap:8px; padding:10px 14px; border-bottom:1px solid var(--color-border); font-size:12px; }
.log-row:last-child { border-bottom:none; }
.log-action { padding:2px 8px; border-radius:var(--radius-pill); font-weight:600; flex-shrink:0; }
.log-in  { background:var(--color-active-bg); color:var(--color-active); }
.log-out { background:var(--color-surface-2); color:var(--color-text-2); }
.log-user { font-weight:600; color:var(--color-text-1); flex-shrink:0; }
.log-meta { color:var(--color-text-3); flex-shrink:0; }
</style>
