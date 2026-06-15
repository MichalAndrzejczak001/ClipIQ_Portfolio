import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

interface Options {
  uuid: string
  onDone: () => void
  onFailed: (message: string) => void
}

export function useWebSocket({ uuid, onDone, onFailed }: Options) {
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws') as WebSocket,
      onConnect: () => {
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
  }, [uuid, onDone, onFailed])
}
