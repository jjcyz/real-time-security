import { useQuery } from 'react-query'
import { motion } from 'framer-motion'
import {
  FileText,
  AlertTriangle,
  TrendingUp,
  Shield,
  Activity,
  Clock,
  DollarSign
} from 'lucide-react'
import { dashboardApi, DashboardStats, Transaction, Alert } from '../services/api'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Dashboard() {
  const { data: stats, isLoading: statsLoading } = useQuery<DashboardStats>(
    'dashboard-stats',
    dashboardApi.getStats,
    { refetchInterval: 30000 } // Refetch every 30 seconds
  )

  const { data: recentTransactions, isLoading: transactionsLoading } = useQuery<Transaction[]>(
    'recent-transactions',
    () => dashboardApi.getRecentTransactions(5)
  )

  const { data: recentAlerts, isLoading: alertsLoading } = useQuery<Alert[]>(
    'recent-alerts',
    () => dashboardApi.getRecentAlerts(5)
  )

  const { data: suspiciousTransactions, isLoading: suspiciousLoading } = useQuery<Transaction[]>(
    'suspicious-transactions',
    () => dashboardApi.getSuspiciousTransactions(10)
  )

  if (statsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  const statCards = [
    {
      title: 'Total Transactions',
      value: stats?.total_transactions || 0,
      icon: DollarSign,
      gradient: 'from-blue-500 to-cyan-500',
      bgGradient: 'from-blue-500/20 to-cyan-500/20',
      textColor: 'text-blue-700'
    },
    {
      title: 'Suspicious Transactions',
      value: stats?.suspicious_transactions || 0,
      icon: AlertTriangle,
      gradient: 'from-red-500 to-pink-500',
      bgGradient: 'from-red-500/20 to-pink-500/20',
      textColor: 'text-red-700'
    },
    {
      title: 'Open Alerts',
      value: stats?.open_alerts || 0,
      icon: Shield,
      gradient: 'from-amber-500 to-orange-500',
      bgGradient: 'from-amber-500/20 to-orange-500/20',
      textColor: 'text-amber-700'
    },
    {
      title: 'Files Uploaded',
      value: stats?.files_uploaded || 0,
      icon: FileText,
      gradient: 'from-emerald-500 to-teal-500',
      bgGradient: 'from-emerald-500/20 to-teal-500/20',
      textColor: 'text-emerald-700'
    }
  ]

  return (
    <div className="space-y-8">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="text-center"
      >
        <h1 className="text-4xl font-bold bg-gradient-to-r from-slate-800 to-slate-600 bg-clip-text text-transparent mb-2">
          Security Dashboard
        </h1>
        <p className="text-slate-600 text-lg">
          Real-time monitoring and fraud detection overview
        </p>
      </motion.div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat, index) => {
          const Icon = stat.icon
          return (
            <motion.div
              key={stat.title}
              initial={{ opacity: 0, y: 30, scale: 0.9 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{
                delay: index * 0.1,
                type: "spring",
                damping: 20,
                stiffness: 100
              }}
              className="stat-card group"
              whileHover={{
                y: -5,
                transition: { duration: 0.2 }
              }}
            >
              <div className="card-content">
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <div className={`stat-icon bg-gradient-to-br ${stat.bgGradient} rounded-2xl`}>
                      <Icon className={`h-8 w-8 bg-gradient-to-r ${stat.gradient} bg-clip-text text-transparent`} />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-slate-600">{stat.title}</p>
                      <motion.p
                        className={`text-3xl font-bold ${stat.textColor}`}
                        initial={{ scale: 0.5 }}
                        animate={{ scale: 1 }}
                        transition={{ delay: index * 0.1 + 0.3, type: "spring", damping: 15 }}
                      >
                        {stat.value.toLocaleString()}
                      </motion.p>
                    </div>
                  </div>
                  <motion.div
                    className="opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                    animate={{ rotate: 360 }}
                    transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                  >
                    <div className={`w-3 h-3 rounded-full bg-gradient-to-r ${stat.gradient}`} />
                  </motion.div>
                </div>
              </div>
            </motion.div>
          )
        })}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
        {/* Recent Transactions */}
        <motion.div
          initial={{ opacity: 0, x: -30, scale: 0.95 }}
          animate={{ opacity: 1, x: 0, scale: 1 }}
          transition={{
            delay: 0.4,
            type: "spring",
            damping: 20,
            stiffness: 100
          }}
          className="card group"
          whileHover={{ scale: 1.02 }}
        >
          <div className="card-header">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="p-2 rounded-xl bg-gradient-to-br from-blue-500/20 to-cyan-500/20">
                  <Activity className="h-6 w-6 text-blue-600" />
                </div>
                <h3 className="ml-3 text-xl font-semibold text-slate-800">Recent Transactions</h3>
              </div>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 3, repeat: Infinity, ease: "linear" }}
              >
                <div className="w-2 h-2 rounded-full bg-gradient-to-r from-blue-500 to-cyan-500" />
              </motion.div>
            </div>
          </div>
          <div className="card-content">
            {transactionsLoading ? (
              <div className="flex justify-center py-8">
                <LoadingSpinner />
              </div>
            ) : recentTransactions && recentTransactions.length > 0 ? (
              <div className="space-y-4">
                {recentTransactions.map((transaction, index) => (
                  <motion.div
                    key={transaction.id}
                    className="flex items-center justify-between p-4 rounded-xl bg-white/60 hover:bg-white/80 border border-white/40 hover:border-white/60 transition-all duration-300 cursor-pointer shadow-sm hover:shadow-md"
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: index * 0.1 }}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => {
                      // TODO: Add transaction detail modal
                      console.log('Transaction clicked:', transaction);
                    }}
                  >
                    <div className="flex items-center">
                      <motion.div
                        className={`w-3 h-3 rounded-full mr-4 ${
                          transaction.is_suspicious ? 'bg-gradient-to-r from-red-500 to-pink-500' : 'bg-gradient-to-r from-emerald-500 to-teal-500'
                        }`}
                        animate={{ scale: [1, 1.2, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                      />
                      <div>
                        <p className="text-sm font-semibold text-slate-800">
                          {transaction.merchant || 'Unknown Merchant'}
                        </p>
                        <p className="text-xs text-slate-500">
                          {transaction.timestamp ? new Date(transaction.timestamp).toLocaleDateString() : 'No date'}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-bold text-slate-800">
                        ${transaction.amount?.toFixed(2) || '0.00'}
                      </p>
                      {transaction.is_suspicious && (
                        <span className="badge-danger">
                          Suspicious
                        </span>
                      )}
                    </div>
                  </motion.div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Activity className="h-12 w-12 text-slate-300 mx-auto mb-3" />
                <p className="text-slate-500">No recent transactions</p>
              </div>
            )}
          </div>
        </motion.div>

        {/* Recent Alerts */}
        <motion.div
          initial={{ opacity: 0, x: 30, scale: 0.95 }}
          animate={{ opacity: 1, x: 0, scale: 1 }}
          transition={{
            delay: 0.5,
            type: "spring",
            damping: 20,
            stiffness: 100
          }}
          className="card group"
          whileHover={{ scale: 1.02 }}
        >
          <div className="card-header">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="p-2 rounded-xl bg-gradient-to-br from-amber-500/20 to-orange-500/20">
                  <AlertTriangle className="h-6 w-6 text-amber-600" />
                </div>
                <h3 className="ml-3 text-xl font-semibold text-slate-800">Recent Alerts</h3>
              </div>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 3, repeat: Infinity, ease: "linear" }}
              >
                <div className="w-2 h-2 rounded-full bg-gradient-to-r from-amber-500 to-orange-500" />
              </motion.div>
            </div>
          </div>
          <div className="card-content">
            {alertsLoading ? (
              <div className="flex justify-center py-8">
                <LoadingSpinner />
              </div>
            ) : recentAlerts && recentAlerts.length > 0 ? (
              <div className="space-y-4">
                {recentAlerts.map((alert, index) => (
                  <motion.div
                    key={alert.id}
                    className="flex items-start justify-between p-4 rounded-xl bg-white/60 hover:bg-white/80 border border-white/40 hover:border-white/60 transition-all duration-300 cursor-pointer shadow-sm hover:shadow-md"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: index * 0.1 }}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => {
                      // TODO: Add alert detail modal
                      console.log('Alert clicked:', alert);
                    }}
                  >
                    <div className="flex-1">
                      <p className="text-sm font-semibold text-slate-800">{alert.title}</p>
                      <p className="text-xs text-slate-500 mt-1">{alert.description}</p>
                      <div className="flex items-center mt-3 space-x-2">
                        <span className={`badge ${
                          alert.severity === 'critical' ? 'badge-danger' :
                          alert.severity === 'high' ? 'badge-warning' :
                          alert.severity === 'medium' ? 'badge-warning' :
                          'badge-info'
                        }`}>
                          {alert.severity}
                        </span>
                        <span className={`badge ${
                          alert.status === 'open' ? 'badge-danger' :
                          alert.status === 'investigating' ? 'badge-warning' :
                          'badge-success'
                        }`}>
                          {alert.status}
                        </span>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-slate-500">
                        {new Date(alert.created_at).toLocaleDateString()}
                      </p>
                    </div>
                  </motion.div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <AlertTriangle className="h-12 w-12 text-slate-300 mx-auto mb-3" />
                <p className="text-slate-500">No recent alerts</p>
              </div>
            )}
          </div>
        </motion.div>
      </div>

      {/* Suspicious Transactions */}
      {suspiciousTransactions && suspiciousTransactions.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 30, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{
            delay: 0.6,
            type: "spring",
            damping: 20,
            stiffness: 100
          }}
          className="card group"
          whileHover={{ scale: 1.01 }}
        >
          <div className="card-header">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="p-2 rounded-xl bg-gradient-to-br from-red-500/20 to-pink-500/20">
                  <TrendingUp className="h-6 w-6 text-red-600" />
                </div>
                <h3 className="ml-3 text-xl font-semibold text-slate-800">High-Risk Transactions</h3>
              </div>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 3, repeat: Infinity, ease: "linear" }}
              >
                <div className="w-2 h-2 rounded-full bg-gradient-to-r from-red-500 to-pink-500" />
              </motion.div>
            </div>
          </div>
          <div className="card-content">
            <div className="overflow-x-auto custom-scrollbar">
              <table className="min-w-full">
                <thead>
                  <tr className="border-b border-white/20">
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Transaction
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Amount
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Risk Score
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Date
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/10">
                  {suspiciousTransactions.map((transaction, index) => (
                    <motion.tr
                      key={transaction.id}
                      className="hover:bg-white/40 transition-all duration-300 cursor-pointer border-b border-white/20 hover:border-white/40"
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                      whileHover={{ scale: 1.01 }}
                      whileTap={{ scale: 0.99 }}
                      onClick={() => {
                        // TODO: Add transaction detail modal
                        console.log('Suspicious transaction clicked:', transaction);
                      }}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-semibold text-slate-800">
                            {transaction.merchant || 'Unknown'}
                          </div>
                          <div className="text-sm text-slate-500">
                            {transaction.location || 'No location'}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-bold text-slate-800">
                          ${transaction.amount?.toFixed(2) || '0.00'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`badge ${
                          transaction.risk_score > 0.8 ? 'badge-danger' :
                          transaction.risk_score > 0.6 ? 'badge-warning' :
                          'badge-warning'
                        }`}>
                          {(transaction.risk_score * 100).toFixed(0)}%
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
                        {transaction.timestamp ? new Date(transaction.timestamp).toLocaleDateString() : 'No date'}
                      </td>
                    </motion.tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </motion.div>
      )}
    </div>
  )
}
