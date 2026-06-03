<template>
  <div class="app-shell admin-sched">
    <!-- 헤더 -->
    <header class="admin-header">
      <button class="back-btn" @click="router.push('/admin')">‹</button>
      <span class="admin-title">전체 일정 조회</span>
      <div class="view-toggle">
        <button :class="['toggle-btn', { active: viewMode === 'list' }]" @click="viewMode = 'list'">≡</button>
        <button :class="['toggle-btn', { active: viewMode === 'calendar' }]" @click="viewMode = 'calendar'">▦</button>
      </div>
    </header>

    <div class="admin-body">

      <!-- 필터 -->
      <div class="filter-section">
        <!-- 사용자 필터 -->
        <div class="filter-row">
          <span class="filter-label">사용자</span>
          <div class="filter-chips">
            <button
              class="chip" :class="{ active: userFilter === null }"
              @click="userFilter = null"
            >전체</button>
            <button
              v-for="u in activeUserList" :key="u.id"
              class="chip" :class="{ active: userFilter === u.id }"
              @click="userFilter = u.id"
            >{{ u.name }}</button>
          </div>
        </div>

        <!-- 카테고리 필터 -->
        <div class="filter-row">
          <span class="filter-label">종목</span>
          <div class="filter-chips">
            <button
              class="chip" :class="{ active: catFilter === null }"
              @click="catFilter = null"
            >전체</button>
            <button
              v-for="cat in CATEGORIES" :key="cat.value"
              class="chip" :class="{ active: catFilter === cat.value }"
              :style="catFilter === cat.value ? { background: DOT_COLORS[cat.value], color: '#fff', borderColor: DOT_COLORS[cat.value] } : {}"
              @click="catFilter = cat.value as Category"
            >{{ cat.label }}</button>
          </div>
        </div>

        <!-- 상태 필터 -->
        <div class="filter-row">
          <span class="filter-label">상태</span>
          <div class="filter-chips">
            <button class="chip" :class="{ active: statusFilter === null }" @click="statusFilter = null">전체</button>
            <button v-for="s in STATUS_OPTIONS" :key="s.value"
              class="chip" :class="{ active: statusFilter === s.value }"
              @click="statusFilter = s.value">{{ s.label }}</button>
          </div>
        </div>

        <!-- 월 이동 -->
        <div class="filter-row month-row">
          <button class="nav-sm" @click="prevMonth">‹</button>
          <span class="month-label">{{ viewYear }}년 {{ viewMonth }}월</span>
          <button class="nav-sm" @click="nextMonth">›</button>
        </div>
      </div>

      <!-- 결과 수 -->
      <div class="result-count">{{ filteredSchedules.length }}건</div>

      <!-- 목록 뷰 -->
      <template v-if="viewMode === 'list'">
        <div class="sched-list" v-if="filteredSchedules.length > 0">
          <div
            v-for="s in filteredSchedules" :key="s.id"
            class="sched-card"
            @click="openEdit(s)"
          >
            <div class="sched-dot-wrap">
              <span class="sched-dot" :style="{ background: DOT_COLORS[s.category] }"></span>
              <span class="sched-date-col">{{ formatDay(s.date) }}</span>
            </div>
            <div class="sched-body">
              <div class="sched-top">
                <span class="sched-badge"
                  :style="{ color: DOT_COLORS[s.category], background: DOT_COLORS[s.category]+'20' }">
                  {{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType === 'HOME' ? ' · 홈' : ' · 원정') : '' }}
                </span>
                <span class="sched-user-tag">{{ getUserName(s.userId) }}</span>
              </div>
              <span class="sched-title">{{ s.title }}</span>
              <span v-if="s.memo" class="sched-memo">{{ s.memo }}</span>
            </div>
            <span class="sched-arrow">›</span>
          </div>
        </div>
        <div class="empty-msg" v-else>해당하는 일정이 없습니다.</div>
      </template>

      <!-- 캘린더 뷰 -->
      <template v-else>
        <div class="cal-grid-wrap">
          <div class="cal-dow-row">
            <span v-for="d in ['일','월','화','수','목','금','토']" :key="d" class="cal-dow">{{ d }}</span>
          </div>
          <div class="cal-grid">
            <div
              v-for="(cell, idx) in calCells" :key="idx"
              class="cal-cell-admin"
              :class="{ empty: !cell.isCurrentMonth, today: cell.isToday }"
            >
              <span class="cal-day-num" v-if="cell.isCurrentMonth">{{ cell.day }}</span>
              <div class="cal-events" v-if="cell.isCurrentMonth">
                <div
                  v-for="s in getSchedulesByDate(cell.date)" :key="s.id"
                  class="cal-event"
                  :style="{ background: DOT_COLORS[s.category] }"
                  @click="openEdit(s)"
                >
                  <span class="cal-event-user">{{ getUserName(s.userId) }}</span>
                  <span class="cal-event-title">{{ s.title }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

    </div>

    <!-- 수정/삭제 모달 (관리자용) -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="editTarget" @click.self="editTarget = null">
        <div class="modal-sheet">
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3>일정 수정 (관리자)</h3>
            <button class="modal-close" @click="editTarget = null">✕</button>
          </div>

          <div class="modal-info">
            <span class="info-label">작성자</span>
            <span class="info-val">{{ getUserName(editTarget.userId) }}</span>
          </div>

          <form class="modal-form" @submit.prevent="handleSave">
            <div class="form-field">
              <label class="form-label">시작일</label>
              <input v-model="formData.date" type="date" class="form-input" />
            </div>
            <div class="form-field">
              <label class="form-label">시작 시간 <span style="font-size:11px;color:var(--color-text-secondary);font-weight:400">(선택)</span></label>
              <input v-model="formData.startTime" type="time" class="form-input" />
            </div>
            <div class="form-field">
              <label class="form-label">종료일 <span style="font-size:11px;color:var(--color-text-secondary);font-weight:400">(선택)</span></label>
              <input v-model="formData.endDate" type="date" class="form-input" :min="formData.date" />
            </div>
            <div class="form-field">
              <label class="form-label">종료 시간 <span style="font-size:11px;color:var(--color-text-secondary);font-weight:400">(선택)</span></label>
              <input v-model="formData.endTime" type="time" class="form-input" />
            </div>
            <div class="form-field">
              <label class="form-label">장소 <span style="font-size:11px;color:var(--color-text-secondary);font-weight:400">(선택)</span></label>
              <input v-model="formData.location" type="text" class="form-input" maxlength="200" placeholder="장소 입력" />
            </div>
            <div class="form-field">
              <label class="form-label">상태</label>
              <select v-model="formData.status" class="form-input">
                <option value="SCHEDULED">예정</option>
                <option value="CONFIRMED">확정</option>
                <option value="CHANGED">변경</option>
                <option value="CANCELLED">취소</option>
              </select>
            </div>
            <div class="form-field">
              <label class="form-label">제목</label>
              <input v-model="formData.title" type="text" class="form-input" maxlength="100" />
            </div>
            <div class="form-field">
              <label class="form-label">카테고리</label>
              <div class="radio-grid">
                <label
                  v-for="cat in CATEGORIES" :key="cat.value"
                  class="radio-item"
                  :class="{ selected: formData.category === cat.value }"
                  :style="formData.category === cat.value
                    ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category]+'15' }
                    : {}"
                >
                  <input type="radio" :value="cat.value" v-model="formData.category" class="radio-input" />
                  <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                  <span class="radio-label">{{ cat.label }}</span>
                </label>
              </div>
            </div>
            <Transition name="slide">
              <div class="form-field" v-if="formData.category === 'BASEBALL'">
                <label class="form-label">홈 / 원정</label>
                <div class="radio-row">
                  <label
                    v-for="bt in [{ value: 'HOME', label: '홈' }, { value: 'AWAY', label: '원정' }]"
                    :key="bt.value"
                    class="radio-item"
                    :class="{ selected: formData.baseballType === bt.value }"
                    :style="formData.baseballType === bt.value
                      ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-light)' }
                      : {}"
                  >
                    <input type="radio" :value="bt.value" v-model="formData.baseballType" class="radio-input" />
                    <span class="radio-label">{{ bt.label }}</span>
                  </label>
                </div>
              </div>
            </Transition>
            <div class="form-field">
              <label class="form-label">메모</label>
              <textarea v-model="formData.memo" class="form-input form-textarea" rows="3" maxlength="500" />
            </div>
            <div class="modal-actions">
              <button type="button" class="btn-delete" @click="handleDelete">삭제</button>
              <div class="action-right">
                <button type="button" class="btn-cancel" @click="editTarget = null">취소</button>
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
import { useRoute, useRouter } from 'vue-router'
import { useScheduleStore } from '@/stores/schedule'
import { useUsersStore } from '@/stores/users'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ScheduleFormData, ScheduleStatus } from '@/types'
import { CATEGORY_LABELS, SCHEDULE_STATUS_LABELS } from '@/types'

