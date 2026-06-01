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

  // 내 일정: 관리자도 접근 가능
  if (to.name === 'my-schedules') {
    if (status !== 'ACTIVE') return '/pending'
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
