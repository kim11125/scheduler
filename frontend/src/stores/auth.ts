import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const saved = localStorage.getItem('auth_user')
  const user = ref<User | null>(saved ? JSON.parse(saved) : null)
  const error = ref<string | null>(null)

  async function login(username: string, password: string): Promise<boolean> {
    error.value = null
    try {
      const res = await authApi.login(username, password)
      const data = res.data
      localStorage.setItem('token', data.token)
      user.value = {
        id: data.userId,
        username,
        name: data.name,
        role: data.role as 'ADMIN' | 'USER',
        status: data.status as any,
      }
      localStorage.setItem('auth_user', JSON.stringify(user.value))
      return true
    } catch (e: any) {
      error.value = e.response?.data?.message || '로그인에 실패했습니다.'
      return false
    }
  }

  async function register(username: string, password: string, name: string): Promise<boolean> {
    error.value = null
    try {
      await authApi.register(username, password, name)
      return true
    } catch (e: any) {
      error.value = e.response?.data?.message || '회원가입에 실패했습니다.'
      return false
    }
  }

  function logout() {
    // 로그아웃 API는 백그라운드로 실행 (기다리지 않음)
    if (user.value) authApi.logout(user.value.username).catch(() => {})
    user.value = null
    error.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('auth_user')
  }

  return { user, error, login, register, logout }
})
