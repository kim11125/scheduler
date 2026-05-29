import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Schedule, ScheduleFormData, Category } from '@/types'

function today() {
  return new Date()
}

function dateStr(d: Date): string {
  return d.toISOString().slice(0, 10)
}

function mockDate(dayOffset: number): string {
  const d = today()
  d.setDate(1)  // 이번 달 1일
  d.setDate(d.getDate() + dayOffset)
  return dateStr(d)
}

const INITIAL_SCHEDULES: Schedule[] = [
  { id:1,  userId:2, title:'두산 vs LG',         category:'BASEBALL',          baseballType:'HOME',  date:mockDate(2),  memo:'1루 응원석', createdAt:'', updatedAt:'' },
  { id:2,  userId:2, title:'KBL 직관',            category:'BASKETBALL',        baseballType:null,    date:mockDate(4),  memo:null,         createdAt:'', updatedAt:'' },
  { id:3,  userId:2, title:'두산 @ 잠실',          category:'BASEBALL',          baseballType:'AWAY',  date:mockDate(7),  memo:'원정 응원',   createdAt:'', updatedAt:'' },
  { id:4,  userId:2, title:'K리그 직관',           category:'SOCCER',            baseballType:null,    date:mockDate(9),  memo:null,         createdAt:'', updatedAt:'' },
  { id:5,  userId:2, title:'흥국생명 경기',         category:'WOMENS_VOLLEYBALL', baseballType:null,    date:mockDate(11), memo:'2층 좌석',   createdAt:'', updatedAt:'' },
  { id:6,  userId:2, title:'두산 vs SSG',          category:'BASEBALL',          baseballType:'HOME',  date:mockDate(14), memo:null,         createdAt:'', updatedAt:'' },
  { id:7,  userId:2, title:'KBL 플레이오프',        category:'BASKETBALL',        baseballType:null,    date:mockDate(17), memo:'결승전',     createdAt:'', updatedAt:'' },
  { id:8,  userId:2, title:'두산 vs KT',           category:'BASEBALL',          baseballType:'HOME',  date:mockDate(19), memo:null,         createdAt:'', updatedAt:'' },
  { id:9,  userId:2, title:'운동 모임',             category:'ETC',               baseballType:null,    date:mockDate(21), memo:'저녁 7시',   createdAt:'', updatedAt:'' },
  { id:10, userId:2, title:'축구 교실',             category:'SOCCER',            baseballType:null,    date:mockDate(24), memo:null,         createdAt:'', updatedAt:'' },
  { id:11, userId:2, title:'남자배구 한국전력',      category:'MENS_VOLLEYBALL',   baseballType:null,    date:mockDate(26), memo:null,         createdAt:'', updatedAt:'' },
  { id:12, userId:2, title:'두산 @ 문학',           category:'BASEBALL',          baseballType:'AWAY',  date:mockDate(28), memo:'원정',       createdAt:'', updatedAt:'' },
]

let nextId = 100

export const useScheduleStore = defineStore('schedule', () => {
  const schedules = ref<Schedule[]>([...INITIAL_SCHEDULES])

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

  function add(data: ScheduleFormData): Schedule {
    const now = new Date().toISOString()
    const s: Schedule = {
      id: nextId++,
      userId: 2,
      title: data.title,
      category: data.category as Category,
      baseballType: data.baseballType,
      date: data.date,
      memo: data.memo || null,
      createdAt: now,
      updatedAt: now,
    }
    schedules.value.unshift(s)
    return s
  }

  function update(id: number, data: ScheduleFormData) {
    const idx = schedules.value.findIndex(s => s.id === id)
    if (idx === -1) return
    schedules.value[idx] = {
      ...schedules.value[idx],
      title: data.title,
      category: data.category as Category,
      baseballType: data.baseballType,
      date: data.date,
      memo: data.memo || null,
      updatedAt: new Date().toISOString(),
    }
  }

  function remove(id: number) {
    schedules.value = schedules.value.filter(s => s.id !== id)
  }

  return { schedules, getByDate, getByMonth, getDotsByDate, getExtraCount, add, update, remove }
})
