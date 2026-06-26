import os

from selenium import webdriver
from selenium.webdriver.chrome.options import Options


def before_all(context):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')


def before_scenario(context, scenario):
    context.base_url = os.environ.get('BASE_URL', 'http://localhost:3000')
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
