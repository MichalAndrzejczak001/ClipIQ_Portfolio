from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class HomePage:
    _TITLE       = (By.XPATH, "//*[text()='ClipIQ']")
    _URL_TAB     = (By.XPATH, "//button[contains(text(),'Link')]")
    _FILE_TAB    = (By.XPATH, "//button[contains(text(),'Plik')]")
    _URL_INPUT   = (By.CSS_SELECTOR, "input[type='url']")
    _SUBMIT_BTN  = (By.XPATH, "//button[@type='submit']")
    _ERROR_MSG   = (By.CSS_SELECTOR, "p.text-red-400")
    _DROP_ZONE   = (By.XPATH, "//*[contains(text(),'Przeciągnij plik')]")
    _HISTORY_LINK = (By.XPATH, "//a[@aria-label='Historia analiz']")

    def __init__(self, driver, base_url: str):
        self._driver = driver
        self._base_url = base_url
        self._wait = WebDriverWait(driver, 10)

    def open(self) -> "HomePage":
        self._driver.get(self._base_url)
        self._wait.until(EC.presence_of_element_located(self._SUBMIT_BTN))
        return self

    def wait_for_load(self) -> "HomePage":
        self._wait.until(EC.presence_of_element_located(self._SUBMIT_BTN))
        return self

    def title_text(self) -> str:
        return self._driver.find_element(*self._TITLE).text

    def switch_to_url_mode(self) -> "HomePage":
        self._driver.find_element(*self._URL_TAB).click()
        self._wait.until(EC.presence_of_element_located(self._URL_INPUT))
        return self

    def switch_to_file_mode(self) -> "HomePage":
        self._driver.find_element(*self._FILE_TAB).click()
        self._wait.until(EC.presence_of_element_located(self._DROP_ZONE))
        return self

    def enter_url(self, url: str) -> "HomePage":
        self._driver.find_element(*self._URL_INPUT).send_keys(url)
        return self

    def submit(self) -> "HomePage":
        self._driver.find_element(*self._SUBMIT_BTN).click()
        return self

    def error_message(self) -> str:
        el = self._wait.until(EC.presence_of_element_located(self._ERROR_MSG))
        return el.text

    def is_file_mode_active(self) -> bool:
        try:
            self._driver.find_element(*self._DROP_ZONE)
            return True
        except Exception:
            return False

    def go_to_history(self) -> "HomePage":
        self._driver.find_element(*self._HISTORY_LINK).click()
        return self
