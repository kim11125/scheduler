<template>
  <div class="page">
    <header class="app-header">
      <span class="header-title">👥 사용자 관리</span>
    </header>

    <!-- Search -->
    <div class="search-bar">
      <input v-model="searchQuery" type="text" class="search-input" placeholder="이름 또는 아이디 검색..." />
    </div>

    <!-- Status filter -->
    <div class="filter-bar">
      <button v-for="f in STATUS_FILTERS" :key="f.value"
        :class="['chip', { active: statusFilter === f.value }]"
        @click="statusFilter = f.value">{{ f.label }}</button>
    </div>

    <div class="list-body">
      <!-- Pending approvals -->
      <template v-if="statusFilter === 'ALL' && usersStore.pendingUsers.length > 0">
        <div class="section-hdr">
          <span class="section-hdr-title">승인 대기</span>
          <span class="badge-count">{{ usersStore.pendingUsers.length }}</span>
        </div>
        <div class="user-card-list">
          <div v-for="u in usersStore.pendingUsers" :key="u.id" class="user-card pending-card" @click="openUserModal(u)">
            <div class="user-avatar" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
            <div class="user-meta">
              <span class="user-name">{{ u.name }}</span>
              <span class="user-id">@{{ u.username }}</span>
            </div>
            <div class="user-actions">
              <button class="btn-approve" @click.stop="usersStore.approve(u.id)">승인</button>
              <button class="btn-reject" @click.stop="rejectUser(u.id)">거절</button>
            </div>
          </div>
        </div>
        <div class="divider"></div>
      </template>

      <!-- Users list -->
      <div class="user-card-list">
        <div v-for="u in filteredUsers" :key="u.id" class="user-card" @click="openUserModal(u)">
          <div class="user-avatar" :style="{ background: avatarColor(u.name) }">{{ u.name[0] }}</div>
          <div class="user-meta">
            <span class="user-name">{{ u.name }}</span>
            <span class="user-id">@{{ u.username }}</span>
          </div>
          <span class="status-chip" :class="u.status.toLowerCase()">{{ STATUS_LABELS[u.status] }}</span>
          <span class="card-arrow">›</span>
        </div>
        <div v-if="filteredUsers.length === 0" class="empty-state">
          <span class="empty-icon">👤</span>
          <p class="empty-text">사용자가 없어요</p>
        </div>
      </div>
    </div>

    <!-- User detail modal -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="selectedUser" @click.self="closeUserModal">
        <div class="modal-sheet">
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3 class="modal-title">사용자 정보</h3>
            <button class="modal-close" @click="closeUserModal">✕</button>
          </div>
          <div class="modal-body-scroll">
            <div class="user-profile-row">
              <div class="profile-avatar-lg" :style="{ background: avatarColor(selectedUser.name) }">{{ selectedUser.name[0] }}</div>
              <div class="profile-info">
                <span class="profile-name">{{ selectedUser.name }}</span>
                <span class="profile-id">@{{ selectedUser.username }}</span>
                <span class="status-chip" :class="selectedUser.status.toLowerCase()">{{ STATUS_LABELS[selectedUser.status] }}</span>
              </div>
            </div>
            <div class="detail-section">
              <div class="detail-row"><span class="detail-lbl">등록일</span><span>{{ formatDate(selectedUser.createdAt) }}</span></div>
              <div class="detail-row">
                <span class="detail-lbl">역할</span>
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
            <div class="action-row">
              <button v-if="selectedUser.status === 'PENDING'" class="btn-approve" @click="handleApprove(selectedUser.id)">승인</button>
              <button v-if="selectedUser.status === 'PENDING'" class="btn-reject" @click="handleReject(selectedUser.id)">거절</button>
              <button v-if="selectedUser.status === 'ACTIVE'" class="btn-sm btn-disable" @click="usersStore.disable(selectedUser.id)">비활성화</button>
              <button v-if="selectedUser.status === 'DISABLED'" class="btn-sm btn-activate" @click="usersStore.activate(selectedUser.id)">재활성화</button>
              <button v-if="selectedUser.status === 'REJECTED'" class="btn-sm btn-activate" @click="usersStore.activate(selectedUser.id)">승인</button>
            </div>
            <!-- Company relation -->
            <div class="relation-section">
              <h4 class="relation-title">소속 회사</h4>
              <div v-if="userCompanies.length > 0" class="relation-chips">
                <span v-for="uc in userCompanies" :key="uc.companyId" class="relation-chip primary">
                  {{ uc.companyName }}<button class="chip-remove" @click="removeCompany(uc.companyId)">✕</button>
                </span>
              </div>
              <div class="relation-add-row">
                <select v-model="addCompanyId" class="relation-select">
                  <option :value="null">회사 선택...</option>
                  <option v-for="c in availableCompanies" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
                <button class="btn-sm btn-add-rel" :disabled="!addCompanyId" @click="addCompany">등록</button>
              </div>
            </div>
            <!-- Team relation -->
            <div class="relation-section">
              <h4 class="relation-title">소속 팀</h4>
              <div class="relation-chips">
                <span v-for="ut in userTeams" :key="ut.teamId" class="relation-chip" :class="{ primary: ut.isPrimary }">
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
            <!-- Password change -->
            <div class="pw-section">
              <h4 class="pw-title">비밀번호 변경</h4>
              <input v-model="newPw" type="password" class="pw-input" placeholder="새 비밀번호 입력" />
              <input v-model="newPwConfirm" type="password" class="pw-input" placeholder="새 비밀번호 확인" />
              <p v-if="pwError" class="pw-error">{{ pwError }}</p>
              <button class="btn-pw-save" @click="handleAdminPwChange">변경</button>
            </div>
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
import { useUsersStore } from '@/stores/users'
import { adminApi } from '@/api/admin'
import { companyApi } from '@/api/company'
import { teamApi } from '@/api/team'
import type { UserStatus } from '@/types'
import type { ManagedUser } from '@/stores/users'
import BottomNavAdmin from '@/components/BottomNavAdmin.vue'

