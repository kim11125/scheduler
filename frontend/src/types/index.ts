export type UserRole = 'ADMIN' | 'MANAGER' | 'USER'
export type UserStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'DISABLED'
export type Category = 'BASEBALL' | 'BASKETBALL' | 'SOCCER' | 'WOMENS_VOLLEYBALL' | 'MENS_VOLLEYBALL' | 'ETC'
export type BaseballType = 'HOME' | 'AWAY'
export type ThemeKey = 'light' | 'dark' | 'orange'

export interface User {
  id: number
  username: string
  name: string
  role: UserRole
  status: UserStatus
}

export interface Schedule {
  id: number
  userId: number
  userName?: string
  title: string
  category: Category
  baseballType: BaseballType | null
  date: string
  endDate?: string | null
  memo: string | null
  createdAt: string
  updatedAt: string
}

export interface ScheduleFormData {
  title: string
  category: Category | ''
  baseballType: BaseballType | null
  date: string
  endDate?: string
  memo: string
  targetUserId?: number | null
}

export const CATEGORY_LABELS: Record<Category, string> = {
  BASEBALL: '야구',
  BASKETBALL: '농구',
  SOCCER: '축구',
  WOMENS_VOLLEYBALL: '여자배구',
  MENS_VOLLEYBALL: '남자배구',
  ETC: '기타',
}

export const CATEGORY_DOT_VARS: Record<Category, string> = {
  BASEBALL: 'var(--color-dot-baseball)',
  BASKETBALL: 'var(--color-dot-basketball)',
  SOCCER: 'var(--color-dot-soccer)',
  WOMENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  MENS_VOLLEYBALL: 'var(--color-dot-volleyball)',
  ETC: 'var(--color-dot-etc)',
}
