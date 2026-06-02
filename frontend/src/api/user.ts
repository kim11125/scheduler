import client from './client'

export const userApi = {
  getMe: () => client.get('/api/users/me'),
  changePassword: (currentPassword: string, newPassword: string) =>
    client.put('/api/users/me/password', { currentPassword, newPassword }),

  getMyProfile: () => client.get('/api/users/me/profile'),
  updateMyProfile: (data: { name: string }) => client.patch('/api/users/me/profile', data),
  changeMyLoginId: (data: { currentPassword: string; newLoginId: string }) =>
    client.patch('/api/users/me/login-id', data),
  uploadMyProfileImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return client.post('/api/users/me/profile-image', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  updateMyProfileImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return client.put('/api/users/me/profile-image', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  deleteMyProfileImage: () => client.delete('/api/users/me/profile-image'),
}