const router = useRouter()
const authStore = useAuthStore()
const usersStore = useUsersStore()

const searchQuery = ref('')
const statusFilter = ref<UserStatus | 'ALL'>('ALL')

const STATUS_FILTERS: { value: UserStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: '전체' }, { value: 'ACTIVE', label: '활성' },
  { value: 'PENDING', label: '대기' }, { value: 'REJECTED', label: '거절' }, { value: 'DISABLED', label: '비활성' },
]
const STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE: '활성', PENDING: '대기', REJECTED: '거절', DISABLED: '비활성',
}
const ROLE_LABELS: Record<string, string> = {
  ADMIN: '최고 관리자', MANAGER: '일반 관리자', USER: '일반 사용자'
}

const filteredUsers = computed(() => {
  let list = statusFilter.value === 'ALL' ? usersStore.allUsers : usersStore.allUsers.filter(u => u.status === statusFilter.value)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(u => u.name.toLowerCase().includes(q) || u.username.toLowerCase().includes(q))
  }
  return list
})

function avatarColor(name: string): string {
  const colors = ['#1976D2','#00897B','#F4511E','#9C27B0','#FF9800','#607D8B','#E91E63']
  return colors[name.charCodeAt(0) % colors.length]
}
function formatDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// User modal
const selectedUser = ref<ManagedUser | null>(null)
const newPw = ref('')
const newPwConfirm = ref('')
const pwError = ref('')
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
      adminApi.getUserCompanies(userId), adminApi.getUserTeams(userId),
      companyApi.getAll(), teamApi.getAll(),
    ])
    userCompanies.value = compRes.data; userTeams.value = teamRes.data
    allCompanies.value = allCompRes.data; allTeams.value = allTeamRes.data
  } catch {}
}

function openUserModal(u: ManagedUser) {
  selectedUser.value = u; newPw.value = ''; newPwConfirm.value = ''; pwError.value = ''
  userCompanies.value = []; userTeams.value = []; addCompanyId.value = null; addTeamId.value = null
  loadUserRelations(u.id)
}
function closeUserModal() { selectedUser.value = null }

