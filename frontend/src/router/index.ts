import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login',    name: 'login',    component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/pending',  name: 'pending',  component: () => import('@/views/PendingView.vue') },
    { path: '/',         name: 'home',     component: () => import('@/views/HomeView.vue') },
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

  // 이미 로그인 상태에서 공개 페이지 접근 → 자동 분기
  if (publicRoutes.includes(to.name as string)) {
    if (role === 'ADMIN' && status === 'ACTIVE') return '/admin'
    if (status === 'ACTIVE') return '/'
    return '/pending'
  }

  // 관리자 페이지: ADMIN + ACTIVE만
  if (to.path.startsWith('/admin')) {
    if (role !== 'ADMIN' || status !== 'ACTIVE') return '/'
    return true
  }

  // 일반 사용자 페이지: ACTIVE만
  if (to.name === 'home') {
    if (status !== 'ACTIVE') return '/pending'
    if (role === 'ADMIN') return '/admin'   // 관리자는 / 대신 /admin
  }

  if (to.name === 'pending') {
    if (status === 'ACTIVE') {
      return role === 'ADMIN' ? '/admin' : '/'
    }
  }

  return true
})

export default router
