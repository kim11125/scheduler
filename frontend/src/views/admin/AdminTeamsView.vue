<template>
  <div class="app-shell admin-teams">
    <!-- 헤더 -->
    <header class="admin-header">
      <button class="back-btn" @click="router.push('/admin')">&#8249;</button>
      <span class="admin-title">팀 관리</span>
      <button class="btn-add" @click="openAddModal">+ 등록</button>
    </header>

    <div class="admin-body">

      <!-- 검색 + 카테고리 필터 -->
      <div class="filter-section">
        <input v-model="searchQuery" class="search-input" placeholder="팀명 검색..." />
        <div class="filter-chips">
          <button
            class="chip" :class="{ active: catFilter === null }"
            @click="catFilter = null"
          >전체</button>
          <button
            v-for="cat in CATEGORIES" :key="cat.value"
            class="chip" :class="{ active: catFilter === cat.value }"
            :style="catFilter === cat.value ? { background: DOT_COLORS[cat.value as Category], color: '#fff', borderColor: DOT_COLORS[cat.value as Category] } : {}"
            @click="catFilter = cat.value as Category"
          >{{ cat.label }}</button>
        </div>
      </div>

      <!-- 목록 -->
      <div class="list-wrap">
        <div v-if="filteredTeams.length === 0" class="empty-msg">해당하는 팀이 없습니다.</div>
        <div
          v-for="t in filteredTeams"
          :key="t.id"
          class="team-card"
          @click="openDetail(t)"
        >
          <div class="team-left">
            <span class="team-dot" :style="{ background: DOT_COLORS[t.category] }"></span>
            <div class="team-meta">
              <span class="team-name">{{ t.name }}</span>
              <span class="team-cat">{{ CATEGORY_LABELS[t.category] }}</span>
            </div>
          </div>
          <div class="team-right">
            <span class="active-badge" :class="t.isActive ? 'active' : 'inactive'">
              {{ t.isActive ? '활성' : '비활성' }}
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
            <h3>{{ editingTeam ? '팀 수정' : '팀 등록' }}</h3>
            <button class="modal-close" @click="formModal = false">✕</button>
          </div>
          <form class="modal-form" @submit.prevent="saveTeam">
            <div class="form-field">
              <label class="form-label">팀명 <span class="required">*</span></label>
              <input v-model="form.name" class="form-input" placeholder="팀명을 입력하세요" required />
            </div>
            <div class="form-field">
              <label class="form-label">카테고리 <span class="required">*</span></label>
              <div class="radio-grid">
                <label
                  v-for="cat in CATEGORIES" :key="cat.value"
                  class="radio-item"
                  :class="{ selected: form.category === cat.value }"
                  :style="form.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category]+'15' } : {}"
                >
                  <input type="radio" :value="cat.value" v-model="form.category" class="radio-input" />
                  <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                  <span class="radio-label">{{ cat.label }}</span>
                </label>
              </div>
            </div>
            <div class="form-field">
              <label class="form-label">설명</label>
              <textarea v-model="form.description" class="form-input form-textarea" placeholder="팀 설명" rows="2" />
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
      <div class="modal-overlay" v-if="detailTeam" @click.self="detailTeam = null">
        <div class="modal-sheet detail-sheet" @click.stop>
          <div class="modal-handle"></div>
          <div class="modal-header">
            <div class="detail-title-row">
              <span class="team-dot" :style="{ background: DOT_COLORS[detailTeam.category] }"></span>
              <h3>{{ detailTeam.name }}</h3>
              <span class="cat-label">{{ CATEGORY_LABELS[detailTeam.category] }}</span>
            </div>
            <button class="modal-close" @click="detailTeam = null">✕</button>
          </div>

          <div class="detail-body">
            <div class="detail-actions">
              <button class="btn-sm btn-edit" @click="openEditModal(detailTeam)">수정</button>
              <button
                class="btn-sm"
                :class="detailTeam.isActive ? 'btn-deactivate' : 'btn-activate'"
                @click="toggleActive(detailTeam)"
              >{{ detailTeam.isActive ? '비활성화' : '활성화' }}</button>
            </div>
            <div v-if="detailTeam.description" class="detail-desc">{{ detailTeam.description }}</div>

            <!-- 연결 회사 -->
            <div class="detail-section">
              <h4 class="detail-section-title">연결 회사</h4>
              <div v-if="detailCompanies.length === 0" class="empty-sm">연결된 회사가 없습니다.</div>
              <div v-else class="user-chips">
                <span v-for="c in detailCompanies" :key="c.id" class="user-chip">{{ c.name }}</span>
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
import type { Team, Company, Category } from '@/types'
import { CATEGORY_LABELS } from '@/types'

