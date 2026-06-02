import client from './client'

export interface LoginResponse {
  token: string
  userId: number
  name: string
  role: string
  status: string
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<LoginResponse>('/api/auth/login', { username, password }),

  register: (username: string, password: string, name: string) =>
    client.post('/api/auth/register', { username, password, name }),

  logout: (username: string) =>
    client.post(`/api/auth/logout?username=${encodeURIComponent(username)}`),
}
