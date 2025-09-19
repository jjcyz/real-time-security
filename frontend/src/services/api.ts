import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

// Create axios instance
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      localStorage.removeItem('access_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Types
export interface User {
  id: number
  email: string
  username: string
  full_name?: string
  is_active: boolean
  role: 'admin' | 'user'
  created_at: string
  updated_at?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  email: string
  username: string
  password: string
  full_name?: string
}

export interface TokenResponse {
  access_token: string
  token_type: string
}

export interface DashboardStats {
  total_transactions: number
  suspicious_transactions: number
  total_alerts: number
  open_alerts: number
  high_severity_alerts: number
  files_uploaded: number
  last_upload?: string
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
  is_suspicious: boolean
  risk_score: number
  fraud_reasons?: string[]
  created_at: string
}

export interface Alert {
  id: number
  title: string
  description: string
  severity: 'low' | 'medium' | 'high' | 'critical'
  status: 'open' | 'investigating' | 'resolved' | 'false_positive'
  rule_name: string
  confidence_score: number
  risk_score: number
  transaction_ids?: number[]
  user_ids?: string[]
  alert_metadata?: Record<string, any>
  created_at: string
  updated_at?: string
  resolved_at?: string
}

export interface UploadedFile {
  id: number
  filename: string
  original_filename: string
  file_size: number
  content_type: string
  status: 'uploaded' | 'processing' | 'completed' | 'failed'
  total_rows: number
  processed_rows: number
  error_message?: string
  created_at: string
  updated_at?: string
}

// Auth API
export const authApi = {
  login: (data: LoginRequest): Promise<TokenResponse> =>
    api.post('/auth/login', data).then(res => res.data),

  register: (data: RegisterRequest): Promise<User> =>
    api.post('/auth/register', data).then(res => res.data),

  getMe: (): Promise<User> =>
    api.get('/auth/me').then(res => res.data),

  refreshToken: (): Promise<TokenResponse> =>
    api.post('/auth/refresh').then(res => res.data),
}

// Dashboard API
export const dashboardApi = {
  getStats: (): Promise<DashboardStats> =>
    api.get('/dashboard/stats').then(res => res.data),

  getRecentTransactions: (limit = 10): Promise<Transaction[]> =>
    api.get(`/dashboard/recent-transactions?limit=${limit}`).then(res => res.data),

  getRecentAlerts: (limit = 10): Promise<Alert[]> =>
    api.get(`/dashboard/recent-alerts?limit=${limit}`).then(res => res.data),

  getSuspiciousTransactions: (limit = 20): Promise<Transaction[]> =>
    api.get(`/dashboard/suspicious-transactions?limit=${limit}`).then(res => res.data),

  getFiles: (): Promise<UploadedFile[]> =>
    api.get('/dashboard/files').then(res => res.data),

  getAlertsBySeverity: (): Promise<Record<string, number>> =>
    api.get('/dashboard/alerts-by-severity').then(res => res.data),

  getTransactionsByDay: (days = 30): Promise<Record<string, number>> =>
    api.get(`/dashboard/transactions-by-day?days=${days}`).then(res => res.data),
}

// Files API
export const filesApi = {
  uploadFile: (file: File): Promise<UploadedFile> => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }).then(res => res.data)
  },

  getFiles: (): Promise<UploadedFile[]> =>
    api.get('/files/').then(res => res.data),

  getFile: (id: number): Promise<UploadedFile> =>
    api.get(`/files/${id}`).then(res => res.data),

  deleteFile: (id: number): Promise<void> =>
    api.delete(`/files/${id}`).then(res => res.data),

  analyzeFile: (id: number): Promise<any> =>
    api.post(`/files/${id}/analyze`).then(res => res.data),
}

// Alerts API
export const alertsApi = {
  getAlerts: (params?: {
    status?: string
    severity?: string
    limit?: number
    offset?: number
  }): Promise<Alert[]> => {
    const searchParams = new URLSearchParams()
    if (params?.status) searchParams.append('status', params.status)
    if (params?.severity) searchParams.append('severity', params.severity)
    if (params?.limit) searchParams.append('limit', params.limit.toString())
    if (params?.offset) searchParams.append('offset', params.offset.toString())

    return api.get(`/alerts/?${searchParams}`).then(res => res.data)
  },

  getAlert: (id: number): Promise<Alert> =>
    api.get(`/alerts/${id}`).then(res => res.data),

  updateAlert: (id: number, data: { status: string; resolved_at?: string }): Promise<Alert> =>
    api.patch(`/alerts/${id}`, data).then(res => res.data),

  resolveAlert: (id: number): Promise<void> =>
    api.post(`/alerts/${id}/resolve`).then(res => res.data),

  markFalsePositive: (id: number): Promise<void> =>
    api.post(`/alerts/${id}/false-positive`).then(res => res.data),

  getAlertStats: (): Promise<Record<string, any>> =>
    api.get('/alerts/stats/summary').then(res => res.data),
}
