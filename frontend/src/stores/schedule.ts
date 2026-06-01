import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Schedule, ScheduleFormData, Category } from '@/types'
import { scheduleApi } from '@/api/schedule'

export const useScheduleStore = defineStore('schedule', () => {
  const schedules = ref<Schedule[]>([])

  async function fetchAll() {
    const res = await scheduleApi.getAll()
    schedules.value = res.data
  }

  function getByDate(date: string): Schedule[] {
    return schedules.value
      .filter(s => s.date === date)
      .sort((a, b) => b.id - a.id)
  }

  function getByMonth(year: number, month: number): Schedule[] {
    const prefix = `${year}-${String(month).padStart(2, '0')}`
    return schedules.value.filter(s => s.date.startsWith(prefix))
  }

  function getDotsByDate(date: string): Category[] {
    const items = getByDate(date)
    const cats = [...new Set(items.map(s => s.category))]
    return cats.slice(0, 3)
  }

  function getExtraCount(date: string): number {
    const items = getByDate(date)
    const unique = [...new Set(items.map(s => s.category))]
    return Math.max(0, unique.length - 3)
  }

  async function add(data: ScheduleFormData): Promise<Schedule> {
    const res = await scheduleApi.create({
      title: data.title,
      category: data.category as Category,
      baseballType: data.baseballType || null,
      date: data.date,
      endDate: data.endDate || null,
      memo: data.memo || undefined,
      targetUserId: data.targetUserId || null,
    })
    schedules.value.unshift(res.data)
    return res.data
  }

  async function update(id: number, data: ScheduleFormData) {
    const res = await scheduleApi.update(id, {
      title: data.title,
      category: data.category as Category,
      baseballType: data.baseballType || null,
      date: data.date,
      endDate: data.endDate || null,
      memo: data.memo || undefined,
    })
    const idx = schedules.value.findIndex(s => s.id === id)
    if (idx !== -1) schedules.value[idx] = res.data
  }

  async function remove(id: number) {
    await scheduleApi.delete(id)
    schedules.value = schedules.value.filter(s => s.id !== id)
  }

  return { schedules, fetchAll, getByDate, getByMonth, getDotsByDate, getExtraCount, add, update, remove }
})
