export type UserRole = 'ADMIN' | 'MANAGER' | 'USER'
export type UserStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'DISABLED'
export type Category = 'BASEBALL' | 'BASKETBALL' | 'SOCCER' | 'WOMENS_VOLLEYBALL' | 'MENS_VOLLEYBALL' | 'ETC'
export type BaseballType = 'HOME' | 'AWAY'
export type ThemeKey = 'light' | 'dark' | 'orange'
export type ScheduleStatus = 'SCHEDULED' | 'CONFIRMED' | 'CHANGED' | 'CANCELLED'

export interface User {
  id: number
  username: string
  name: string
  role: UserRole
  status: UserStatus
}

export interface UserProfile {
  id: number
  loginId: string
  name: string
  profileImageUrl: string | null
  companies: CompanyRef[]
  teams: TeamRef[]
}

export interface CompanyRef {
  id: number
  name: string
  isPrimary?: boolean
}

export interface TeamRef {
  id: number
  name: string
  category: Category
  isPrimary?: boolean
}

export interface Company {
  id: number
  name: string
  description: string | null
  logoUrl: string | null
  isActive: boolean
  teams?: Team[]
  users?: User[]
}

export interface Team {
  id: number
  name: string
  category: Category
  description: string | null
  logoUrl: string | null
  isActive: boolean
}

export interface Schedule {
  id: number
  userId: number
  userName?: string
  teamId?: number | null
  teamName?: string | null
  title: string
  category: Category
  baseballType: BaseballType | null
  date: string
  startTime?: string | null
  endDate?: string | null
  endTime?: string | null
  location?: string | null
  memo: string | null
  status?: ScheduleStatus
  createdAt: string
  updatedAt: string
}

export interface ScheduleFormData {
  title: string
  category: Category | ''
  baseballType: BaseballType | null
  date: string
  startTime?: string
  endDate?: string
  endTime?: string
  location?: string
  memo: string
  targetUserId?: number | null
  teamId?: number | null
  status?: ScheduleStatus
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

export const SCHEDULE_STATUS_LABELS: Record<ScheduleStatus, string> = {
  SCHEDULED: '예정',
  CONFIRMED: '확정',
  CHANGED: '변경',
  CANCELLED: '취소',
}
