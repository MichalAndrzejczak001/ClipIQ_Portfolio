import requests
import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options


@pytest.fixture(scope="session", autouse=True)
def require_app(base_url):
    try:
        requests.get(base_url, timeout=5)
    except Exception:
        pytest.skip(f"Application not running at {base_url}")


@pytest.fixture
def driver():
    options = Options()
    options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--window-size=1280,800")
    d = webdriver.Chrome(options=options)
    d.implicitly_wait(10)
    yield d
    d.quit()
