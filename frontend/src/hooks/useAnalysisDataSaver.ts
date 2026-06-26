import type jsPDF from 'jspdf'
import type { Analysis } from '../types'

function writeField(doc: jsPDF, label: string, value: string, y: number): number {
  if (y > 270) {
    doc.addPage()
    y = 20
  }
  doc.setTextColor(2, 132, 199)
  doc.text(`${label}:`, 10, y)
  doc.setTextColor(30, 30, 30)
  const lines = doc.splitTextToSize(value, 180) as string[]
  let lineY = y + 6
  for (const line of lines) {
    if (lineY > 280) {
      doc.addPage()
      lineY = 20
    }
    doc.text(line, 10, lineY)
    lineY += 6
  }
  return lineY + 4
}

export function useAnalysisDataSaver() {
  const downloadPdf = async (analysis: Analysis) => {
    const { default: jsPDF } = await import('jspdf')
    const doc = new jsPDF()

    doc.setFillColor(14, 165, 233)
    doc.rect(0, 0, 210, 20, 'F')
    doc.setTextColor(255, 255, 255)
    doc.setFontSize(16)
    doc.text('ClipIQ — Wyniki analizy', 10, 13)
    doc.setFontSize(11)

    let y = 32
    y = writeField(doc, 'Nazwa', analysis.name, y)
    y = writeField(doc, 'Data rozpoczęcia', new Date(analysis.startDate).toLocaleString('pl-PL'), y)
    if (analysis.finishDate) {
      y = writeField(doc, 'Data zakończenia', new Date(analysis.finishDate).toLocaleString('pl-PL'), y)
    }
    y = writeField(doc, 'Status', analysis.status, y)
    y = writeField(doc, 'Typ pliku', analysis.fileType, y)
    if (analysis.authorAttitude) {
      y = writeField(doc, 'Nastrój autora', analysis.authorAttitude, y)
    }
    if (analysis.videoSummary) {
      y = writeField(doc, 'Streszczenie', analysis.videoSummary, y)
    }
    if (analysis.fullTranscription) {
      writeField(doc, 'Transkrypcja', analysis.fullTranscription, y)
    }

    doc.save(`clipiq_${analysis.uuid}.pdf`)
  }

  const copySummary = async (analysis: Analysis) => {
    if (!analysis.videoSummary) return
    try {
      await navigator.clipboard.writeText(analysis.videoSummary)
    } catch {
      alert(analysis.videoSummary)
    }
  }

  return { downloadPdf, copySummary }
}
