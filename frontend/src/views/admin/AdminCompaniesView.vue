<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <button class="topbar-back" @click="router.push('/admin')"><AppIcon name="chevron-left" size="md" /></button>
      <span class="topbar-title">회사 관리</span>
      <button class="topbar-action" @click="openAddModal" style="margin-left:auto">+ 등록</button>
      <div style="display:flex;align-items:center;gap:4px">
        <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
          <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
        </button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃">
          <AppIcon name="logout" size="md" />
        </button>
      </div>
    </header>

    <!-- Search -->
    <div style="padding:10px 16px;background:var(--color-surface);border-bottom:1px solid var(--color-border)">
      <div class="search-box">
        <AppIcon name="search" size="sm" style="color:var(--color-text-3);flex-shrink:0" />
        <input v-model="searchQuery" class="search-box-input" placeholder="회사명 검색" />
      </div>
    </div>

    <!-- Compact summary -->
    <div class="summary-inline">
      <span class="sum-num">{{ companies.length }}</span>개
      <span class="sum-sep">·</span>
      활성 <span class="sum-num">{{ companies.filter(c => c.isActive).length }}</span>
      <span class="sum-sep">·</span>
      비활성 <span class="sum-num">{{ companies.filter(c => !c.isActive).length }}</span>
    </div>

    <!-- Company list -->
    <div style="flex:1;overflow-y:auto">
      <div v-if="filteredCompanies.length === 0" class="empty">
        <AppIcon name="building" size="lg" class="empty-icon" style="color:var(--color-text-3)" />
        <p class="empty-text">{{ searchQuery ? '검색 결과가 없습니다' : '등록된 회사가 없습니다' }}</p>
      </div>
      <div v-else class="row-list">
        <div v-for="c in filteredCompanies" :key="c.id" class="row-item" @click="openDetail(c)">
          <div class="company-logo-sm">
            <img v-if="c.logoUrl" :src="c.logoUrl" style="width:100%;height:100%;object-fit:cover;border-radius:6px" />
            <span v-else style="font-size:14px;font-weight:700;color:var(--color-text-2)">{{ c.name[0] }}</span>
          </div>
          <div class="row-body">
            <div class="row-title">{{ c.name }}</div>
            <div v-if="c.description" class="row-sub">{{ c.description }}</div>
          </div>
          <span :class="['badge', c.isActive ? 'badge-active' : 'badge-disabled']">{{ c.isActive ? '활성' : '비활성' }}</span>
          <AppIcon name="chevron-right" size="sm" style="color:var(--color-text-3)" />
        </div>
      </div>
    </div>

    <!-- Add/edit sheet -->
    <Teleport to="body">
      <div class="overlay" v-if="formModal" @click.self="formModal = false">
        <div class="sheet" @click.stop>
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">{{ editingCompany ? '회사 수정' : '회사 등록' }}</span>
            <button class="sheet-close" @click="formModal = false"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <form @submit.prevent="saveCompany">
              <div class="field">
                <label class="field-label">회사명 <span style="color:var(--color-danger)">*</span></label>
                <input v-model="form.name" class="field-input" placeholder="회사명을 입력하세요" required />
              </div>
              <div class="field" style="margin-top:12px">
                <label class="field-label">설명</label>
                <textarea v-model="form.description" class="field-input field-textarea" placeholder="회사 설명" rows="3" />
              </div>
              <div class="field" style="margin-top:12px">
                <label class="field-label">로고 URL</label>
                <input v-model="form.logoUrl" class="field-input" placeholder="https://..." />
              </div>
              <p v-if="formError" class="field-err" style="margin-top:6px">{{ formError }}</p>
            </form>
          </div>
          <div class="sheet-footer">
            <button type="button" class="btn btn-secondary btn-full" @click="formModal = false">취소</button>
            <button type="button" class="btn btn-primary btn-full" :disabled="saving" @click="saveCompany">{{ saving ? '저장 중...' : '저장' }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Detail sheet -->
    <Teleport to="body">
      <div class="overlay" v-if="detailCompany" @click.self="detailCompany = null">
        <div class="sheet sheet-full">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">{{ detailCompany.name }}</span>
            <button class="sheet-close" @click="detailCompany = null"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <div style="display:flex;gap:8px;margin-bottom:14px">
              <button class="btn btn-outline btn-sm" @click="openEditModal(detailCompany)">수정</button>
              <button class="btn btn-sm" :class="detailCompany.isActive ? 'btn-danger' : 'btn-primary'" @click="toggleActive(detailCompany)">
                {{ detailCompany.isActive ? '비활성화' : '활성화' }}
              </button>
            </div>
            <p v-if="detailCompany.description" style="font-size:14px;color:var(--color-text-2);margin-bottom:14px">{{ detailCompany.description }}</p>
            <div class="divider-sm"></div>

            <!-- Teams -->
            <div class="field" style="margin-bottom:14px">
              <span class="field-label">담당 팀</span>
              <div v-if="detailTeams.length === 0" style="font-size:13px;color:var(--color-text-3);margin-top:6px">연결된 팀이 없습니다</div>
              <div v-else style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                <span v-for="t in detailTeams" :key="t.id" class="chip active" style="gap:6px">
                  <span style="width:7px;height:7px;border-radius:50%;background:currentColor;display:inline-block"></span>
                  {{ t.name }}
                  <button @click.stop="removeTeamFromCompany(t.id)" style="display:flex;align-items:center;opacity:0.7"><AppIcon name="close" size="sm" /></button>
                </span>
              </div>
              <div v-if="availableTeams.length > 0" style="display:flex;gap:8px;margin-top:10px">
                <select v-model="selectedTeamId" class="field-input field-select" style="height:36px;font-size:13px;flex:1">
                  <option :value="null">팀 선택...</option>
                  <option v-for="t in availableTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <button class="btn btn-primary btn-sm" :disabled="!selectedTeamId" @click="addTeamToCompany">연결</button>
              </div>
            </div>
            <div class="divider-sm"></div>

            <!-- Users -->
            <div class="field">
              <span class="field-label">소속 사용자</span>
              <div v-if="!detailCompany.users || detailCompany.users.length === 0" style="font-size:13px;color:var(--color-text-3);margin-top:6px">소속 사용자가 없습니다</div>
              <div v-else style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                <span v-for="u in detailCompany.users" :key="u.id" class="chip">{{ u.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <BottomNavAdmin />
    <ThemeSheet :open="themeSheetOpen" @close="themeSheetOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BottomNavAdmin from '@/components/BottomNavAdmin.vue'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'
import { adminApi } from '@/api/admin'
import type { Company, Team, Category } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const themeSheetOpen = ref(false)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
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
.search-box { display: flex; align-items: center; gap: 8px; background: var(--color-surface-2); border-radius: 10px; padding: 0 12px; height: 38px; border: 1px solid var(--color-border); }
.search-box-input { flex: 1; background: none; border: none; outline: none; font-size: 14px; color: var(--color-text-1); }
.search-box-input::placeholder { color: var(--color-text-3); }
.summary-inline { padding: 8px 16px; font-size: 13px; color: var(--color-text-2); border-bottom: 1px solid var(--color-border); }
.sum-num { font-weight: 600; color: var(--color-text-1); }
.sum-sep { margin: 0 6px; color: var(--color-text-3); }
.company-logo-sm { width: 36px; height: 36px; border-radius: 8px; background: var(--color-surface-2); display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: hidden; border: 1px solid var(--color-border); }
</style>