async function addCompany() {
  if (!addCompanyId.value || !selectedUser.value) return
  try { await adminApi.addUserCompany(selectedUser.value.id, addCompanyId.value); await loadUserRelations(selectedUser.value.id); addCompanyId.value = null }
  catch (e: any) { alert(e.response?.data?.message || '추가 실패') }
}
async function removeCompany(companyId: number) {
  if (!selectedUser.value) return
  try { await adminApi.removeUserCompany(selectedUser.value.id, companyId); await loadUserRelations(selectedUser.value.id) }
  catch (e: any) { alert(e.response?.data?.message || '해제 실패') }
}
async function addTeam() {
  if (!addTeamId.value || !selectedUser.value) return
  try { await adminApi.addUserTeam(selectedUser.value.id, addTeamId.value); await loadUserRelations(selectedUser.value.id); addTeamId.value = null }
  catch (e: any) { alert(e.response?.data?.message || '추가 실패') }
}
async function removeTeam(teamId: number) {
  if (!selectedUser.value) return
  try { await adminApi.removeUserTeam(selectedUser.value.id, teamId); await loadUserRelations(selectedUser.value.id) }
  catch (e: any) { alert(e.response?.data?.message || '해제 실패') }
}
async function setPrimaryTeam(teamId: number) {
  if (!selectedUser.value) return
  try { await adminApi.setPrimaryUserTeam(selectedUser.value.id, teamId); await loadUserRelations(selectedUser.value.id) }
  catch (e: any) { alert(e.response?.data?.message || '설정 실패') }
}

async function handleAdminPwChange() {
  pwError.value = ''
  if (newPw.value.length < 6) { pwError.value = '6자 이상 입력하세요.'; return }
  if (newPw.value !== newPwConfirm.value) { pwError.value = '비밀번호가 일치하지 않습니다.'; return }
  try {
    await adminApi.changeUserPassword(selectedUser.value!.id, newPw.value)
    newPw.value = ''; newPwConfirm.value = ''; alert('비밀번호가 변경됐습니다.')
  } catch { pwError.value = '변경에 실패했습니다.' }
}

async function handleRoleChange(id: number, role: string) {
  try {
    await adminApi.changeUserRole(id, role)
    const u = usersStore.users.find(u => u.id === id)
    if (u) u.role = role as any
    if (selectedUser.value?.id === id) selectedUser.value = { ...selectedUser.value, role: role as any }
  } catch { alert('역할 변경에 실패했습니다.') }
}

function handleApprove(id: number) { usersStore.approve(id); if (selectedUser.value?.id === id) selectedUser.value = null }
function handleReject(id: number) {
  if (window.confirm('가입을 거절하시겠습니까?')) { usersStore.reject(id); if (selectedUser.value?.id === id) selectedUser.value = null }
}
function rejectUser(id: number) {
  if (window.confirm('거절하시겠습니까?')) { usersStore.reject(id) }
}

onMounted(() => { usersStore.fetchAll() })
</script>

