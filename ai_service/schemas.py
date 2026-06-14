from typing import Literal

from pydantic import BaseModel, Field


class TranscribeResponse(BaseModel):
    transcription: str


class SummarizeRequest(BaseModel):
    text: str = Field(min_length=1, max_length=50_000)


class SummarizeResponse(BaseModel):
    summary: str


class SentimentRequest(BaseModel):
    text: str = Field(min_length=1, max_length=50_000)


class SentimentResponse(BaseModel):
    sentiment: Literal["positive", "negative", "neutral"]
