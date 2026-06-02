import client from './client'

export interface LoginResponse {
  token: string
  userId: number
  name: string
  role: string
  status: string
}

export interface CheckLoginIdResponse {
  loginId: string
  available: boolean
  message: string
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<LoginResponse>('/api/auth/login', { username, password }),

  register: (username: string, password: string, name: string) =>
    client.post('/api/auth/register', { username, password, name }),

  logout: (username: string) =>
    client.post(`/api/auth/logout?username=${encodeURIComponent(username)}`),

  checkLoginId: (loginId: string) =>
    client.get<CheckLoginIdResponse>(`/api/auth/check-login-id?loginId=${encodeURIComponent(loginId)}`),
}
