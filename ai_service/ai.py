from functools import lru_cache
from typing import Literal

from openai import OpenAI
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

_analyzer = SentimentIntensityAnalyzer()

_MIME_TYPES = {
    ".mp3": "audio/mpeg",
    ".mp4": "audio/mp4",
    ".wav": "audio/wav",
    ".m4a": "audio/m4a",
}


@lru_cache(maxsize=1)
def _openai() -> OpenAI:
    return OpenAI()


def transcribe(audio: bytes, filename: str) -> str:
    ext = "." + filename.rsplit(".", 1)[-1].lower() if "." in filename else ""
    mime = _MIME_TYPES.get(ext, "audio/mpeg")
    resp = _openai().audio.transcriptions.create(
        model="whisper-1",
        file=(filename, audio, mime),
    )
    return resp.text


def summarize(text: str) -> str:
    resp = _openai().chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[{"role": "user", "content": f"Summarize the following:\n\n{text}"}],
    )
    content = resp.choices[0].message.content
    if content is None:
        raise RuntimeError(f"OpenAI returned no content (finish_reason={resp.choices[0].finish_reason})")
    return content


def get_sentiment(text: str) -> Literal["positive", "negative", "neutral"]:
    compound = _analyzer.polarity_scores(text)["compound"]
    if compound >= 0.05:
        return "positive"
    if compound <= -0.05:
        return "negative"
    return "neutral"
