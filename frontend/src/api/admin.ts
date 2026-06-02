import client from './client'

export const adminApi = {
  getUsers: () => client.get('/api/admin/users'),
  getPendingUsers: () => client.get('/api/admin/users/pending'),
  approve: (id: number) => client.put(`/api/admin/users/${id}/approve`),
  reject: (id: number) => client.put(`/api/admin/users/${id}/reject`),
  disable: (id: number) => client.put(`/api/admin/users/${id}/disable`),
  activate: (id: number) => client.put(`/api/admin/users/${id}/activate`),
  getAllSchedules: () => client.get('/api/admin/schedules'),
  getUserSchedules: (userId: number) => client.get(`/api/admin/schedules/user/${userId}`),
  deleteSchedule: (id: number) => client.delete(`/api/admin/schedules/${id}`),
  changeUserPassword: (id: number, newPassword: string) =>
    client.put(`/api/admin/users/${id}/password`, { newPassword }),
  changeUserRole: (id: number, role: string) =>
    client.put(`/api/admin/users/${id}/role`, { role }),
  getLogs: (page = 0, size = 50) =>
    client.get(`/api/admin/logs?page=${page}&size=${size}`),
  getUserLogs: (userId: number, page = 0, size = 50) =>
    client.get(`/api/admin/logs/user/${userId}?page=${page}&size=${size}`),
}
