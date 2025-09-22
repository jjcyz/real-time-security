import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { motion } from 'framer-motion'
import { useDropzone } from 'react-dropzone'
import {
  Upload,
  FileText,
  Trash2,
  Play,
  CheckCircle,
  XCircle,
  Clock,
  AlertTriangle
} from 'lucide-react'
import { filesApi, UploadedFile } from '../services/api'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

export default function Files() {
  const [isUploading, setIsUploading] = useState(false)
  const queryClient = useQueryClient()

  const { data: files, isLoading } = useQuery<UploadedFile[]>(
    'files',
    filesApi.getFiles
  )

  const uploadMutation = useMutation(filesApi.uploadFile, {
    onSuccess: () => {
      queryClient.invalidateQueries('files')
      queryClient.invalidateQueries('dashboard-stats')
      toast.success('File uploaded successfully!')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.detail || 'Upload failed')
    },
    onSettled: () => {
      setIsUploading(false)
    }
  })

  const deleteMutation = useMutation(filesApi.deleteFile, {
    onSuccess: () => {
      queryClient.invalidateQueries('files')
      queryClient.invalidateQueries('dashboard-stats')
      toast.success('File deleted successfully!')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.detail || 'Delete failed')
    }
  })

  const analyzeMutation = useMutation(filesApi.analyzeFile, {
    onSuccess: () => {
      queryClient.invalidateQueries('files')
      queryClient.invalidateQueries('dashboard-stats')
      queryClient.invalidateQueries('recent-alerts')
      queryClient.invalidateQueries('suspicious-transactions')
      toast.success('Analysis completed!')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.detail || 'Analysis failed')
    }
  })

  const onDrop = async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0]
    if (!file) return

    if (!file.name.endsWith('.csv')) {
      toast.error('Only CSV files are allowed')
      return
    }

    setIsUploading(true)
    uploadMutation.mutate(file)
  }

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv']
    },
    multiple: false
  })

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-5 w-5 text-green-500" />
      case 'failed':
        return <XCircle className="h-5 w-5 text-red-500" />
      case 'processing':
        return <Clock className="h-5 w-5 text-yellow-500" />
      default:
        return <FileText className="h-5 w-5 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800'
      case 'failed':
        return 'bg-red-100 text-red-800'
      case 'processing':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

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
          File Management
        </h1>
        <p className="text-slate-600 text-lg">
          Upload and manage CSV files for fraud analysis
        </p>
      </motion.div>

      {/* Upload Area */}
      <motion.div
        initial={{ opacity: 0, y: 30, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{
          delay: 0.2,
          type: "spring",
          damping: 20,
          stiffness: 100
        }}
        className="card group"
        whileHover={{ scale: 1.01 }}
      >
        <div className="card-header">
          <div className="flex items-center">
            <div className="p-2 rounded-xl bg-gradient-to-br from-blue-500/20 to-cyan-500/20">
              <Upload className="h-6 w-6 text-blue-600" />
            </div>
            <h3 className="ml-3 text-xl font-semibold text-slate-800">Upload CSV File</h3>
          </div>
        </div>
        <div className="card-content">
          <motion.div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-2xl p-12 text-center cursor-pointer transition-all duration-300 ${
              isDragActive
                ? 'border-blue-500 bg-blue-500/10 scale-105'
                : 'border-slate-300 hover:border-blue-400 hover:bg-white/30'
            } ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
            whileHover={{ scale: isUploading ? 1 : 1.02 }}
            whileTap={{ scale: isUploading ? 1 : 0.98 }}
          >
            <input {...getInputProps()} disabled={isUploading} />
            <motion.div
              animate={{
                y: isDragActive ? -5 : 0,
                scale: isDragActive ? 1.1 : 1
              }}
              transition={{ duration: 0.2 }}
            >
              <Upload className="mx-auto h-16 w-16 text-slate-400" />
            </motion.div>
            <div className="mt-6">
              {isUploading ? (
                <div className="flex items-center justify-center">
                  <LoadingSpinner variant="glass" className="mr-3" />
                  <span className="text-sm text-slate-600 font-medium">Uploading...</span>
                </div>
              ) : isDragActive ? (
                <p className="text-lg text-blue-600 font-semibold">Drop the CSV file here...</p>
              ) : (
                <div>
                  <p className="text-lg text-slate-600 font-medium">
                    Drag and drop a CSV file here, or click to select
                  </p>
                  <p className="text-sm text-slate-500 mt-2">
                    Supports CSV files up to 10MB
                  </p>
                </div>
              )}
            </div>
          </motion.div>
        </div>
      </motion.div>

      {/* Files List */}
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
        <div className="card-header">
          <div className="flex items-center">
            <div className="p-2 rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/20">
              <FileText className="h-6 w-6 text-emerald-600" />
            </div>
            <h3 className="ml-3 text-xl font-semibold text-slate-800">Uploaded Files</h3>
          </div>
        </div>
        <div className="card-content">
          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner variant="glass" size="lg" />
            </div>
          ) : files && files.length > 0 ? (
            <div className="overflow-x-auto custom-scrollbar">
              <table className="min-w-full">
                <thead>
                  <tr className="border-b border-white/20">
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      File
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Rows
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Size
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Uploaded
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/10">
                  {files.map((file, index) => (
                    <motion.tr
                      key={file.id}
                      className="hover:bg-white/40 transition-all duration-300 cursor-pointer border-b border-white/20 hover:border-white/40"
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                      whileHover={{ scale: 1.01 }}
                      whileTap={{ scale: 0.99 }}
                      onClick={() => {
                        // TODO: Add file detail modal
                        console.log('File clicked:', file);
                      }}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <motion.div
                            animate={{ rotate: file.status === 'processing' ? 360 : 0 }}
                            transition={{ duration: 2, repeat: file.status === 'processing' ? Infinity : 0, ease: "linear" }}
                          >
                            {getStatusIcon(file.status)}
                          </motion.div>
                          <div className="ml-3">
                            <div className="text-sm font-semibold text-slate-800">
                              {file.original_filename}
                            </div>
                            {file.error_message && (
                              <div className="text-xs text-red-600 mt-1">
                                {file.error_message}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`badge ${getStatusColor(file.status).replace('bg-', 'badge-').replace('text-', '').replace('-', '-')}`}>
                          {file.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-800">
                        {file.processed_rows} / {file.total_rows}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-800">
                        {formatFileSize(file.file_size)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
                        {new Date(file.created_at).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        {file.status === 'completed' && (
                          <motion.button
                            onClick={() => analyzeMutation.mutate(file.id)}
                            disabled={analyzeMutation.isLoading}
                            className="text-blue-600 hover:text-blue-800 disabled:opacity-50 p-2 rounded-lg hover:bg-blue-100 transition-all duration-300"
                            whileHover={{ scale: 1.1 }}
                            whileTap={{ scale: 0.9 }}
                          >
                            <Play className="h-4 w-4" />
                          </motion.button>
                        )}
                        <motion.button
                          onClick={() => {
                            if (window.confirm('Are you sure you want to delete this file?')) {
                              deleteMutation.mutate(file.id)
                            }
                          }}
                          disabled={deleteMutation.isLoading}
                          className="text-red-600 hover:text-red-800 disabled:opacity-50 p-2 rounded-lg hover:bg-red-100 transition-all duration-300"
                          whileHover={{ scale: 1.1 }}
                          whileTap={{ scale: 0.9 }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </motion.button>
                      </td>
                    </motion.tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12">
              <FileText className="mx-auto h-16 w-16 text-slate-300 mb-4" />
              <h3 className="text-lg font-semibold text-slate-800">No files uploaded</h3>
              <p className="mt-2 text-slate-500">
                Get started by uploading a CSV file above.
              </p>
            </div>
          )}
        </div>
      </motion.div>
    </div>
  )
}
