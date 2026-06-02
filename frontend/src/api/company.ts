import client from './client'

export const companyApi = {
  getAll: () => client.get('/api/companies'),
  getById: (id: number) => client.get(`/api/companies/${id}`),
  create: (data: any) => client.post('/api/admin/companies', data),
  update: (id: number, data: any) => client.put(`/api/admin/companies/${id}`, data),
  setActive: (id: number, active: boolean) =>
    client.patch(`/api/admin/companies/${id}/active`, { active }),
  addTeam: (companyId: number, teamId: number) =>
    client.post(`/api/admin/companies/${companyId}/teams/${teamId}`),
  removeTeam: (companyId: number, teamId: number) =>
    client.delete(`/api/admin/companies/${companyId}/teams/${teamId}`),
  getUserCompanies: (userId: number) =>
    client.get(`/api/admin/users/${userId}/companies`),
  addUserCompany: (userId: number, companyId: number) =>
    client.post(`/api/admin/users/${userId}/companies/${companyId}`),
  setUserCompanyPrimary: (userId: number, companyId: number) =>
    client.patch(`/api/admin/users/${userId}/companies/${companyId}/primary`),
  removeUserCompany: (userId: number, companyId: number) =>
    client.delete(`/api/admin/users/${userId}/companies/${companyId}`),
}
