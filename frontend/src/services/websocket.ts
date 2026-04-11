import { ref } from 'vue'
import { ElNotification } from 'element-plus'

class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private pingInterval: number | null = null
  private soundEnabled = ref(true)
  private onNotificationCallback: ((notification: any) => void) | null = null
  
  public connected = ref(false)
  public notifications = ref<any[]>([])
  
  connect(userId?: number) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = userId 
      ? `${protocol}//${host}/ws?userId=${userId}`
      : `${protocol}//${host}/ws`
    
    try {
      this.ws = new WebSocket(url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.connected.value = true
        this.reconnectAttempts = 0
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
        console.log('WebSocket disconnected')
        this.connected.value = false
        this.stopPing()
        this.scheduleReconnect(userId)
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
      id: Date.now(),
      read: false,
      type: this.getNotificationCategory(data.type),
      priority: this.getPriority(data.type),
      link: this.getNotificationLink(data)
    }
    
    this.notifications.value.unshift(notification)
    
    if (this.notifications.value.length > 100) {
      this.notifications.value = this.notifications.value.slice(0, 100)
    }
    
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
      contract_update: 'info'
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
      setTimeout(() => {
        console.log(`WebSocket reconnect attempt ${this.reconnectAttempts}`)
        this.connect(userId)
      }, this.reconnectDelay * this.reconnectAttempts)
    }
  }

  send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }
  
  disconnect() {
    this.stopPing()
    this.reconnectAttempts = this.maxReconnectAttempts // Prevent reconnect
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
    }
  }
  
  markAllAsRead() {
    this.notifications.value.forEach(n => n.read = true)
  }
  
  clearNotifications() {
    this.notifications.value = []
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
}

export const webSocketService = new WebSocketService()
export default webSocketService
