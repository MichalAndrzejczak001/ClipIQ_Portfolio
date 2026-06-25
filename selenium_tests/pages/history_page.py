from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class HistoryPage:
    _TITLE          = (By.XPATH, "//h2[contains(text(),'Historia analiz')]")
    _SEARCH_INPUT   = (By.CSS_SELECTOR, "input[placeholder^='Szukaj']")
    _EMPTY_STATE    = (By.XPATH, "//*[contains(text(),'Brak analiz')]")
    _NO_RESULTS     = (By.XPATH, "//*[contains(text(),'Brak wyników dla')]")
    _NEW_ANALYSIS_LINK = (By.XPATH, "//a[@aria-label='Nowa analiza']")

    def __init__(self, driver, base_url: str):
        self._driver = driver
        self._base_url = base_url
        self._wait = WebDriverWait(driver, 10)

    def open(self) -> "HistoryPage":
        self._driver.get(f"{self._base_url}/history")
        self._wait.until(EC.presence_of_element_located(self._TITLE))
        return self

    def title_text(self) -> str:
        return self._driver.find_element(*self._TITLE).text

    def search_input_visible(self) -> bool:
        try:
            self._driver.find_element(*self._SEARCH_INPUT)
            return True
        except Exception:
            return False

    def is_empty_state_visible(self) -> bool:
        try:
            self._driver.find_element(*self._EMPTY_STATE)
            return True
        except Exception:
            return False

    def has_no_results_message(self) -> bool:
        try:
            self._driver.find_element(*self._NO_RESULTS)
            return True
        except Exception:
            return False

    def search(self, query: str) -> "HistoryPage":
        field = self._driver.find_element(*self._SEARCH_INPUT)
        field.clear()
        field.send_keys(query)
        return self

    def contains_entry(self, text_fragment: str) -> bool:
        try:
            self._driver.find_element(By.XPATH, f"//li[contains(., '{text_fragment}')]")
            return True
        except Exception:
            return False

    def remove_entry(self, text_fragment: str) -> "HistoryPage":
        row = self._driver.find_element(By.XPATH, f"//li[contains(., '{text_fragment}')]")
        row.find_element(By.XPATH, ".//button[@aria-label='Usuń z historii']").click()
        self._wait.until(EC.staleness_of(row))
        return self

    def go_to_new_analysis(self) -> "HistoryPage":
        self._driver.find_element(*self._NEW_ANALYSIS_LINK).click()
        return self
