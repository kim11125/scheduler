<template>
  <div class="app-shell home">
    <!-- ── 헤더 ── -->
    <header class="app-header">
      <div class="header-left">
        <button v-if="isAdminUser" class="back-to-admin" @click="router.push('/admin')">‹ 관리</button>
        <span class="app-title">📅 스케줄</span>
      </div>
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
        <!-- 내 정보 / 로그아웃 -->
        <button class="header-profile-btn" @click="profileOpen = true">
          <img v-if="profileImageUrl" :src="profileImageUrl" class="header-profile-img" alt="프로필" />
          <span v-else class="header-profile-initial">{{ authStore.user?.name?.[0] }}</span>
        </button>
        <button class="header-btn header-btn-logout" @click="handleLogout">로그아웃</button>
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
            <span v-if="s.startTime" class="card-memo">{{ s.startTime }}{{ s.endTime ? ' ~ ' + s.endTime : '' }}</span>
            <span v-if="s.endDate && s.endDate !== s.date" class="card-memo">~ {{ s.endDate }}</span>
            <span v-if="s.location" class="card-memo">📍 {{ s.location }}</span>
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

    <!-- ── 프로필 모달 ── -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="profileOpen" @click.self="profileOpen = false">
        <div class="modal-sheet profile-modal-sheet">
          <div class="modal-handle"></div>
          <div class="profile-modal-inner">
            <div class="modal-header">
              <h3>내 정보</h3>
              <button class="modal-close" @click="profileOpen = false">✕</button>
            </div>
            <div class="user-profile-row">
              <div class="profile-avatar-wrap" @click="triggerImageUpload">
                <img v-if="profileImageUrl" :src="profileImageUrl" class="profile-avatar-img" alt="프로필" />
                <div v-else class="profile-avatar-lg" :style="{ background: 'var(--color-primary)' }">
                  {{ authStore.user?.name?.[0] }}
                </div>
                <div class="profile-avatar-overlay">📷</div>
              </div>
              <div class="profile-meta">
                <span class="profile-name">{{ authStore.user?.name }}</span>
                <span class="profile-id">@{{ authStore.user?.username }}</span>
                <span class="profile-img-hint">사진을 클릭해 변경</span>
              </div>
            </div>
            <input ref="imageInputRef" type="file" accept="image/jpeg,image/png,image/webp" class="hidden-input" @change="handleImageUpload" />

            <div class="pw-section">
              <h4 class="pw-title">비밀번호 변경</h4>
              <input v-model="currentPw" type="password" class="pw-input" placeholder="현재 비밀번호" />
              <input v-model="newPw" type="password" class="pw-input" placeholder="새 비밀번호" />
              <input v-model="newPwConfirm" type="password" class="pw-input" placeholder="새 비밀번호 확인" />
              <p v-if="pwError" class="pw-error">{{ pwError }}</p>
              <p v-if="pwSuccess" class="pw-success">비밀번호가 변경됐습니다.</p>
              <button class="btn-pw-save" @click="handlePwChange">변경</button>
            </div>

          </div>
        </div>
      </div>
    </Teleport>

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
            <!-- 관리자: 대상 사용자 선택 -->
            <div class="form-field" v-if="authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'">
              <label class="form-label">대상 사용자</label>
              <select v-model="formData.targetUserId" class="form-input">
                <option :value="null">내 일정</option>
                <option v-for="u in adminUsers" :key="u.id" :value="u.id">{{ u.name }} (@{{ u.username }})</option>
              </select>
            </div>

            <!-- 팀 선택 -->
            <div class="form-field" v-if="myTeams.length > 0">
              <label class="form-label">팀 <span class="optional">(선택)</span></label>
              <select v-model="formData.teamId" class="form-input">
                <option :value="null">팀 없음</option>
                <option v-for="t in myTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
            </div>

            <!-- 날짜 -->
            <div class="form-field">
              <label class="form-label">시작일</label>
              <input v-model="formData.date" type="date" class="form-input" required />
            </div>
            <div class="form-field">
              <label class="form-label">시작 시간 <span class="optional">(선택)</span></label>
              <input v-model="formData.startTime" type="time" class="form-input" />
            </div>
            <div class="form-field">
              <label class="form-label">종료일 <span class="label-optional">(선택)</span></label>
              <input v-model="formData.endDate" type="date" class="form-input" :min="formData.date" />
            </div>
            <div class="form-field">
              <label class="form-label">종료 시간 <span class="optional">(선택)</span></label>
              <input v-model="formData.endTime" type="time" class="form-input" />
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

            <!-- 장소 -->
            <div class="form-field">
              <label class="form-label">장소 <span class="optional">(선택)</span></label>
              <input v-model="formData.location" type="text" class="form-input" placeholder="장소를 입력하세요" maxlength="200" />
            </div>

            <!-- 상태 -->
            <div class="form-field">
              <label class="form-label">상태</label>
              <select v-model="formData.status" class="form-input">
                <option value="SCHEDULED">예정</option>
                <option value="CONFIRMED">확정</option>
                <option value="CHANGED">변경</option>
                <option value="CANCELLED">취소</option>
              </select>
            </div>

            <!-- 메모 -->
            <div class="form-field">
              <label class="form-label">메모 <span class="optional">(선택)</span></label>
              <textarea v-model="formData.memo" class="form-input form-textarea" placeholder="메모를 입력하세요" rows="3" maxlength="500" />
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
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ThemeKey, ScheduleStatus, TeamRef } from '@/types'
import { CATEGORY_LABELS } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const scheduleStore = useScheduleStore()

