import { ref, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { uploadContractFile, generateContractPdf } from '@/api/contract'
import type { ContractAttachmentDraft, ContractFormState } from '@/views/contract/contractFormTypes'

export interface GeneratedPdfState {
  name: string
  url: string
  blob?: Blob | null
  serverUrl?: string
}

export function useContractFormFileActions(
  form: ContractFormState,
  attachments: Ref<ContractAttachmentDraft[]>
) {
  const { t } = useI18n()

  const uploadedFile = ref<UploadFile | null>(null)
  const uploadedFileUrl = ref('')
  const pdfGenerating = ref(false)
  const generatedPdf = ref<GeneratedPdfState | null>(null)

  const handleFileUpload = async (file: UploadFile) => {
    uploadedFile.value = file
    const ext = file.name.split('.').pop()?.toLowerCase()

    if (ext && ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
      const reader = new FileReader()
      reader.onload = (e) => {
        uploadedFileUrl.value = e.target?.result as string
        form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><img src="${uploadedFileUrl.value}" style="max-width: 100%;" /></div>`
      }
      reader.readAsDataURL(file.raw!)
    } else if (ext && ['pdf'].includes(ext)) {
      const reader = new FileReader()
      reader.onload = (e) => {
        uploadedFileUrl.value = e.target?.result as string
        form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><iframe src="${uploadedFileUrl.value}" style="width: 100%; height: 600px; border: none;"></iframe></div>`
      }
      reader.readAsDataURL(file.raw!)
    } else {
      try {
        const res = await uploadContractFile(file.raw!)
        const fileUrl = res.data?.fileUrl || `/api/contracts/download/${res.data?.fileName}`
        form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><div class="file-placeholder"><p>${t('contract.uploadedFileHint')} <a href="${fileUrl}" target="_blank">${file.name}</a></p></div></div>`
        uploadedFileUrl.value = fileUrl
      } catch {
        ElMessage.error(t('common.error'))
        form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><div class="file-placeholder"><p>文件上传失败，请重试</p></div></div>`
      }
    }
    return false
  }

  const clearUpload = () => {
    uploadedFile.value = null
    uploadedFileUrl.value = ''
    form.content = ''
  }

  const handleAttachmentChange = async (file: UploadFile) => {
    try {
      const res = await uploadContractFile(file.raw!)
      const fileUrl = res.data?.fileUrl || `/api/contracts/download/${res.data?.fileName}`
      attachments.value.push({
        uid: file.uid || Date.now(),
        name: file.name,
        fileName: file.name,
        url: fileUrl,
        fileUrl: fileUrl,
        size: file.size,
        fileSize: file.size,
        type: file.raw?.type,
        fileCategory: 'support',
        uploadTime: new Date().toISOString()
      })
    } catch {
      ElMessage.error(t('contract.uploadFailed'))
    }
  }

  const handleAttachmentRemove = (file: UploadFile) => {
    const idx = attachments.value.findIndex((a) => a.uid === file.uid)
    if (idx !== -1) attachments.value.splice(idx, 1)
  }

  const removeAttachment = (index: number) => {
    attachments.value.splice(index, 1)
  }

  const generatePdf = async () => {
    if (!form.content || form.content === '<p><br></p>') {
      ElMessage.error(t('contract.error.contentRequired'))
      return
    }
    pdfGenerating.value = true
    try {
      const blob = await generateContractPdf({
        content: form.content,
        contractNo: form.contractNo,
        watermark: form.contractNo || 'CONFIDENTIAL'
      })
      const fileName = `${form.contractNo || 'contract'}.pdf`

      const url = URL.createObjectURL(blob)
      if (generatedPdf.value?.url) {
        URL.revokeObjectURL(generatedPdf.value.url)
      }
      generatedPdf.value = {
        name: fileName,
        url,
        blob
      }

      const file = new File([blob], fileName, { type: 'application/pdf' })
      try {
        const uploadRes = await uploadContractFile(file)
        const fileUrl = uploadRes.data?.fileUrl || `/api/contracts/download/${uploadRes.data?.fileName}`
        generatedPdf.value.serverUrl = fileUrl
      } catch (uploadError) {
        console.error('Failed to upload PDF to server:', uploadError)
      }

      ElMessage.success(t('common.success'))
    } catch {
      ElMessage.error(t('common.error'))
    } finally {
      pdfGenerating.value = false
    }
  }

  const removeGeneratedPdf = () => {
    if (generatedPdf.value?.url) {
      URL.revokeObjectURL(generatedPdf.value.url)
    }
    generatedPdf.value = null
  }

  const handleDownloadGeneratedPdf = () => {
    if (!generatedPdf.value) return

    const name = generatedPdf.value.name
    const url = generatedPdf.value.serverUrl || generatedPdf.value.url
    const blob = generatedPdf.value.blob

    if (blob) {
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    } else if (url) {
      fetch(url, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error(t('common.error'))
          }
          return response.blob()
        })
        .then((blob) => {
          const getContentType = (filename: string) => {
            const ext = filename.split('.').pop()?.toLowerCase()
            const mimeTypes: Record<string, string> = {
              pdf: 'application/pdf',
              doc: 'application/msword',
              docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
              xls: 'application/vnd.ms-excel',
              xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
              jpg: 'image/jpeg',
              jpeg: 'image/jpeg',
              png: 'image/png',
              gif: 'image/gif',
              txt: 'text/plain'
            }
            return mimeTypes[ext || ''] || 'application/octet-stream'
          }

          const fileBlob = new Blob([blob], { type: getContentType(name) })
          const downloadUrl = window.URL.createObjectURL(fileBlob)
          const link = document.createElement('a')
          link.href = downloadUrl
          link.download = name
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(downloadUrl)
        })
        .catch((error) => {
          console.error('下载失败:', error)
          const link = document.createElement('a')
          link.href = url
          link.download = name
          link.click()
        })
    }
  }

  return {
    uploadedFile,
    uploadedFileUrl,
    pdfGenerating,
    generatedPdf,
    handleFileUpload,
    clearUpload,
    handleAttachmentChange,
    handleAttachmentRemove,
    removeAttachment,
    generatePdf,
    removeGeneratedPdf,
    handleDownloadGeneratedPdf
  }
}
