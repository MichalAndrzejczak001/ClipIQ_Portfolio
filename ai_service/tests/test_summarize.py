from unittest.mock import MagicMock, patch

import httpx
from fastapi.testclient import TestClient


def _mock_openai(summary: str | None) -> MagicMock:
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


def test_summarize_text_at_max_length(client: TestClient) -> None:
    with patch("ai._openai", return_value=_mock_openai("Summary.")):
        response: httpx.Response = client.post("/summarize", json={"text": "x" * 50_000})

    assert response.status_code == 200


def test_summarize_whitespace_only_text(client: TestClient) -> None:
    response: httpx.Response = client.post("/summarize", json={"text": "   \n\t "})

    assert response.status_code == 422


def test_summarize_openai_returns_no_content_returns_500(client: TestClient) -> None:
    with patch("ai._openai", return_value=_mock_openai(None)):
        response: httpx.Response = client.post("/summarize", json={"text": "Some text."})

    assert response.status_code == 500
    assert response.json() == {"detail": "Summarization failed"}


def test_summarize_openai_error_returns_500(client: TestClient) -> None:
    failing_client = MagicMock()
    failing_client.chat.completions.create.side_effect = RuntimeError("OpenAI unavailable")
    with patch("ai._openai", return_value=failing_client):
        response: httpx.Response = client.post("/summarize", json={"text": "Some text."})

    assert response.status_code == 500
    assert response.json() == {"detail": "Summarization failed"}
