<template>
  <div class="shell shell-nav">
    <!-- Top bar -->
    <header class="topbar">
      <button class="profile-pill" @click="profileOpen = true">
        <img v-if="profileImageUrl" :src="profileImageUrl" class="profile-pill-img" alt="" />
        <span v-else class="profile-pill-init">{{ authStore.user?.name?.[0] }}</span>
        <span class="profile-pill-name">{{ authStore.user?.name }}</span>
      </button>
      <div style="display:flex;align-items:center;gap:8px;margin-left:auto">
        <div class="theme-row">
          <button v-for="t in themes" :key="t.key"
            :class="['theme-dot-btn', { 'is-active': themeStore.current === t.key }]"
            :style="{ '--dot-c': t.color }"
            @click="themeStore.setTheme(t.key)" />
        </div>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃" style="font-size:15px;color:var(--color-text-2)">↩</button>
      </div>
    </header>

    <!-- Admin shortcut -->
    <div v-if="isAdminUser" class="admin-banner" @click="router.push('/admin')">
      <span>⚙️ 관리자 화면</span>
      <span>›</span>
    </div>

    <div class="home-scroll">
      <!-- Greeting -->
      <div class="home-greeting">
        <span class="home-date">{{ todayLabel }}</span>
        <h1 class="home-hi">안녕하세요 👋</h1>
      </div>

      <!-- Today section -->
      <div class="section" style="margin-top:8px">
        <div class="section-header">
          <span class="section-title">오늘 일정</span>
          <span class="today-count-badge" v-if="todaySchedules.length > 0">{{ todaySchedules.length }}개</span>
        </div>
        <div v-if="todaySchedules.length === 0" class="today-empty">
          <span>📭</span>
          <span>오늘은 일정이 없어요</span>
          <button class="btn btn-primary btn-sm" style="margin-top:10px" @click="openAddModal()">+ 일정 추가</button>
        </div>
        <div v-else class="today-agenda">
          <div v-for="s in todaySchedules" :key="s.id" class="today-row" @click="openEditModal(s)">
            <div class="today-dot" :style="{background: DOT_COLORS[s.category]}"></div>
            <div class="today-content">
              <span class="today-title">{{ s.title }}</span>
              <span class="today-cat">{{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType==='HOME' ? ' · 홈' : ' · 원정') : '' }}</span>
            </div>
            <span class="today-arrow">›</span>
          </div>
        </div>
      </div>

      <!-- Upcoming -->
      <div class="section" v-if="upcomingSchedules.length > 0">
        <div class="section-header">
          <span class="section-title">다가오는 일정</span>
          <button class="see-all" @click="router.push('/calendar')">캘린더 보기</button>
        </div>
        <div class="upcoming-list">
          <div v-for="s in upcomingSchedules" :key="s.id" class="upcoming-row" @click="openEditModal(s)">
            <div class="upcoming-date">
              <span class="upcoming-m">{{ s.date.slice(5,7) }}월</span>
              <span class="upcoming-d">{{ s.date.slice(8,10) }}</span>
            </div>
            <div class="upcoming-line">
              <div class="upcoming-dot" :style="{background: DOT_COLORS[s.category]}"></div>
            </div>
            <div class="upcoming-body">
              <span class="upcoming-title">{{ s.title }}</span>
              <span class="upcoming-cat">{{ CATEGORY_LABELS[s.category] }}</span>
            </div>
          </div>
        </div>
      </div>

      <div style="height:16px"></div>
    </div>

    <!-- FAB -->
    <button class="fab" @click="openAddModal()">+</button>

    <!-- Profile modal -->
    <Teleport to="body">
      <div class="overlay" v-if="profileOpen" @click.self="profileOpen=false">
        <div class="sheet">
          <div class="sheet-handle"></div>
          <div class="sheet-header">
            <span class="sheet-title">내 정보</span>
            <button class="sheet-close" @click="profileOpen=false">✕</button>
          </div>
          <div class="sheet-body">
            <div class="profile-hero">
              <div class="profile-img-wrap" @click="triggerImageUpload">
                <img v-if="profileImageUrl" :src="profileImageUrl" class="profile-img-circle" alt="프로필" />
                <div v-else class="avatar avatar-xl" :style="{background:'var(--color-primary)'}">{{ authStore.user?.name?.[0] }}</div>
                <span class="profile-img-edit">📷</span>
              </div>
              <input ref="imageInputRef" type="file" accept="image/jpeg,image/png,image/webp" style="display:none" @change="handleImageUpload" />
              <h3 style="font-size:18px;font-weight:700;margin-top:10px">{{ authStore.user?.name }}</h3>
              <p style="font-size:13px;color:var(--color-text-2)">@{{ authStore.user?.username }}</p>
            </div>
            <div class="divider"></div>
            <div class="field" style="margin-bottom:10px"><span class="field-label">비밀번호 변경</span></div>
            <div class="field"><input v-model="currentPw" type="password" class="field-input" placeholder="현재 비밀번호" /></div>
            <div class="field" style="margin-top:8px"><input v-model="newPw" type="password" class="field-input" placeholder="새 비밀번호" /></div>
            <div class="field" style="margin-top:8px"><input v-model="newPwConfirm" type="password" class="field-input" placeholder="새 비밀번호 확인" /></div>
            <p v-if="pwError" class="field-err" style="margin-top:4px">{{ pwError }}</p>
            <p v-if="pwSuccess" class="field-ok" style="margin-top:4px">변경됐습니다.</p>
            <button class="btn btn-primary btn-full" style="margin-top:14px" @click="handlePwChange">변경</button>
            <div class="divider"></div>
            <button class="btn btn-danger btn-full" @click="handleLogout">로그아웃</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Schedule add/edit modal -->
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
                  <label class="field-label">시작 시간 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
                  <input v-model="formData.startTime" type="time" class="field-input" />
                </div>
              </div>
              <div class="form-row-2">
                <div class="field">
                  <label class="field-label">종료일 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
                  <input v-model="formData.endDate" type="date" class="field-input" :min="formData.date" />
                </div>
                <div class="field">
                  <label class="field-label">종료 시간 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
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
                <label class="field-label">장소 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
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
                <label class="field-label">메모 <span style="font-weight:400;color:var(--color-text-3)">(선택)</span></label>
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
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ThemeKey, ScheduleStatus, TeamRef } from '@/types'
import { CATEGORY_LABELS } from '@/types'
import BottomNavUser from '@/components/BottomNavUser.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const scheduleStore = useScheduleStore()

