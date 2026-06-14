from unittest.mock import MagicMock, patch

import httpx
from fastapi.testclient import TestClient


def _mock_analyzer(compound: float) -> MagicMock:
    mock = MagicMock()
    mock.polarity_scores.return_value = {"compound": compound}
    return mock


def test_sentiment_positive(client: TestClient) -> None:
    with patch("ai._analyzer", _mock_analyzer(0.8)):
        response: httpx.Response = client.post("/sentiment", json={"text": "This is great!"})

    assert response.status_code == 200
    assert response.json() == {"sentiment": "positive"}


def test_sentiment_negative(client: TestClient) -> None:
    with patch("ai._analyzer", _mock_analyzer(-0.7)):
        response: httpx.Response = client.post("/sentiment", json={"text": "This is terrible."})

    assert response.status_code == 200
    assert response.json() == {"sentiment": "negative"}


def test_sentiment_neutral(client: TestClient) -> None:
    with patch("ai._analyzer", _mock_analyzer(0.0)):
        response: httpx.Response = client.post("/sentiment", json={"text": "The sky is blue."})

    assert response.status_code == 200
    assert response.json() == {"sentiment": "neutral"}


def test_sentiment_empty_text(client: TestClient) -> None:
    response: httpx.Response = client.post("/sentiment", json={"text": ""})

    assert response.status_code == 422
