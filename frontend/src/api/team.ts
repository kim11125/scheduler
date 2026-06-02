import client from './client'

export const teamApi = {
  getAll: () => client.get('/api/teams'),
  getById: (id: number) => client.get(`/api/teams/${id}`),
  create: (data: any) => client.post('/api/admin/teams', data),
  update: (id: number, data: any) => client.put(`/api/admin/teams/${id}`, data),
  setActive: (id: number, active: boolean) =>
    client.patch(`/api/admin/teams/${id}/active`, { active }),
  getUserTeams: (userId: number) =>
    client.get(`/api/admin/users/${userId}/teams`),
  addUserTeam: (userId: number, teamId: number) =>
    client.post(`/api/admin/users/${userId}/teams/${teamId}`),
  setUserTeamPrimary: (userId: number, teamId: number) =>
    client.patch(`/api/admin/users/${userId}/teams/${teamId}/primary`),
  removeUserTeam: (userId: number, teamId: number) =>
    client.delete(`/api/admin/users/${userId}/teams/${teamId}`),
}
