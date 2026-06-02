<template>
  <div class="app-shell admin-companies">
    <!-- 헤더 -->
    <header class="admin-header">
      <button class="back-btn" @click="router.push('/admin')">&#8249;</button>
      <span class="admin-title">회사 관리</span>
      <button class="btn-add" @click="openAddModal">+ 등록</button>
    </header>

    <div class="admin-body">

      <!-- 검색 -->
      <div class="search-wrap">
        <input v-model="searchQuery" class="search-input" placeholder="회사명 검색..." />
      </div>

      <!-- 목록 -->
      <div class="list-wrap">
        <div v-if="filteredCompanies.length === 0" class="empty-msg">등록된 회사가 없습니다.</div>
        <div
          v-for="c in filteredCompanies"
          :key="c.id"
          class="company-card"
          @click="openDetail(c)"
        >
          <div class="company-info">
            <img v-if="c.logoUrl" :src="c.logoUrl" class="company-logo" alt="" />
            <div v-else class="company-logo-placeholder">{{ c.name[0] }}</div>
            <div class="company-meta">
              <span class="company-name">{{ c.name }}</span>
              <span v-if="c.description" class="company-desc">{{ c.description }}</span>
            </div>
          </div>
          <div class="company-right">
            <span class="active-badge" :class="c.isActive ? 'active' : 'inactive'">
              {{ c.isActive ? '활성' : '비활성' }}
            </span>
            <span class="card-arrow">›</span>
          </div>
        </div>
      </div>

    </div>

    <!-- 등록/수정 모달 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="formModal" @click.self="formModal = false">
        <div class="modal-sheet" @click.stop>
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3>{{ editingCompany ? '회사 수정' : '회사 등록' }}</h3>
            <button class="modal-close" @click="formModal = false">✕</button>
          </div>
          <form class="modal-form" @submit.prevent="saveCompany">
            <div class="form-field">
              <label class="form-label">회사명 <span class="required">*</span></label>
              <input v-model="form.name" class="form-input" placeholder="회사명을 입력하세요" required />
            </div>
            <div class="form-field">
              <label class="form-label">설명</label>
              <textarea v-model="form.description" class="form-input form-textarea" placeholder="회사 설명" rows="3" />
            </div>
            <div class="form-field">
              <label class="form-label">로고 URL</label>
              <input v-model="form.logoUrl" class="form-input" placeholder="https://..." />
            </div>
            <p v-if="formError" class="form-err">{{ formError }}</p>
            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="formModal = false">취소</button>
              <button type="submit" class="btn-save" :disabled="saving">{{ saving ? '저장 중...' : '저장' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 상세 모달 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="detailCompany" @click.self="detailCompany = null">
        <div class="modal-sheet detail-sheet" @click.stop>
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3>{{ detailCompany.name }}</h3>
            <button class="modal-close" @click="detailCompany = null">✕</button>
          </div>

          <div class="detail-body">
            <div class="detail-actions">
              <button class="btn-sm btn-edit" @click="openEditModal(detailCompany)">수정</button>
              <button
                class="btn-sm"
                :class="detailCompany.isActive ? 'btn-deactivate' : 'btn-activate'"
                @click="toggleActive(detailCompany)"
              >{{ detailCompany.isActive ? '비활성화' : '활성화' }}</button>
            </div>

            <div v-if="detailCompany.description" class="detail-desc">{{ detailCompany.description }}</div>

            <!-- 담당 팀 목록 -->
            <div class="detail-section">
              <h4 class="detail-section-title">담당 팀</h4>
              <div v-if="detailTeams.length === 0" class="empty-sm">연결된 팀이 없습니다.</div>
              <div v-else class="team-chips">
                <span v-for="t in detailTeams" :key="t.id" class="team-chip">
                  <span class="chip-dot" :style="{ background: DOT_COLORS[t.category] }"></span>
                  {{ t.name }}
                  <button class="chip-remove" @click.stop="removeTeamFromCompany(t.id)">✕</button>
                </span>
              </div>
              <!-- 팀 연결 -->
              <div class="add-team-row">
                <select v-model="selectedTeamId" class="form-input select-sm">
                  <option :value="null">팀 선택...</option>
                  <option v-for="t in availableTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <button class="btn-sm btn-add-team" :disabled="!selectedTeamId" @click="addTeamToCompany">연결</button>
              </div>
            </div>

            <!-- 소속 사용자 목록 -->
            <div class="detail-section">
              <h4 class="detail-section-title">소속 사용자</h4>
              <div v-if="!detailCompany.users || detailCompany.users.length === 0" class="empty-sm">소속 사용자가 없습니다.</div>
              <div v-else class="user-chips">
                <span v-for="u in detailCompany.users" :key="u.id" class="user-chip">{{ u.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import type { Company, Team, Category } from '@/types'

const router = useRouter()
const companies = ref<Company[]>([])
const allTeams = ref<Team[]>([])
const searchQuery = ref('')
const formModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editingCompany = ref<Company | null>(null)
const detailCompany = ref<Company | null>(null)
const detailTeams = ref<Team[]>([])
const selectedTeamId = ref<number | null>(null)

const form = reactive({ name: '', description: '', logoUrl: '' })

const DOT_COLORS: Record<Category, string> = {
  BASEBALL: '#F44336',
  BASKETBALL: '#FF9800',
  SOCCER: '#4CAF50',
  WOMENS_VOLLEYBALL: '#9C27B0',
  MENS_VOLLEYBALL: '#9C27B0',
  ETC: '#607D8B',
}

const filteredCompanies = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return companies.value.filter(c => c.name.toLowerCase().includes(q))
})

const availableTeams = computed(() => {
  const linkedIds = new Set(detailTeams.value.map(t => t.id))
  return allTeams.value.filter(t => !linkedIds.has(t.id) && t.isActive)
})

async function loadData() {
  const [companiesRes, teamsRes] = await Promise.all([
    adminApi.getCompanies(),
    adminApi.getTeams(),
  ])
  companies.value = companiesRes.data
  allTeams.value = teamsRes.data
}

onMounted(loadData)

function openAddModal() {
  editingCompany.value = null
  form.name = ''
  form.description = ''
  form.logoUrl = ''
  formError.value = ''
  formModal.value = true
}

function openEditModal(c: Company) {
  editingCompany.value = c
  form.name = c.name
  form.description = c.description ?? ''
  form.logoUrl = c.logoUrl ?? ''
  formError.value = ''
  formModal.value = true
  detailCompany.value = null
}

async function saveCompany() {
  if (!form.name.trim()) { formError.value = '회사명을 입력하세요.'; return }
  saving.value = true
  formError.value = ''
  try {
    const payload = { name: form.name.trim(), description: form.description, logoUrl: form.logoUrl }
    if (editingCompany.value) {
      await adminApi.updateCompany(editingCompany.value.id, payload)
    } else {
      await adminApi.createCompany(payload)
    }
    await loadData()
    formModal.value = false
  } catch (e: any) {
    formError.value = e.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function openDetail(c: Company) {
  try {
    const res = await adminApi.getCompany(c.id)
    detailCompany.value = res.data
    detailTeams.value = res.data.teams ?? []
    selectedTeamId.value = null
  } catch {
    detailCompany.value = c
    detailTeams.value = []
  }
}

async function toggleActive(c: Company) {
  try {
    await adminApi.toggleCompanyActive(c.id)
    c.isActive = !c.isActive
    if (detailCompany.value?.id === c.id) detailCompany.value = { ...detailCompany.value, isActive: c.isActive }
    await loadData()
  } catch (e: any) {
    alert(e.response?.data?.message || '상태 변경에 실패했습니다.')
  }
}

async function addTeamToCompany() {
  if (!selectedTeamId.value || !detailCompany.value) return
  try {
    await adminApi.addTeamToCompany(detailCompany.value.id, selectedTeamId.value)
    const team = allTeams.value.find(t => t.id === selectedTeamId.value)
    if (team) detailTeams.value.push(team)
    selectedTeamId.value = null
  } catch (e: any) {
    alert(e.response?.data?.message || '팀 연결에 실패했습니다.')
  }
}

async function removeTeamFromCompany(teamId: number) {
  if (!detailCompany.value) return
  if (!confirm('이 팀의 연결을 해제하시겠습니까?')) return
  try {
    await adminApi.removeTeamFromCompany(detailCompany.value.id, teamId)
    detailTeams.value = detailTeams.value.filter(t => t.id !== teamId)
  } catch (e: any) {
    alert(e.response?.data?.message || '해제에 실패했습니다.')
  }
}
</script>

<style scoped>
.admin-companies {
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
.back-btn { font-size: 26px; color: #fff; width: 32px; }
.admin-title { font-size: 17px; font-weight: 700; }
.btn-add {
  font-size: 13px; font-weight: 700; color: #fff;
  padding: 6px 12px; border-radius: 10px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.35);
}

.admin-body { padding: 14px; display: flex; flex-direction: column; gap: 12px; }

.search-wrap { display: flex; gap: 8px; }
.search-input {
  flex: 1; padding: 11px 14px; border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none;
}
.search-input:focus { border-color: var(--color-primary); }

.list-wrap { display: flex; flex-direction: column; gap: 8px; }
.company-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px;
  background: var(--color-card);
  border-radius: var(--radius-card);
  border: 1px solid var(--color-separator);
  box-shadow: var(--shadow-card);
  cursor: pointer; gap: 10px;
}
.company-info { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.company-logo {
  width: 38px; height: 38px; border-radius: 8px; object-fit: cover; flex-shrink: 0;
}
.company-logo-placeholder {
  width: 38px; height: 38px; border-radius: 8px;
  background: var(--color-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.company-meta { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.company-name { font-size: 14px; font-weight: 600; color: var(--color-text); }
.company-desc {
  font-size: 12px; color: var(--color-text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.company-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.active-badge {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 8px;
}
.active-badge.active   { background: #E8F5E9; color: #2E7D32; }
.active-badge.inactive { background: var(--color-surface); color: var(--color-text-secondary); }
.card-arrow { font-size: 18px; color: var(--color-text-secondary); }

.empty-msg { text-align: center; padding: 32px; font-size: 14px; color: var(--color-text-secondary); }
.empty-sm  { font-size: 13px; color: var(--color-text-secondary); padding: 8px 0; }

/* 모달 */
.modal-overlay {
  position: fixed; inset: 0;
  background: var(--color-overlay);
  display: flex; align-items: flex-end; justify-content: center;
  z-index: 500;
}
.modal-sheet {
  width: 100%; max-width: 430px; max-height: 90vh;
  background: var(--color-card);
  border-radius: 20px 20px 0 0;
  overflow-y: auto;
  animation: slideUp 0.25s ease;
}
.detail-sheet { max-height: 85vh; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle {
  width: 40px; height: 4px;
  background: var(--color-separator); border-radius: 2px;
  margin: 10px auto 0;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px 12px;
  border-bottom: 1px solid var(--color-separator);
}
.modal-header h3 { font-size: 16px; font-weight: 700; color: var(--color-text); }
.modal-close { font-size: 18px; color: var(--color-text-secondary); padding: 4px 8px; }

.modal-form { padding: 16px 20px; display: flex; flex-direction: column; gap: 16px; }
.form-field  { display: flex; flex-direction: column; gap: 6px; }
.form-label  { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }
.required    { color: #F44336; }
.form-input {
  padding: 10px 13px; border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none; width: 100%;
}
.form-input:focus { border-color: var(--color-primary); }
.form-textarea { resize: none; font-family: inherit; }
.form-err { font-size: 12px; color: #F44336; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; padding-top: 4px; }
.btn-cancel {
  padding: 10px 16px; border-radius: 10px;
  background: var(--color-surface); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600; border: 1px solid var(--color-separator);
}
.btn-save {
  padding: 10px 22px; border-radius: 10px;
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 14px; font-weight: 700; transition: opacity 0.2s;
}
.btn-save:disabled { opacity: 0.5; }

/* 상세 */
.detail-body { padding: 16px 20px; display: flex; flex-direction: column; gap: 16px; }
.detail-actions { display: flex; gap: 8px; }
.detail-desc { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.detail-section { display: flex; flex-direction: column; gap: 8px; }
.detail-section-title { font-size: 14px; font-weight: 700; color: var(--color-text); }
.btn-sm {
  padding: 6px 12px; border-radius: 8px;
  font-size: 12px; font-weight: 600; cursor: pointer;
}
.btn-edit     { background: var(--color-primary-light); color: var(--color-primary); }
.btn-deactivate { background: #FFF3E0; color: #E65100; }
.btn-activate   { background: #E8F5E9; color: #2E7D32; }
.btn-add-team   {
  padding: 8px 14px; background: var(--color-primary);
  color: var(--color-on-primary); font-size: 13px; font-weight: 700;
  border-radius: 10px;
}
.btn-add-team:disabled { opacity: 0.4; cursor: not-allowed; }

.team-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.team-chip {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 10px; border-radius: 16px;
  background: var(--color-surface); color: var(--color-text);
  font-size: 12px; font-weight: 500;
  border: 1px solid var(--color-separator);
}
.chip-dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }
.chip-remove {
  font-size: 11px; color: var(--color-text-secondary);
  padding: 0 2px; cursor: pointer; margin-left: 2px;
}
.chip-remove:hover { color: #F44336; }

.add-team-row { display: flex; gap: 8px; align-items: center; margin-top: 4px; }
.select-sm { flex: 1; padding: 8px 10px; font-size: 13px; }

.user-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.user-chip {
  padding: 4px 10px; border-radius: 14px;
  background: var(--color-primary-light); color: var(--color-primary);
  font-size: 12px; font-weight: 600;
}
</style>
