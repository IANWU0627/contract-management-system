import { ref } from 'vue'
import { ElNotification } from 'element-plus'

class WebSocketService {
  private ws: WebSocket | null = null
  private hasConnectedOnce = false
  private reconnectAttempts = 0
  private maxReconnectAttempts = 20
  private reconnectDelay = 1500
  private maxReconnectDelay = 30000
  private reconnectTimer: number | null = null
  private reconnectUserId?: number
  private shouldRetryInCurrentSession = true
  private pingInterval: number | null = null
  private storageKey = 'ws-notifications'
  private soundEnabled = ref(localStorage.getItem('notificationSound') !== 'false')
  private onNotificationCallback: ((notification: any) => void) | null = null
  
  public connected = ref(false)
  public notifications = ref<any[]>([])

  constructor() {
    this.restoreNotifications()
    window.addEventListener('online', this.handleOnline)
  }
  
  connect(userId?: number) {
    this.reconnectUserId = userId
    if (!this.shouldRetryInCurrentSession) {
      this.connected.value = false
      return
    }
    // Avoid duplicate websocket connections.
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const token = localStorage.getItem('token')
    if (!token) {
      this.connected.value = false
      return
    }
    const url = `${protocol}//${host}/ws?token=${encodeURIComponent(token)}`
    
    try {
      this.ws = new WebSocket(url)
      
      this.ws.onopen = () => {
        this.hasConnectedOnce = true
        this.connected.value = true
        this.reconnectAttempts = 0
        if (this.reconnectTimer) {
          clearTimeout(this.reconnectTimer)
          this.reconnectTimer = null
        }
        this.startPing()
      }
      
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.handleMessage(data)
        } catch (e) {
          // Silently ignore parse errors
        }
      }
      
      this.ws.onclose = () => {
        this.connected.value = false
        this.stopPing()
        // If the connection never succeeded once in this session, do not keep retrying.
        // Avoid endless reconnect when backend WebSocket is unavailable.
        if (!this.hasConnectedOnce) {
          this.shouldRetryInCurrentSession = false
        } else {
          this.scheduleReconnect(userId)
        }
        this.ws = null
      }
      
      this.ws.onerror = () => {
        this.connected.value = false
      }
    } catch (error) {
      this.connected.value = false
      this.scheduleReconnect(userId)
    }
  }
  
  private handleMessage(data: any) {
    if (data.type === 'pong') return
    
    const notification = {
      ...data,
      id: this.buildNotificationId(data),
      read: false,
      type: this.getNotificationCategory(data.type),
      priority: this.getPriority(data.type),
      link: this.getNotificationLink(data)
    }
    
    this.notifications.value.unshift(notification)
    
    if (this.notifications.value.length > 100) {
      this.notifications.value = this.notifications.value.slice(0, 100)
    }
    this.persistNotifications()
    
    // Play sound
    if (this.soundEnabled.value) {
      this.playNotificationSound()
    }
    
    // Show system notification
    ElNotification({
      title: data.title || '通知',
      message: data.content || '',
      type: this.getNotificationType(data.type),
      duration: 5000,
      onClick: () => {
        this.handleNotificationClick(notification)
      }
    })
    
    // Call custom callback if set
    if (this.onNotificationCallback) {
      this.onNotificationCallback(notification)
    }
  }
  
  private getNotificationCategory(type: string): string {
    if (['approval_request', 'approval_result'].includes(type)) return 'approval'
    if (['renewal_request', 'renewal_result'].includes(type)) return 'renewal'
    if (['reminder'].includes(type)) return 'reminder'
    if (['contract_comment', 'security_alert'].includes(type)) return 'system'
    return 'system'
  }
  
  private getPriority(type: string): string {
    if (['approval_request', 'renewal_request'].includes(type)) return 'important'
    return 'normal'
  }
  
  private getNotificationLink(data: any): string | undefined {
    if (data.data?.contractId) {
      return `/contracts/${data.data.contractId}`
    }
    return undefined
  }
  
  private handleNotificationClick(notification: any) {
    if (notification.link) {
      window.location.href = notification.link
    }
  }
  
  private getNotificationType(type: string): 'success' | 'warning' | 'info' | 'error' {
    const typeMap: Record<string, 'success' | 'warning' | 'info' | 'error'> = {
      approval_result: 'success',
      approval_request: 'warning',
      renewal_result: 'success',
      renewal_request: 'warning',
      reminder: 'info',
      contract_comment: 'info',
      contract_update: 'info',
      security_alert: 'warning'
    }
    return typeMap[type] || 'info'
  }
  
  private startPing() {
    this.pingInterval = window.setInterval(() => {
      this.send({ type: 'ping' })
    }, 30000)
  }
  
  private stopPing() {
    if (this.pingInterval) {
      clearInterval(this.pingInterval)
      this.pingInterval = null
    }
  }
  
  private scheduleReconnect(userId?: number) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      const jitter = Math.floor(Math.random() * 500)
      const delay = Math.min(this.reconnectDelay * Math.pow(1.6, this.reconnectAttempts - 1), this.maxReconnectDelay) + jitter
      this.reconnectTimer = window.setTimeout(() => {
        this.connect(userId)
      }, delay)
    }
  }

  send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }
  
  disconnect() {
    this.stopPing()
    this.shouldRetryInCurrentSession = false
    this.reconnectAttempts = this.maxReconnectAttempts // Prevent reconnect
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.connected.value = false
  }
  
  markAsRead(id: number) {
    const notification = this.notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
      this.persistNotifications()
    }
  }
  
  markAllAsRead() {
    this.notifications.value.forEach(n => n.read = true)
    this.persistNotifications()
  }
  
  clearNotifications() {
    this.notifications.value = []
    this.persistNotifications()
  }
  
  getUnreadCount() {
    return this.notifications.value.filter(n => !n.read).length
  }
  
  setSoundEnabled(enabled: boolean) {
    this.soundEnabled.value = enabled
    localStorage.setItem('notificationSound', String(enabled))
  }
  
  isSoundEnabled() {
    return this.soundEnabled.value
  }
  
  setOnNotification(callback: (notification: any) => void) {
    this.onNotificationCallback = callback
  }
  
  private playNotificationSound() {
    try {
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      
      oscillator.frequency.value = 800
      oscillator.type = 'sine'
      
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3)
      
      oscillator.start(audioContext.currentTime)
      oscillator.stop(audioContext.currentTime + 0.3)
    } catch (e) {
      // Sound not supported
    }
  }

  private buildNotificationId(data: any): string {
    const base = `${data.type || 'system'}|${data.title || ''}|${data.content || ''}|${data.timestamp || Date.now()}`
    return `${base}|${Math.random().toString(36).slice(2, 8)}`
  }

  private persistNotifications() {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(this.notifications.value.slice(0, 100)))
    } catch (e) {
      // Ignore localStorage quota errors silently.
    }
  }

  private restoreNotifications() {
    try {
      const raw = localStorage.getItem(this.storageKey)
      if (!raw) return
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) {
        this.notifications.value = parsed.slice(0, 100)
      }
    } catch (e) {
      this.notifications.value = []
    }
  }

  private handleOnline = () => {
    if (!this.connected.value && this.reconnectUserId) {
      this.connect(this.reconnectUserId)
    }
  }
}

export const webSocketService = new WebSocketService()
export default webSocketService