const isAdminUser = computed(() =>
  authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
)

// ── Theme ──────────────────────────────────────────────────────────────────
const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',   color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',      color: '#E8836A', label: '피치' },
  { key: 'mint',       color: '#4DB896', label: '민트' },
  { key: 'dark',       color: '#A695F0', label: '다크' },
  { key: 'rose-milk',  color: '#D4789A', label: '로즈' },
]

// ── Month navigation ───────────────────────────────────────────────────────
const now = new Date()
const currentYear  = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
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
  const hasSchedules = scheduleStore.getByDate(cell.date).length > 0
  if (!hasSchedules) {
    // 일정 없으면 바로 추가 모달
    selectedDate.value = cell.date
    openAddModal()
  } else if (selectedDate.value === cell.date) {
    // 같은 날짜 재클릭 → 추가 모달
    openAddModal()
  } else {
    // 다른 날짜 → 목록만 보여주기
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
  startTime: '',
  endDate: '',
  endTime: '',
  location: '',
  memo: '',
  targetUserId: null as number | null,
  teamId: null as number | null,
  status: 'SCHEDULED' as ScheduleStatus,
})
const formErrors = reactive<Record<string, string>>({})

function resetForm() {
  formData.title = ''
  formData.category = ''
  formData.baseballType = null
  formData.date = selectedDate.value || todayStr
  formData.startTime = ''
  formData.endDate = ''
  formData.endTime = ''
  formData.location = ''
  formData.memo = ''
  formData.targetUserId = null
  formData.teamId = null
  formData.status = 'SCHEDULED'
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
  formData.startTime = s.startTime ?? ''
  formData.endDate = s.endDate || ''
  formData.endTime = s.endTime ?? ''
  formData.location = s.location ?? ''
  formData.memo = s.memo ?? ''
  formData.targetUserId = null
  formData.teamId = s.teamId ?? null
  formData.status = s.status ?? 'SCHEDULED'
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

// 프로필 모달
const profileOpen = ref(false)
const currentPw = ref('')
const newPw = ref('')
const newPwConfirm = ref('')
const pwError = ref('')
const pwSuccess = ref(false)

// 프로필 이미지
const profileImageUrl = ref<string | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)

async function loadProfileImage() {
  try {
    const res = await userApi.getMe()
    profileImageUrl.value = res.data.profileImageUrl || null
  } catch {}
}

function triggerImageUpload() {
  imageInputRef.value?.click()
}

async function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await userApi.uploadMyProfileImage(file)
    profileImageUrl.value = res.data.profileImageUrl
  } catch (err: any) {
    alert(err.response?.data?.message || '이미지 업로드에 실패했습니다.')
  }
  if (imageInputRef.value) imageInputRef.value.value = ''
}

async function handlePwChange() {
  pwError.value = ''
  pwSuccess.value = false
  if (!currentPw.value) { pwError.value = '현재 비밀번호를 입력하세요.'; return }
  if (newPw.value.length < 6) { pwError.value = '6자 이상 입력하세요.'; return }
  if (newPw.value !== newPwConfirm.value) { pwError.value = '비밀번호가 일치하지 않습니다.'; return }
  try {
    await userApi.changePassword(currentPw.value, newPw.value)
    currentPw.value = ''; newPw.value = ''; newPwConfirm.value = ''
    pwSuccess.value = true
  } catch (e: any) {
    pwError.value = e.response?.data?.message || '변경에 실패했습니다.'
  }
}