<style scoped>
.search-bar { padding: 12px 16px 4px; }
.search-input { width: 100%; padding: 10px 14px; border-radius: 12px; border: 1.5px solid var(--color-border); background: var(--color-surface); color: var(--color-text-primary); font-size: 14px; outline: none; }
.search-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-soft); }
.filter-bar { display: flex; gap: 8px; padding: 8px 16px 12px; overflow-x: auto; }
.filter-bar::-webkit-scrollbar { display: none; }
.list-body { padding: 0 16px 100px; }
.section-hdr { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.section-hdr-title { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); text-transform: uppercase; }
.badge-count { background: var(--color-primary); color: var(--color-btn-text, #fff); font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
.divider { height: 1px; background: var(--color-border); margin: 16px 0; }
.user-card-list { display: flex; flex-direction: column; gap: 8px; }
.user-card { display: flex; align-items: center; gap: 10px; padding: 12px 14px; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 12px; box-shadow: var(--shadow-soft); cursor: pointer; transition: background 0.1s; }
.user-card:active { background: var(--color-surface-muted); }
.pending-card { border-left: 3px solid var(--color-primary); }
.user-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 15px; font-weight: 700; flex-shrink: 0; }
.user-meta { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name { font-size: 14px; font-weight: 600; color: var(--color-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-id { font-size: 12px; color: var(--color-text-secondary); }
.status-chip { font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: 999px; flex-shrink: 0; }
.status-chip.active { background: var(--color-success-soft); color: var(--color-success); }
.status-chip.pending { background: var(--color-warning-soft); color: var(--color-warning); }
.status-chip.rejected { background: var(--color-danger-soft); color: var(--color-danger); }
.status-chip.disabled { background: var(--color-surface-muted); color: var(--color-text-secondary); }
.card-arrow { font-size: 16px; color: var(--color-text-secondary); }
.user-actions { display: flex; gap: 6px; flex-shrink: 0; }
.btn-approve { padding: 6px 12px; border-radius: 8px; background: var(--color-primary); color: var(--color-btn-text); font-size: 12px; font-weight: 700; }
.btn-reject { padding: 6px 12px; border-radius: 8px; background: var(--color-danger-soft); color: var(--color-danger); font-size: 12px; font-weight: 600; }
.btn-sm { padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; cursor: pointer; }
.btn-disable { background: var(--color-warning-soft); color: var(--color-warning); }
.btn-activate { background: var(--color-success-soft); color: var(--color-success); }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: var(--color-overlay); z-index: 200; display: flex; align-items: flex-end; justify-content: center; }
.modal-sheet { width: 100%; max-width: 430px; background: var(--color-surface); border-radius: var(--radius-lg) var(--radius-lg) 0 0; max-height: 90vh; display: flex; flex-direction: column; box-shadow: var(--shadow-floating); animation: slideUp 0.25s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle { width: 32px; height: 4px; border-radius: 999px; background: var(--color-border); margin: 10px auto; flex-shrink: 0; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 0 20px 12px; flex-shrink: 0; }
.modal-title { font-size: 16px; font-weight: 700; }
.modal-close { font-size: 18px; color: var(--color-text-secondary); cursor: pointer; background: none; border: none; }
.modal-body-scroll { flex: 1; overflow-y: auto; padding: 0 20px 40px; }
.user-profile-row { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid var(--color-border); margin-bottom: 12px; }
.profile-avatar-lg { width: 54px; height: 54px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; font-weight: 700; flex-shrink: 0; }
.profile-info { display: flex; flex-direction: column; gap: 4px; }
.profile-name { font-size: 17px; font-weight: 700; color: var(--color-text-primary); }
.profile-id { font-size: 13px; color: var(--color-text-secondary); }
.detail-section { display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px; }
.detail-row { display: flex; justify-content: space-between; font-size: 13px; color: var(--color-text-primary); }
.detail-lbl { color: var(--color-text-secondary); }
.role-select { font-size: 13px; padding: 3px 8px; border-radius: 8px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-text-primary); }
.action-row { display: flex; gap: 8px; flex-wrap: wrap; padding: 12px 0; border-top: 1px solid var(--color-border); border-bottom: 1px solid var(--color-border); margin-bottom: 16px; }
.relation-section { display: flex; flex-direction: column; gap: 8px; padding: 12px 0; border-top: 1px solid var(--color-border); }
.relation-title { font-size: 13px; font-weight: 700; color: var(--color-text-primary); }
.relation-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.relation-chip { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 999px; background: var(--color-surface-muted); border: 1px solid var(--color-border); font-size: 12px; color: var(--color-text-primary); }
.relation-chip.primary { background: var(--color-primary-soft); border-color: var(--color-primary); color: var(--color-primary); }
.chip-primary-badge { font-size: 10px; font-weight: 700; background: var(--color-primary); color: var(--color-btn-text, #fff); padding: 1px 6px; border-radius: 999px; }
.chip-action { font-size: 10px; color: var(--color-primary); cursor: pointer; padding: 0 2px; background: none; border: none; }
.chip-remove { font-size: 11px; color: var(--color-text-secondary); cursor: pointer; padding: 0 2px; background: none; border: none; }
.chip-remove:hover { color: var(--color-danger); }
.empty-sm { font-size: 12px; color: var(--color-text-secondary); }
.relation-add-row { display: flex; gap: 8px; align-items: center; }
.relation-select { flex: 1; padding: 7px 10px; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: var(--color-surface); color: var(--color-text-primary); font-size: 13px; outline: none; }
.btn-add-rel { background: var(--color-primary); color: var(--color-btn-text); padding: 7px 12px; border-radius: var(--radius-md); font-size: 12px; font-weight: 700; cursor: pointer; }
.btn-add-rel:disabled { opacity: 0.4; cursor: not-allowed; }
.pw-section { display: flex; flex-direction: column; gap: 10px; padding-top: 12px; border-top: 1px solid var(--color-border); margin-top: 4px; }
.pw-title { font-size: 14px; font-weight: 700; color: var(--color-text-primary); }
.pw-input { padding: 10px 12px; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: var(--color-surface-muted); color: var(--color-text-primary); font-size: 14px; outline: none; width: 100%; }
.pw-input:focus { border-color: var(--color-primary); }
.pw-error { font-size: 12px; color: var(--color-danger); }
.btn-pw-save { padding: 10px; border-radius: var(--radius-md); background: var(--color-primary); color: var(--color-btn-text); font-size: 14px; font-weight: 700; cursor: pointer; }
</style>
