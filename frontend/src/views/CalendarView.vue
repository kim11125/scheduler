<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <span class="topbar-title">📅 캘린더</span>
      <div class="theme-row">
        <button v-for="t in themes" :key="t.key"
          :class="['theme-dot-btn', { 'is-active': themeStore.current === t.key }]"
          :style="{ '--dot-c': t.color }"
          @click="themeStore.setTheme(t.key)" />
      </div>
    </header>

    <!-- Month nav -->
    <div class="month-nav">
      <button class="month-arrow" @click="prevMonth">‹</button>
      <span class="month-label">{{ currentYear }}년 {{ currentMonth }}월</span>
      <button class="month-arrow" @click="nextMonth">›</button>
    </div>

    <!-- Calendar grid -->
    <div class="cal-wrap">
      <div class="dow-row">
        <span v-for="d in DOW_LABELS" :key="d.label" class="dow-cell" :class="d.cls">{{ d.label }}</span>
      </div>
      <div class="cal-grid">
        <div v-for="(cell, idx) in calDays" :key="idx"
          class="cal-cell"
          :class="{
            empty: !cell.isCurrentMonth,
            today: cell.isToday,
            selected: cell.date === selectedDate,
            sunday: cell.dayOfWeek === 0,
            saturday: cell.dayOfWeek === 6,
          }"
          @click="handleDayClick(cell)">
          <template v-if="cell.isCurrentMonth">
            <span class="day-num">{{ cell.day }}</span>
            <div class="dot-row">
              <span v-for="(cat, ci) in scheduleStore.getDotsByDate(cell.date)" :key="ci"
                class="dot" :style="{ background: DOT_COLORS[cat] }" />
              <span v-if="scheduleStore.getExtraCount(cell.date) > 0" class="extra-dot">
                +{{ scheduleStore.getExtraCount(cell.date) }}
              </span>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Day panel -->
    <div class="day-panel">
      <div class="day-panel-hd">
        <span class="day-panel-title">{{ selectedDateLabel }}</span>
        <button class="topbar-action" @click="openAddModal()">+ 새 일정</button>
      </div>

      <div v-if="selectedSchedules.length > 0" class="sched-list">
        <div v-for="s in selectedSchedules" :key="s.id" class="sched-row" @click="openEditModal(s)">
          <div class="sched-bar" :style="{ background: DOT_COLORS[s.category] }"></div>
          <div class="sched-body">
            <div class="sched-top">
              <span class="sched-cat-badge" :style="{ color: DOT_COLORS[s.category], background: DOT_COLORS[s.category] + '22' }">
                {{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType === 'HOME' ? ' · 홈' : ' · 원정') : '' }}
              </span>
              <span v-if="s.status && s.status !== 'SCHEDULED'" class="badge"
                :class="{
                  'badge-active': s.status === 'CONFIRMED',
                  'badge-pending': s.status === 'CHANGED',
                  'badge-rejected': s.status === 'CANCELLED',
                }">
                {{ { CONFIRMED: '확정', CHANGED: '변경', CANCELLED: '취소' }[s.status] }}
              </span>
            </div>
            <span class="sched-title">{{ s.title }}</span>
            <span v-if="s.startTime" class="sched-meta">{{ s.startTime }}{{ s.endTime ? ' ~ ' + s.endTime : '' }}</span>
            <span v-if="s.endDate && s.endDate !== s.date" class="sched-meta">~ {{ s.endDate }}</span>
            <span v-if="s.location" class="sched-meta">📍 {{ s.location }}</span>
          </div>
          <span class="row-arrow">›</span>
        </div>
      </div>

      <div v-else class="empty" style="padding:32px 24px">
        <span class="empty-icon">🗓</span>
        <p class="empty-text">이 날은 일정이 없어요</p>
        <button class="btn btn-outline btn-sm empty-action" @click="openAddModal()">일정 추가하기</button>
      </div>
    </div>

    <!-- FAB -->
    <button class="fab" @click="openAddModal()">+</button>

    <!-- Schedule modal -->
    <Teleport to="body">
      <div class="overlay" v-if="modalOpen" @click.self="closeModal">
        <div class="sheet sheet-full" @click.stop>
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">{{ editTarget ? '일정 수정' : '일정 추가' }}</span>
            <button class="sheet-close" @click="closeModal">✕</button>
          </div>
          <div class="sheet-body">
            <form @submit.prevent="handleSave">
              <div class="field" v-if="authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'">
                <label class="field-label">대상 사용자</label>
                <select v-model="formData.targetUserId" class="field-input field-select">
                  <option :value="null">내 일정</option>
                  <option v-for="u in adminUsers" :key="u.id" :value="u.id">{{ u.name }} (@{{ u.username }})</option>
                </select>
              </div>
              <div class="field" v-if="myTeams.length > 0">
                <label class="field-label">팀 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
                <select v-model="formData.teamId" class="field-input field-select">
                  <option :value="null">팀 없음</option>
                  <option v-for="t in myTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
              </div>
              <div class="form-section-sep">일정 날짜</div>
              <div class="form-row-2">
                <div class="field">
                  <label class="field-label">시작일</label>
                  <input v-model="formData.date" type="date" class="field-input" required />
                </div>
                <div class="field">
                  <label class="field-label">시작 시간</label>
                  <input v-model="formData.startTime" type="time" class="field-input" />
                </div>
              </div>
              <div class="form-row-2">
                <div class="field">
                  <label class="field-label">종료일</label>
                  <input v-model="formData.endDate" type="date" class="field-input" :min="formData.date" />
                </div>
                <div class="field">
                  <label class="field-label">종료 시간</label>
                  <input v-model="formData.endTime" type="time" class="field-input" />
                </div>
              </div>
              <div class="form-section-sep">기본 정보</div>
              <div class="field">
                <label class="field-label">제목 <span style="color:var(--color-danger)">*</span></label>
                <input v-model="formData.title" type="text" class="field-input" placeholder="일정 제목을 입력하세요" maxlength="100" />
                <p v-if="formErrors.title" class="field-err">{{ formErrors.title }}</p>
              </div>
              <div class="field">
                <label class="field-label">카테고리 <span style="color:var(--color-danger)">*</span></label>
                <div class="radio-grid">
                  <label v-for="cat in CATEGORIES" :key="cat.value" class="radio-item"
                    :class="{ selected: formData.category === cat.value }"
                    :style="formData.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category] + '18' } : {}">
                    <input type="radio" :value="cat.value" v-model="formData.category" class="radio-input" />
                    <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                    <span class="radio-label">{{ cat.label }}</span>
                  </label>
                </div>
                <p v-if="formErrors.category" class="field-err">{{ formErrors.category }}</p>
              </div>
              <Transition name="slide">
                <div class="field" v-if="['BASEBALL','WOMENS_VOLLEYBALL','MENS_VOLLEYBALL'].includes(formData.category)">
                  <label class="field-label">홈 / 원정 <span style="color:var(--color-danger)">*</span></label>
                  <div class="radio-row-2">
                    <label v-for="bt in BASEBALL_TYPES" :key="bt.value" class="radio-item"
                      :class="{ selected: formData.baseballType === bt.value }"
                      :style="formData.baseballType === bt.value ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-light)' } : {}">
                      <input type="radio" :value="bt.value" v-model="formData.baseballType" class="radio-input" />
                      <span class="radio-label">{{ bt.label }}</span>
                    </label>
                  </div>
                  <p v-if="formErrors.baseballType" class="field-err">{{ formErrors.baseballType }}</p>
                </div>
              </Transition>
              <div class="form-section-sep">추가 정보</div>
              <div class="field">
                <label class="field-label">장소</label>
                <input v-model="formData.location" type="text" class="field-input" placeholder="장소를 입력하세요" maxlength="200" />
              </div>
              <div class="field">
                <label class="field-label">상태</label>
                <select v-model="formData.status" class="field-input field-select">
                  <option value="SCHEDULED">예정</option>
                  <option value="CONFIRMED">확정</option>
                  <option value="CHANGED">변경</option>
                  <option value="CANCELLED">취소</option>
                </select>
              </div>
              <div class="field">
                <label class="field-label">메모</label>
                <textarea v-model="formData.memo" class="field-input field-textarea" placeholder="메모를 입력하세요" rows="3" maxlength="500" />
              </div>
            </form>
          </div>
          <div class="sheet-footer">
            <button v-if="editTarget" type="button" class="btn btn-danger btn-sm" @click="handleDelete">삭제</button>
            <div style="display:flex;gap:8px;margin-left:auto">
              <button type="button" class="btn btn-secondary btn-sm" @click="closeModal">취소</button>
              <button type="button" class="btn btn-primary btn-sm" @click="handleSave">저장</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <BottomNavUser />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useScheduleStore } from '@/stores/schedule'
