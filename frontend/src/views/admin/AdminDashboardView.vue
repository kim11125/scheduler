<template>
  <div class="app-shell admin">
    <!-- 헤더 -->
    <header class="admin-header">
      <span class="admin-title">⚙️ 관리자</span>
      <div class="header-right">
        <div class="theme-switcher">
          <button
            v-for="t in themes" :key="t.key"
            class="theme-dot-btn" :class="{ active: themeStore.current === t.key }"
            :style="{ '--dot-color': t.color }" :title="t.label"
            @click="themeStore.setTheme(t.key)"
          />
        </div>
        <button class="logout-btn" @click="handleLogout">
          <span>{{ authStore.user?.name }}</span>
          <span class="logout-icon">↩</span>
        </button>
      </div>
    </header>

    <div class="admin-body">

      <!-- 요약 카드 -->
      <div class="stat-row">
        <div class="stat-card" :class="{ highlight: usersStore.pendingUsers.length > 0 }">
          <span class="stat-num">{{ usersStore.pendingUsers.length }}</span>
          <span class="stat-label">승인 대기</span>
        </div>
        <div class="stat-card">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">활성 사용자</span>
        </div>
        <div class="stat-card">
          <span class="stat-num">{{ scheduleStore.schedules.length }}</span>
          <span class="stat-label">전체 일정</span>
        </div>
      </div>

      <!-- 승인 대기 섹션 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">
            승인 대기
            <span v-if="usersStore.pendingUsers.length" class="badge-count">
              {{ usersStore.pendingUsers.length }}
            </span>
          </h2>
        </div>

        <div v-if="usersStore.pendingUsers.length === 0" class="empty-msg">
          대기 중인 가입 신청이 없습니다.
        </div>

        <div class="user-list" v-else>
          <div
            v-for="u in usersStore.pendingUsers" :key="u.id"
            class="user-card pending"
          >
            <div class="user-info">
              <div class="user-avatar" :style="{ background: avatarColor(u.name) }">
                {{ u.name[0] }}
              </div>
              <div class="user-meta">
                <span class="user-name">{{ u.name }}</span>
                <span class="user-id">@{{ u.username }}</span>
                <span class="user-date">신청일: {{ formatDate(u.createdAt) }}</span>
              </div>
            </div>
            <div class="user-actions">
              <button class="btn-approve" @click="handleApprove(u.id)">승인</button>
              <button class="btn-reject"  @click="handleReject(u.id)">거절</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 전체 사용자 섹션 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">전체 사용자</h2>
          <div class="filter-tabs">
            <button
              v-for="f in STATUS_FILTERS" :key="f.value"
              class="filter-tab" :class="{ active: statusFilter === f.value }"
              @click="statusFilter = f.value"
            >{{ f.label }}</button>
          </div>
        </div>

        <div class="user-list">
          <div
            v-for="u in filteredUsers" :key="u.id"
            class="user-card"
          >
            <div class="user-info">
              <div class="user-avatar" :style="{ background: avatarColor(u.name) }">
                {{ u.name[0] }}
              </div>
              <div class="user-meta">
                <span class="user-name">{{ u.name }}</span>
                <span class="user-id">@{{ u.username }}</span>
                <span class="status-badge" :class="u.status.toLowerCase()">
                  {{ STATUS_LABELS[u.status] }}
                </span>
              </div>
            </div>
            <div class="user-actions">
              <button v-if="u.status === 'ACTIVE'"    class="btn-sm btn-disable"  @click="usersStore.disable(u.id)">비활성화</button>
              <button v-if="u.status === 'DISABLED'"  class="btn-sm btn-activate" @click="usersStore.activate(u.id)">재활성화</button>
              <button v-if="u.status === 'REJECTED'"  class="btn-sm btn-activate" @click="usersStore.activate(u.id)">승인</button>
              <button v-if="u.status === 'ACTIVE'"
                class="btn-sm btn-schedule"
                @click="goToUserSchedules(u.id, u.name)"
              >일정 보기</button>
            </div>
          </div>

          <div v-if="filteredUsers.length === 0" class="empty-msg">
            해당하는 사용자가 없습니다.
          </div>
        </div>
      </section>

      <!-- 전체 일정 바로가기 -->
      <section class="section">
        <button class="btn-all-schedules" @click="router.push('/admin/schedules')">
          📋 전체 일정 조회하기
          <span class="arrow">›</span>
        </button>
      </section>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useUsersStore } from '@/stores/users'
import { useScheduleStore } from '@/stores/schedule'
import type { ThemeKey, UserStatus } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const usersStore = useUsersStore()
const scheduleStore = useScheduleStore()

const themes: { key: ThemeKey; color: string; label: string }[] = [
  { key: 'light',  color: '#1976D2', label: '라이트' },
  { key: 'dark',   color: '#00CBA8', label: '다크' },
  { key: 'orange', color: '#F4511E', label: '오렌지' },
]

const STATUS_FILTERS: { value: UserStatus | 'ALL'; label: string }[] = [
  { value: 'ALL',      label: '전체' },
  { value: 'ACTIVE',   label: '활성' },
  { value: 'PENDING',  label: '대기' },
  { value: 'REJECTED', label: '거절' },
  { value: 'DISABLED', label: '비활성' },
]

const STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE:   '활성',
  PENDING:  '대기',
  REJECTED: '거절',
  DISABLED: '비활성',
}

const statusFilter = ref<UserStatus | 'ALL'>('ALL')

const activeCount = computed(() =>
  usersStore.allUsers.filter(u => u.status === 'ACTIVE').length
)

