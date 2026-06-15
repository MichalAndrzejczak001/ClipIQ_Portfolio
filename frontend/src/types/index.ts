export type Status = 'IN_PROGRESS' | 'SUCCESS' | 'FAILED'
export type FileType = 'RAW' | 'YOUTUBE' | 'TIKTOK'
export type AuthorAttitude = 'positive' | 'negative' | 'neutral'

export interface Analysis {
  uuid: string
  name: string
  startDate: string
  finishDate: string | null
  status: Status
  fileType: FileType
  link: string | null
  fullTranscription: string | null
  videoSummary: string | null
  authorAttitude: AuthorAttitude | null
}

export interface RegisterResponse {
  analysisUuid: string
}
