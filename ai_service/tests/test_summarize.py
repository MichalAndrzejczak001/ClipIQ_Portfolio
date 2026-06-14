from unittest.mock import MagicMock, patch

import httpx
from fastapi.testclient import TestClient


def _mock_openai(summary: str) -> MagicMock:
    mock_client = MagicMock()
    mock_client.chat.completions.create.return_value = MagicMock(
        choices=[MagicMock(message=MagicMock(content=summary))]
    )
    return mock_client


def test_summarize_valid_text(client: TestClient) -> None:
    with patch("ai._openai", return_value=_mock_openai("A short summary.")):
        response: httpx.Response = client.post("/summarize", json={"text": "Some long text to summarize."})

    assert response.status_code == 200
    assert response.json() == {"summary": "A short summary."}


def test_summarize_empty_text(client: TestClient) -> None:
    response: httpx.Response = client.post("/summarize", json={"text": ""})

    assert response.status_code == 422


def test_summarize_text_too_long(client: TestClient) -> None:
    response: httpx.Response = client.post("/summarize", json={"text": "x" * 50_001})

    assert response.status_code == 422