const filteredUsers = computed(() => {
  if (statusFilter.value === 'ALL') return usersStore.allUsers
  return usersStore.allUsers.filter(u => u.status === statusFilter.value)
})

function handleApprove(id: number) {
  usersStore.approve(id)
}
function handleReject(id: number) {
  if (confirm('가입을 거절하시겠습니까?')) usersStore.reject(id)
}
function goToUserSchedules(userId: number, name: string) {
  router.push({ path: '/admin/schedules', query: { userId: String(userId), name } })
}

function avatarColor(name: string): string {
  const colors = ['#1976D2','#00897B','#F4511E','#9C27B0','#FF9800','#607D8B','#E91E63']
  return colors[name.charCodeAt(0) % colors.length]
}

function formatDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => {
  usersStore.fetchAll()
  scheduleStore.fetchAll()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin {
  min-height: 100vh;
  background: var(--color-background);
  overflow-y: auto;
}

.admin-header {
  position: sticky; top: 0; z-index: 50;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 52px;
  background: var(--color-status-bar); color: #fff;
}
.admin-title { font-size: 17px; font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 12px; }
.theme-switcher { display: flex; gap: 6px; align-items: center; }
.theme-dot-btn {
  width: 16px; height: 16px; border-radius: 50%;
  background: var(--dot-color);
  border: 2px solid transparent;
  cursor: pointer; transition: border-color 0.2s, transform 0.2s;
}
.theme-dot-btn.active { border-color: #fff; transform: scale(1.25); }
.logout-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: rgba(255,255,255,0.85);
  padding: 4px 8px; border-radius: 12px;
  background: rgba(255,255,255,0.15);
}
.logout-icon { font-size: 14px; }

.admin-body { padding: 16px; display: flex; flex-direction: column; gap: 20px; }

/* 요약 카드 */
.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.stat-card {
  background: var(--color-card);
  border: 1px solid var(--color-separator);
  border-radius: 12px;
  padding: 14px 10px;
  text-align: center;
  box-shadow: var(--shadow-card);
  display: flex; flex-direction: column; gap: 4px;
}
.stat-card.highlight {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
.stat-num { font-size: 26px; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 12px; color: var(--color-text-secondary); }

/* 섹션 */
.section { display: flex; flex-direction: column; gap: 10px; }
.section-header {
  display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px;
}
.section-title {
  font-size: 15px; font-weight: 700; color: var(--color-text);
  display: flex; align-items: center; gap: 6px;
}
.badge-count {
  background: var(--color-primary); color: #fff;
  font-size: 11px; font-weight: 700;
  padding: 2px 7px; border-radius: 10px;
}

/* 필터 탭 */
.filter-tabs { display: flex; gap: 4px; flex-wrap: wrap; }
.filter-tab {
  font-size: 12px; padding: 4px 10px; border-radius: 12px;
  background: var(--color-surface); color: var(--color-text-secondary);
  border: 1px solid var(--color-separator);
  cursor: pointer; transition: all 0.15s;
}
.filter-tab.active {
  background: var(--color-primary); color: var(--color-on-primary);
  border-color: var(--color-primary);
}

/* 유저 카드 */
.user-list { display: flex; flex-direction: column; gap: 8px; }
.user-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px;
  background: var(--color-card);
  border-radius: var(--radius-card);
  border: 1px solid var(--color-separator);
  box-shadow: var(--shadow-card);
  gap: 10px;
}
.user-card.pending { border-left: 3px solid var(--color-primary); }

.user-info { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.user-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 700; flex-shrink: 0;
}
.user-meta {
  display: flex; flex-direction: column; gap: 2px; min-width: 0;
}
.user-name { font-size: 14px; font-weight: 600; color: var(--color-text); }
.user-id   { font-size: 12px; color: var(--color-text-secondary); }
.user-date { font-size: 11px; color: var(--color-text-secondary); }

/* 상태 배지 */
.status-badge {
  display: inline-block;
  font-size: 11px; font-weight: 600;
  padding: 2px 8px; border-radius: 8px;
  width: fit-content;
}
.status-badge.active   { background: #E8F5E9; color: #2E7D32; }
.status-badge.pending  { background: #FFF3E0; color: #E65100; }
.status-badge.rejected { background: #FCE4EC; color: #880E4F; }
.status-badge.disabled { background: var(--color-surface); color: var(--color-text-secondary); }

/* 버튼 */
.user-actions { display: flex; gap: 6px; flex-shrink: 0; flex-wrap: wrap; justify-content: flex-end; }
.btn-approve {
  padding: 7px 14px; border-radius: 8px;
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 13px; font-weight: 700;
}
.btn-reject {
  padding: 7px 14px; border-radius: 8px;
  background: #FFEBEE; color: #C62828;
  font-size: 13px; font-weight: 600;
}
.btn-sm {
  padding: 5px 10px; border-radius: 7px;
  font-size: 12px; font-weight: 600; cursor: pointer;
}
.btn-disable  { background: #FFF3E0; color: #E65100; }
.btn-activate { background: #E8F5E9; color: #2E7D32; }
.btn-schedule { background: var(--color-primary-light); color: var(--color-primary); }

.empty-msg {
  text-align: center; padding: 24px;
  font-size: 14px; color: var(--color-text-secondary);
}

/* 전체 일정 버튼 */
.btn-all-schedules {
  width: 100%; padding: 16px;
  background: var(--color-card);
  border: 1.5px solid var(--color-primary);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: space-between;
  font-size: 15px; font-weight: 700; color: var(--color-primary);
  cursor: pointer;
}
.btn-all-schedules .arrow { font-size: 20px; }
</style>
