import axios from 'axios'
import type { Analysis, RegisterResponse } from '../types'

const http = axios.create({ timeout: 30_000 })

export const registerFile = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return http.post<RegisterResponse>('/register/file', form)
}

export const registerUrl = (url: string) =>
  http.post<RegisterResponse>('/register/url', { url })

export const fetchAnalysis = (uuid: string) =>
  http.get<Analysis>(`/analyse/${uuid}`)

export const fetchAnalyses = (uuids: string[]) =>
  http.get<Analysis[]>('/analyse', { params: { uuids: uuids.join(',') } })

export const deleteAnalysis = (uuid: string) => http.delete<void>(`/analyse/${uuid}`)
