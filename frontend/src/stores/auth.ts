import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

const MOCK_USERS: (User & { password: string })[] = [
  { id: 1, username: 'admin',  password: 'admin1234', name: '관리자',  role: 'ADMIN', status: 'ACTIVE' },
  { id: 2, username: 'user1',  password: 'pass1234',  name: '홍길동',  role: 'USER',  status: 'ACTIVE' },
  { id: 3, username: 'user2',  password: 'pass1234',  name: '김철수',  role: 'USER',  status: 'PENDING' },
  { id: 4, username: 'user3',  password: 'pass1234',  name: '이영희',  role: 'USER',  status: 'REJECTED' },
]

export const useAuthStore = defineStore('auth', () => {
  const saved = localStorage.getItem('auth_user')
  const user = ref<User | null>(saved ? JSON.parse(saved) : null)
  const error = ref<string | null>(null)

  function login(username: string, password: string): boolean {
    const found = MOCK_USERS.find(u => u.username === username && u.password === password)
    if (!found) {
      error.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
      return false
    }
    const { password: _, ...userInfo } = found
    user.value = userInfo
    localStorage.setItem('auth_user', JSON.stringify(userInfo))
    error.value = null
    return true
  }

  function logout() {
    user.value = null
    localStorage.removeItem('auth_user')
  }

  return { user, error, login, logout }
})
