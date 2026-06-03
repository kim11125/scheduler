<template>
  <div class="app-shell admin">
    <!-- 헤더 -->
    <header class="admin-header">
      <span class="admin-title">⚙️ 관리자</span>
      <div class="header-right">
        <div class="theme-switcher">
          <button
            v-for="t in themes" :key="t.key"
            class="theme-dot-btn" :class="{ active: themeStore.current === t.key }"
            :style="{ '--dot-color': t.color }" :title="t.label"
            @click="themeStore.setTheme(t.key)"
          />
        </div>
        <button class="my-schedule-btn" @click="router.push('/my-schedules')">📅 내 일정</button>
        <button class="logout-btn" @click="handleLogout">
          <span>{{ authStore.user?.name }}</span>
          <span class="logout-label">로그아웃</span>
        </button>
      </div>
    </header>

    <div class="admin-body">

      <!-- 요약 카드 -->
      <div class="stat-row">
        <div class="stat-card" :class="{ highlight: usersStore.pendingUsers.length > 0 }"
             @click="scrollTo('pending')">
          <span class="stat-num">{{ usersStore.pendingUsers.length }}</span>
          <span class="stat-label">승인 대기</span>
        </div>
        <div class="stat-card" @click="statusFilter = 'ACTIVE'; scrollTo('users')">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">활성 사용자</span>
        </div>
        <div class="stat-card" @click="router.push('/admin/schedules')">
          <span class="stat-num">{{ totalScheduleCount }}</span>
          <span class="stat-label">전체 일정</span>
        </div>
      </div>

      <!-- 빠른 메뉴 -->
      <div class="quick-menu">
        <button class="quick-btn" @click="router.push('/admin/companies')">
          <span class="quick-icon">🏢</span>
          <span class="quick-label">회사 관리</span>
        </button>
        <button class="quick-btn" @click="router.push('/admin/teams')">
          <span class="quick-icon">⚽</span>
          <span class="quick-label">팀 관리</span>
        </button>
        <button class="quick-btn" @click="router.push('/admin/schedules')">
          <span class="quick-icon">📅</span>
          <span class="quick-label">일정 관리</span>
        </button>
      </div>

      <!-- 승인 대기 섹션 -->
      <section class="section" ref="pendingRef">
        <div class="section-header">
          <h2 class="section-title">
            승인 대기
            <span v-if="usersStore.pendingUsers.length" class="badge-count">
              {{ usersStore.pendingUsers.length }}
            </span>
          </h2>
        </div>

        <div v-if="usersStore.pendingUsers.length === 0" class="empty-msg">
          대기 중인 가입 신청이 없습니다.
        </div>

        <div class="user-list" v-else>
          <div
            v-for="u in usersStore.pendingUsers" :key="u.id"
            class="user-card pending"
          >
            <div class="user-info" @click="openUserModal(u)">
              <div class="user-avatar" :style="{ background: avatarColor(u.name) }">
                {{ u.name[0] }}
              </div>
              <div class="user-meta">
                <span class="user-name">{{ u.name }}</span>
                <span class="user-id">@{{ u.username }}</span>
                <span class="user-date">등록일: {{ formatDate(u.createdAt) }}</span>
              </div>
            </div>
            <div class="user-actions">
              <button class="btn-approve" @click="handleApprove(u.id)">승인</button>
              <button class="btn-reject"  @click="handleReject(u.id)">거절</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 전체 사용자 섹션 -->
      <section class="section" ref="usersRef">
        <div class="section-header">
          <h2 class="section-title">전체 사용자</h2>
          <div class="filter-tabs">
            <button
              v-for="f in STATUS_FILTERS" :key="f.value"
              class="filter-tab" :class="{ active: statusFilter === f.value }"
              @click="statusFilter = f.value"
            >{{ f.label }}</button>
          </div>
        </div>

        <div class="user-list">
          <div
            v-for="u in filteredUsers" :key="u.id"
            class="user-card"
            @click="openUserModal(u)"
          >
            <div class="user-info">
              <div class="user-avatar" :style="{ background: avatarColor(u.name) }">
                {{ u.name[0] }}
              </div>
              <div class="user-meta">
                <span class="user-name">{{ u.name }}</span>
                <span class="user-id">@{{ u.username }}</span>
                <span class="status-badge" :class="u.status.toLowerCase()">
                  {{ STATUS_LABELS[u.status] }}
                </span>
              </div>
            </div>
            <span class="card-arrow">›</span>
          </div>

          <div v-if="filteredUsers.length === 0" class="empty-msg">
            해당하는 사용자가 없습니다.
          </div>
        </div>
      </section>

      <!-- 로그인 로그 (ADMIN만) -->
      <section class="section" v-if="authStore.user?.role === 'ADMIN'">
        <div class="section-header">
          <h2 class="section-title">로그인 로그</h2>
          <button class="filter-tab active" @click="fetchLogs">새로고침</button>
        </div>
        <div class="log-list">
          <div v-if="logs.length === 0" class="empty-msg">로그가 없습니다.</div>
          <div v-for="log in logs" :key="log.id" class="log-item">
            <span class="log-action" :class="log.action === 'LOGIN' ? 'login' : 'logout'">
              {{ log.action === 'LOGIN' ? '로그인' : '로그아웃' }}
            </span>
            <span class="log-user">{{ log.username }}</span>
            <span class="log-ip">{{ log.ipAddress || '-' }}</span>
            <span class="log-time">{{ formatLogDate(log.createdAt) }}</span>
          </div>
        </div>
      </section>

    </div>

    <!-- 사용자 상세 모달 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="selectedUser" @click.self="closeUserModal">
        <div class="modal-sheet">
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3>사용자 정보</h3>
            <button class="modal-close" @click="closeUserModal">✕</button>
          </div>

          <div class="user-profile">
            <div class="profile-avatar" :style="{ background: avatarColor(selectedUser.name) }">
              {{ selectedUser.name[0] }}
            </div>
            <div class="profile-info">
              <span class="profile-name">{{ selectedUser.name }}</span>
              <span class="profile-id">@{{ selectedUser.username }}</span>
              <span class="status-badge" :class="selectedUser.status.toLowerCase()">
                {{ STATUS_LABELS[selectedUser.status] }}
              </span>
            </div>
          </div>

          <div class="profile-detail">
            <div class="detail-row"><span class="detail-label">등록일</span><span>{{ formatDate(selectedUser.createdAt) }}</span></div>
            <div class="detail-row">
              <span class="detail-label">역할</span>
              <!-- ADMIN만 역할 변경 가능 -->
              <select v-if="authStore.user?.role === 'ADMIN'" class="role-select"
                :value="selectedUser.role"
                @change="handleRoleChange(selectedUser.id, ($event.target as HTMLSelectElement).value)">
                <option value="USER">일반 사용자</option>
                <option value="MANAGER">일반 관리자</option>
                <option value="ADMIN">최고 관리자</option>
              </select>
              <span v-else>{{ ROLE_LABELS[selectedUser.role] || selectedUser.role }}</span>
            </div>
          </div>

          <!-- 상태 변경 버튼 -->
          <div class="modal-actions-row">
            <button v-if="selectedUser.status === 'PENDING'"   class="btn-approve" @click="handleApprove(selectedUser.id)">승인</button>
            <button v-if="selectedUser.status === 'PENDING'"   class="btn-reject"  @click="handleReject(selectedUser.id)">거절</button>
            <button v-if="selectedUser.status === 'ACTIVE'"    class="btn-sm btn-disable"  @click="usersStore.disable(selectedUser.id)">비활성화</button>
            <button v-if="selectedUser.status === 'DISABLED'"  class="btn-sm btn-activate" @click="usersStore.activate(selectedUser.id)">재활성화</button>
            <button v-if="selectedUser.status === 'REJECTED'"  class="btn-sm btn-activate" @click="usersStore.activate(selectedUser.id)">승인</button>
            <button v-if="selectedUser.status === 'ACTIVE'" class="btn-sm btn-schedule"
              @click="goToUserSchedules(selectedUser.id, selectedUser.name)">일정 보기</button>
          </div>

          <!-- 소속 회사 (1:1) -->
          <div class="relation-section">
            <h4 class="relation-title">소속 회사</h4>
            <div v-if="userCompanies.length > 0" class="relation-chips">
              <span v-for="uc in userCompanies" :key="uc.companyId" class="relation-chip primary">
                {{ uc.companyName }}
                <button class="chip-remove" @click="removeCompany(uc.companyId)">✕</button>
              </span>
            </div>
            <div v-else class="relation-add-row">
              <select v-model="addCompanyId" class="relation-select">
                <option :value="null">회사 선택...</option>
                <option v-for="c in availableCompanies" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <button class="btn-sm btn-add-rel" :disabled="!addCompanyId" @click="addCompany">등록</button>
            </div>
          </div>

          <!-- 소속 팀 -->
          <div class="relation-section">
            <h4 class="relation-title">소속 팀</h4>
            <div class="relation-chips">
              <span v-for="ut in userTeams" :key="ut.teamId" class="relation-chip"
                :class="{ primary: ut.isPrimary }">
                {{ ut.teamName }}
                <span v-if="ut.isPrimary" class="chip-primary-badge">대표</span>
                <button v-if="!ut.isPrimary" class="chip-action" @click="setPrimaryTeam(ut.teamId)">대표설정</button>
                <button class="chip-remove" @click="removeTeam(ut.teamId)">✕</button>
              </span>
              <span v-if="userTeams.length === 0" class="empty-sm">없음</span>
            </div>
            <div class="relation-add-row">
              <select v-model="addTeamId" class="relation-select">
                <option :value="null">팀 선택...</option>
                <option v-for="t in availableTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
              <button class="btn-sm btn-add-rel" :disabled="!addTeamId" @click="addTeam">추가</button>
            </div>
          </div>

          <!-- 비밀번호 변경 -->
          <div class="pw-section">
            <h4 class="pw-title">비밀번호 변경</h4>
            <input v-model="newPw" type="password" class="pw-input" placeholder="새 비밀번호 입력" />
            <input v-model="newPwConfirm" type="password" class="pw-input" placeholder="새 비밀번호 확인" />
            <p v-if="pwError" class="pw-error">{{ pwError }}</p>
            <button class="btn-pw-save" @click="handleAdminPwChange">변경</button>
          </div>
        </div>
      </div>
    </Teleport>
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

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const usersStore = useUsersStore()
const totalScheduleCount = ref(0)