import { useCalendar } from '@/composables/useCalendar'
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ThemeKey, ScheduleStatus, TeamRef } from '@/types'
import { CATEGORY_LABELS } from '@/types'
import BottomNavUser from '@/components/BottomNavUser.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const scheduleStore = useScheduleStore()

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender', color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',    color: '#E8836A', label: '피치' },
  { key: 'mint',     color: '#4DB896', label: '민트' },
  { key: 'dark',     color: '#A695F0', label: '다크' },
  { key: 'rose-milk',color: '#D4789A', label: '로즈' },
]

const now = new Date()
const currentYear  = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
  selectedDate.value = `${currentYear.value}-${String(currentMonth.value).padStart(2,'0')}-01`
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
  selectedDate.value = `${currentYear.value}-${String(currentMonth.value).padStart(2,'0')}-01`
}

const { days: calDays } = useCalendar(() => currentYear.value, () => currentMonth.value)
const todayStr = now.toISOString().slice(0, 10)
const selectedDate = ref(todayStr)

const DOW_LABELS = [
  { label: '일', cls: 'sun' }, { label: '월', cls: '' }, { label: '화', cls: '' },
  { label: '수', cls: '' }, { label: '목', cls: '' }, { label: '금', cls: '' },
  { label: '토', cls: 'sat' },
]

