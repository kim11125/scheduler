<template>
  <div class="shell shell-nav">
    <header class="topbar">
      <span class="topbar-title">내 일정</span>
      <div style="display:flex;align-items:center;gap:4px;margin-left:auto">
        <button class="theme-dot-btn-sm" @click="themeSheetOpen = true" title="테마 변경">
          <span style="display:block;width:14px;height:14px;border-radius:50%;background:var(--color-primary)"></span>
        </button>
        <button class="topbar-icon-btn" @click="handleLogout" title="로그아웃">
          <AppIcon name="logout" size="md" />
        </button>
      </div>
    </header>

    <!-- Category filter chips -->
    <div class="filter-bar">
      <button
        v-for="f in filters" :key="f.value"
        :class="['chip', { active: activeFilter === f.value }]"
        @click="activeFilter = f.value">
        {{ f.label }}
      </button>
    </div>

    <div class="list-body">
      <template v-if="groupedSchedules.length > 0">
        <div v-for="group in groupedSchedules" :key="group.date" class="date-group">
          <div class="date-group-label">{{ formatGroupDate(group.date) }}</div>
          <div class="group-items">
            <div v-for="s in group.items" :key="s.id" class="agenda-item" @click="openEditModal(s)">
              <div class="agenda-color" :style="{ background: DOT_COLORS[s.category] }"></div>
              <div class="agenda-body">
                <span class="agenda-title">{{ s.title }}</span>
                <span class="agenda-meta">{{ CATEGORY_LABELS[s.category] }}{{ s.baseballType ? (s.baseballType === 'HOME' ? ' · 홈' : ' · 원정') : '' }}{{ s.startTime ? ' · ' + s.startTime : '' }}</span>
              </div>
              <span v-if="s.status && s.status !== 'SCHEDULED'" class="agenda-status" :class="'st-' + s.status.toLowerCase()">
                {{ { CONFIRMED: '확정', CHANGED: '변경', CANCELLED: '취소' }[s.status] }}
              </span>
              <AppIcon name="chevron-right" size="sm" class="agenda-arrow" />
            </div>
          </div>
        </div>
      </template>
      <div v-else class="empty-state">
        <AppIcon name="list" size="lg" class="empty-icon" style="color:var(--color-text-3)" />
        <p class="empty-text">일정이 없어요</p>
      </div>
    </div>

    <!-- FAB -->
    <button class="fab" @click="openAddModal()">+</button>

    <!-- Schedule modal -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="modalOpen" @click.self="closeModal">
        <div class="modal-sheet" @click.stop>
          <div class="modal-handle"></div>
          <div class="modal-header">
            <h3 class="modal-title">{{ editTarget ? '일정 수정' : '일정 추가' }}</h3>
            <button class="modal-close" @click="closeModal"><AppIcon name="close" size="sm" /></button>
          </div>
          <form class="modal-form" @submit.prevent="handleSave">
            <div class="form-field" v-if="authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'">
              <label class="form-label">대상 사용자</label>
              <select v-model="formData.targetUserId" class="form-input">
                <option :value="null">내 일정</option>
                <option v-for="u in adminUsers" :key="u.id" :value="u.id">{{ u.name }} (@{{ u.username }})</option>
              </select>
            </div>
            <div class="form-field" v-if="myTeams.length > 0">
              <label class="form-label">팀 <span class="optional">(선택)</span></label>
              <select v-model="formData.teamId" class="form-input">
                <option :value="null">팀 없음</option>
                <option v-for="t in myTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
            </div>
            <div class="form-section-label">일정 날짜</div>
            <div class="form-row">
              <div class="form-field">
                <label class="form-label">시작일</label>
                <input v-model="formData.date" type="date" class="form-input" required />
              </div>
              <div class="form-field">
                <label class="form-label">시작 시간</label>
                <input v-model="formData.startTime" type="time" class="form-input" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label class="form-label">종료일</label>
                <input v-model="formData.endDate" type="date" class="form-input" :min="formData.date" />
              </div>
              <div class="form-field">
                <label class="form-label">종료 시간</label>
                <input v-model="formData.endTime" type="time" class="form-input" />
              </div>
            </div>
            <div class="form-section-label">기본 정보</div>
            <div class="form-field">
              <label class="form-label">제목 <span class="required">*</span></label>
              <input v-model="formData.title" type="text" class="form-input" placeholder="일정 제목을 입력하세요" maxlength="100" />
              <p v-if="formErrors.title" class="form-err">{{ formErrors.title }}</p>
            </div>
            <div class="form-field">
              <label class="form-label">카테고리 <span class="required">*</span></label>
              <div class="radio-grid">
                <label v-for="cat in CATEGORIES" :key="cat.value" class="radio-item"
                  :class="{ selected: formData.category === cat.value }"
                  :style="formData.category === cat.value ? { borderColor: DOT_COLORS[cat.value as Category], background: DOT_COLORS[cat.value as Category] + '18' } : {}">
                  <input type="radio" :value="cat.value" v-model="formData.category" class="radio-input" />
                  <span class="radio-dot" :style="{ background: DOT_COLORS[cat.value as Category] }"></span>
                  <span class="radio-label">{{ cat.label }}</span>
                </label>
              </div>
              <p v-if="formErrors.category" class="form-err">{{ formErrors.category }}</p>
            </div>
            <Transition name="slide">
              <div class="form-field" v-if="formData.category === 'BASEBALL'">
                <label class="form-label">홈 / 원정 <span class="required">*</span></label>
                <div class="radio-row">
                  <label v-for="bt in BASEBALL_TYPES" :key="bt.value" class="radio-item"
                    :class="{ selected: formData.baseballType === bt.value }"
                    :style="formData.baseballType === bt.value ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-soft)' } : {}">
                    <input type="radio" :value="bt.value" v-model="formData.baseballType" class="radio-input" />
                    <span class="radio-label">{{ bt.label }}</span>
                  </label>
                </div>
                <p v-if="formErrors.baseballType" class="form-err">{{ formErrors.baseballType }}</p>
              </div>
            </Transition>
            <div class="form-section-label">추가 정보</div>
            <div class="form-field">
              <label class="form-label">장소</label>
              <input v-model="formData.location" type="text" class="form-input" placeholder="장소를 입력하세요" maxlength="200" />
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
              <label class="form-label">메모</label>
              <textarea v-model="formData.memo" class="form-input form-textarea" placeholder="메모를 입력하세요" rows="3" maxlength="500" />
            </div>
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

    <BottomNavUser />
    <ThemeSheet :open="themeSheetOpen" @close="themeSheetOpen = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedule'
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import type { Schedule, Category, ScheduleStatus, TeamRef } from '@/types'
import { CATEGORY_LABELS } from '@/types'
import BottomNavUser from '@/components/BottomNavUser.vue'
import AppIcon from '@/components/AppIcon.vue'
import ThemeSheet from '@/components/ThemeSheet.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeSheetOpen = ref(false)
const scheduleStore = useScheduleStore()