const pendingRef = ref<HTMLElement | null>(null)
const usersRef  = ref<HTMLElement | null>(null)

function scrollTo(target: 'pending' | 'users') {
  const el = target === 'pending' ? pendingRef.value : usersRef.value
  el?.scrollIntoView({ behavior: 'smooth' })
}

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',   color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',      color: '#E8836A', label: '피치' },
  { key: 'mint',       color: '#4DB896', label: '민트' },
  { key: 'dark',       color: '#A695F0', label: '다크' },
  { key: 'rose-milk',  color: '#D4789A', label: '로즈' },
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

const filteredUsers = computed(() => {
  if (statusFilter.value === 'ALL') return usersStore.allUsers
  return usersStore.allUsers.filter(u => u.status === statusFilter.value)
})

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
async function setPrimaryCompany(companyId: number) {
  if (!selectedUser.value) return
  try {
    await adminApi.setPrimaryUserCompany(selectedUser.value.id, companyId)
    await loadUserRelations(selectedUser.value.id)
  } catch (e: any) { alert(e.response?.data?.message || '설정 실패') }
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
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}.${mm}.${dd} ${hh}:${mi}:${ss}`
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
.admin {
  min-height: 100vh;
  background: var(--color-bg, var(--color-background));
  overflow-y: auto;
}

.admin-header {
  position: sticky; top: 0; z-index: 50;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 56px;
  background: var(--color-header-bg, var(--color-status-bar));
  color: var(--color-header-text, #fff);
}
.admin-title { font-size: 17px; font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 10px; }
.theme-switcher { display: flex; gap: 5px; align-items: center; }
.theme-dot-btn {
  width: 14px; height: 14px; border-radius: 50%;
  background: var(--dot-color);
  border: 2px solid rgba(255,255,255,0.3);
  cursor: pointer; transition: border-color 0.2s, transform 0.2s;
}
.theme-dot-btn.active { border-color: #fff; transform: scale(1.3); }
.my-schedule-btn {
  font-size: 12px; color: rgba(255,255,255,0.9);
  padding: 5px 10px; border-radius: var(--radius-pill);
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.25);
  cursor: pointer; font-weight: 600;
}
.logout-btn {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: rgba(255,255,255,0.9);
  padding: 5px 12px; border-radius: var(--radius-pill);
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  font-weight: 600; cursor: pointer;
}
.logout-label { font-size: 12px; }

.admin-body { padding: 16px; display: flex; flex-direction: column; gap: 20px; }

/* 빠른 메뉴 */
.quick-menu { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.quick-btn {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 14px 8px; border-radius: var(--radius-md);
  background: var(--color-surface); border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card); cursor: pointer; transition: transform 0.1s;
}
.quick-btn:active { transform: scale(0.96); }
.quick-icon { font-size: 22px; }
.quick-label { font-size: 12px; font-weight: 600; color: var(--color-text); }

/* 요약 카드 */
.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 14px 10px;
  text-align: center;
  box-shadow: var(--shadow-card);
  display: flex; flex-direction: column; gap: 4px;
  cursor: pointer; transition: transform 0.1s;
}
.stat-card:active { transform: scale(0.97); }
.stat-card.highlight {
  border-color: var(--color-primary);
  background: var(--color-primary-soft, var(--color-primary-light));
}
.stat-num { font-size: 22px; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 11px; color: var(--color-text-secondary); }

/* 섹션 */
.section { display: flex; flex-direction: column; gap: 10px; }
.section-header {
  display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px;
}
.section-title {
  font-size: 15px; font-weight: 700; color: var(--color-text);
  display: flex; align-items: center; gap: 6px;
}
.badge-count {
  background: var(--color-primary); color: var(--color-btn-text, #fff);
  font-size: 11px; font-weight: 700;
  padding: 2px 8px; border-radius: var(--radius-pill);
}

/* 필터 탭 */
.filter-tabs { display: flex; gap: 4px; flex-wrap: wrap; }
.filter-tab {
  font-size: 12px; padding: 4px 12px; border-radius: var(--radius-pill);
  background: var(--color-surface); color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  cursor: pointer; transition: all 0.15s;
}
.filter-tab.active {
  background: var(--color-primary); color: var(--color-on-primary);
  border-color: var(--color-primary);
}

/* 유저 카드 */
.user-list { display: flex; flex-direction: column; gap: 8px; }
.user-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
  gap: 10px; cursor: pointer;
  transition: opacity 0.15s;
}
.user-card:active { opacity: 0.8; }
.user-card.pending { border-left: 3px solid var(--color-primary); }
.card-arrow { font-size: 18px; color: var(--color-border); }

.user-info { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.user-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 15px; font-weight: 700; flex-shrink: 0;
}
.user-meta {
  display: flex; flex-direction: column; gap: 2px; min-width: 0;
}
.user-name { font-size: 14px; font-weight: 600; color: var(--color-text); }
.user-id   { font-size: 12px; color: var(--color-text-secondary); }
.user-date { font-size: 11px; color: var(--color-text-secondary); }

/* 상태 배지 */
.status-badge {
  display: inline-block;
  font-size: 11px; font-weight: 600;
  padding: 2px 9px; border-radius: var(--radius-pill);
  width: fit-content;
}
.status-badge.active   { background: var(--color-success-soft); color: var(--color-success); }
.status-badge.pending  { background: var(--color-warning-soft); color: var(--color-warning); }
.status-badge.rejected { background: var(--color-danger-soft); color: var(--color-danger); }
.status-badge.disabled { background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary); }

/* 버튼 */
.user-actions { display: flex; gap: 6px; flex-shrink: 0; flex-wrap: wrap; justify-content: flex-end; }
.btn-approve {
  padding: 7px 14px; border-radius: var(--radius-md);
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 13px; font-weight: 700; cursor: pointer;
}
.btn-reject {
  padding: 7px 14px; border-radius: var(--radius-md);
  background: var(--color-danger-soft); color: var(--color-danger);
  font-size: 13px; font-weight: 600; cursor: pointer;
}
.btn-sm {
  padding: 5px 12px; border-radius: var(--radius-pill);
  font-size: 12px; font-weight: 600; cursor: pointer;
}
.btn-disable  { background: var(--color-warning-soft); color: var(--color-warning); }
.btn-activate { background: var(--color-success-soft); color: var(--color-success); }
.btn-schedule { background: var(--color-primary-soft, var(--color-primary-light)); color: var(--color-primary); }
.btn-profile  { background: var(--color-primary-soft, var(--color-primary-light)); color: var(--color-primary); }

.empty-msg {
  text-align: center; padding: 24px;
  font-size: 14px; color: var(--color-text-secondary);
}

/* 모달 */
.modal-overlay {
  position: fixed; inset: 0; background: var(--color-overlay); z-index: 200;
  display: flex; align-items: flex-end; justify-content: center;
}
.modal-sheet {
  width: 100%; max-width: 430px; margin: 0 auto;
  background: var(--color-surface); border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  padding: 12px 20px 40px;
  max-height: 85vh; overflow-y: auto;
  box-shadow: var(--shadow-modal);
  animation: slideUp 0.25s ease;
}
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle {
  width: 32px; height: 4px; border-radius: var(--radius-pill);
  background: var(--color-border); margin: 0 auto 14px;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.modal-header h3 { font-size: 16px; font-weight: 700; }
.modal-close { font-size: 18px; color: var(--color-text-secondary); cursor: pointer; }

.user-profile {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 0; border-bottom: 1px solid var(--color-border);
  margin-bottom: 12px;
}
.profile-avatar {
  width: 54px; height: 54px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.profile-info { display: flex; flex-direction: column; gap: 4px; }
.profile-name { font-size: 17px; font-weight: 700; }
.profile-id { font-size: 13px; color: var(--color-text-secondary); }

.profile-detail {
  display: flex; flex-direction: column; gap: 8px;
  margin-bottom: 14px;
}
.detail-row {
  display: flex; justify-content: space-between;
  font-size: 13px; color: var(--color-text);
}
.detail-label { color: var(--color-text-secondary); }
.role-select {
  font-size: 13px; padding: 3px 8px; border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-input-bg); color: var(--color-text);
}
.label-optional { font-size: 11px; color: var(--color-text-secondary); font-weight: 400; }

/* 회사/팀 관계 */
.relation-section { display: flex; flex-direction: column; gap: 8px; padding: 12px 0; border-top: 1px solid var(--color-border); }
.relation-title { font-size: 13px; font-weight: 700; color: var(--color-text); }
.relation-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.relation-chip {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; border-radius: var(--radius-pill);
  background: var(--color-surface-muted, var(--color-surface)); border: 1px solid var(--color-border);
  font-size: 12px; color: var(--color-text);
}
.relation-chip.primary {
  background: var(--color-primary-soft, var(--color-primary-light));
  border-color: var(--color-primary); color: var(--color-primary);
}
.chip-primary-badge {
  font-size: 10px; font-weight: 700;
  background: var(--color-primary); color: var(--color-btn-text, #fff);
  padding: 1px 6px; border-radius: var(--radius-pill);
}
.chip-action {
  font-size: 10px; color: var(--color-primary); cursor: pointer; padding: 0 2px;
}
.chip-remove {
  font-size: 11px; color: var(--color-text-secondary); cursor: pointer; padding: 0 2px;
}
.chip-remove:hover { color: var(--color-danger); }
.empty-sm { font-size: 12px; color: var(--color-text-secondary); }
.relation-add-row { display: flex; gap: 8px; align-items: center; }
.relation-select {
  flex: 1; padding: 7px 10px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text); font-size: 13px;
}
.btn-add-rel {
  background: var(--color-primary); color: var(--color-on-primary);
  padding: 7px 12px; border-radius: var(--radius-md); font-size: 12px; font-weight: 700;
}
.btn-add-rel:disabled { opacity: 0.4; cursor: not-allowed; }

/* 로그 */
.log-list { display: flex; flex-direction: column; gap: 6px; }
.log-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 12px;
}
.log-action {
  font-weight: 700; padding: 2px 9px; border-radius: var(--radius-pill); font-size: 11px; flex-shrink: 0;
}
.log-action.login  { background: var(--color-success-soft); color: var(--color-success); }
.log-action.logout { background: var(--color-warning-soft); color: var(--color-warning); }
.log-user { font-weight: 600; color: var(--color-text); flex: 1; }
.log-ip   { color: var(--color-text-secondary); font-size: 11px; }
.log-time { color: var(--color-text-secondary); font-size: 11px; flex-shrink: 0; }

.modal-actions-row {
  display: flex; gap: 8px; flex-wrap: wrap;
  padding: 12px 0; border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 16px;
}

/* 비밀번호 변경 */
.pw-section { display: flex; flex-direction: column; gap: 10px; }
.pw-title { font-size: 14px; font-weight: 700; color: var(--color-text); }
.pw-input {
  padding: 10px 12px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text); font-size: 14px; outline: none; width: 100%;
}
.pw-input:focus { border-color: var(--color-input-focus); }
.pw-error { font-size: 12px; color: var(--color-danger); }
.btn-pw-save {
  padding: 10px; border-radius: var(--radius-md);
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 14px; font-weight: 700; cursor: pointer;
}
</style>