// 관리자용 유저 목록
const adminUsers = ref<{id: number; name: string; username: string}[]>([])
// 내 팀 목록
const myTeams = ref<TeamRef[]>([])

onMounted(async () => {
  scheduleStore.fetchByMonth(currentYear.value, currentMonth.value)
  loadProfileImage()
  const role = authStore.user?.role
  if (role === 'ADMIN' || role === 'MANAGER') {
    const res = await adminApi.getUsers()
    adminUsers.value = res.data.filter((u: any) => u.status === 'ACTIVE' && u.role === 'USER')
  }
  try {
    const profileRes = await userApi.getMyProfile()
    myTeams.value = profileRes.data.teams ?? []
  } catch {
    myTeams.value = []
  }
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
  background: var(--color-bg, var(--color-background));
}

/* ── Header ── */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
  background: var(--color-header-bg, var(--color-status-bar));
  color: var(--color-header-text, #fff);
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.app-title { font-size: 17px; font-weight: 700; }
.back-to-admin {
  font-size: 12px; color: rgba(255,255,255,0.85);
  padding: 4px 10px; border-radius: var(--radius-pill);
  background: rgba(255,255,255,0.15);
  cursor: pointer;
}
.header-right { display: flex; align-items: center; gap: 10px; }

.theme-switcher { display: flex; gap: 5px; align-items: center; }
.theme-dot-btn {
  width: 14px; height: 14px;
  border-radius: 50%;
  background: var(--dot-color);
  border: 2px solid rgba(255,255,255,0.3);
  transition: border-color 0.2s, transform 0.2s;
  cursor: pointer;
}
.theme-dot-btn.active {
  border-color: #fff;
  transform: scale(1.3);
}

.header-profile-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.45);
  cursor: pointer; padding: 0; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: opacity 0.2s;
}
.header-profile-btn:active { opacity: 0.8; }
.header-profile-img {
  width: 100%; height: 100%; object-fit: cover; border-radius: 50%;
}
.header-profile-initial {
  font-size: 13px; font-weight: 700; color: #fff;
}
.header-btn {
  font-size: 12px; font-weight: 600; color: rgba(255,255,255,0.9);
  padding: 5px 12px; border-radius: var(--radius-pill);
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  cursor: pointer;
  transition: opacity 0.2s;
}
.header-btn:active { opacity: 0.75; }
.header-btn-logout {
  background: rgba(255,255,255,0.08);
  border-color: rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.75);
}
.hidden-input { display: none; }
.profile-avatar-wrap {
  position: relative; width: 54px; height: 54px; cursor: pointer; flex-shrink: 0;
}
.profile-avatar-img {
  width: 54px; height: 54px; border-radius: 50%; object-fit: cover;
}
.profile-avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; opacity: 0; transition: opacity 0.2s;
}
.profile-avatar-wrap:hover .profile-avatar-overlay { opacity: 1; }
.profile-img-hint { font-size: 11px; color: var(--color-text-secondary); }

