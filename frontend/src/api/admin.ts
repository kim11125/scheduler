import client from './client'
import type { Category, ScheduleStatus } from '@/types'

export const adminApi = {
  // 사용자 관리
  getUsers: () => client.get('/api/admin/users'),
  getPendingUsers: () => client.get('/api/admin/users/pending'),
  approve: (id: number) => client.put(`/api/admin/users/${id}/approve`),
  reject: (id: number) => client.put(`/api/admin/users/${id}/reject`),
  disable: (id: number) => client.put(`/api/admin/users/${id}/disable`),
  activate: (id: number) => client.put(`/api/admin/users/${id}/activate`),
  changeUserPassword: (id: number, newPassword: string) =>
    client.put(`/api/admin/users/${id}/password`, { newPassword }),
  changeUserRole: (id: number, role: string) =>
    client.put(`/api/admin/users/${id}/role`, { role }),

  // 사용자 프로필 관리
  getUserProfile: (userId: number) => client.get(`/api/admin/users/${userId}/profile`),
  updateUserProfile: (userId: number, data: { name: string }) =>
    client.patch(`/api/admin/users/${userId}/profile`, data),
  changeUserLoginId: (userId: number, data: { newLoginId: string }) =>
    client.patch(`/api/admin/users/${userId}/login-id`, data),
  uploadUserProfileImage: (userId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return client.post(`/api/admin/users/${userId}/profile-image`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  updateUserProfileImage: (userId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return client.put(`/api/admin/users/${userId}/profile-image`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  deleteUserProfileImage: (userId: number) =>
    client.delete(`/api/admin/users/${userId}/profile-image`),

  // 사용자 소속 회사 관리
  getUserCompanies: (userId: number) => client.get(`/api/admin/users/${userId}/companies`),
  addUserCompany: (userId: number, companyId: number) =>
    client.post(`/api/admin/users/${userId}/companies/${companyId}`),
  setPrimaryUserCompany: (userId: number, companyId: number) =>
    client.patch(`/api/admin/users/${userId}/companies/${companyId}/primary`),
  removeUserCompany: (userId: number, companyId: number) =>
    client.delete(`/api/admin/users/${userId}/companies/${companyId}`),

  // 사용자 소속 팀 관리
  getUserTeams: (userId: number) => client.get(`/api/admin/users/${userId}/teams`),
  addUserTeam: (userId: number, teamId: number) =>
    client.post(`/api/admin/users/${userId}/teams/${teamId}`),
  setPrimaryUserTeam: (userId: number, teamId: number) =>
    client.patch(`/api/admin/users/${userId}/teams/${teamId}/primary`),
  removeUserTeam: (userId: number, teamId: number) =>
    client.delete(`/api/admin/users/${userId}/teams/${teamId}`),

  // 일정 관리
  getAllSchedules: () => client.get('/api/admin/schedules'),
  getUserSchedules: (userId: number) => client.get(`/api/admin/schedules/user/${userId}`),
  deleteSchedule: (id: number) => client.delete(`/api/admin/schedules/${id}`),
  createSchedule: (data: {
    targetUserId?: number | null
    teamId?: number | null
    title: string
    category: Category
    startDate: string
    startTime?: string | null
    endDate?: string | null
    endTime?: string | null
    location?: string | null
    memo?: string | null
    status?: ScheduleStatus
  }) => client.post('/api/admin/schedules', data),

  // 로그
  getLogs: (page = 0, size = 50) =>
    client.get(`/api/admin/logs?page=${page}&size=${size}`),
  getUserLogs: (userId: number, page = 0, size = 50) =>
    client.get(`/api/admin/logs/user/${userId}?page=${page}&size=${size}`),

  // 회사 관리
  getCompanies: () => client.get('/api/companies'),
  getCompany: (id: number) => client.get(`/api/companies/${id}`),
  createCompany: (data: { name: string; description?: string; logoUrl?: string }) =>
    client.post('/api/admin/companies', data),
  updateCompany: (id: number, data: { name: string; description?: string; logoUrl?: string }) =>
    client.put(`/api/admin/companies/${id}`, data),
  toggleCompanyActive: (id: number) =>
    client.patch(`/api/admin/companies/${id}/active`),

  // 회사-팀 연결
  addTeamToCompany: (companyId: number, teamId: number) =>
    client.post(`/api/admin/companies/${companyId}/teams/${teamId}`),
  removeTeamFromCompany: (companyId: number, teamId: number) =>
    client.delete(`/api/admin/companies/${companyId}/teams/${teamId}`),

  // 팀 관리
  getTeams: () => client.get('/api/teams'),
  getTeam: (id: number) => client.get(`/api/teams/${id}`),
  createTeam: (data: { name: string; category: Category; description?: string; logoUrl?: string }) =>
    client.post('/api/admin/teams', data),
  updateTeam: (id: number, data: { name: string; category: Category; description?: string; logoUrl?: string }) =>
    client.put(`/api/admin/teams/${id}`, data),
  toggleTeamActive: (id: number) =>
    client.patch(`/api/admin/teams/${id}/active`),
}
