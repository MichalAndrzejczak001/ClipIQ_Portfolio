import os
import sys
import traceback

from selenium import webdriver
from selenium.webdriver.chrome.options import Options


def before_all(context):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
    sys.stderr.write(f"[behave] before_all: base_url={context.base_url}\n")


def before_scenario(context, scenario):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
    sys.stderr.write(f"[behave] before_scenario: {scenario.name}\n")
    try:
        options = Options()
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--window-size=1280,800')
        context.driver = webdriver.Chrome(options=options)
        context.driver.implicitly_wait(10)
        sys.stderr.write("[behave] Chrome started OK\n")
    except Exception as e:
        sys.stderr.write(f"[behave] Chrome FAILED: {type(e).__name__}: {e}\n")
        traceback.print_exc(file=sys.stderr)
        raise


def after_scenario(context, scenario):
    if hasattr(context, 'driver'):
        context.driver.quit()
