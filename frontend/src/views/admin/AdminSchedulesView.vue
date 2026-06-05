<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <button class="topbar-back" @click="router.push('/admin')"><AppIcon name="chevron-left" size="md" /></button>
      <span class="topbar-title">전체 일정</span>
      <div style="display:flex;align-items:center;gap:6px">
        <button class="topbar-action" @click="filterOpen = true">
          <AppIcon name="filter" size="sm" />
          필터<span v-if="activeFilterCount > 0" class="topbar-filter-count">{{ activeFilterCount }}</span>
        </button>
        <button class="topbar-action" @click="viewMode = viewMode === 'list' ? 'calendar' : 'list'">
          {{ viewMode === 'list' ? '▦' : '≡' }}
        </button>
        <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
          <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
        </button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃">
          <AppIcon name="logout" size="md" />
        </button>
      </div>
    </header>

    <!-- Month nav -->
    <div class="month-nav">
      <button class="month-arrow" @click="prevMonth"><AppIcon name="chevron-left" size="md" /></button>
      <span class="month-label">{{ viewYear }}년 {{ viewMonth }}월</span>
      <button class="month-arrow" @click="nextMonth"><AppIcon name="chevron-right" size="md" /></button>
    </div>

    <!-- Active filter chips -->
    <div v-if="hasActiveFilter" class="filter-chips-bar">
      <button v-if="userFilter !== null" class="chip active" @click="userFilter = null">
        {{ getUserName(userFilter) }} <AppIcon name="close" size="sm" />
      </button>
      <button v-if="catFilter !== null" class="chip active" @click="catFilter = null">
        {{ CATEGORY_LABELS[catFilter] }} <AppIcon name="close" size="sm" />
      </button>
      <button v-if="statusFilter !== null" class="chip active" @click="statusFilter = null">
        {{ SCHEDULE_STATUS_LABELS[statusFilter] }} <AppIcon name="close" size="sm" />
      </button>
    </div>

    <!-- Result count -->
    <div class="result-bar">{{ filteredSchedules.length }}건</div>

    <!-- List view -->
    <div v-if="viewMode === 'list'" class="sched-scroll">
      <template v-if="filteredSchedules.length > 0">
        <template v-for="(group, date) in groupedSchedules" :key="date">
          <div class="agenda-date">{{ date }}</div>
          <div style="background:var(--color-surface);border-bottom:1px solid var(--color-border)">
            <div v-for="s in group" :key="s.id" class="agenda-item" @click="openEdit(s)">
              <div class="agenda-bar" :style="{ background: DOT_COLORS[s.category] }"></div>
              <div class="agenda-body">
                <div class="agenda-title">{{ s.title }}</div>
                <div class="agenda-meta">
                  <span>{{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType === 'HOME' ? ' · 홈' : ' · 원정') : '' }}</span>
                  <span>{{ getUserName(s.userId) }}</span>
                  <span v-if="s.startTime">{{ s.startTime }}</span>
                </div>
              </div>
              <AppIcon name="chevron-right" size="sm" class="row-arrow" />
            </div>
          </div>
        </template>
      </template>
      <div v-else class="empty"><AppIcon name="calendar" size="lg" class="empty-icon" style="color:var(--color-text-3)" /><p class="empty-text">해당하는 일정이 없습니다</p></div>
    </div>

    <!-- Calendar view -->
    <div v-else class="cal-scroll">
      <div class="cal-wrap">
        <div class="dow-row">
          <span v-for="d in ['일','월','화','수','목','금','토']" :key="d" class="dow-cell">{{ d }}</span>
        </div>
        <div class="cal-grid">
          <div v-for="(cell, idx) in calCells" :key="idx"
            class="cal-cell"
            :class="{ empty: !cell.isCurrentMonth, today: cell.isToday }">
            <span class="day-num" v-if="cell.isCurrentMonth">{{ cell.day }}</span>
            <div v-if="cell.isCurrentMonth" class="cal-events">
              <div v-for="s in getSchedulesByDate(cell.date)" :key="s.id"
                class="cal-ev" :style="{ background: DOT_COLORS[s.category] + 'dd' }"
                @click="openEdit(s)">
                <span class="cal-ev-text">{{ s.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filter sheet -->
    <Teleport to="body">
      <div class="overlay" v-if="filterOpen" @click.self="filterOpen = false">
        <div class="sheet">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">필터</span>
            <button class="sheet-close" @click="filterOpen = false"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <div class="field">
              <label class="field-label">사용자</label>
              <div class="chip-row" style="flex-wrap:wrap;gap:6px;margin-top:6px">
                <button :class="['chip', { active: userFilter === null }]" @click="userFilter = null">전체</button>
                <button v-for="u in activeUserList" :key="u.id"
                  :class="['chip', { active: userFilter === u.id }]"
                  @click="userFilter = u.id">{{ u.name }}</button>
              </div>
            </div>
            <div class="field" style="margin-top:16px">
              <label class="field-label">종목</label>
              <div class="chip-row" style="flex-wrap:wrap;gap:6px;margin-top:6px">
                <button :class="['chip', { active: catFilter === null }]" @click="catFilter = null">전체</button>
                <button v-for="cat in CATEGORIES" :key="cat.value"
                  :class="['chip', { active: catFilter === cat.value }]"
                  @click="catFilter = cat.value as Category">{{ cat.label }}</button>
              </div>
            </div>
            <div class="field" style="margin-top:16px">
              <label class="field-label">상태</label>
              <div class="chip-row" style="flex-wrap:wrap;gap:6px;margin-top:6px">
                <button :class="['chip', { active: statusFilter === null }]" @click="statusFilter = null">전체</button>
                <button v-for="s in STATUS_OPTIONS" :key="s.value"
                  :class="['chip', { active: statusFilter === s.value }]"
                  @click="statusFilter = s.value">{{ s.label }}</button>
              </div>
            </div>
          </div>
          <div class="sheet-footer">
            <button class="btn btn-secondary btn-full" @click="userFilter=null; catFilter=null; statusFilter=null">초기화</button>
            <button class="btn btn-primary btn-full" @click="filterOpen = false">적용</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Edit modal -->
    <Teleport to="body">
      <div class="overlay" v-if="editTarget" @click.self="editTarget = null">
        <div class="sheet sheet-full">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">일정 수정 (관리자)</span>
            <button class="sheet-close" @click="editTarget = null"><AppIcon name="close" size="sm" /></button>
          </div>
          <div class="sheet-body">
            <div style="font-size:13px;color:var(--color-text-2);margin-bottom:12px">작성자: {{ getUserName(editTarget.userId) }}</div>
            <form @submit.prevent="handleSave">
              <div class="form-row-2">
                <div class="field">
                  <label class="field-label">시작일</label>
                  <input v-model="formData.date" type="date" class="field-input" />
                </div>
                <div class="field">
                  <label class="field-label">시작 시간</label>
                  <input v-model="formData.startTime" type="time" class="field-input" />
                </div>
              </div>
              <div class="form-row-2" style="margin-top:8px">
                <div class="field">
                  <label class="field-label">종료일</label>
                  <input v-model="formData.endDate" type="date" class="field-input" :min="formData.date" />
                </div>
                <div class="field">
                  <label class="field-label">종료 시간</label>
                  <input v-model="formData.endTime" type="time" class="field-input" />
                </div>
              </div>
              <div class="field" style="margin-top:10px">
                <label class="field-label">제목</label>
                <input v-model="formData.title" type="text" class="field-input" maxlength="100" />
              </div>
              <div class="field" style="margin-top:10px">
                <label class="field-label">카테고리</label>
                <div class="radio-grid">
                  <label v-for="cat in CATEGORIES" :key="cat.value" class="radio-item"
                    :class="{ selected: formData.category === cat.value }"
                    :style="formData.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category] + '15' } : {}">
                    <input type="radio" :value="cat.value" v-model="formData.category" class="radio-input" />
                    <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                    <span class="radio-label">{{ cat.label }}</span>
                  </label>
                </div>
              </div>
              <Transition name="slide">
                <div class="field" v-if="formData.category === 'BASEBALL'" style="margin-top:10px">
                  <label class="field-label">홈 / 원정</label>
                  <div class="radio-row-2">
                    <label v-for="bt in [{ value: 'HOME', label: '홈' }, { value: 'AWAY', label: '원정' }]" :key="bt.value"
                      class="radio-item"
                      :class="{ selected: formData.baseballType === bt.value }"
                      :style="formData.baseballType === bt.value ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-light)' } : {}">
                      <input type="radio" :value="bt.value" v-model="formData.baseballType" class="radio-input" />
                      <span class="radio-label">{{ bt.label }}</span>
                    </label>
                  </div>
                </div>
              </Transition>
              <div class="field" style="margin-top:10px">
                <label class="field-label">장소</label>
                <input v-model="formData.location" type="text" class="field-input" maxlength="200" placeholder="장소 입력" />
              </div>
              <div class="field" style="margin-top:10px">
                <label class="field-label">상태</label>
                <select v-model="formData.status" class="field-input field-select">
                  <option value="SCHEDULED">예정</option>
                  <option value="CONFIRMED">확정</option>
                  <option value="CHANGED">변경</option>
                  <option value="CANCELLED">취소</option>
                </select>
              </div>
              <div class="field" style="margin-top:10px">
                <label class="field-label">메모</label>
                <textarea v-model="formData.memo" class="field-input field-textarea" rows="3" maxlength="500" />
              </div>
            </form>
          </div>
          <div class="sheet-footer">
            <button type="button" class="btn btn-danger btn-sm" @click="handleDelete">삭제</button>
            <div style="display:flex;gap:8px;margin-left:auto">
              <button type="button" class="btn btn-secondary btn-sm" @click="editTarget = null">취소</button>
              <button type="button" class="btn btn-primary btn-sm" @click="handleSave">저장</button>
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
import { ref, computed, reactive, watch, onMounted } from 'vue'
import BottomNavAdmin from '@/components/BottomNavAdmin.vue'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedule'
import { useUsersStore } from '@/stores/users'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ScheduleFormData, ScheduleStatus } from '@/types'
import { CATEGORY_LABELS, SCHEDULE_STATUS_LABELS } from '@/types'

const router = useRouter()
const themeSheetOpen = ref(false)
const route  = useRoute()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
const scheduleStore = useScheduleStore()
const usersStore    = useUsersStore()

const allSchedules = ref<Schedule[]>([])
const viewMode = ref<'list' | 'calendar'>('list')
const filterOpen = ref(false)

onMounted(async () => {
  await usersStore.fetchAll()
  const res = await adminApi.getAllSchedules()
  allSchedules.value = res.data
})

// Calendar cells
const calCells = computed(() => {
  const todayStr = new Date().toISOString().slice(0, 10)
  const firstDay = new Date(viewYear.value, viewMonth.value - 1, 1)
  const lastDay  = new Date(viewYear.value, viewMonth.value, 0)
  const cells = []
  for (let i = 0; i < firstDay.getDay(); i++) cells.push({ isCurrentMonth: false, date: '', day: 0, isToday: false })
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const dateStr = `${viewYear.value}-${String(viewMonth.value).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    cells.push({ isCurrentMonth: true, date: dateStr, day: d, isToday: dateStr === todayStr })
  }
  return cells
})

function getSchedulesByDate(date: string): Schedule[] {
  return filteredSchedules.value.filter(s => s.date === date)
}

const DOT_COLORS: Record<Category, string> = {
  BASEBALL:          'var(--color-dot-baseball)',
  BASKETBALL:        'var(--color-dot-basketball)',
  SOCCER:            'var(--color-dot-soccer)',
  WOMENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  MENS_VOLLEYBALL:   'var(--color-dot-volleyball)',
  ETC:               'var(--color-dot-etc)',
}
const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))

const now = new Date()
const viewYear  = ref(now.getFullYear())
const viewMonth = ref(now.getMonth() + 1)
function prevMonth() {
  if (viewMonth.value === 1) { viewMonth.value = 12; viewYear.value-- }
  else viewMonth.value--
}
function nextMonth() {
  if (viewMonth.value === 12) { viewMonth.value = 1; viewYear.value++ }
  else viewMonth.value++
}

const initialUserId = route.query.userId ? Number(route.query.userId) : null
const userFilter   = ref<number | null>(initialUserId)
const catFilter    = ref<Category | null>(null)
const statusFilter = ref<ScheduleStatus | null>(null)

const hasActiveFilter = computed(() => userFilter.value !== null || catFilter.value !== null || statusFilter.value !== null)
const activeFilterCount = computed(() => {
  let count = 0
  if (userFilter.value !== null) count++
  if (catFilter.value !== null) count++
  if (statusFilter.value !== null) count++
  return count
})

const STATUS_OPTIONS = Object.entries(SCHEDULE_STATUS_LABELS).map(([value, label]) => ({ value: value as ScheduleStatus, label }))

const activeUserList = computed(() =>
  usersStore.users.filter(u => u.role !== 'ADMIN')
)

function getUserName(userId: number): string {
  return usersStore.users.find(u => u.id === userId)?.name ?? `#${userId}`
}

const filteredSchedules = computed(() => {
  const prefix = `${viewYear.value}-${String(viewMonth.value).padStart(2, '0')}`
  return allSchedules.value
    .filter(s => {
      if (!s.date.startsWith(prefix)) return false
      if (userFilter.value !== null && s.userId !== userFilter.value) return false
      if (catFilter.value !== null && s.category !== catFilter.value) return false
      if (statusFilter.value !== null && s.status !== statusFilter.value) return false
      return true
    })
    .sort((a, b) => a.date.localeCompare(b.date) || b.id - a.id)
})

const groupedSchedules = computed(() => {
  const groups: Record<string, Schedule[]> = {}
  for (const s of filteredSchedules.value) {
    const d = new Date(s.date + 'T00:00:00')
    const label = `${d.getMonth()+1}월 ${d.getDate()}일 (${['일','월','화','수','목','금','토'][d.getDay()]})`
    if (!groups[label]) groups[label] = []
    groups[label].push(s)
  }
  return groups
})

const editTarget = ref<Schedule | null>(null)
const formData = reactive<ScheduleFormData>({
  title: '', category: '', baseballType: null, date: '', startTime: '', endDate: '', endTime: '', location: '', memo: '', status: 'SCHEDULED',
})

function openEdit(s: Schedule) {
  editTarget.value = s
  formData.title = s.title
  formData.category = s.category
  formData.baseballType = s.baseballType
  formData.date = s.date
  formData.startTime = s.startTime ?? ''
  formData.endDate = s.endDate || ''
  formData.endTime = s.endTime ?? ''
  formData.location = s.location ?? ''
  formData.memo = s.memo ?? ''
  formData.status = s.status ?? 'SCHEDULED'
}

watch(() => formData.category, (cat) => {
  if (cat !== 'BASEBALL') formData.baseballType = null
})

function handleSave() {
  if (!editTarget.value) return
  scheduleStore.update(editTarget.value.id, { ...formData })
  editTarget.value = null
}

function handleDelete() {
  if (!editTarget.value) return
  if (confirm('이 일정을 삭제하시겠습니까?')) {
    scheduleStore.remove(editTarget.value.id)
    editTarget.value = null
  }
}
</script>

<style scoped>
.month-nav { display:flex; align-items:center; justify-content:center; gap:20px; padding:10px 16px; background:var(--color-bg); flex-shrink:0; border-bottom:1px solid var(--color-border); }
.month-label { font-size:14px; font-weight:700; color:var(--color-text-1); min-width:110px; text-align:center; }
.month-arrow { font-size:20px; color:var(--color-primary); width:30px; height:30px; display:flex; align-items:center; justify-content:center; border-radius:50%; background:var(--color-primary-light); }
.filter-chips-bar { display:flex; gap:6px; padding:8px 16px; overflow-x:auto; scrollbar-width:none; border-bottom:1px solid var(--color-border); }
.filter-chips-bar::-webkit-scrollbar { display:none; }
.result-bar { font-size:12px; color:var(--color-text-3); padding:6px 16px; }
.sched-scroll { flex:1; overflow-y:auto; padding-bottom:16px; }
.cal-scroll { flex:1; overflow-y:auto; }
.cal-wrap { padding:4px 8px; }
.dow-row { display:grid; grid-template-columns:repeat(7,1fr); margin-bottom:2px; }
.dow-cell { text-align:center; font-size:11px; font-weight:600; color:var(--color-text-2); padding:4px 0; }
.cal-grid { display:grid; grid-template-columns:repeat(7,1fr); gap:2px; overflow:hidden; }
.cal-cell { min-height:60px; display:flex; flex-direction:column; padding:3px; border-radius:6px; min-width:0; overflow:hidden; }
.cal-cell.empty { background:transparent; }
.cal-cell.today .day-num { background:var(--color-primary); color:#fff; border-radius:50%; }
.day-num { font-size:11px; font-weight:600; color:var(--color-text-1); width:20px; height:20px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.cal-events { display:flex; flex-direction:column; gap:1px; margin-top:1px; overflow:hidden; min-width:0; }
.cal-ev { border-radius:2px; padding:1px 3px; cursor:pointer; min-width:0; overflow:hidden; }
.cal-ev-text { font-size:9px; color:#fff; display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:100%; }
/* edit form */
.form-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.radio-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; margin-top:4px; }
.radio-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-top:4px; }
.radio-item { display:flex; align-items:center; gap:6px; padding:8px 10px; border-radius:var(--radius-md); border:1.5px solid var(--color-border); background:var(--color-surface-2); cursor:pointer; font-size:13px; font-weight:500; color:var(--color-text-1); transition:border-color 0.15s,background 0.15s; }
.radio-item.selected { font-weight:700; }
.radio-input { display:none; }
.radio-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.radio-label { font-size:13px; }
.slide-enter-active, .slide-leave-active { transition:all 0.2s ease; overflow:hidden; }
.slide-enter-from, .slide-leave-to { opacity:0; max-height:0; }
.slide-enter-to, .slide-leave-from { opacity:1; max-height:200px; }
.topbar-filter-count { background:var(--color-primary); color:#fff; border-radius:10px; padding:0 5px; font-size:10px; font-weight:700; margin-left:3px; }
</style>
