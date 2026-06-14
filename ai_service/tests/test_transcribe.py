from unittest.mock import MagicMock, patch

import httpx
from fastapi.testclient import TestClient


def _mock_openai(text: str) -> MagicMock:
    mock_client = MagicMock()
    mock_client.audio.transcriptions.create.return_value = MagicMock(text=text)
    return mock_client


def test_transcribe_valid_mp3(client: TestClient) -> None:
    with patch("ai._openai", return_value=_mock_openai("Hello world")):
        response: httpx.Response = client.post(
            "/transcribe",
            files={"file": ("audio.mp3", b"fake audio data", "audio/mpeg")},
        )

    assert response.status_code == 200
    assert response.json() == {"transcription": "Hello world"}


def test_transcribe_valid_mp4(client: TestClient) -> None:
    with patch("ai._openai", return_value=_mock_openai("Video transcription")):
        response: httpx.Response = client.post(
            "/transcribe",
            files={"file": ("video.mp4", b"fake video data", "audio/mp4")},
        )

    assert response.status_code == 200
    assert response.json() == {"transcription": "Video transcription"}


def test_transcribe_no_extension(client: TestClient) -> None:
    response: httpx.Response = client.post(
        "/transcribe",
        files={"file": ("audiofile", b"fake audio data", "audio/mpeg")},
    )

    assert response.status_code == 422


def test_transcribe_unsupported_extension(client: TestClient) -> None:
    response: httpx.Response = client.post(
        "/transcribe",
        files={"file": ("audio.avi", b"fake audio data", "video/avi")},
    )

    assert response.status_code == 422


def test_transcribe_file_too_large(client: TestClient) -> None:
    with patch("routes._MAX_FILE_BYTES", 5):
        response: httpx.Response = client.post(
            "/transcribe",
            files={"file": ("audio.mp3", b"more than five bytes", "audio/mpeg")},
        )

    assert response.status_code == 413