const isAdminUser = computed(() =>
  authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
)

// ── Theme ──
const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'lavender',  color: '#8B7FD4', label: '라벤더' },
  { key: 'peach',     color: '#E8836A', label: '피치' },
  { key: 'mint',      color: '#4DB896', label: '민트' },
  { key: 'dark',      color: '#A695F0', label: '다크' },
  { key: 'rose-milk', color: '#D4789A', label: '로즈' },
]

// ── Dashboard computed ──
const todayStr0 = new Date().toISOString().slice(0, 10)
const todayLabel = (() => {
  const d = new Date()
  const days = ['일','월','화','수','목','금','토']
  return `${d.getFullYear()}년 ${d.getMonth()+1}월 ${d.getDate()}일 (${days[d.getDay()]})`
})()

const todaySchedules = computed(() => scheduleStore.getByDate(todayStr0))
const upcomingSchedules = computed(() =>
  scheduleStore.schedules.filter(s => s.date > todayStr0).sort((a, b) => a.date.localeCompare(b.date)).slice(0, 5)
)

// ── Month navigation (for modal date default) ──
const now = new Date()
const currentYear  = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

const todayStr = now.toISOString().slice(0, 10)
const selectedDate = ref(todayStr)

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

// ── Modal ──
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
  formData.date = todayStr0
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

const HOME_AWAY_CATEGORIES = ['BASEBALL', 'WOMENS_VOLLEYBALL', 'MENS_VOLLEYBALL']

watch(() => formData.category, (cat) => {
  if (!HOME_AWAY_CATEGORIES.includes(cat)) formData.baseballType = null
})