const DOT_COLORS: Record<Category, string> = {
  BASEBALL: 'var(--color-dot-baseball)', BASKETBALL: 'var(--color-dot-basketball)',
  SOCCER: 'var(--color-dot-soccer)', WOMENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  MENS_VOLLEYBALL: 'var(--color-dot-volleyball)', ETC: 'var(--color-dot-etc)',
}
const CATEGORIES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))
const BASEBALL_TYPES = [{ value: 'HOME', label: '홈' }, { value: 'AWAY', label: '원정' }]

const filters = [
  { value: 'ALL', label: '전체' },
  { value: 'BASEBALL', label: '야구' },
  { value: 'BASKETBALL', label: '농구' },
  { value: 'SOCCER', label: '축구' },
  { value: 'WOMENS_VOLLEYBALL', label: '여배' },
  { value: 'MENS_VOLLEYBALL', label: '남배' },
  { value: 'ETC', label: '기타' },
]
const activeFilter = ref<string>('ALL')

const filteredSchedules = computed(() => {
  const sorted = [...scheduleStore.schedules].sort((a, b) => b.date.localeCompare(a.date))
  if (activeFilter.value === 'ALL') return sorted
  return sorted.filter(s => s.category === activeFilter.value)
})

const groupedSchedules = computed(() => {
  const groups: { date: string; items: typeof filteredSchedules.value }[] = []
  for (const s of filteredSchedules.value) {
    let g = groups.find(g => g.date === s.date)
    if (!g) { g = { date: s.date, items: [] }; groups.push(g) }
    g.items.push(s)
  }
  return groups
})

