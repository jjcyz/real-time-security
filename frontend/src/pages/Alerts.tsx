import { motion } from 'framer-motion'
import { AlertTriangle } from 'lucide-react'

export default function Alerts() {
  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="bg-white rounded-lg shadow-sm p-6"
        >
          <div className="flex items-center gap-3 mb-6">
            <AlertTriangle className="h-6 w-6 text-red-600" />
            <h1 className="text-2xl font-bold text-gray-900">Security Alerts</h1>
          </div>

          <div className="text-center py-12">
            <AlertTriangle className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              Alert System Coming Soon
            </h3>
            <p className="text-gray-500 max-w-md mx-auto">
              Advanced alert management and fraud detection alerts will be available in a future update.
              For now, you can view suspicious transactions in the dashboard.
            </p>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
