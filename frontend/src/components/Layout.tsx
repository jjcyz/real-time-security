import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import {
  LayoutDashboard,
  FileText,
  AlertTriangle,
  LogOut,
  Menu,
  X,
  Shield,
  Sparkles
} from 'lucide-react'
import { useAuth } from '../services/auth'

interface LayoutProps {
  children: React.ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { user, logout } = useAuth()
  const location = useLocation()

  const navigation = [
    { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    { name: 'Files', href: '/files', icon: FileText },
    { name: 'Alerts', href: '/alerts', icon: AlertTriangle },
  ]

  const isActive = (href: string) => location.pathname === href

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      {/* Mobile sidebar */}
      <AnimatePresence>
        {sidebarOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 lg:hidden"
          >
            <motion.div
              className="fixed inset-0 bg-black/20 backdrop-blur-sm"
              onClick={() => setSidebarOpen(false)}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
            />
            <motion.div
              className="relative flex w-80 flex-col glass-card m-4 h-[calc(100vh-2rem)]"
              initial={{ x: -320, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: -320, opacity: 0 }}
              transition={{ type: "spring", damping: 25, stiffness: 200 }}
            >
              <div className="flex h-20 items-center justify-between px-6">
                <div className="flex items-center">
                  <motion.div
                    animate={{
                      scale: [1, 1.1, 1],
                      opacity: [0.8, 1, 0.8]
                    }}
                    transition={{
                      duration: 2,
                      repeat: Infinity,
                      ease: "easeInOut"
                    }}
                  >
                    <Shield className="h-10 w-10 text-blue-600" />
                  </motion.div>
                  <div className="ml-3">
                    <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                      Security Dashboard
                    </span>
                    <p className="text-xs text-slate-500">Real-time monitoring</p>
                  </div>
                </div>
                <motion.button
                  onClick={() => setSidebarOpen(false)}
                  className="p-2 text-slate-400 hover:text-slate-600 hover:bg-white/20 rounded-xl transition-all duration-300"
                  whileHover={{ scale: 1.1 }}
                  whileTap={{ scale: 0.9 }}
                >
                  <X className="h-6 w-6" />
                </motion.button>
              </div>
              <nav className="flex-1 space-y-2 px-4 py-4 custom-scrollbar">
                {navigation.map((item, index) => {
                  const Icon = item.icon
                  const active = isActive(item.href)
                  return (
                    <motion.div
                      key={item.name}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.1 }}
                    >
                      <Link
                        to={item.href}
                        onClick={() => setSidebarOpen(false)}
                        className={`nav-item ${active ? 'nav-item-active' : ''}`}
                      >
                        <motion.div
                          whileHover={{ scale: 1.1 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          <Icon className="mr-3 h-5 w-5" />
                        </motion.div>
                        <span>{item.name}</span>
                        {active && (
                          <motion.div
                            className="ml-auto"
                            animate={{ rotate: 360 }}
                            transition={{ duration: 0.5 }}
                          >
                            <Sparkles className="h-4 w-4 text-blue-500" />
                          </motion.div>
                        )}
                      </Link>
                    </motion.div>
                  )
                })}
              </nav>
              <div className="border-t border-white/20 p-6">
                <div className="flex items-center mb-4">
                  <div className="flex-shrink-0">
                    <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-lg">
                      <span className="text-lg font-bold text-white">
                        {user?.username?.charAt(0).toUpperCase()}
                      </span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-semibold text-slate-700">{user?.username}</p>
                    <p className="text-xs text-slate-500">{user?.email}</p>
                  </div>
                </div>
                <motion.button
                  onClick={logout}
                  className="btn-ghost w-full justify-start"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  <LogOut className="mr-3 h-4 w-4" />
                  Sign out
                </motion.button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Desktop sidebar */}
      <motion.div
        className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-80 lg:flex-col"
        initial={{ x: -320, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.6, ease: "easeOut" }}
      >
        <div className="flex flex-col flex-grow glass-card m-4 h-[calc(100vh-2rem)]">
          <div className="flex h-20 items-center px-6">
            <motion.div
              animate={{
                scale: [1, 1.1, 1],
                opacity: [0.8, 1, 0.8]
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                ease: "easeInOut"
              }}
            >
              <Shield className="h-10 w-10 text-blue-600" />
            </motion.div>
            <div className="ml-3">
              <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                Security Dashboard
              </span>
              <p className="text-xs text-slate-500">Real-time monitoring</p>
            </div>
          </div>
          <nav className="flex-1 space-y-2 px-4 py-4 custom-scrollbar">
            {navigation.map((item, index) => {
              const Icon = item.icon
              const active = isActive(item.href)
              return (
                <motion.div
                  key={item.name}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.1 }}
                >
                  <Link
                    to={item.href}
                    className={`nav-item ${active ? 'nav-item-active' : ''}`}
                  >
                    <motion.div
                      whileHover={{ scale: 1.1 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      <Icon className="mr-3 h-5 w-5" />
                    </motion.div>
                    <span>{item.name}</span>
                    {active && (
                      <motion.div
                        className="ml-auto"
                        animate={{ rotate: 360 }}
                        transition={{ duration: 0.5 }}
                      >
                        <Sparkles className="h-4 w-4 text-blue-500" />
                      </motion.div>
                    )}
                  </Link>
                </motion.div>
              )
            })}
          </nav>
          <div className="border-t border-white/20 p-6">
            <div className="flex items-center mb-4">
              <div className="flex-shrink-0">
                <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-lg">
                  <span className="text-lg font-bold text-white">
                    {user?.username?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-semibold text-slate-700">{user?.username}</p>
                <p className="text-xs text-slate-500">{user?.email}</p>
              </div>
            </div>
            <motion.button
              onClick={logout}
              className="btn-ghost w-full justify-start"
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              <LogOut className="mr-3 h-4 w-4" />
              Sign out
            </motion.button>
          </div>
        </div>
      </motion.div>

      {/* Main content */}
      <div className="lg:pl-88">
        {/* Top bar */}
        <motion.div
          className="sticky top-0 z-10 flex h-20 glass-panel m-4 lg:hidden"
          initial={{ y: -80, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.5 }}
        >
          <motion.button
            onClick={() => setSidebarOpen(true)}
            className="px-6 text-slate-500 hover:text-slate-700 hover:bg-white/20 rounded-xl transition-all duration-300"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <Menu className="h-6 w-6" />
          </motion.button>
          <div className="flex flex-1 items-center justify-center">
            <motion.div
              animate={{
                scale: [1, 1.1, 1],
                opacity: [0.8, 1, 0.8]
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                ease: "easeInOut"
              }}
            >
              <Shield className="h-8 w-8 text-blue-600" />
            </motion.div>
            <span className="ml-2 text-xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
              Security Dashboard
            </span>
          </div>
        </motion.div>

        {/* Page content */}
        <main className="flex-1">
          <motion.div
            className="py-6"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
              {children}
            </div>
          </motion.div>
        </main>
      </div>
    </div>
  )
}
