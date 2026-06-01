<template>
  <div class="app-shell home">
    <!-- ── 헤더 ── -->
    <header class="app-header">
      <span class="app-title">📅 스케줄</span>
      <div class="header-right">
        <!-- 테마 전환 -->
        <div class="theme-switcher">
          <button
            v-for="t in themes"
            :key="t.key"
            class="theme-dot-btn"
            :class="{ active: themeStore.current === t.key }"
            :style="{ '--dot-color': t.color }"
            :title="t.label"
            @click="themeStore.setTheme(t.key)"
          />
        </div>
        <!-- 로그아웃 -->
        <button class="logout-btn" @click="handleLogout" title="로그아웃">
          <span>{{ authStore.user?.name }}</span>
          <span class="logout-icon">&#8617;</span>
        </button>
      </div>
    </header>

    <!-- ── 월 이동 ── -->
    <div class="month-nav">
      <button class="nav-arrow" @click="prevMonth">&#8249;</button>
      <span class="month-title">{{ currentYear }}년 {{ currentMonth }}월</span>
      <button class="nav-arrow" @click="nextMonth">&#8250;</button>
    </div>

    <!-- ── 캘린더 그리드 ── -->
    <div class="calendar-wrap">
      <!-- 요일 헤더 -->
      <div class="dow-row">
        <span v-for="d in DOW_LABELS" :key="d.label" class="dow-cell" :class="d.cls">{{ d.label }}</span>
      </div>

      <!-- 날짜 그리드 -->
      <div class="cal-grid">
        <div
          v-for="(cell, idx) in calDays"
          :key="idx"
          class="cal-cell"
          :class="{
            empty: !cell.isCurrentMonth,
            today: cell.isToday,
            selected: cell.date === selectedDate,
            sunday: cell.dayOfWeek === 0,
            saturday: cell.dayOfWeek === 6,
          }"
          @click="handleDayClick(cell)"
        >
          <template v-if="cell.isCurrentMonth">
            <span class="day-num">{{ cell.day }}</span>
            <div class="dot-row">
              <span
                v-for="(cat, ci) in scheduleStore.getDotsByDate(cell.date)"
                :key="ci"
                class="dot"
                :style="{ background: DOT_COLORS[cat] }"
              />
              <span v-if="scheduleStore.getExtraCount(cell.date) > 0" class="extra-dot">
                +{{ scheduleStore.getExtraCount(cell.date) }}
              </span>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- ── 하단 패널: 선택 날짜 일정 목록 ── -->
    <div class="day-panel">
      <div class="day-panel-header">
        <span class="day-panel-title">{{ selectedDateLabel }}</span>
        <button class="add-text-btn" @click="openAddModal()">+ 추가</button>
      </div>

      <div class="schedule-list" v-if="selectedSchedules.length > 0">
        <div
          v-for="s in selectedSchedules"
          :key="s.id"
          class="schedule-card"
          @click="openEditModal(s)"
        >
          <span class="card-dot" :style="{ background: DOT_COLORS[s.category] }"></span>
          <div class="card-body">
            <div class="card-top">
              <span class="card-badge" :style="{ color: DOT_COLORS[s.category], background: DOT_COLORS[s.category] + '20' }">
                {{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType === 'HOME' ? ' · 홈' : ' · 원정') : '' }}
              </span>
            </div>
            <span class="card-title">{{ s.title }}</span>
            <span v-if="s.memo" class="card-memo">{{ s.memo }}</span>
          </div>
          <span class="card-arrow">&#8250;</span>
        </div>
      </div>
      <div class="empty-day" v-else>
        <span>이 날짜에 일정이 없습니다</span>
        <button class="add-link-btn" @click="openAddModal()">일정 추가하기</button>
      </div>
    </div>

    <!-- ── FAB ── -->
    <button class="fab" @click="openAddModal()" aria-label="일정 추가">
      <span class="fab-icon">+</span>
    </button>

    <!-- ── 일정 추가/수정 모달 ── -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="modalOpen" @click.self="closeModal">
        <div class="modal-sheet" @click.stop>
          <!-- Modal header -->
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3>{{ editTarget ? '일정 수정' : '일정 추가' }}</h3>
            <button class="modal-close" @click="closeModal">&#10005;</button>
          </div>

          <!-- Form -->
          <form class="modal-form" @submit.prevent="handleSave">
            <!-- 날짜 -->
            <div class="form-field">
              <label class="form-label">날짜</label>
              <input v-model="formData.date" type="date" class="form-input" required />
            </div>

            <!-- 제목 -->
            <div class="form-field">
              <label class="form-label">제목 <span class="required">*</span></label>
              <input v-model="formData.title" type="text" class="form-input" placeholder="일정 제목을 입력하세요" maxlength="100" />
              <p v-if="formErrors.title" class="form-err">{{ formErrors.title }}</p>
            </div>

            <!-- 카테고리 -->
            <div class="form-field">
              <label class="form-label">카테고리 <span class="required">*</span></label>
              <div class="radio-grid">
                <label
                  v-for="cat in CATEGORIES"
                  :key="cat.value"
                  class="radio-item"
                  :class="{ selected: formData.category === cat.value }"
                  :style="formData.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category] + '15' } : {}"
                >
                  <input type="radio" :value="cat.value" v-model="formData.category" class="radio-input" />
                  <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                  <span class="radio-label">{{ cat.label }}</span>
                </label>
              </div>
              <p v-if="formErrors.category" class="form-err">{{ formErrors.category }}</p>
            </div>

            <!-- 홈/원정 (야구만) -->
            <Transition name="slide">
              <div class="form-field" v-if="formData.category === 'BASEBALL'">
                <label class="form-label">홈 / 원정 <span class="required">*</span></label>
                <div class="radio-row">
                  <label
                    v-for="bt in BASEBALL_TYPES"
                    :key="bt.value"
                    class="radio-item"
                    :class="{ selected: formData.baseballType === bt.value }"
                    :style="formData.baseballType === bt.value ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-light)' } : {}"
                  >
                    <input type="radio" :value="bt.value" v-model="formData.baseballType" class="radio-input" />
                    <span class="radio-label">{{ bt.label }}</span>
                  </label>
                </div>
                <p v-if="formErrors.baseballType" class="form-err">{{ formErrors.baseballType }}</p>
              </div>
            </Transition>

            <!-- 메모 -->
            <div class="form-field">
              <label class="form-label">메모 <span class="optional">(선택)</span></label>
              <textarea v-model="formData.memo" class="form-input form-textarea" placeholder="메모를 입력하세요" rows="3" maxlength="2000" />
            </div>

            <!-- 버튼 -->
            <div class="modal-actions">
              <button v-if="editTarget" type="button" class="btn-delete" @click="handleDelete">삭제</button>
              <div class="action-right">
                <button type="button" class="btn-cancel" @click="closeModal">취소</button>
                <button type="submit" class="btn-save">저장</button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useScheduleStore } from '@/stores/schedule'
