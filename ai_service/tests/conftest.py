import os

import pytest
from fastapi.testclient import TestClient

os.environ["OPENAI_API_KEY"] = "test-key"

from main import app


@pytest.fixture
def client():
    with TestClient(app) as c:
        yield c
