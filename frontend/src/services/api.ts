import axios from 'axios'

const API_BASE_URL = (import.meta as any).env?.VITE_API_URL || '/api'

// Create axios instance
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add basic auth
api.interceptors.request.use(
  (config) => {
    const username = localStorage.getItem('username')
    const password = localStorage.getItem('password')
    if (username && password) {
      const credentials = btoa(`${username}:${password}`)
      config.headers.Authorization = `Basic ${credentials}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('username')
      localStorage.removeItem('password')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface User {
  id: number
  email: string
  username: string
  full_name?: string
  is_active: boolean
  role: 'admin' | 'user'
  created_at: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface Transaction {
  id: number
  transaction_id?: string
  amount?: number
  currency: string
  timestamp?: string
  merchant?: string
  location?: string
  payment_method?: string
  user_id?: string
  ip_address?: string
  device_info?: string
  is_fraudulent: boolean
  fraud_score?: number
  created_at: string
}

// Auth API
export const authApi = {
  login: (data: LoginRequest): Promise<{ success: boolean; user: User }> => {
    // Store credentials for basic auth
    localStorage.setItem('username', data.username)
    localStorage.setItem('password', data.password)

    // Test the credentials by making a request to the home endpoint
    return api.get('/').then(() => ({
      success: true,
      user: {
        id: 1,
        email: `${data.username}@example.com`,
        username: data.username,
        full_name: data.username,
        is_active: true,
        role: 'admin' as const,
        created_at: new Date().toISOString()
      }
    }))
  },

  getMe: (): Promise<User> => {
    const username = localStorage.getItem('username')
    if (!username) {
      return Promise.reject(new Error('Not authenticated'))
    }
    return Promise.resolve({
      id: 1,
      email: `${username}@example.com`,
      username: username,
      full_name: username,
      is_active: true,
      role: 'admin' as const,
      created_at: new Date().toISOString()
    })
  },

  logout: (): void => {
    localStorage.removeItem('username')
    localStorage.removeItem('password')
  }
}

// Transactions API - matches Java backend endpoints
export const transactionsApi = {
  getTransactions: (page = 0, size = 20): Promise<{ content: Transaction[]; totalElements: number; totalPages: number }> =>
    api.get(`/api/transactions?page=${page}&size=${size}`).then(res => res.data),

  getSuspiciousTransactions: (page = 0, size = 20): Promise<{ content: Transaction[]; totalElements: number; totalPages: number }> =>
    api.get(`/api/transactions/suspicious?page=${page}&size=${size}`).then(res => res.data),

  createTransaction: (transaction: Partial<Transaction>): Promise<Transaction> =>
    api.post('/api/transactions', transaction).then(res => res.data),

  getTransaction: (id: number): Promise<Transaction> =>
    api.get(`/api/transactions/${id}`).then(res => res.data),

  updateTransaction: (id: number, transaction: Partial<Transaction>): Promise<Transaction> =>
    api.put(`/api/transactions/${id}`, transaction).then(res => res.data),

  deleteTransaction: (id: number): Promise<void> =>
    api.delete(`/api/transactions/${id}`).then(res => res.data),
}

export const dashboardApi = {
  getStats: (): Promise<{ total_transactions: number; suspicious_transactions: number }> => {
    return Promise.resolve({
      total_transactions: 0,
      suspicious_transactions: 0
    })
  },

  getRecentTransactions: (limit = 10): Promise<Transaction[]> =>
    transactionsApi.getTransactions(0, limit).then(res => res.content),

  getSuspiciousTransactions: (limit = 20): Promise<Transaction[]> =>
    transactionsApi.getSuspiciousTransactions(0, limit).then(res => res.content),
}
