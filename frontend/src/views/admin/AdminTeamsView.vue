<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <button class="topbar-back" @click="router.push('/admin')"><AppIcon name="chevron-left" size="md" /></button>
      <span class="topbar-title">팀 관리</span>
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
        <input v-model="searchQuery" class="search-box-input" placeholder="팀명 검색" />
      </div>
    </div>

    <!-- Category filter chips -->
    <div class="filter-chips-bar">
      <button :class="['chip', { active: catFilter === null }]" @click="catFilter = null">전체</button>
      <button v-for="cat in CATEGORIES" :key="cat.value"
        :class="['chip', { active: catFilter === cat.value }]"
        @click="catFilter = cat.value as Category">{{ cat.label }}</button>
    </div>

    <!-- Compact summary -->
    <div class="summary-inline">
      <span class="sum-num">{{ teams.length }}</span>개
      <span class="sum-sep">·</span>
      활성 <span class="sum-num">{{ teams.filter(t => t.isActive).length }}</span>
      <span class="sum-sep">·</span>
      비활성 <span class="sum-num">{{ teams.filter(t => !t.isActive).length }}</span>
    </div>

    <!-- Team list -->
    <div style="flex:1;overflow-y:auto">
      <div v-if="filteredTeams.length === 0" class="empty">
        <AppIcon name="team" size="lg" class="empty-icon" style="color:var(--color-text-3)" />
        <p class="empty-text">{{ catFilter ? '해당하는 팀이 없습니다' : '등록된 팀이 없습니다' }}</p>
      </div>
      <div v-else class="row-list">
        <div v-for="t in filteredTeams" :key="t.id" class="row-item" @click="openDetail(t)">
          <div class="team-cat-icon" :style="{ background: DOT_COLORS[t.category] + '22' }">
            <span style="width:10px;height:10px;border-radius:50%;display:inline-block" :style="{ background: DOT_COLORS[t.category] }"></span>
          </div>
          <div class="row-body">
            <div class="row-title">{{ t.name }}</div>
            <div class="row-sub">{{ CATEGORY_LABELS[t.category] }}</div>
          </div>
          <span :class="['badge', t.isActive ? 'badge-active' : 'badge-disabled']">{{ t.isActive ? '활성' : '비활성' }}</span>
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
            <span class="sheet-title">{{ editingTeam ? '팀 수정' : '팀 등록' }}</span>
            <button class="sheet-close" @click="formModal = false"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <form @submit.prevent="saveTeam">
              <div class="field">
                <label class="field-label">팀명 <span style="color:var(--color-danger)">*</span></label>
                <input v-model="form.name" class="field-input" placeholder="팀명을 입력하세요" required />
              </div>
              <div class="field" style="margin-top:12px">
                <label class="field-label">카테고리 <span style="color:var(--color-danger)">*</span></label>
                <div class="radio-grid">
                  <label v-for="cat in CATEGORIES" :key="cat.value" class="radio-item"
                    :class="{ selected: form.category === cat.value }"
                    :style="form.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category] + '15' } : {}">
                    <input type="radio" :value="cat.value" v-model="form.category" class="radio-input" />
                    <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                    <span class="radio-label">{{ cat.label }}</span>
                  </label>
                </div>
              </div>
              <div class="field" style="margin-top:12px">
                <label class="field-label">설명</label>
                <textarea v-model="form.description" class="field-input field-textarea" placeholder="팀 설명" rows="2" />
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
            <button type="button" class="btn btn-primary btn-full" :disabled="saving" @click="saveTeam">{{ saving ? '저장 중...' : '저장' }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Detail sheet -->
    <Teleport to="body">
      <div class="overlay" v-if="detailTeam" @click.self="detailTeam = null">
        <div class="sheet sheet-full">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <div style="display:flex;align-items:center;gap:8px;flex:1;min-width:0">
              <span style="width:10px;height:10px;border-radius:50%;flex-shrink:0;display:inline-block" :style="{ background: DOT_COLORS[detailTeam.category] }"></span>
              <span class="sheet-title">{{ detailTeam.name }}</span>
              <span style="font-size:11px;color:var(--color-text-3);font-weight:500">{{ CATEGORY_LABELS[detailTeam.category] }}</span>
            </div>
            <button class="sheet-close" @click="detailTeam = null"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <div style="display:flex;gap:8px;margin-bottom:14px">
              <button class="btn btn-outline btn-sm" @click="openEditModal(detailTeam)">수정</button>
              <button class="btn btn-sm" :class="detailTeam.isActive ? 'btn-danger' : 'btn-primary'" @click="toggleActive(detailTeam)">
                {{ detailTeam.isActive ? '비활성화' : '활성화' }}
              </button>
            </div>
            <p v-if="detailTeam.description" style="font-size:14px;color:var(--color-text-2);margin-bottom:14px">{{ detailTeam.description }}</p>
            <div class="divider-sm"></div>
            <div class="field">
              <span class="field-label">연결 회사</span>
              <div v-if="detailCompanies.length === 0" style="font-size:13px;color:var(--color-text-3);margin-top:6px">연결된 회사가 없습니다</div>
              <div v-else style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                <span v-for="c in detailCompanies" :key="c.id" class="chip">{{ c.name }}</span>
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
import type { Team, Company, Category } from '@/types'
import { CATEGORY_LABELS } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const themeSheetOpen = ref(false)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
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
.filter-chips-bar { display:flex; gap:6px; padding:8px 16px; overflow-x:auto; scrollbar-width:none; border-bottom:1px solid var(--color-border); }
.filter-chips-bar::-webkit-scrollbar { display:none; }
.search-box { display: flex; align-items: center; gap: 8px; background: var(--color-surface-2); border-radius: 10px; padding: 0 12px; height: 38px; border: 1px solid var(--color-border); }
.search-box-input { flex: 1; background: none; border: none; outline: none; font-size: 14px; color: var(--color-text-1); }
.search-box-input::placeholder { color: var(--color-text-3); }
.summary-inline { padding: 8px 16px; font-size: 13px; color: var(--color-text-2); border-bottom: 1px solid var(--color-border); }
.sum-num { font-weight: 600; color: var(--color-text-1); }
.sum-sep { margin: 0 6px; color: var(--color-text-3); }
.team-cat-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.radio-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; margin-top:4px; }
.radio-item { display:flex; align-items:center; gap:6px; padding:8px 10px; border-radius:var(--radius-md); border:1.5px solid var(--color-border); background:var(--color-surface-2); cursor:pointer; font-size:13px; font-weight:500; color:var(--color-text-1); transition:border-color 0.15s,background 0.15s; }
.radio-item.selected { font-weight:700; }
.radio-input { display:none; }
.radio-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.radio-label { font-size:13px; }
</style>