const router = useRouter()
const teams = ref<Team[]>([])
const searchQuery = ref('')
const catFilter = ref<Category | null>(null)
const formModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editingTeam = ref<Team | null>(null)
const detailTeam = ref<Team | null>(null)
const detailCompanies = ref<Company[]>([])

const form = reactive<{ name: string; category: Category | ''; description: string; logoUrl: string }>({
  name: '', category: '', description: '', logoUrl: ''
})

const DOT_COLORS: Record<Category, string> = {
  BASEBALL: '#F44336',
  BASKETBALL: '#FF9800',
  SOCCER: '#4CAF50',
  WOMENS_VOLLEYBALL: '#9C27B0',
  MENS_VOLLEYBALL: '#9C27B0',
  ETC: '#607D8B',
}

const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))

const filteredTeams = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return teams.value.filter(t => {
    if (catFilter.value && t.category !== catFilter.value) return false
    if (q && !t.name.toLowerCase().includes(q)) return false
    return true
  })
})

async function loadTeams() {
  const res = await adminApi.getTeams()
  teams.value = res.data
}

onMounted(loadTeams)

function openAddModal() {
  editingTeam.value = null
  form.name = ''
  form.category = ''
  form.description = ''
  form.logoUrl = ''
  formError.value = ''
  formModal.value = true
}

function openEditModal(t: Team) {
  editingTeam.value = t
  form.name = t.name
  form.category = t.category
  form.description = t.description ?? ''
  form.logoUrl = t.logoUrl ?? ''
  formError.value = ''
  formModal.value = true
  detailTeam.value = null
}

