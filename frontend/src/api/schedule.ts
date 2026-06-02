import client from './client'
import type { Category, BaseballType, ScheduleStatus } from '@/types'

export interface SchedulePayload {
  title: string
  category: Category
  baseballType?: BaseballType | null
  date: string
  startTime?: string | null
  endDate?: string | null
  endTime?: string | null
  location?: string | null
  memo?: string
  targetUserId?: number | null
  teamId?: number | null
  status?: ScheduleStatus
}

export const scheduleApi = {
  getAll: () => client.get('/api/schedules'),
  getByMonth: (year: number, month: number) =>
    client.get(`/api/schedules?year=${year}&month=${month}`),
  getMe: () => client.get('/api/schedules/me'),
  getByUser: (userId: number) => client.get(`/api/schedules/users/${userId}`),
  getByTeam: (teamId: number) => client.get(`/api/schedules/teams/${teamId}`),
  getByCompanyMembers: (companyId: number) => client.get(`/api/schedules/companies/${companyId}/members`),
  getByCompanyTeams: (companyId: number) => client.get(`/api/schedules/companies/${companyId}/teams`),
  create: (data: SchedulePayload) => client.post('/api/schedules', data),
  update: (id: number, data: SchedulePayload) => client.put(`/api/schedules/${id}`, data),
  delete: (id: number) => client.delete(`/api/schedules/${id}`),
}
