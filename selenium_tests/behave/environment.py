import os
import sys
import traceback

from selenium import webdriver
from selenium.webdriver.chrome.options import Options

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')))


def before_all(context):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
    print(f"\n[behave] before_all: base_url={context.base_url}")


def before_scenario(context, scenario):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
    print(f"\n[behave] before_scenario: {scenario.name}")
    try:
        options = Options()
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--window-size=1280,800')
        context.driver = webdriver.Chrome(options=options)
        context.driver.implicitly_wait(10)
        print("[behave] Chrome started OK")
    except Exception as e:
        print(f"[behave] Chrome FAILED: {type(e).__name__}: {e}")
        traceback.print_exc()
        raise


def after_scenario(context, scenario):
    if hasattr(context, 'driver'):
        context.driver.quit()