async function saveTeam() {
  if (!form.name.trim()) { formError.value = '팀명을 입력하세요.'; return }
  if (!form.category) { formError.value = '카테고리를 선택하세요.'; return }
  saving.value = true
  formError.value = ''
  try {
    const payload = {
      name: form.name.trim(),
      category: form.category as Category,
      description: form.description,
      logoUrl: form.logoUrl,
    }
    if (editingTeam.value) {
      await adminApi.updateTeam(editingTeam.value.id, payload)
    } else {
      await adminApi.createTeam(payload)
    }
    await loadTeams()
    formModal.value = false
  } catch (e: any) {
    formError.value = e.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function openDetail(t: Team) {
  try {
    const res = await adminApi.getTeam(t.id)
    detailTeam.value = res.data
    detailCompanies.value = res.data.companies ?? []
  } catch {
    detailTeam.value = t
    detailCompanies.value = []
  }
}

async function toggleActive(t: Team) {
  try {
    await adminApi.toggleTeamActive(t.id)
    t.isActive = !t.isActive
    if (detailTeam.value?.id === t.id) detailTeam.value = { ...detailTeam.value, isActive: t.isActive }
    await loadTeams()
  } catch (e: any) {
    alert(e.response?.data?.message || '상태 변경에 실패했습니다.')
  }
}
</script>

<style scoped>
.admin-teams {
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
.back-btn { font-size: 26px; color: var(--color-header-text, #fff); width: 32px; }
.admin-title { font-size: 17px; font-weight: 700; }
.btn-add {
  font-size: 12px; font-weight: 700; color: rgba(255,255,255,0.9);
  padding: 5px 12px; border-radius: var(--radius-pill);
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.3);
}

.admin-body { padding: 14px; display: flex; flex-direction: column; gap: 12px; }

.filter-section { display: flex; flex-direction: column; gap: 8px; }
.search-input {
  padding: 11px 14px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none; width: 100%;
}
.search-input:focus { border-color: var(--color-input-focus); }
.filter-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  font-size: 12px; padding: 4px 12px; border-radius: var(--radius-pill);
  background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary);
  border: 1px solid var(--color-border); cursor: pointer; transition: all 0.15s;
}
.chip.active {
  background: var(--color-primary); color: var(--color-on-primary);
  border-color: var(--color-primary);
}

.list-wrap { display: flex; flex-direction: column; gap: 8px; }
.team-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
  cursor: pointer; gap: 10px;
  transition: opacity 0.15s;
}
.team-card:active { opacity: 0.8; }
.team-left { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.team-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.team-meta { display: flex; flex-direction: column; gap: 2px; }
.team-name { font-size: 14px; font-weight: 600; color: var(--color-text); }
.team-cat { font-size: 12px; color: var(--color-text-secondary); }
.team-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.active-badge {
  font-size: 11px; font-weight: 600; padding: 2px 9px; border-radius: var(--radius-pill);
}
.active-badge.active   { background: var(--color-success-soft); color: var(--color-success); }
.active-badge.inactive { background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary); }
.card-arrow { font-size: 18px; color: var(--color-border); }

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
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  overflow-y: auto;
  box-shadow: var(--shadow-modal);
  animation: slideUp 0.25s ease;
}
.detail-sheet { max-height: 85vh; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle {
  width: 32px; height: 4px;
  background: var(--color-border); border-radius: var(--radius-pill);
  margin: 12px auto 0;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px 12px;
  border-bottom: 1px solid var(--color-border);
}
.detail-title-row { display: flex; align-items: center; gap: 8px; }
.cat-label { font-size: 12px; color: var(--color-text-secondary); }
.modal-header h3 { font-size: 16px; font-weight: 700; color: var(--color-text); }
.modal-close { font-size: 18px; color: var(--color-text-secondary); padding: 4px 8px; }

.modal-form { padding: 16px 20px; display: flex; flex-direction: column; gap: 16px; }
.form-field  { display: flex; flex-direction: column; gap: 6px; }
.form-label  { font-size: 12px; font-weight: 600; color: var(--color-text-secondary); }
.required    { color: var(--color-danger); }
.form-input {
  padding: 10px 13px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none; width: 100%;
}
.form-input:focus { border-color: var(--color-input-focus); }
.form-textarea { resize: none; font-family: inherit; }
.form-err { font-size: 12px; color: var(--color-danger); }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; padding-top: 4px; }
.btn-cancel {
  padding: 10px 16px; border-radius: var(--radius-md);
  background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600; border: 1px solid var(--color-border);
}
.btn-save {
  padding: 10px 22px; border-radius: var(--radius-md);
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 14px; font-weight: 700; transition: opacity 0.2s;
}
.btn-save:disabled { opacity: 0.5; }

.radio-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.radio-item {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 10px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  cursor: pointer; font-size: 13px; color: var(--color-text); transition: all 0.15s;
}
.radio-item.selected { font-weight: 700; }
.radio-input { display: none; }
.radio-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.radio-label { font-size: 13px; }

/* 상세 */
.detail-body { padding: 16px 20px; display: flex; flex-direction: column; gap: 14px; }
.detail-actions { display: flex; gap: 8px; }
.detail-desc { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.detail-section { display: flex; flex-direction: column; gap: 8px; }
.detail-section-title { font-size: 14px; font-weight: 700; color: var(--color-text); }
.btn-sm {
  padding: 5px 12px; border-radius: var(--radius-pill);
  font-size: 12px; font-weight: 600; cursor: pointer;
}
.btn-edit       { background: var(--color-primary-soft, var(--color-primary-light)); color: var(--color-primary); }
.btn-deactivate { background: var(--color-warning-soft); color: var(--color-warning); }
.btn-activate   { background: var(--color-success-soft); color: var(--color-success); }

.user-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.user-chip {
  padding: 4px 10px; border-radius: var(--radius-pill);
  background: var(--color-primary-soft, var(--color-primary-light)); color: var(--color-primary);
  font-size: 12px; font-weight: 600;
}
</style>
