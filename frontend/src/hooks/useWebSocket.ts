import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

interface Options {
  uuid: string
  onProgress?: (progress: number) => void
  onDone: () => void
  onFailed: (message: string) => void
}

export function useWebSocket({ uuid, onProgress, onDone, onFailed }: Options) {
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws') as WebSocket,
      onConnect: () => {
        client.subscribe(`/topic/analysis/${uuid}/progress`, (frame) => {
          const value = parseInt(frame.body, 10)
          if (!Number.isNaN(value)) onProgress?.(value)
        })
        client.subscribe(`/topic/analysis/${uuid}/done`, () => onDone())
        client.subscribe(`/topic/analysis/${uuid}/failed`, (frame) =>
          onFailed(frame.body),
        )
        client.publish({ destination: '/app/analyse', body: uuid })
      },
      reconnectDelay: 0,
    })
    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [uuid, onProgress, onDone, onFailed])
}
