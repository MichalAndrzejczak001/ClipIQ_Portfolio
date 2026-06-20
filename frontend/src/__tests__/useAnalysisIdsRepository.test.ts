import { act, renderHook } from '@testing-library/react'
import { useAnalysisIdsRepository } from '../hooks/useAnalysisIdsRepository'

describe('useAnalysisIdsRepository', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts with an empty list', () => {
    const { result } = renderHook(() => useAnalysisIdsRepository())
    expect(result.current.uuids).toEqual([])
  })

  it('addUuid persists to localStorage and prepends new ids', () => {
    const { result } = renderHook(() => useAnalysisIdsRepository())

    act(() => result.current.addUuid('uuid-1'))
    act(() => result.current.addUuid('uuid-2'))

    expect(result.current.uuids).toEqual(['uuid-2', 'uuid-1'])
    expect(JSON.parse(localStorage.getItem('clipiq_analysis_uuids')!)).toEqual([
      'uuid-2',
      'uuid-1',
    ])
  })

  it('addUuid deduplicates and moves existing id to the front', () => {
    const { result } = renderHook(() => useAnalysisIdsRepository())

    act(() => result.current.addUuid('uuid-1'))
    act(() => result.current.addUuid('uuid-2'))
    act(() => result.current.addUuid('uuid-1'))

    expect(result.current.uuids).toEqual(['uuid-1', 'uuid-2'])
  })

  it('removeUuid removes from state and localStorage', () => {
    const { result } = renderHook(() => useAnalysisIdsRepository())

    act(() => result.current.addUuid('uuid-1'))
    act(() => result.current.addUuid('uuid-2'))
    act(() => result.current.removeUuid('uuid-1'))

    expect(result.current.uuids).toEqual(['uuid-2'])
    expect(JSON.parse(localStorage.getItem('clipiq_analysis_uuids')!)).toEqual(['uuid-2'])
  })

  it('loads previously persisted uuids on mount', () => {
    localStorage.setItem('clipiq_analysis_uuids', JSON.stringify(['existing-uuid']))
    const { result } = renderHook(() => useAnalysisIdsRepository())
    expect(result.current.uuids).toEqual(['existing-uuid'])
  })

  it('falls back to an empty list when localStorage content is invalid JSON', () => {
    localStorage.setItem('clipiq_analysis_uuids', 'not-json')
    const { result } = renderHook(() => useAnalysisIdsRepository())
    expect(result.current.uuids).toEqual([])
  })
})
