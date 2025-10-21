import axios from 'axios'

const API_BASE_URL = (import.meta as any).env?.VITE_API_URL || '/api'

// Create axios instance
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

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
  id?: number
  amount?: number
  currency?: string
  merchant?: string
  location?: string
  payment_method?: string
  user_id?: string
  ip_address?: string
  device_info?: string
  isFraudulent?: boolean
  fraud_score?: number
  createdAt?: string
}

// Auth API - Simple login without real authentication
export const authApi = {
  login: (data: LoginRequest): Promise<{ success: boolean; user: User }> => {
    // Simple demo login - accept any username/password
    return Promise.resolve({
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
    })
  },

  getMe: (): Promise<User> => {
    const username = localStorage.getItem('username') || 'admin'
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

// Transactions API - matches our simplified backend
export const transactionsApi = {
  getTransactions: (): Promise<Transaction[]> =>
    api.get('/transactions').then(res => res.data),

  getSuspiciousTransactions: (): Promise<Transaction[]> =>
    api.get('/transactions/suspicious').then(res => res.data),

  createTransaction: (transaction: Partial<Transaction>): Promise<Transaction> =>
    api.post('/transactions', transaction).then(res => res.data),
}

export const dashboardApi = {
  getStats: (): Promise<{ total_transactions: number; suspicious_transactions: number }> => {
    return Promise.resolve({
      total_transactions: 0,
      suspicious_transactions: 0
    })
  },

  getRecentTransactions: (limit = 10): Promise<Transaction[]> =>
    transactionsApi.getTransactions().then(transactions => transactions.slice(0, limit)),

  getSuspiciousTransactions: (limit = 20): Promise<Transaction[]> =>
    transactionsApi.getSuspiciousTransactions().then(transactions => transactions.slice(0, limit)),
}
