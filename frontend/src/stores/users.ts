import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserStatus } from '@/types'

export interface ManagedUser extends User {
  createdAt: string
}

const INITIAL_USERS: ManagedUser[] = [
  { id: 1, username: 'admin',  name: '관리자',  role: 'ADMIN', status: 'ACTIVE',   createdAt: '2026-04-01T09:00:00' },
  { id: 2, username: 'user1',  name: '홍길동',  role: 'USER',  status: 'ACTIVE',   createdAt: '2026-04-10T14:22:00' },
  { id: 3, username: 'user2',  name: '김철수',  role: 'USER',  status: 'PENDING',  createdAt: '2026-05-28T10:11:00' },
  { id: 4, username: 'user3',  name: '이영희',  role: 'USER',  status: 'REJECTED', createdAt: '2026-05-20T08:30:00' },
  { id: 5, username: 'user4',  name: '박민준',  role: 'USER',  status: 'PENDING',  createdAt: '2026-05-29T07:45:00' },
  { id: 6, username: 'user5',  name: '최서연',  role: 'USER',  status: 'ACTIVE',   createdAt: '2026-05-15T16:50:00' },
  { id: 7, username: 'user6',  name: '정도윤',  role: 'USER',  status: 'DISABLED', createdAt: '2026-04-25T11:00:00' },
  { id: 8, username: 'user7',  name: '강지우',  role: 'USER',  status: 'PENDING',  createdAt: '2026-05-29T19:03:00' },
]

export const useUsersStore = defineStore('users', () => {
  const users = ref<ManagedUser[]>([...INITIAL_USERS])

  const pendingUsers = computed(() =>
    users.value
      .filter(u => u.status === 'PENDING')
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  )

  const allUsers = computed(() =>
    users.value
      .filter(u => u.role !== 'ADMIN')
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  )

  function setStatus(userId: number, status: UserStatus) {
    const u = users.value.find(u => u.id === userId)
    if (u) u.status = status
  }

  function approve(userId: number)  { setStatus(userId, 'ACTIVE') }
  function reject(userId: number)   { setStatus(userId, 'REJECTED') }
  function disable(userId: number)  { setStatus(userId, 'DISABLED') }
  function activate(userId: number) { setStatus(userId, 'ACTIVE') }

  return { users, pendingUsers, allUsers, approve, reject, disable, activate }
})