import { useCalendar } from '@/composables/useCalendar'
import type { Schedule, Category, ThemeKey } from '@/types'
import { CATEGORY_LABELS } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const scheduleStore = useScheduleStore()

// ── Theme ──────────────────────────────────────────────────────────────────
const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'light',  color: '#1976D2', label: '라이트' },
  { key: 'dark',   color: '#00CBA8', label: '다크' },
  { key: 'orange', color: '#F4511E', label: '오렌지' },
]

// ── Month navigation ───────────────────────────────────────────────────────
const now = new Date()
const currentYear  = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
}

// ── Calendar ───────────────────────────────────────────────────────────────
const { days: calDays } = useCalendar(
  () => currentYear.value,
  () => currentMonth.value,
)

const todayStr = now.toISOString().slice(0, 10)
const selectedDate = ref(todayStr)

const DOW_LABELS = [
  { label: '일', cls: 'sun' },
  { label: '월', cls: '' },
  { label: '화', cls: '' },
  { label: '수', cls: '' },
  { label: '목', cls: '' },
  { label: '금', cls: '' },
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
  if (selectedDate.value === cell.date) {
    // 같은 날짜 재클릭 → 추가 모달
    openAddModal()
  } else {
    selectedDate.value = cell.date
  }
}

// ── Day panel ──────────────────────────────────────────────────────────────
const selectedSchedules = computed(() => scheduleStore.getByDate(selectedDate.value))

