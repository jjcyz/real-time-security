import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { motion } from 'framer-motion'
import {
  AlertTriangle,
  CheckCircle,
  XCircle,
  Clock,
  Filter,
  Search
} from 'lucide-react'
import { alertsApi, Alert } from '../services/api'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

export default function Alerts() {
  const [filters, setFilters] = useState({
    status: '',
    severity: '',
    search: ''
  })
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null)
  const queryClient = useQueryClient()

  const { data: alerts, isLoading } = useQuery<Alert[]>(
    ['alerts', filters],
    () => alertsApi.getAlerts({
      status: filters.status || undefined,
      severity: filters.severity || undefined,
      limit: 50
    })
  )

  const { data: alertStats } = useQuery(
    'alert-stats',
    alertsApi.getAlertStats
  )

  const resolveMutation = useMutation(alertsApi.resolveAlert, {
    onSuccess: () => {
      queryClient.invalidateQueries('alerts')
      queryClient.invalidateQueries('alert-stats')
      queryClient.invalidateQueries('dashboard-stats')
      toast.success('Alert resolved successfully!')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.detail || 'Failed to resolve alert')
    }
  })

  const falsePositiveMutation = useMutation(alertsApi.markFalsePositive, {
    onSuccess: () => {
      queryClient.invalidateQueries('alerts')
      queryClient.invalidateQueries('alert-stats')
      queryClient.invalidateQueries('dashboard-stats')
      toast.success('Alert marked as false positive!')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.detail || 'Failed to mark as false positive')
    }
  })

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'critical':
        return 'bg-red-100 text-red-800 border-red-200'
      case 'high':
        return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'medium':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'low':
        return 'bg-blue-100 text-blue-800 border-blue-200'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'open':
        return 'bg-red-100 text-red-800'
      case 'investigating':
        return 'bg-yellow-100 text-yellow-800'
      case 'resolved':
        return 'bg-green-100 text-green-800'
      case 'false_positive':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'open':
        return <AlertTriangle className="h-4 w-4" />
      case 'investigating':
        return <Clock className="h-4 w-4" />
      case 'resolved':
        return <CheckCircle className="h-4 w-4" />
      case 'false_positive':
        return <XCircle className="h-4 w-4" />
      default:
        return <AlertTriangle className="h-4 w-4" />
    }
  }

  const filteredAlerts = alerts?.filter(alert => {
    if (filters.search) {
      const searchLower = filters.search.toLowerCase()
      return (
        alert.title.toLowerCase().includes(searchLower) ||
        alert.description.toLowerCase().includes(searchLower) ||
        alert.rule_name.toLowerCase().includes(searchLower)
      )
    }
    return true
  })

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
          Security Alerts
        </h1>
        <p className="text-slate-600 text-lg">
          Monitor and manage fraud detection alerts
        </p>
      </motion.div>

      {/* Stats */}
      {alertStats && (
        <motion.div
          initial={{ opacity: 0, y: 30, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{
            delay: 0.2,
            type: "spring",
            damping: 20,
            stiffness: 100
          }}
          className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4"
        >
          <motion.div
            className="stat-card group"
            whileHover={{ y: -5 }}
            transition={{ duration: 0.2 }}
          >
            <div className="card-content">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="stat-icon bg-gradient-to-br from-red-500/20 to-pink-500/20">
                    <AlertTriangle className="h-8 w-8 bg-gradient-to-r from-red-500 to-pink-500 bg-clip-text text-transparent" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-slate-600">Total Alerts</p>
                    <motion.p
                      className="text-3xl font-bold text-red-700"
                      initial={{ scale: 0.5 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.3, type: "spring", damping: 15 }}
                    >
                      {alertStats.total_alerts}
                    </motion.p>
                  </div>
                </div>
                <motion.div
                  className="opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                >
                  <div className="w-3 h-3 rounded-full bg-gradient-to-r from-red-500 to-pink-500" />
                </motion.div>
              </div>
            </div>
          </motion.div>

          <motion.div
            className="stat-card group"
            whileHover={{ y: -5 }}
            transition={{ duration: 0.2 }}
          >
            <div className="card-content">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="stat-icon bg-gradient-to-br from-amber-500/20 to-orange-500/20">
                    <Clock className="h-8 w-8 bg-gradient-to-r from-amber-500 to-orange-500 bg-clip-text text-transparent" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-slate-600">Open</p>
                    <motion.p
                      className="text-3xl font-bold text-amber-700"
                      initial={{ scale: 0.5 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.4, type: "spring", damping: 15 }}
                    >
                      {alertStats.by_status?.open || 0}
                    </motion.p>
                  </div>
                </div>
                <motion.div
                  className="opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                >
                  <div className="w-3 h-3 rounded-full bg-gradient-to-r from-amber-500 to-orange-500" />
                </motion.div>
              </div>
            </div>
          </motion.div>

          <motion.div
            className="stat-card group"
            whileHover={{ y: -5 }}
            transition={{ duration: 0.2 }}
          >
            <div className="card-content">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="stat-icon bg-gradient-to-br from-emerald-500/20 to-teal-500/20">
                    <CheckCircle className="h-8 w-8 bg-gradient-to-r from-emerald-500 to-teal-500 bg-clip-text text-transparent" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-slate-600">Resolved</p>
                    <motion.p
                      className="text-3xl font-bold text-emerald-700"
                      initial={{ scale: 0.5 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.5, type: "spring", damping: 15 }}
                    >
                      {alertStats.by_status?.resolved || 0}
                    </motion.p>
                  </div>
                </div>
                <motion.div
                  className="opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                >
                  <div className="w-3 h-3 rounded-full bg-gradient-to-r from-emerald-500 to-teal-500" />
                </motion.div>
              </div>
            </div>
          </motion.div>

          <motion.div
            className="stat-card group"
            whileHover={{ y: -5 }}
            transition={{ duration: 0.2 }}
          >
            <div className="card-content">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="stat-icon bg-gradient-to-br from-slate-500/20 to-gray-500/20">
                    <XCircle className="h-8 w-8 bg-gradient-to-r from-slate-500 to-gray-500 bg-clip-text text-transparent" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-slate-600">False Positives</p>
                    <motion.p
                      className="text-3xl font-bold text-slate-700"
                      initial={{ scale: 0.5 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.6, type: "spring", damping: 15 }}
                    >
                      {alertStats.by_status?.false_positive || 0}
                    </motion.p>
                  </div>
                </div>
                <motion.div
                  className="opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                >
                  <div className="w-3 h-3 rounded-full bg-gradient-to-r from-slate-500 to-gray-500" />
                </motion.div>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}

      {/* Filters */}
      <motion.div
        initial={{ opacity: 0, y: 30, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{
          delay: 0.3,
          type: "spring",
          damping: 20,
          stiffness: 100
        }}
        className="card group"
        whileHover={{ scale: 1.01 }}
      >
        <div className="card-content">
          <div className="flex flex-col sm:flex-row gap-6">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
                <motion.input
                  type="text"
                  placeholder="Search alerts..."
                  value={filters.search}
                  onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                  className="input pl-12 h-14 text-lg"
                  whileFocus={{ scale: 1.02 }}
                />
              </div>
            </div>
            <div className="flex gap-3">
              <motion.select
                value={filters.status}
                onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                className="input h-14 text-lg"
                whileFocus={{ scale: 1.02 }}
              >
                <option value="">All Status</option>
                <option value="open">Open</option>
                <option value="investigating">Investigating</option>
                <option value="resolved">Resolved</option>
                <option value="false_positive">False Positive</option>
              </motion.select>
              <motion.select
                value={filters.severity}
                onChange={(e) => setFilters(prev => ({ ...prev, severity: e.target.value }))}
                className="input h-14 text-lg"
                whileFocus={{ scale: 1.02 }}
              >
                <option value="">All Severity</option>
                <option value="critical">Critical</option>
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </motion.select>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Alerts List */}
      <motion.div
        initial={{ opacity: 0, y: 30, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{
          delay: 0.4,
          type: "spring",
          damping: 20,
          stiffness: 100
        }}
        className="card group"
        whileHover={{ scale: 1.01 }}
      >
        <div className="card-header">
          <div className="flex items-center">
            <div className="p-2 rounded-xl bg-gradient-to-br from-amber-500/20 to-orange-500/20">
              <AlertTriangle className="h-6 w-6 text-amber-600" />
            </div>
            <h3 className="ml-3 text-xl font-semibold text-slate-800">Alerts</h3>
          </div>
        </div>
        <div className="card-content">
          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner variant="glass" size="lg" />
            </div>
          ) : filteredAlerts && filteredAlerts.length > 0 ? (
            <div className="space-y-4">
              {filteredAlerts.map((alert, index) => (
                <motion.div
                  key={alert.id}
                  className={`rounded-2xl p-6 cursor-pointer transition-all duration-300 bg-white/70 hover:bg-white/90 border border-white/50 hover:border-white/70 shadow-md hover:shadow-lg ${
                    selectedAlert?.id === alert.id ? 'ring-2 ring-blue-500 bg-white/90 border-blue-300' : ''
                  }`}
                  onClick={() => setSelectedAlert(alert)}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-3">
                        <h4 className="text-lg font-semibold text-slate-800">{alert.title}</h4>
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
                          alert.status === 'resolved' ? 'badge-success' :
                          'badge-info'
                        }`}>
                          {getStatusIcon(alert.status)}
                          <span className="ml-1">{alert.status.replace('_', ' ')}</span>
                        </span>
                      </div>
                      <p className="text-slate-600 mb-3">{alert.description}</p>
                      <div className="flex items-center space-x-6 text-sm text-slate-500">
                        <span className="font-medium">Rule: {alert.rule_name}</span>
                        <span className="font-medium">Confidence: {(alert.confidence_score * 100).toFixed(0)}%</span>
                        <span className="font-medium">Risk: {(alert.risk_score * 100).toFixed(0)}%</span>
                        <span className="font-medium">{new Date(alert.created_at).toLocaleDateString()}</span>
                      </div>
                    </div>
                    {alert.status === 'open' && (
                      <div className="flex space-x-3 ml-6">
                        <motion.button
                          onClick={(e) => {
                            e.stopPropagation()
                            resolveMutation.mutate(alert.id)
                          }}
                          disabled={resolveMutation.isLoading}
                          className="btn-outline text-sm px-4 py-2"
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          Resolve
                        </motion.button>
                        <motion.button
                          onClick={(e) => {
                            e.stopPropagation()
                            falsePositiveMutation.mutate(alert.id)
                          }}
                          disabled={falsePositiveMutation.isLoading}
                          className="btn-secondary text-sm px-4 py-2"
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          False Positive
                        </motion.button>
                      </div>
                    )}
                  </div>
                </motion.div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <AlertTriangle className="mx-auto h-16 w-16 text-slate-300 mb-4" />
              <h3 className="text-lg font-semibold text-slate-800">No alerts found</h3>
              <p className="mt-2 text-slate-500">
                {filters.search || filters.status || filters.severity
                  ? 'Try adjusting your filters.'
                  : 'Upload and analyze CSV files to generate alerts.'}
              </p>
            </div>
          )}
        </div>
      </motion.div>

      {/* Alert Details Modal */}
      {selectedAlert && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-900">{selectedAlert.title}</h3>
                <button
                  onClick={() => setSelectedAlert(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <XCircle className="h-6 w-6" />
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <p className="text-sm text-gray-600">{selectedAlert.description}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs font-medium text-gray-500">Severity</label>
                    <p className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getSeverityColor(selectedAlert.severity)}`}>
                      {selectedAlert.severity}
                    </p>
                  </div>
                  <div>
                    <label className="text-xs font-medium text-gray-500">Status</label>
                    <p className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(selectedAlert.status)}`}>
                      {getStatusIcon(selectedAlert.status)}
                      <span className="ml-1">{selectedAlert.status.replace('_', ' ')}</span>
                    </p>
                  </div>
                  <div>
                    <label className="text-xs font-medium text-gray-500">Rule</label>
                    <p className="text-sm text-gray-900">{selectedAlert.rule_name}</p>
                  </div>
                  <div>
                    <label className="text-xs font-medium text-gray-500">Confidence</label>
                    <p className="text-sm text-gray-900">{(selectedAlert.confidence_score * 100).toFixed(0)}%</p>
                  </div>
                </div>

                {selectedAlert.metadata && (
                  <div>
                    <label className="text-xs font-medium text-gray-500">Additional Details</label>
                    <pre className="text-xs text-gray-600 bg-gray-50 p-2 rounded mt-1 overflow-auto">
                      {JSON.stringify(selectedAlert.metadata, null, 2)}
                    </pre>
                  </div>
                )}

                <div className="flex justify-end space-x-2 pt-4 border-t">
                  <button
                    onClick={() => setSelectedAlert(null)}
                    className="btn-secondary"
                  >
                    Close
                  </button>
                  {selectedAlert.status === 'open' && (
                    <>
                      <button
                        onClick={() => {
                          resolveMutation.mutate(selectedAlert.id)
                          setSelectedAlert(null)
                        }}
                        disabled={resolveMutation.isLoading}
                        className="btn-primary"
                      >
                        Resolve
                      </button>
                      <button
                        onClick={() => {
                          falsePositiveMutation.mutate(selectedAlert.id)
                          setSelectedAlert(null)
                        }}
                        disabled={falsePositiveMutation.isLoading}
                        className="btn-outline"
                      >
                        Mark False Positive
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
