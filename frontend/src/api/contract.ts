import { get, post, put, del, download } from './index'

export interface Contract {
  id?: number
  contractNo: string
  title: string
  type: string
  counterparty: string
  amount: number
  startDate: string
  endDate: string
  status: string
  content?: string
  attachment?: string
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

export interface ContractQuery {
  page?: number
  pageSize?: number
  title?: string
  type?: string
  status?: string
  counterparty?: string
  contractNo?: string
  startDateFrom?: string
  startDateTo?: string
  endDateFrom?: string
  endDateTo?: string
  amountMin?: number
  amountMax?: number
  keyword?: string
  creatorName?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

// 合同列表
export const getContractList = (params: ContractQuery) => 
  get('/contracts', { params })

// 合同详情
export const getContract = (id: number) => 
  get(`/contracts/${id}`)

// 创建合同
export const createContract = (data: Partial<Contract>) => 
  post('/contracts', data)

// 更新合同
export const updateContract = (id: number, data: Partial<Contract>) => 
  put(`/contracts/${id}`, data)

// 删除合同
export const deleteContract = (id: number) => 
  del(`/contracts/${id}`)

// 复制合同
export const copyContract = (id: number) => 
  post(`/contracts/${id}/copy`)

// 批量删除合同
export const batchDeleteContracts = (ids: number[]) => 
  post('/contracts/batch-delete', { ids })

// 提交审批
export const submitContract = (id: number) => 
  post(`/contracts/${id}/submit`)

// 审批通过
export const approveContract = (id: number, comment?: string) => 
  post(`/contracts/${id}/approve`, { comment })

// 审批拒绝
export const rejectContract = (id: number, comment: string) => 
  post(`/contracts/${id}/reject`, { comment })

// 签署合同
export const signContract = (id: number) => 
  post(`/contracts/${id}/sign`)

// 归档合同
export const archiveContract = (id: number) => 
  post(`/contracts/${id}/archive`)

// 终止合同
export const terminateContract = (id: number) => 
  post(`/contracts/${id}/terminate`)

// 下载PDF
export const downloadContractPdf = (id: number) => 
  download(`/contracts/${id}/pdf`, `合同_${id}.pdf`)

// AI分析合同
export const analyzeContract = (id: number, aiConfig?: any) => 
  post(`/contracts/${id}/analyze`, aiConfig || {})

// 上传合同附件
export const uploadContractFile = (file: File, contractId?: number) => {
  const formData = new FormData()
  formData.append('file', file)
  if (contractId) {
    formData.append('contractId', contractId.toString())
  }
  return post('/contracts/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除合同附件
export const deleteContractAttachment = (fileName: string) => {
  return del(`/contracts/attachments/${encodeURIComponent(fileName)}`)
}

// 批量更新状态
export const batchUpdateStatus = (ids: number[], status: string) =>
  post('/contracts/batch-status', { ids, status })

// 批量审批
export const batchApprove = (ids: number[]) =>
  post('/contracts/batch-approve', { ids })

// 批量提交审批
export const batchSubmit = (ids: number[]) =>
  post('/contracts/batch-submit', { ids })

// 批量编辑
export const batchEdit = (ids: number[], data: { type?: string; counterparty?: string; remark?: string }) =>
  post('/contracts/batch-edit', { ids, ...data })

// 获取审批历史
export const getApprovalHistory = (contractId: number) =>
  get(`/contracts/${contractId}/approvals`)

// 下载Word
export const downloadContractWord = (id: number) => {
  const baseURL = import.meta.env.VITE_API_URL || ''
  const token = localStorage.getItem('token')
  const url = `${baseURL}/api/contracts/${id}/word`
  const link = document.createElement('a')
  link.href = url
  if (token) {
    link.setAttribute('Authorization', `Bearer ${token}`)
  }
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 获取到期预警列表
export const getExpiringContracts = (params: {
  days?: number
  type?: string
  status?: string
}) => get('/contracts/expiring', { params })

// 获取到期预警统计
export const getExpirationStats = () => get('/contracts/statistics/expiration')

// 获取下一个合同编号
export const getNextContractNo = () => get('/contracts/next-number')

// 导出Excel
export const exportContractsExcel = (params?: ContractQuery) => {
  const baseURL = import.meta.env.VITE_API_URL || ''
  const token = localStorage.getItem('token')
  const queryString = params ? '?' + new URLSearchParams(
    Object.entries(params).filter(([_, v]) => v != null).reduce((acc, [k, v]) => ({ ...acc, [k]: v }), {})
  ).toString() : ''
  const url = `${baseURL}/api/contracts/export/excel${queryString}`
  const link = document.createElement('a')
  link.href = url
  if (token) {
    link.setAttribute('Authorization', `Bearer ${token}`)
  }
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 生成PDF - 前端生成，保留格式
export const generateContractPdf = async (data: { content: string; contractNo?: string; watermark?: string }): Promise<Blob> => {
  const { content, contractNo = 'contract', watermark } = data
  
  const { default: html2canvas } = await import('html2canvas')
  const { jsPDF } = await import('jspdf')
  
  const container = document.createElement('div')
  container.style.position = 'absolute'
  container.style.left = '-9999px'
  container.style.top = '0'
  container.style.width = '210mm'
  container.style.backgroundColor = 'white'
  container.style.padding = '20px'
  container.style.fontFamily = "'Microsoft YaHei', 'SimSun', 'Noto Sans SC', sans-serif"
  container.style.fontSize = '14px'
  container.style.lineHeight = '1.8'
  container.style.color = '#333'
  container.style.webkitFontSmoothing = 'antialiased'
  container.style.mozOsxFontSmoothing = 'grayscale'
  
  if (watermark) {
    const watermarkDiv = document.createElement('div')
    watermarkDiv.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) rotate(-45deg);
      opacity: 0.12;
      font-size: 64px;
      font-weight: bold;
      color: #666;
      pointer-events: none;
      z-index: 9999;
      white-space: nowrap;
      user-select: none;
    `
    watermarkDiv.textContent = watermark
    container.appendChild(watermarkDiv)
  }
  
  const contentDiv = document.createElement('div')
  contentDiv.innerHTML = content
  contentDiv.style.position = 'relative'
  contentDiv.style.zIndex = '1'
  container.appendChild(contentDiv)
  
  document.body.appendChild(container)
  
  try {
    // 等待DOM渲染完成
    await new Promise(resolve => setTimeout(resolve, 100))
    
    const canvas = await html2canvas(container, {
      scale: 3,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      width: container.offsetWidth,
      height: container.offsetHeight,
      logging: false,
      imageTimeout: 15000,
      removeContainer: false
    })
    
    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4',
      compress: false
    })
    
    let heightLeft = imgHeight
    let position = 0
    
    // 使用PNG格式，保证最高质量，无压缩损失
    const imgData = canvas.toDataURL('image/png')
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    while (heightLeft > 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    // 将PDF转为Blob，确保不可编辑（因为是图片格式的PDF）
    return pdf.output('blob')
  } finally {
    document.body.removeChild(container)
  }
}
