from typing import Annotated, Literal

from pydantic import BaseModel, StringConstraints

# Leading/trailing whitespace is stripped before the length checks, so "   " is rejected as empty.
InputText = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=50_000)]


class TranscribeResponse(BaseModel):
    transcription: str


class SummarizeRequest(BaseModel):
    text: InputText


class SummarizeResponse(BaseModel):
    summary: str


class SentimentRequest(BaseModel):
    text: InputText


class SentimentResponse(BaseModel):
    sentiment: Literal["positive", "negative", "neutral"]