const DOT_COLORS: Record<Category, string> = {
  BASEBALL:          'var(--color-dot-baseball)',
  BASKETBALL:        'var(--color-dot-basketball)',
  SOCCER:            'var(--color-dot-soccer)',
  WOMENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  MENS_VOLLEYBALL:   'var(--color-dot-volleyball)',
  ETC:               'var(--color-dot-etc)',
}
const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))
const BASEBALL_TYPES = [{ value: 'HOME', label: '홈' }, { value: 'AWAY', label: '원정' }]

function handleDayClick(cell: { date: string; isCurrentMonth: boolean }) {
  if (!cell.isCurrentMonth || !cell.date) return
  const hasSchedules = scheduleStore.getByDate(cell.date).length > 0
  if (!hasSchedules) { selectedDate.value = cell.date; openAddModal() }
  else if (selectedDate.value === cell.date) openAddModal()
  else selectedDate.value = cell.date
}

const selectedSchedules = computed(() => scheduleStore.getByDate(selectedDate.value))
const selectedDateLabel = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value + 'T00:00:00')
  return `${d.getMonth()+1}월 ${d.getDate()}일 (${['일','월','화','수','목','금','토'][d.getDay()]})`
})

// Modal
const modalOpen  = ref(false)
const editTarget = ref<Schedule | null>(null)
const formData = reactive({
  title: '', category: '' as Category | '', baseballType: null as 'HOME' | 'AWAY' | null,
  date: '', startTime: '', endDate: '', endTime: '', location: '', memo: '',
  targetUserId: null as number | null, teamId: null as number | null,
  status: 'SCHEDULED' as ScheduleStatus,
})
const formErrors = reactive<Record<string, string>>({})

function resetForm() {
  formData.title = ''; formData.category = ''; formData.baseballType = null
  formData.date = selectedDate.value || todayStr; formData.startTime = ''
  formData.endDate = ''; formData.endTime = ''; formData.location = ''
  formData.memo = ''; formData.targetUserId = null; formData.teamId = null
  formData.status = 'SCHEDULED'
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
}

function openAddModal() {
  editTarget.value = null; resetForm()
  formData.date = selectedDate.value || todayStr
  modalOpen.value = true
}

function openEditModal(s: Schedule) {
  editTarget.value = s
  formData.title = s.title; formData.category = s.category; formData.baseballType = s.baseballType
  formData.date = s.date; formData.startTime = s.startTime ?? ''; formData.endDate = s.endDate || ''
  formData.endTime = s.endTime ?? ''; formData.location = s.location ?? ''; formData.memo = s.memo ?? ''
  formData.targetUserId = null; formData.teamId = s.teamId ?? null; formData.status = s.status ?? 'SCHEDULED'
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  modalOpen.value = true
}

function closeModal() { modalOpen.value = false; editTarget.value = null }

const HOME_AWAY_CATS = ['BASEBALL', 'WOMENS_VOLLEYBALL', 'MENS_VOLLEYBALL']

watch(() => formData.category, (cat) => { if (!HOME_AWAY_CATS.includes(cat)) formData.baseballType = null })

function validateForm(): boolean {
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  if (!formData.title.trim()) formErrors.title = '제목을 입력하세요'
  if (!formData.category) formErrors.category = '카테고리를 선택하세요'
  if (HOME_AWAY_CATS.includes(formData.category) && !formData.baseballType) formErrors.baseballType = '홈 또는 원정을 선택하세요'
  return Object.keys(formErrors).length === 0
}

async function handleSave() {
  if (!validateForm()) return
  if (editTarget.value) await scheduleStore.update(editTarget.value.id, { ...formData })
  else { await scheduleStore.add({ ...formData }); selectedDate.value = formData.date }
  closeModal()
}

async function handleDelete() {
  if (!editTarget.value) return
  if (confirm('일정을 삭제하시겠습니까?')) { await scheduleStore.remove(editTarget.value.id); closeModal() }
}

const adminUsers = ref<{id: number; name: string; username: string}[]>([])
const myTeams = ref<TeamRef[]>([])

