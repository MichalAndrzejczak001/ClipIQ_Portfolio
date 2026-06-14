import asyncio
import logging
import os

from fastapi import APIRouter, File, HTTPException, UploadFile

import ai
from schemas import SentimentRequest, SentimentResponse, SummarizeRequest, SummarizeResponse, TranscribeResponse

logger = logging.getLogger(__name__)

router = APIRouter()

_SUPPORTED_EXTENSIONS = {".mp3", ".mp4", ".wav", ".m4a"}
_MAX_FILE_SIZE_MB = int(os.getenv("MAX_FILE_SIZE_MB", "15"))
_MAX_FILE_BYTES = _MAX_FILE_SIZE_MB * 1024 * 1024


@router.post("/transcribe", response_model=TranscribeResponse)
async def transcribe(file: UploadFile = File(...)):
    if not file.filename:
        raise HTTPException(status_code=422, detail="Filename is required")
    if "." not in file.filename:
        raise HTTPException(status_code=422, detail="File must have an extension")
    ext = "." + file.filename.rsplit(".", 1)[-1].lower()
    if ext not in _SUPPORTED_EXTENSIONS:
        raise HTTPException(status_code=422, detail=f"Unsupported format: {ext}")

    if file.size is not None and file.size > _MAX_FILE_BYTES:
        raise HTTPException(status_code=413, detail=f"File exceeds {_MAX_FILE_SIZE_MB} MB limit")
    data = await file.read()
    if len(data) > _MAX_FILE_BYTES:
        raise HTTPException(status_code=413, detail=f"File exceeds {_MAX_FILE_SIZE_MB} MB limit")

    try:
        result = await asyncio.to_thread(ai.transcribe, data, file.filename)
        return TranscribeResponse(transcription=result)
    except Exception as e:
        logger.error("transcribe failed: %s", e, exc_info=True)
        raise HTTPException(status_code=500, detail="Transcription failed")


@router.post("/summarize", response_model=SummarizeResponse)
async def summarize(body: SummarizeRequest):
    try:
        result = await asyncio.to_thread(ai.summarize, body.text)
        return SummarizeResponse(summary=result)
    except Exception as e:
        logger.error("summarize failed: %s", e, exc_info=True)
        raise HTTPException(status_code=500, detail="Summarization failed")


@router.post("/sentiment", response_model=SentimentResponse)
async def sentiment(body: SentimentRequest):
    try:
        result = await asyncio.to_thread(ai.get_sentiment, body.text)
        return SentimentResponse(sentiment=result)
    except Exception as e:
        logger.error("sentiment failed: %s", e, exc_info=True)
        raise HTTPException(status_code=500, detail="Sentiment analysis failed")