const selectedDateLabel = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value + 'T00:00:00')
  const m = d.getMonth() + 1
  const day = d.getDate()
  const dow = ['일', '월', '화', '수', '목', '금', '토'][d.getDay()]
  return `${m}월 ${day}일 (${dow})`
})

// ── Modal ──────────────────────────────────────────────────────────────────
const modalOpen  = ref(false)
const editTarget = ref<Schedule | null>(null)

const formData = reactive({
  title: '',
  category: '' as Category | '',
  baseballType: null as 'HOME' | 'AWAY' | null,
  date: '',
  memo: '',
})
const formErrors = reactive<Record<string, string>>({})

function resetForm() {
  formData.title = ''
  formData.category = ''
  formData.baseballType = null
  formData.date = selectedDate.value || todayStr
  formData.memo = ''
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
}

function openAddModal() {
  editTarget.value = null
  resetForm()
  formData.date = selectedDate.value || todayStr
  modalOpen.value = true
}

function openEditModal(s: Schedule) {
  editTarget.value = s
  formData.title = s.title
  formData.category = s.category
  formData.baseballType = s.baseballType
  formData.date = s.date
  formData.memo = s.memo ?? ''
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  editTarget.value = null
}

// baseballType 자동 초기화
watch(() => formData.category, (cat) => {
  if (cat !== 'BASEBALL') formData.baseballType = null
})

function validateForm(): boolean {
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  if (!formData.title.trim())    formErrors.title = '제목을 입력하세요'
  if (!formData.category)        formErrors.category = '카테고리를 선택하세요'
  if (formData.category === 'BASEBALL' && !formData.baseballType)
    formErrors.baseballType = '홈 또는 원정을 선택하세요'
  return Object.keys(formErrors).length === 0
}

async function handleSave() {
  if (!validateForm()) return
  if (editTarget.value) {
    await scheduleStore.update(editTarget.value.id, { ...formData })
  } else {
    await scheduleStore.add({ ...formData })
    selectedDate.value = formData.date
  }
  closeModal()
}

async function handleDelete() {
  if (!editTarget.value) return
  if (confirm('일정을 삭제하시겠습니까?')) {
    await scheduleStore.remove(editTarget.value.id)
    closeModal()
  }
}

onMounted(() => {
  scheduleStore.fetchAll()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--color-background);
}

/* ── Header ── */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 52px;
  background: var(--color-status-bar);
  color: #fff;
  flex-shrink: 0;
}
.app-title { font-size: 17px; font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 12px; }

.theme-switcher { display: flex; gap: 6px; align-items: center; }
.theme-dot-btn {
  width: 16px; height: 16px;
  border-radius: 50%;
  background: var(--dot-color);
  border: 2px solid transparent;
  transition: border-color 0.2s, transform 0.2s;
  cursor: pointer;
}
.theme-dot-btn.active {
  border-color: #fff;
  transform: scale(1.25);
}

.logout-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: rgba(255,255,255,0.85);
  padding: 4px 8px; border-radius: 12px;
  background: rgba(255,255,255,0.15);
}
.logout-icon { font-size: 14px; }

/* ── Month nav ── */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: var(--color-status-bar);
  color: #fff;
  flex-shrink: 0;
}
.month-title { font-size: 16px; font-weight: 700; }
.nav-arrow {
  font-size: 22px; color: rgba(255,255,255,0.9);
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  transition: background 0.15s;
}
.nav-arrow:active { background: rgba(255,255,255,0.2); }

/* ── Calendar ── */
.calendar-wrap {
  flex-shrink: 0;
  background: var(--color-background);
  padding: 4px 8px 0;
}

.dow-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 2px;
}
.dow-cell {
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 4px 0;
}
.dow-cell.sun { color: var(--color-sunday); }
.dow-cell.sat { color: var(--color-saturday); }

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
}
.cal-cell {
  aspect-ratio: 1 / 1.1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 4px;
  cursor: pointer;
  border-radius: 6px;
  position: relative;
  transition: background 0.15s;
  min-height: 46px;
}
.cal-cell:active { background: var(--color-surface); }
.cal-cell.empty { cursor: default; }

