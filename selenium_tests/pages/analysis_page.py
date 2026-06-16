from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class AnalysisPage:
    _BACK_LINK       = (By.XPATH, "//a[contains(text(),'Nowa analiza')]")
    _UUID_LABEL      = (By.CSS_SELECTOR, "p.font-mono")
    _PROCESSING_MSG  = (By.XPATH, "//*[contains(text(),'Analizuję plik')]")
    _ERROR_HEADING   = (By.XPATH, "//*[contains(text(),'Błąd analizy')]")
    _SUMMARY_SECTION = (By.XPATH, "//*[contains(text(),'Podsumowanie')]")
    _SENTIMENT_BADGE = (By.CSS_SELECTOR, "span.rounded-full")

    def __init__(self, driver):
        self._driver = driver
        self._wait = WebDriverWait(driver, 15)

    def wait_for_redirect(self) -> "AnalysisPage":
        self._wait.until(lambda d: "/analysis/" in d.current_url)
        return self

    def uuid_from_url(self) -> str:
        return self._driver.current_url.split("/analysis/")[-1]

    def displayed_uuid(self) -> str:
        return self._driver.find_element(*self._UUID_LABEL).text

    def is_processing(self) -> bool:
        try:
            self._driver.find_element(*self._PROCESSING_MSG)
            return True
        except Exception:
            return False

    def has_error(self) -> bool:
        try:
            self._wait.until(EC.presence_of_element_located(self._ERROR_HEADING))
            return True
        except Exception:
            return False

    def back_link_visible(self) -> bool:
        try:
            self._driver.find_element(*self._BACK_LINK)
            return True
        except Exception:
            return False