const router = useRouter()
const route  = useRoute()
const scheduleStore = useScheduleStore()
const usersStore    = useUsersStore()

const allSchedules = ref<Schedule[]>([])
const viewMode = ref<'list' | 'calendar'>('list')

onMounted(async () => {
  await usersStore.fetchAll()
  const res = await adminApi.getAllSchedules()
  allSchedules.value = res.data
})

// 캘린더 셀 생성
const calCells = computed(() => {
  const todayStr = new Date().toISOString().slice(0, 10)
  const firstDay = new Date(viewYear.value, viewMonth.value - 1, 1)
  const lastDay  = new Date(viewYear.value, viewMonth.value, 0)
  const cells = []
  // 앞 빈칸
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

// ── 색상 맵 ──────────────────────────────────────────────────────────────
const DOT_COLORS: Record<Category, string> = {
  BASEBALL:          '#F44336',
  BASKETBALL:        '#FF9800',
  SOCCER:            '#4CAF50',
  WOMENS_VOLLEYBALL: '#9C27B0',
  MENS_VOLLEYBALL:   '#9C27B0',
  ETC:               '#607D8B',
}
const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))

// ── 월 이동 ───────────────────────────────────────────────────────────────
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

// ── 필터 ─────────────────────────────────────────────────────────────────
// URL query로 사용자 필터 초기값 세팅
const initialUserId = route.query.userId ? Number(route.query.userId) : null
const userFilter   = ref<number | null>(initialUserId)
const catFilter    = ref<Category | null>(null)
const statusFilter = ref<ScheduleStatus | null>(null)

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

