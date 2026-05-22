const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (response.status === 204) {
    return null
  }

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(payload?.message ?? 'Nao foi possivel concluir a acao.')
  }

  return payload
}

export const api = {
  me: () => request('/auth/me'),
  login: (data) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  register: (data) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  logout: () => request('/auth/logout', { method: 'POST' }),
  listHouses: () => request('/houses'),
  createHouse: (data) =>
    request('/houses', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  addMember: (houseId, data) =>
    request(`/houses/${houseId}/members`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getDashboard: (houseId) => request(`/houses/${houseId}/dashboard`),
  createExpense: (houseId, data) =>
    request(`/houses/${houseId}/expenses`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  markPaymentAsPaid: (expenseId) =>
    request(`/expenses/${expenseId}/payments/me/paid`, { method: 'POST' }),
  createTask: (houseId, data) =>
    request(`/houses/${houseId}/tasks`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  markTaskAsDone: (taskId) => request(`/tasks/${taskId}/done`, { method: 'POST' }),
}
