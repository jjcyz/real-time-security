import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  AlertTriangle,
  Shield,
  Activity,
  Clock,
  DollarSign
} from 'lucide-react'
import { dashboardApi, Transaction } from '../services/api'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Dashboard() {
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: dashboardApi.getStats,
    refetchInterval: 300000, // 5 minutes instead of 30 seconds
    refetchOnWindowFocus: false, // Don't refetch when window gains focus
    staleTime: 120000, // Consider data fresh for 2 minutes
    gcTime: 600000 // Keep in cache for 10 minutes
  })

  const { data: recentTransactions, isLoading: transactionsLoading } = useQuery<Transaction[]>({
    queryKey: ['recent-transactions'],
    queryFn: () => dashboardApi.getRecentTransactions(5),
    refetchOnWindowFocus: false,
    staleTime: 60000, // Consider data fresh for 1 minute
    gcTime: 300000 // Keep in cache for 5 minutes
  })

  const { data: suspiciousTransactions, isLoading: suspiciousLoading } = useQuery<Transaction[]>({
    queryKey: ['suspicious-transactions'],
    queryFn: () => dashboardApi.getSuspiciousTransactions(10),
    refetchOnWindowFocus: false,
    staleTime: 60000, // Consider data fresh for 1 minute
    gcTime: 300000 // Keep in cache for 5 minutes
  })

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
      value: (stats as any)?.total_transactions || 0,
      icon: DollarSign,
      gradient: 'from-blue-500 to-cyan-500',
      change: '+12%'
    },
    {
      title: 'Suspicious Transactions',
      value: (stats as any)?.suspicious_transactions || 0,
      icon: AlertTriangle,
      gradient: 'from-red-500 to-pink-500',
      change: '+3%'
    },
    {
      title: 'Security Score',
      value: '95%',
      icon: Shield,
      gradient: 'from-green-500 to-emerald-500',
      change: '+2%'
    },
    {
      title: 'Active Monitoring',
      value: '24/7',
      icon: Activity,
      gradient: 'from-purple-500 to-violet-500',
      change: 'Active'
    }
  ]

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, ease: "easeOut" }}
        >
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Security Dashboard</h1>
          <p className="text-gray-600">Monitor transactions and security metrics in real-time</p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {statCards.map((card, index) => (
            <motion.div
              key={card.title}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.2, delay: index * 0.05 }}
              className="bg-white rounded-lg shadow-sm p-6"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">{card.title}</p>
                  <p className="text-2xl font-bold text-gray-900">{card.value}</p>
                  <p className="text-sm text-green-600">{card.change}</p>
                </div>
                <div className={`p-3 rounded-lg bg-gradient-to-r ${card.gradient}`}>
                  <card.icon className="h-6 w-6 text-white" />
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.2 }}
            className="bg-white rounded-lg shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-4">
              <Clock className="h-5 w-5 text-blue-600" />
              <h2 className="text-lg font-semibold text-gray-900">Recent Transactions</h2>
            </div>
            {transactionsLoading ? (
              <LoadingSpinner />
            ) : recentTransactions && recentTransactions.length > 0 ? (
              <div className="space-y-3">
                {recentTransactions.map((transaction: Transaction) => (
                  <div key={transaction.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <div>
                      <p className="font-medium text-gray-900">
                        {transaction.merchant || 'Unknown Merchant'}
                      </p>
                      <p className="text-sm text-gray-600">
                        {transaction.amount ? `$${transaction.amount}` : 'N/A'} • {transaction.currency}
                      </p>
                    </div>
                    <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                      transaction.isFraudulent
                        ? 'bg-red-100 text-red-800'
                        : 'bg-green-100 text-green-800'
                    }`}>
                      {transaction.isFraudulent ? 'Suspicious' : 'Normal'}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">No recent transactions</p>
            )}
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.25 }}
            className="bg-white rounded-lg shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-4">
              <AlertTriangle className="h-5 w-5 text-red-600" />
              <h2 className="text-lg font-semibold text-gray-900">Suspicious Activity</h2>
            </div>
            {suspiciousLoading ? (
              <LoadingSpinner />
            ) : suspiciousTransactions && suspiciousTransactions.length > 0 ? (
              <div className="space-y-3">
                {suspiciousTransactions.map((transaction: Transaction) => (
                  <div key={transaction.id} className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
                    <div>
                      <p className="font-medium text-gray-900">
                        {transaction.merchant || 'Unknown Merchant'}
                      </p>
                      <p className="text-sm text-gray-600">
                        {transaction.amount ? `$${transaction.amount}` : 'N/A'} • {transaction.currency}
                      </p>
                    </div>
                    <div className="px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                      Risk: {transaction.fraud_score || 'High'}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">No suspicious activity detected</p>
            )}
          </motion.div>
        </div>
      </div>
    </div>
  )
}
