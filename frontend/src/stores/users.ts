import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { adminApi } from '@/api/admin'

export interface ManagedUser extends User {
  createdAt: string
}

export const useUsersStore = defineStore('users', () => {
  const users = ref<ManagedUser[]>([])

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

  async function fetchAll() {
    const res = await adminApi.getUsers()
    users.value = res.data
  }

  async function approve(userId: number) {
    await adminApi.approve(userId)
    const u = users.value.find(u => u.id === userId)
    if (u) u.status = 'ACTIVE'
  }

  async function reject(userId: number) {
    await adminApi.reject(userId)
    const u = users.value.find(u => u.id === userId)
    if (u) u.status = 'REJECTED'
  }

  async function disable(userId: number) {
    await adminApi.disable(userId)
    const u = users.value.find(u => u.id === userId)
    if (u) u.status = 'DISABLED'
  }

  async function activate(userId: number) {
    await adminApi.activate(userId)
    const u = users.value.find(u => u.id === userId)
    if (u) u.status = 'ACTIVE'
  }

  return { users, pendingUsers, allUsers, fetchAll, approve, reject, disable, activate }
})
