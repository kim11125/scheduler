import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login',    name: 'login',    component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/pending',  name: 'pending',  component: () => import('@/views/PendingView.vue') },
    { path: '/',            name: 'home',         component: () => import('@/views/HomeView.vue') },
    { path: '/my-schedules', name: 'my-schedules', component: () => import('@/views/HomeView.vue') },
    { path: '/my-profile',   name: 'my-profile',   component: () => import('@/views/MyProfileView.vue') },
    { path: '/calendar', name: 'calendar', component: () => import('@/views/CalendarView.vue') },
    { path: '/schedules', name: 'my-schedules-page', component: () => import('@/views/MySchedulesView.vue') },
    { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminDashboardView.vue'),
    },
    {
      path: '/admin/schedules',
      name: 'admin-schedules',
      component: () => import('@/views/admin/AdminSchedulesView.vue'),
    },
    {
      path: '/admin/companies',
      name: 'admin-companies',
      component: () => import('@/views/admin/AdminCompaniesView.vue'),
    },
    {
      path: '/admin/teams',
      name: 'admin-teams',
      component: () => import('@/views/admin/AdminTeamsView.vue'),
    },
    {
      path: '/admin/users/:id',
      name: 'admin-user-detail',
      component: () => import('@/views/admin/AdminUserDetailView.vue'),
    },
    { path: '/admin/users', name: 'admin-users', component: () => import('@/views/admin/AdminUsersView.vue') },
    { path: '/admin/management', name: 'admin-management', component: () => import('@/views/admin/AdminManagementView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const publicRoutes = ['login', 'register']

  // 비로그인
  if (!auth.user) {
    if (!publicRoutes.includes(to.name as string)) return '/login'
    return true
  }

  const { role, status } = auth.user

  const isAdmin = role === 'ADMIN' || role === 'MANAGER'

  // 이미 로그인 상태에서 공개 페이지 접근 → 자동 분기
  if (publicRoutes.includes(to.name as string)) {
    if (isAdmin && status === 'ACTIVE') return '/admin'
    if (status === 'ACTIVE') return '/'
    return '/pending'
  }

  // 관리자 페이지: ADMIN or MANAGER + ACTIVE만
  if (to.path.startsWith('/admin')) {
    if (!isAdmin || status !== 'ACTIVE') return '/'
    return true
  }

  // 내 일정, 내 프로필: 관리자도 접근 가능
  if (to.name === 'my-schedules' || to.name === 'my-profile') {
    if (status !== 'ACTIVE') return '/pending'
    return true
  }

  // 새 사용자 페이지들: ACTIVE 사용자 접근 가능
  if (to.name === 'calendar' || to.name === 'my-schedules-page' || to.name === 'profile') {
    if (status !== 'ACTIVE') return '/pending'
    return true
  }

  // 관리자 전용 신규 라우트
  if (to.name === 'admin-users' || to.name === 'admin-management') {
    if (!isAdmin || status !== 'ACTIVE') return '/'
    return true
  }

  // 일반 사용자 페이지: ACTIVE만
  if (to.name === 'home') {
    if (status !== 'ACTIVE') return '/pending'
    if (isAdmin) return '/admin'
  }

  if (to.name === 'pending') {
    if (status === 'ACTIVE') {
      return isAdmin ? '/admin' : '/'
    }
  }

  return true
})

export default router
