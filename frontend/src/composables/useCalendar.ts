import { computed } from 'vue'

export interface CalendarDay {
  date: string        // 'YYYY-MM-DD' or ''
  day: number         // 1-31 or 0 (empty)
  isCurrentMonth: boolean
  isToday: boolean
  dayOfWeek: number   // 0=Sun, 6=Sat
}

export function useCalendar(year: () => number, month: () => number) {
  const todayStr = new Date().toISOString().slice(0, 10)

  const days = computed<CalendarDay[]>(() => {
    const y = year()
    const m = month()
    const firstDay = new Date(y, m - 1, 1).getDay()  // 0=Sun
    const daysInMonth = new Date(y, m, 0).getDate()

    const cells: CalendarDay[] = []

    // Leading empty cells
    for (let i = 0; i < firstDay; i++) {
      cells.push({ date: '', day: 0, isCurrentMonth: false, isToday: false, dayOfWeek: i })
    }

    // Month days
    for (let d = 1; d <= daysInMonth; d++) {
      const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      cells.push({
        date: dateStr,
        day: d,
        isCurrentMonth: true,
        isToday: dateStr === todayStr,
        dayOfWeek: (firstDay + d - 1) % 7,
      })
    }

    // Trailing empty cells to complete 6 rows
    const remainder = cells.length % 7
    if (remainder !== 0) {
      for (let i = 0; i < 7 - remainder; i++) {
        cells.push({ date: '', day: 0, isCurrentMonth: false, isToday: false, dayOfWeek: 0 })
      }
    }

    return cells
  })

  const weeks = computed(() => {
    const result: CalendarDay[][] = []
    for (let i = 0; i < days.value.length; i += 7) {
      result.push(days.value.slice(i, i + 7))
    }
    return result
  })

  return { days, weeks }
}
