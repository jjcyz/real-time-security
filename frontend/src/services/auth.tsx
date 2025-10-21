import React, { createContext, useContext, useEffect, useState } from 'react'
import { authApi, User } from './api'

interface AuthContextType {
  user: User | null
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  register: (email: string, username: string, password: string, fullName?: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const username = localStorage.getItem('username')
    if (username) {
      // Verify credentials and get user info
      authApi.getMe()
        .then(setUser)
        .catch(() => {
          localStorage.removeItem('username')
          localStorage.removeItem('password')
        })
        .finally(() => setIsLoading(false))
    } else {
      setIsLoading(false)
    }
  }, [])

  const login = async (username: string, password: string) => {
    const response = await authApi.login({ username, password })
    // Store credentials for basic auth (no access_token in this implementation)
    localStorage.setItem('username', username)
    localStorage.setItem('password', password)

    setUser(response.user)
  }

  const register = async (_email: string, username: string, password: string, _fullName?: string) => {
    // For now, just log in with the provided credentials
    // In a real app, you'd call a registration API endpoint
    await login(username, password)
  }

  const logout = () => {
    localStorage.removeItem('username')
    localStorage.removeItem('password')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