function validateForm(): boolean {
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  if (!formData.title.trim())    formErrors.title = '제목을 입력하세요'
  if (!formData.category)        formErrors.category = '카테고리를 선택하세요'
  if (HOME_AWAY_CATEGORIES.includes(formData.category) && !formData.baseballType)
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

// ── Profile modal ──
const profileOpen = ref(false)
const currentPw = ref('')
const newPw = ref('')
const newPwConfirm = ref('')
const pwError = ref('')
const pwSuccess = ref(false)

// ── Profile image ──
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

// ── Admin users / my teams ──
const adminUsers = ref<{id: number; name: string; username: string}[]>([])
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
.profile-pill { display:flex; align-items:center; gap:8px; padding:4px 12px 4px 4px; border-radius:var(--radius-pill); background:var(--color-surface-2); border:1px solid var(--color-border); cursor:pointer; }
.profile-pill-img { width:28px; height:28px; border-radius:50%; object-fit:cover; }
.profile-pill-init { width:28px; height:28px; border-radius:50%; background:var(--color-primary); color:#fff; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:700; }
.profile-pill-name { font-size:13px; font-weight:600; color:var(--color-text-1); max-width:80px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.admin-banner { background:var(--color-primary-light); color:var(--color-primary); font-size:13px; font-weight:600; padding:10px 16px; display:flex; justify-content:space-between; cursor:pointer; }
.home-scroll { flex:1; overflow-y:auto; }
.home-greeting { padding:20px 16px 4px; }
.home-date { font-size:12px; color:var(--color-text-3); display:block; margin-bottom:4px; }
.home-hi { font-size:22px; font-weight:800; color:var(--color-text-1); }
.section-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; }
.today-count-badge { font-size:11px; font-weight:700; background:var(--color-primary); color:#fff; padding:2px 8px; border-radius:var(--radius-pill); }
.see-all { font-size:12px; font-weight:600; color:var(--color-primary); background:none; border:none; cursor:pointer; }
.today-empty { text-align:center; padding:24px; background:var(--color-surface); border-radius:var(--radius-lg); border:1px solid var(--color-border); color:var(--color-text-2); font-size:14px; display:flex; flex-direction:column; align-items:center; gap:4px; }
.today-agenda { background:var(--color-surface); border-radius:var(--radius-lg); border:1px solid var(--color-border); overflow:hidden; }
.today-row { display:flex; align-items:center; gap:10px; padding:13px 14px; border-bottom:1px solid var(--color-border); cursor:pointer; transition:background var(--transition); }
.today-row:last-child { border-bottom:none; }
.today-row:active { background:var(--color-surface-2); }
.today-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.today-content { flex:1; min-width:0; }
.today-title { font-size:14px; font-weight:600; color:var(--color-text-1); display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.today-cat { font-size:11px; color:var(--color-text-2); }
.today-arrow { color:var(--color-text-3); font-size:16px; }
.upcoming-list { background:var(--color-surface); border-radius:var(--radius-lg); border:1px solid var(--color-border); overflow:hidden; }
.upcoming-row { display:flex; align-items:center; gap:10px; padding:12px 14px; border-bottom:1px solid var(--color-border); cursor:pointer; transition:background var(--transition); }
.upcoming-row:last-child { border-bottom:none; }
.upcoming-row:active { background:var(--color-surface-2); }
.upcoming-date { width:32px; text-align:center; flex-shrink:0; }
.upcoming-m { font-size:10px; color:var(--color-text-3); display:block; }
.upcoming-d { font-size:17px; font-weight:700; color:var(--color-text-1); line-height:1; }
.upcoming-line { width:12px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.upcoming-dot { width:8px; height:8px; border-radius:50%; }
.upcoming-body { flex:1; min-width:0; }
.upcoming-title { font-size:14px; font-weight:600; color:var(--color-text-1); display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.upcoming-cat { font-size:11px; color:var(--color-text-2); }
.profile-hero { text-align:center; padding:12px 0 6px; }
.profile-img-wrap { position:relative; display:inline-block; cursor:pointer; }
.profile-img-circle { width:72px; height:72px; border-radius:50%; object-fit:cover; }
.profile-img-edit { position:absolute; bottom:0; right:0; background:var(--color-primary); color:#fff; border-radius:50%; width:22px; height:22px; display:flex; align-items:center; justify-content:center; font-size:11px; }
/* Modal form internals */
.form-section-sep { font-size:11px; font-weight:700; color:var(--color-text-3); letter-spacing:0.5px; text-transform:uppercase; padding:14px 0 6px; border-top:1px solid var(--color-border); margin-top:4px; }
.form-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.radio-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; }
.radio-row-2 { display:grid; grid-template-columns:1fr 1fr; gap:8px; }
.radio-item { display:flex; align-items:center; gap:6px; padding:8px 10px; border-radius:var(--radius-md); border:1.5px solid var(--color-border); background:var(--color-surface-2); cursor:pointer; font-size:13px; font-weight:500; color:var(--color-text-1); transition:border-color 0.15s,background 0.15s; }
.radio-item.selected { font-weight:700; }
.radio-input { display:none; }
.radio-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.radio-label { font-size:13px; }
/* Slide transition */
.slide-enter-active, .slide-leave-active { transition:all 0.2s ease; overflow:hidden; }
.slide-enter-from, .slide-leave-to { opacity:0; max-height:0; }
.slide-enter-to, .slide-leave-from { opacity:1; max-height:200px; }
</style>
