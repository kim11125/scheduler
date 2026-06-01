import client from './client'

export const userApi = {
  getMe: () => client.get('/api/users/me'),
  changePassword: (currentPassword: string, newPassword: string) =>
    client.put('/api/users/me/password', { currentPassword, newPassword }),
}
