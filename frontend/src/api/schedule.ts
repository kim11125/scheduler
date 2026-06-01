import client from './client'
import type { Category, BaseballType } from '@/types'

export interface SchedulePayload {
  title: string
  category: Category
  baseballType?: BaseballType | null
  date: string
  endDate?: string | null
  memo?: string
  targetUserId?: number | null
}

export const scheduleApi = {
  getAll: () => client.get('/api/schedules'),
  create: (data: SchedulePayload) => client.post('/api/schedules', data),
  update: (id: number, data: SchedulePayload) => client.put(`/api/schedules/${id}`, data),
  delete: (id: number) => client.delete(`/api/schedules/${id}`),
}