.day-num {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  border-radius: 50%;
  transition: background 0.15s, color 0.15s;
}
.cal-cell.sunday .day-num  { color: var(--color-sunday); }
.cal-cell.saturday .day-num { color: var(--color-saturday); }
.cal-cell.today .day-num {
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: 700;
}
.cal-cell.selected:not(.today) .day-num {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 700;
}

.dot-row {
  display: flex;
  gap: 2px;
  align-items: center;
  margin-top: 2px;
  min-height: 6px;
}
.dot {
  width: 5px; height: 5px;
  border-radius: 50%;
  flex-shrink: 0;
}
.extra-dot {
  font-size: 8px;
  color: var(--color-text-secondary);
}

/* ── Day panel ── */
.day-panel {
  flex: 1;
  overflow-y: auto;
  background: var(--color-surface);
  border-top: 1px solid var(--color-separator);
  display: flex;
  flex-direction: column;
}

.day-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 8px;
  flex-shrink: 0;
}
.day-panel-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}
.add-text-btn {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 600;
  padding: 4px 8px;
}

.schedule-list {
  padding: 0 12px 80px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--color-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: opacity 0.15s;
  border: 1px solid var(--color-separator);
}
.schedule-card:active { opacity: 0.75; }

.card-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-top { display: flex; align-items: center; gap: 6px; }
.card-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}
.card-memo {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.card-arrow {
  font-size: 18px;
  color: var(--color-text-secondary);
}

.empty-day {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.add-link-btn {
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 600;
  text-decoration: underline;
}

/* ── FAB ── */
.fab {
  position: fixed;
  bottom: 24px;
  right: calc(50% - 215px + 20px);
  width: 52px; height: 52px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-on-primary);
  box-shadow: 0 4px 12px rgba(0,0,0,0.25);
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s, box-shadow 0.15s;
  z-index: 100;
}
.fab:active { transform: scale(0.93); box-shadow: 0 2px 6px rgba(0,0,0,0.2); }
.fab-icon { font-size: 26px; line-height: 1; font-weight: 300; }

@media (max-width: 430px) {
  .fab { right: 20px; }
}

/* ── Modal ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--color-overlay);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 500;
  padding: 0;
}
.modal-sheet {
  width: 100%;
  max-width: 430px;
  max-height: 90vh;
  background: var(--color-card);
  border-radius: 20px 20px 0 0;
  overflow-y: auto;
  padding: 0 0 env(safe-area-inset-bottom, 16px);
  box-shadow: var(--shadow-modal);
  animation: slideUp 0.25s ease;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}
.modal-handle {
  width: 40px; height: 4px;
  background: var(--color-separator);
  border-radius: 2px;
  margin: 10px auto 0;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--color-separator);
}
.modal-header h3 { font-size: 17px; font-weight: 700; color: var(--color-text); }
.modal-close { font-size: 18px; color: var(--color-text-secondary); padding: 4px 8px; }

.modal-form {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.required { color: #F44336; margin-left: 2px; }
.optional { font-weight: 400; color: var(--color-text-secondary); }
.form-input {
  padding: 11px 14px;
  border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text);
  font-size: 15px;
  outline: none;
  width: 100%;
  transition: border-color 0.2s;
}
.form-input:focus { border-color: var(--color-primary); }
.form-textarea { resize: none; font-family: inherit; }
.form-err { font-size: 12px; color: #F44336; }

/* Radio grid */
.radio-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}
.radio-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  transition: border-color 0.15s, background 0.15s;
}
.radio-item.selected {
  font-weight: 700;
}
.radio-input { display: none; }
.radio-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.radio-label { font-size: 13px; }

/* Modal actions */
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4px;
  gap: 12px;
}
.action-right { display: flex; gap: 8px; }
.btn-delete {
  padding: 11px 16px; border-radius: 10px;
  background: #FFEBEE; color: #C62828;
  font-size: 14px; font-weight: 600;
}
.btn-cancel {
  padding: 11px 18px; border-radius: 10px;
  background: var(--color-surface); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600;
  border: 1px solid var(--color-separator);
}
.btn-save {
  padding: 11px 24px; border-radius: 10px;
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 14px; font-weight: 700;
}

/* Slide transition */
.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  max-height: 0;
}
.slide-enter-to, .slide-leave-from {
  opacity: 1;
  max-height: 200px;
}
</style>
