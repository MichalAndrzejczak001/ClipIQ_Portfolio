import { useCallback, useState } from 'react'

const STORAGE_KEY = 'clipiq_analysis_uuids'

function loadUuids(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveUuids(uuids: string[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(uuids))
}

export function useAnalysisIdsRepository() {
  const [uuids, setUuids] = useState<string[]>(loadUuids)

  const addUuid = useCallback((uuid: string) => {
    setUuids((prev) => {
      const next = [uuid, ...prev.filter((id) => id !== uuid)]
      saveUuids(next)
      return next
    })
  }, [])

  const removeUuid = useCallback((uuid: string) => {
    setUuids((prev) => {
      const next = prev.filter((id) => id !== uuid)
      saveUuids(next)
      return next
    })
  }, [])

  return { uuids, addUuid, removeUuid }
}
