import os
import sys

import requests
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))


def before_all(context):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
    try:
        requests.get(context.base_url, timeout=5)
        context.app_available = True
    except Exception:
        context.app_available = False


def before_scenario(context, scenario):
    if not context.app_available:
        scenario.skip(f'App not running at {context.base_url}')
        return
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--window-size=1280,800')
    context.driver = webdriver.Chrome(options=options)
    context.driver.implicitly_wait(10)


def after_scenario(context, scenario):
    if hasattr(context, 'driver'):
        context.driver.quit()
