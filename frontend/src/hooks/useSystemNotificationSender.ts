import { useCallback } from 'react'
import type { Analysis } from '../types'

export function useSystemNotificationSender() {
  const sendNotification = useCallback((analysis: Analysis) => {
    const title = 'ClipIQ — analiza zakończona'
    const body = analysis.name

    if (!('Notification' in window)) {
      alert(`${title}: ${body}`)
      return
    }

    if (Notification.permission === 'granted') {
      new Notification(title, { body })
      return
    }

    if (Notification.permission !== 'denied') {
      Notification.requestPermission().then((permission) => {
        if (permission === 'granted') {
          new Notification(title, { body })
        } else {
          alert(`${title}: ${body}`)
        }
      })
      return
    }

    alert(`${title}: ${body}`)
  }, [])

  return { sendNotification }
}
