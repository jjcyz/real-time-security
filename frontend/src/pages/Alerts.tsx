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
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Security Alerts</h1>
        <p className="mt-1 text-sm text-gray-500">
          Monitor and manage fraud detection alerts
        </p>
      </div>

      {/* Stats */}
      {alertStats && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4"
        >
          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <div className="flex-shrink-0 p-3 rounded-md bg-red-100">
                  <AlertTriangle className="h-6 w-6 text-red-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Total Alerts</p>
                  <p className="text-2xl font-semibold text-gray-900">{alertStats.total_alerts}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <div className="flex-shrink-0 p-3 rounded-md bg-yellow-100">
                  <Clock className="h-6 w-6 text-yellow-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Open</p>
                  <p className="text-2xl font-semibold text-gray-900">{alertStats.by_status?.open || 0}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <div className="flex-shrink-0 p-3 rounded-md bg-green-100">
                  <CheckCircle className="h-6 w-6 text-green-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Resolved</p>
                  <p className="text-2xl font-semibold text-gray-900">{alertStats.by_status?.resolved || 0}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <div className="flex-shrink-0 p-3 rounded-md bg-gray-100">
                  <XCircle className="h-6 w-6 text-gray-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">False Positives</p>
                  <p className="text-2xl font-semibold text-gray-900">{alertStats.by_status?.false_positive || 0}</p>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      )}

      {/* Filters */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="card"
      >
        <div className="card-content">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search alerts..."
                  value={filters.search}
                  onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                  className="input pl-10"
                />
              </div>
            </div>
            <div className="flex gap-2">
              <select
                value={filters.status}
                onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                className="input"
              >
                <option value="">All Status</option>
                <option value="open">Open</option>
                <option value="investigating">Investigating</option>
                <option value="resolved">Resolved</option>
                <option value="false_positive">False Positive</option>
              </select>
              <select
                value={filters.severity}
                onChange={(e) => setFilters(prev => ({ ...prev, severity: e.target.value }))}
                className="input"
              >
                <option value="">All Severity</option>
                <option value="critical">Critical</option>
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </select>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Alerts List */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="card"
      >
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Alerts</h3>
        </div>
        <div className="card-content">
          {isLoading ? (
            <div className="flex justify-center py-8">
              <LoadingSpinner size="lg" />
            </div>
          ) : filteredAlerts && filteredAlerts.length > 0 ? (
            <div className="space-y-4">
              {filteredAlerts.map((alert) => (
                <div
                  key={alert.id}
                  className={`border rounded-lg p-4 cursor-pointer transition-colors hover:bg-gray-50 ${
                    selectedAlert?.id === alert.id ? 'ring-2 ring-primary-500' : ''
                  }`}
                  onClick={() => setSelectedAlert(alert)}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <h4 className="text-sm font-medium text-gray-900">{alert.title}</h4>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getSeverityColor(alert.severity)}`}>
                          {alert.severity}
                        </span>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(alert.status)}`}>
                          {getStatusIcon(alert.status)}
                          <span className="ml-1">{alert.status.replace('_', ' ')}</span>
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">{alert.description}</p>
                      <div className="flex items-center space-x-4 text-xs text-gray-500">
                        <span>Rule: {alert.rule_name}</span>
                        <span>Confidence: {(alert.confidence_score * 100).toFixed(0)}%</span>
                        <span>Risk: {(alert.risk_score * 100).toFixed(0)}%</span>
                        <span>{new Date(alert.created_at).toLocaleDateString()}</span>
                      </div>
                    </div>
                    {alert.status === 'open' && (
                      <div className="flex space-x-2 ml-4">
                        <button
                          onClick={(e) => {
                            e.stopPropagation()
                            resolveMutation.mutate(alert.id)
                          }}
                          disabled={resolveMutation.isLoading}
                          className="btn-outline text-xs"
                        >
                          Resolve
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation()
                            falsePositiveMutation.mutate(alert.id)
                          }}
                          disabled={falsePositiveMutation.isLoading}
                          className="btn-secondary text-xs"
                        >
                          False Positive
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <AlertTriangle className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No alerts found</h3>
              <p className="mt-1 text-sm text-gray-500">
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
