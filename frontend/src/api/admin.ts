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
}