function formatGroupDate(d: string): string {
  const dt = new Date(d + 'T00:00:00')
  const days = ['일','월','화','수','목','금','토']
  return `${dt.getMonth()+1}월 ${dt.getDate()}일 (${days[dt.getDay()]})`
}

// Modal
const modalOpen = ref(false)
const editTarget = ref<Schedule | null>(null)
const todayStr = new Date().toISOString().slice(0, 10)
const formData = reactive({
  title: '', category: '' as Category | '', baseballType: null as 'HOME' | 'AWAY' | null,
  date: todayStr, startTime: '', endDate: '', endTime: '', location: '', memo: '',
  targetUserId: null as number | null, teamId: null as number | null,
  status: 'SCHEDULED' as ScheduleStatus,
})
const formErrors = reactive<Record<string, string>>({})

function resetForm() {
  formData.title = ''; formData.category = ''; formData.baseballType = null
  formData.date = todayStr; formData.startTime = ''; formData.endDate = ''
  formData.endTime = ''; formData.location = ''; formData.memo = ''
  formData.targetUserId = null; formData.teamId = null; formData.status = 'SCHEDULED'
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function openAddModal() { editTarget.value = null; resetForm(); modalOpen.value = true }
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

watch(() => formData.category, (cat) => { if (cat !== 'BASEBALL') formData.baseballType = null })

function validateForm(): boolean {
  Object.keys(formErrors).forEach(k => delete (formErrors as Record<string,string>)[k])
  if (!formData.title.trim()) formErrors.title = '제목을 입력하세요'
  if (!formData.category) formErrors.category = '카테고리를 선택하세요'
  if (formData.category === 'BASEBALL' && !formData.baseballType) formErrors.baseballType = '홈 또는 원정을 선택하세요'
  return Object.keys(formErrors).length === 0
}

async function handleSave() {
  if (!validateForm()) return
  if (editTarget.value) await scheduleStore.update(editTarget.value.id, { ...formData })
  else await scheduleStore.add({ ...formData })
  closeModal()
}

async function handleDelete() {
  if (!editTarget.value) return
  if (confirm('일정을 삭제하시겠습니까?')) { await scheduleStore.remove(editTarget.value.id); closeModal() }
}

const adminUsers = ref<{id: number; name: string; username: string}[]>([])
const myTeams = ref<TeamRef[]>([])

onMounted(async () => {
  scheduleStore.fetchAll()
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
.filter-bar { display: flex; gap: 8px; padding: 12px 16px; overflow-x: auto; }
.filter-bar::-webkit-scrollbar { display: none; }
.list-body { padding: 0 16px 100px; }
.date-group { margin-bottom: 20px; }
.date-group-label { font-size: 12px; font-weight: 700; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
.group-items { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 14px; overflow: hidden; box-shadow: var(--shadow-soft); }
.agenda-item { display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-bottom: 1px solid var(--color-border); cursor: pointer; transition: background 0.1s; }
.agenda-item:last-child { border-bottom: none; }
.agenda-item:active { background: var(--color-surface-muted); }
.agenda-color { width: 4px; height: 40px; border-radius: 2px; flex-shrink: 0; }
.agenda-body { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.agenda-title { font-size: 14px; font-weight: 600; color: var(--color-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.agenda-meta { font-size: 11px; color: var(--color-text-secondary); }
.agenda-status { font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: 999px; flex-shrink: 0; }
.st-confirmed { background: var(--color-success-soft); color: var(--color-success); }
.st-changed { background: var(--color-warning-soft); color: var(--color-warning); }
.st-cancelled { background: var(--color-danger-soft); color: var(--color-danger); }
.agenda-arrow { color: var(--color-text-secondary); font-size: 16px; flex-shrink: 0; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: var(--color-overlay); display: flex; align-items: flex-end; justify-content: center; z-index: 500; }
.modal-sheet { width: 100%; max-width: 430px; background: var(--color-surface); border-radius: var(--radius-xl) var(--radius-xl) 0 0; max-height: 92vh; overflow-y: auto; box-shadow: var(--shadow-floating); animation: slideUp 0.28s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle { width: 36px; height: 4px; background: var(--color-border); border-radius: 999px; margin: 12px auto 0; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px 12px; border-bottom: 1px solid var(--color-border); }
.modal-title { font-size: 17px; font-weight: 700; color: var(--color-text-primary); }
.modal-close { width: 32px; height: 32px; border-radius: 50%; background: var(--color-surface-muted); color: var(--color-text-secondary); display: flex; align-items: center; justify-content: center; font-size: 16px; cursor: pointer; }
.modal-form { padding: 16px 20px calc(16px + env(safe-area-inset-bottom, 0px)); display: flex; flex-direction: column; gap: 14px; }
.form-section-label { font-size: 12px; font-weight: 700; color: var(--color-text-secondary); letter-spacing: 0.3px; text-transform: uppercase; padding-top: 4px; border-top: 1px solid var(--color-border); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 12px; font-weight: 600; color: var(--color-text-secondary); }
.required { color: var(--color-danger); margin-left: 2px; }
.optional { font-weight: 400; }
.form-input { padding: 11px 14px; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: var(--color-surface-muted); color: var(--color-text-primary); font-size: 14px; outline: none; width: 100%; transition: border-color 0.2s, box-shadow 0.2s; }
.form-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-soft); background: var(--color-surface); }
.form-textarea { resize: none; font-family: inherit; }
.form-err { font-size: 12px; color: var(--color-danger); }
.radio-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.radio-row { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.radio-item { display: flex; align-items: center; gap: 6px; padding: 8px 10px; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: var(--color-surface-muted); cursor: pointer; font-size: 13px; font-weight: 500; color: var(--color-text-primary); transition: border-color 0.15s, background 0.15s; }
.radio-item.selected { font-weight: 700; }
.radio-input { display: none; }
.radio-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.radio-label { font-size: 13px; }
.modal-actions { display: flex; align-items: center; justify-content: space-between; padding-top: 8px; gap: 10px; border-top: 1px solid var(--color-border); }
.action-right { display: flex; gap: 8px; }
.btn-delete { padding: 11px 16px; border-radius: 999px; background: var(--color-danger-soft); color: var(--color-danger); font-size: 13px; font-weight: 700; border: 1.5px solid var(--color-danger-soft); }
.btn-cancel { padding: 11px 18px; border-radius: 999px; background: var(--color-surface-muted); color: var(--color-text-secondary); font-size: 13px; font-weight: 600; border: 1.5px solid var(--color-border); }
.btn-save { padding: 11px 24px; border-radius: 999px; background: var(--color-btn); color: var(--color-btn-text); font-size: 13px; font-weight: 700; box-shadow: var(--shadow-soft); }
.slide-enter-active, .slide-leave-active { transition: all 0.2s ease; overflow: hidden; }
.slide-enter-from, .slide-leave-to { opacity: 0; max-height: 0; }
.slide-enter-to, .slide-leave-from { opacity: 1; max-height: 200px; }
</style>