/* 프로필 모달 */
.modal-overlay {
  position: fixed; inset: 0; background: var(--color-overlay); z-index: 200;
  display: flex; align-items: flex-end; justify-content: center;
}
.modal-sheet {
  width: 100%; max-width: 430px; margin: 0 auto;
  background: var(--color-surface); border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  padding: 12px 28px 48px; max-height: 85vh; overflow-y: auto;
  box-shadow: var(--shadow-modal);
  animation: slideUp 0.25s ease;
}
.profile-modal-sheet { padding: 12px 0 48px; }
.profile-modal-inner { padding: 0 32px; }
.modal-handle {
  width: 32px; height: 4px; border-radius: var(--radius-pill);
  background: var(--color-border); margin: 0 auto 18px;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.modal-header h3 { font-size: 16px; font-weight: 700; }
.modal-close { font-size: 18px; color: var(--color-text-secondary); cursor: pointer; }

.user-profile-row {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 0 20px; border-bottom: 1px solid var(--color-border);
  margin-bottom: 24px;
}
.profile-avatar-lg {
  width: 54px; height: 54px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.profile-meta { display: flex; flex-direction: column; gap: 4px; }
.profile-name { font-size: 17px; font-weight: 700; }
.profile-id { font-size: 13px; color: var(--color-text-secondary); }

.pw-section { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
.pw-title { font-size: 14px; font-weight: 700; margin-bottom: 2px; }
.pw-input {
  padding: 12px 14px; border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text); font-size: 14px; outline: none;
  width: 100%;
}
.pw-input:focus { border-color: var(--color-input-focus); }
.pw-error { font-size: 12px; color: var(--color-danger); margin-top: -4px; }
.pw-success { font-size: 12px; color: var(--color-success); margin-top: -4px; }
.btn-pw-save {
  padding: 13px; border-radius: var(--radius-md); margin-top: 4px;
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 14px; font-weight: 700; cursor: pointer;
}

/* ── Month nav ── */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  background: var(--color-header-bg, var(--color-status-bar));
  color: var(--color-header-text, #fff);
  flex-shrink: 0;
}
.month-title { font-size: 15px; font-weight: 700; }
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
  background: var(--color-bg, var(--color-background));
  padding: 6px 8px 0;
}

.dow-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 2px;
}
.dow-cell {
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 4px 0;
}
.dow-cell.sun { color: var(--color-sunday); }
.dow-cell.sat { color: var(--color-saturday); }

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}
.cal-cell {
  aspect-ratio: 1 / 1.1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 4px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  position: relative;
  transition: background 0.15s;
  min-height: 46px;
}
.cal-cell:active { background: var(--color-surface-muted, var(--color-surface)); }
.cal-cell.empty { cursor: default; }

.day-num {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px;
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
  background: var(--color-primary-soft, var(--color-primary-light));
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
  background: var(--color-surface-muted, var(--color-surface));
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.day-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 8px;
  flex-shrink: 0;
}
.day-panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-secondary);
}
.add-text-btn {
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 600;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: var(--color-primary-soft, var(--color-primary-light));
}

.schedule-list {
  padding: 4px 12px 80px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: opacity 0.15s, transform 0.1s;
  border: 1px solid var(--color-border);
}
.schedule-card:active { opacity: 0.8; transform: scale(0.99); }

.card-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 5px;
}
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-top { display: flex; align-items: center; gap: 6px; }
.card-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
}
.card-title {
  font-size: 14px; font-weight: 600; color: var(--color-text);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.card-memo {
  font-size: 12px; color: var(--color-text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.card-arrow {
  font-size: 18px;
  color: var(--color-border);
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
  padding: 8px 20px;
  border-radius: var(--radius-pill);
  background: var(--color-primary-soft, var(--color-primary-light));
}

/* ── FAB ── */
.fab {
  position: fixed;
  bottom: calc(24px + env(safe-area-inset-bottom, 0px));
  right: calc(50% - 215px + 20px);
  width: 52px; height: 52px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-btn-text);
  box-shadow: var(--shadow-soft);
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s, box-shadow 0.15s;
  z-index: 100;
}
.fab:active { transform: scale(0.93); }
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
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  overflow-y: auto;
  padding: 0 0 env(safe-area-inset-bottom, 24px);
  box-shadow: var(--shadow-modal);
  animation: slideUp 0.25s ease;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}
.modal-handle {
  width: 32px; height: 4px;
  background: var(--color-border);
  border-radius: var(--radius-pill);
  margin: 12px auto 0;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--color-border);
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
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.required { color: var(--color-danger); margin-left: 2px; }
.optional { font-weight: 400; color: var(--color-text-secondary); }
.form-input {
  padding: 11px 14px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text);
  font-size: 15px;
  outline: none;
  width: 100%;
  transition: border-color 0.2s;
}
.form-input:focus { border-color: var(--color-input-focus); }
.form-textarea { resize: none; font-family: inherit; }
.form-err { font-size: 12px; color: var(--color-danger); }

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
  border-radius: var(--radius-md);
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
  padding: 11px 16px; border-radius: var(--radius-md);
  background: var(--color-danger-soft); color: var(--color-danger);
  font-size: 14px; font-weight: 600;
}
.btn-cancel {
  padding: 11px 18px; border-radius: var(--radius-md);
  background: var(--color-surface-muted, var(--color-surface)); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600;
  border: 1px solid var(--color-border);
}
.btn-save {
  padding: 11px 24px; border-radius: var(--radius-md);
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