onMounted(async () => {
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
  const role = authStore.user?.role
  if (role === 'ADMIN' || role === 'MANAGER') {
    const res = await adminApi.getUsers()
    adminUsers.value = res.data.filter((u: any) => u.status === 'ACTIVE' && u.role === 'USER')
  }
  try {
    const profileRes = await userApi.getMyProfile()
    myTeams.value = profileRes.data.teams ?? []
  } catch { myTeams.value = [] }
})
</script>

<style scoped>
/* Month nav */
.month-nav { display:flex; align-items:center; justify-content:center; gap:20px; padding:12px 16px; background:var(--color-bg); flex-shrink:0; }
.month-label { font-size:15px; font-weight:700; color:var(--color-text-1); min-width:110px; text-align:center; }
.month-arrow { font-size:22px; color:var(--color-primary); width:32px; height:32px; display:flex; align-items:center; justify-content:center; border-radius:50%; background:var(--color-primary-light); }
/* Cal grid */
.cal-wrap { padding:4px 8px 0; flex-shrink:0; }
.dow-row { display:grid; grid-template-columns:repeat(7,1fr); margin-bottom:2px; }
.dow-cell { text-align:center; font-size:11px; font-weight:600; color:var(--color-text-2); padding:4px 0; }
.dow-cell.sun { color:var(--color-sunday); }
.dow-cell.sat { color:var(--color-saturday); }
.cal-grid { display:grid; grid-template-columns:repeat(7,1fr); gap:2px; }
.cal-cell { aspect-ratio:1/1.1; display:flex; flex-direction:column; align-items:center; padding-top:4px; cursor:pointer; border-radius:8px; transition:background 0.15s; min-height:44px; }
.cal-cell:active { background:var(--color-surface-2); }
.cal-cell.empty { cursor:default; }
.day-num { width:28px; height:28px; display:flex; align-items:center; justify-content:center; font-size:13px; font-weight:500; color:var(--color-text-1); border-radius:50%; transition:background 0.15s,color 0.15s; }
.cal-cell.sunday .day-num { color:var(--color-sunday); }
.cal-cell.saturday .day-num { color:var(--color-saturday); }
.cal-cell.today .day-num { background:var(--color-calendar-today,var(--color-primary)); color:#fff; font-weight:700; }
.cal-cell.selected:not(.today) .day-num { background:var(--color-calendar-selected,var(--color-primary-light)); color:var(--color-primary); font-weight:700; border:1.5px solid var(--color-primary); }
.dot-row { display:flex; gap:2px; align-items:center; margin-top:2px; min-height:7px; }
.dot { width:5px; height:5px; border-radius:50%; flex-shrink:0; }
.extra-dot { font-size:8px; color:var(--color-text-2); }
/* Day panel */
.day-panel { flex:1; background:var(--color-surface-2); border-top:1px solid var(--color-border); overflow-y:auto; }
.day-panel-hd { display:flex; align-items:center; justify-content:space-between; padding:14px 16px 10px; position:sticky; top:0; z-index:1; background:var(--color-surface-2); }
.day-panel-title { font-size:15px; font-weight:700; color:var(--color-text-1); }
/* Schedule rows */
.sched-list { background:var(--color-surface); }
.sched-row { display:flex; align-items:stretch; border-bottom:1px solid var(--color-border); cursor:pointer; transition:background 0.15s; }
.sched-row:last-child { border-bottom:none; }
.sched-row:active { background:var(--color-surface-2); }
.sched-bar { width:3px; flex-shrink:0; }
.sched-body { flex:1; padding:12px 12px 12px 10px; display:flex; flex-direction:column; gap:3px; min-width:0; }
.sched-top { display:flex; align-items:center; gap:6px; flex-wrap:wrap; }
.sched-cat-badge { font-size:10px; font-weight:700; padding:2px 8px; border-radius:var(--radius-pill); }
.sched-title { font-size:14px; font-weight:600; color:var(--color-text-1); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.sched-meta { font-size:12px; color:var(--color-text-2); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
/* Modal internals */
.form-section-sep { font-size:11px; font-weight:700; color:var(--color-text-3); letter-spacing:0.5px; text-transform:uppercase; padding:14px 0 6px; border-top:1px solid var(--color-border); margin-top:4px; }
.form-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.radio-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; }
.radio-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:8px; }
.radio-item { display:flex; align-items:center; gap:6px; padding:8px 10px; border-radius:var(--radius-md); border:1.5px solid var(--color-border); background:var(--color-surface-2); cursor:pointer; font-size:13px; font-weight:500; color:var(--color-text-1); transition:border-color 0.15s,background 0.15s; }
.radio-item.selected { font-weight:700; }
.radio-input { display:none; }
.radio-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.radio-label { font-size:13px; }
.slide-enter-active, .slide-leave-active { transition:all 0.2s ease; overflow:hidden; }
.slide-enter-from, .slide-leave-to { opacity:0; max-height:0; }
.slide-enter-to, .slide-leave-from { opacity:1; max-height:200px; }
</style>
