import { motion } from 'framer-motion'
import { FileText, Upload } from 'lucide-react'

export default function Files() {
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
            <FileText className="h-6 w-6 text-blue-600" />
            <h1 className="text-2xl font-bold text-gray-900">File Management</h1>
          </div>

          <div className="text-center py-12">
            <Upload className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              File Upload Coming Soon
            </h3>
            <p className="text-gray-500 max-w-md mx-auto">
              File upload and analysis functionality will be available in a future update.
              For now, you can manage transactions directly through the API.
            </p>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