// ── 수정 모달 ─────────────────────────────────────────────────────────────
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

// ── 날짜 포맷 ─────────────────────────────────────────────────────────────
function formatDay(dateStr: string): string {
  const d = new Date(dateStr + 'T00:00:00')
  const dow = ['일','월','화','수','목','금','토'][d.getDay()]
  return `${d.getDate()}일\n(${dow})`
}
</script>

<style scoped>
.admin-sched {
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
.back-btn { font-size: 24px; color: var(--color-header-text, #fff); width: 32px; }
.admin-title { font-size: 16px; font-weight: 700; }

.admin-body { padding: 14px; display: flex; flex-direction: column; gap: 14px; }

/* 필터 */
.filter-section {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
  padding: 14px;
  display: flex; flex-direction: column; gap: 10px;
}
.filter-row { display: flex; align-items: flex-start; gap: 10px; }
.filter-label {
  font-size: 11px; font-weight: 600; color: var(--color-text-secondary);
  min-width: 28px; padding-top: 5px;
}
.filter-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  font-size: 12px; padding: 4px 12px; border-radius: var(--radius-pill);
  background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary);
  border: 1px solid var(--color-border); cursor: pointer;
  transition: all 0.15s;
}
.chip.active {
  background: var(--color-primary); color: var(--color-on-primary);
  border-color: var(--color-primary);
}
.month-row { justify-content: center; align-items: center; gap: 16px; }
.month-label { font-size: 14px; font-weight: 700; color: var(--color-text); }
.nav-sm {
  font-size: 20px; color: var(--color-primary);
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; background: var(--color-primary-soft, var(--color-primary-light));
}

.result-count {
  font-size: 13px; color: var(--color-text-secondary);
  padding: 0 2px;
}

/* 일정 목록 */
.sched-list { display: flex; flex-direction: column; gap: 8px; }
.sched-card {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: opacity 0.15s;
}
.sched-card:active { opacity: 0.75; }

.sched-dot-wrap {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  min-width: 28px;
}
.sched-dot {
  width: 8px; height: 8px; border-radius: 50%;
}
.sched-date-col {
  font-size: 10px; color: var(--color-text-secondary);
  text-align: center; white-space: pre-line; line-height: 1.4;
}

.sched-body { flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.sched-top { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.sched-badge {
  font-size: 10px; font-weight: 600;
  padding: 2px 8px; border-radius: var(--radius-pill);
}
.sched-user-tag {
  font-size: 11px; color: var(--color-text-secondary);
  background: var(--color-surface-muted, var(--color-surface));
  padding: 2px 8px; border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
}
.sched-title {
  font-size: 14px; font-weight: 600; color: var(--color-text);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.sched-memo  {
  font-size: 12px; color: var(--color-text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.sched-arrow { font-size: 18px; color: var(--color-border); }

.empty-msg {
  text-align: center; padding: 32px;
  font-size: 14px; color: var(--color-text-secondary);
}

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
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle {
  width: 32px; height: 4px;
  background: var(--color-border); border-radius: var(--radius-pill);
  margin: 12px auto 0;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px 10px;
  border-bottom: 1px solid var(--color-border);
}
.modal-header h3 { font-size: 16px; font-weight: 700; color: var(--color-text); }
.modal-close { font-size: 18px; color: var(--color-text-secondary); padding: 4px 8px; }

.modal-info {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 20px 0;
}
.info-label { font-size: 12px; color: var(--color-text-secondary); }
.info-val   { font-size: 13px; font-weight: 600; color: var(--color-primary); }

.modal-form { padding: 14px 20px; display: flex; flex-direction: column; gap: 16px; }
.form-field  { display: flex; flex-direction: column; gap: 6px; }
.form-label  { font-size: 12px; font-weight: 600; color: var(--color-text-secondary); }
.form-input {
  padding: 10px 13px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 14px; outline: none; width: 100%;
}
.form-input:focus { border-color: var(--color-input-focus); }
.form-textarea { resize: none; font-family: inherit; }

.radio-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px;
}
.radio-row { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.radio-item {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 10px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  cursor: pointer; font-size: 13px; font-weight: 500; color: var(--color-text);
  transition: all 0.15s;
}
.radio-item.selected { font-weight: 700; }
.radio-input { display: none; }
.radio-dot   { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.radio-label { font-size: 13px; }

.modal-actions {
  display: flex; align-items: center; justify-content: space-between; padding-top: 4px;
}
.action-right { display: flex; gap: 8px; }
.btn-delete {
  padding: 10px 16px; border-radius: var(--radius-md);
  background: var(--color-danger-soft); color: var(--color-danger);
  font-size: 14px; font-weight: 600;
}
.btn-cancel {
  padding: 10px 16px; border-radius: var(--radius-md);
  background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600;
  border: 1px solid var(--color-border);
}
.btn-save {
  padding: 10px 22px; border-radius: var(--radius-md);
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 14px; font-weight: 700;
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease; overflow: hidden;
}
.slide-enter-from, .slide-leave-to { opacity: 0; max-height: 0; }
.slide-enter-to,   .slide-leave-from { opacity: 1; max-height: 200px; }

/* 뷰 토글 */
.view-toggle { display: flex; gap: 4px; }
.toggle-btn {
  width: 32px; height: 32px; border-radius: var(--radius-sm);
  font-size: 16px; color: rgba(255,255,255,0.7);
  background: rgba(255,255,255,0.12);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
}
.toggle-btn.active { background: rgba(255,255,255,0.3); color: #fff; }

/* 캘린더 뷰 */
.cal-grid-wrap { display: flex; flex-direction: column; gap: 0; }
.cal-dow-row {
  display: grid; grid-template-columns: repeat(7, 1fr);
  text-align: center; padding: 6px 0;
}
.cal-dow { font-size: 11px; color: var(--color-text-secondary); font-weight: 600; }
.cal-grid {
  display: grid; grid-template-columns: repeat(7, 1fr);
  border-left: 1px solid var(--color-border);
  border-top: 1px solid var(--color-border);
}
.cal-cell-admin {
  border-right: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  min-height: 72px; padding: 4px;
  background: var(--color-surface);
}
.cal-cell-admin.empty { background: var(--color-bg, var(--color-background)); }
.cal-cell-admin.today .cal-day-num {
  background: var(--color-primary); color: var(--color-on-primary);
  border-radius: 50%; width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
}
.cal-day-num { font-size: 11px; font-weight: 600; color: var(--color-text); margin-bottom: 2px; }
.cal-events { display: flex; flex-direction: column; gap: 1px; }
.cal-event {
  border-radius: 3px; padding: 1px 3px;
  font-size: 9px; color: #fff;
  display: flex; flex-direction: column;
  cursor: pointer; line-height: 1.3;
  overflow: hidden;
}
.cal-event-user { font-weight: 700; font-size: 8px; opacity: 0.9; }
.cal-event-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
