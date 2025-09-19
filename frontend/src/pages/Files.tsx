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
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">File Management</h1>
        <p className="mt-1 text-sm text-gray-500">
          Upload and manage CSV files for fraud analysis
        </p>
      </div>

      {/* Upload Area */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="card"
      >
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Upload CSV File</h3>
        </div>
        <div className="card-content">
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
              isDragActive
                ? 'border-primary-500 bg-primary-50'
                : 'border-gray-300 hover:border-gray-400'
            } ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            <input {...getInputProps()} disabled={isUploading} />
            <Upload className="mx-auto h-12 w-12 text-gray-400" />
            <div className="mt-4">
              {isUploading ? (
                <div className="flex items-center justify-center">
                  <LoadingSpinner className="mr-2" />
                  <span className="text-sm text-gray-600">Uploading...</span>
                </div>
              ) : isDragActive ? (
                <p className="text-sm text-primary-600">Drop the CSV file here...</p>
              ) : (
                <div>
                  <p className="text-sm text-gray-600">
                    Drag and drop a CSV file here, or click to select
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    Supports CSV files up to 10MB
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </motion.div>

      {/* Files List */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="card"
      >
        <div className="card-header">
          <h3 className="text-lg font-medium text-gray-900">Uploaded Files</h3>
        </div>
        <div className="card-content">
          {isLoading ? (
            <div className="flex justify-center py-8">
              <LoadingSpinner size="lg" />
            </div>
          ) : files && files.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      File
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Rows
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Size
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Uploaded
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {files.map((file) => (
                    <tr key={file.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          {getStatusIcon(file.status)}
                          <div className="ml-3">
                            <div className="text-sm font-medium text-gray-900">
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
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(file.status)}`}>
                          {file.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {file.processed_rows} / {file.total_rows}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {formatFileSize(file.file_size)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(file.created_at).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        {file.status === 'completed' && (
                          <button
                            onClick={() => analyzeMutation.mutate(file.id)}
                            disabled={analyzeMutation.isLoading}
                            className="text-primary-600 hover:text-primary-900 disabled:opacity-50"
                          >
                            <Play className="h-4 w-4" />
                          </button>
                        )}
                        <button
                          onClick={() => {
                            if (window.confirm('Are you sure you want to delete this file?')) {
                              deleteMutation.mutate(file.id)
                            }
                          }}
                          disabled={deleteMutation.isLoading}
                          className="text-red-600 hover:text-red-900 disabled:opacity-50"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-8">
              <FileText className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No files uploaded</h3>
              <p className="mt-1 text-sm text-gray-500">
                Get started by uploading a CSV file above.
              </p>
            </div>
          )}
        </div>
      </motion.div>
    </div>
  )
}
