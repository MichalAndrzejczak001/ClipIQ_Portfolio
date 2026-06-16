import allure
import pytest
from pages.home_page import HomePage


@allure.epic("ClipIQ")
@allure.feature("Home Page")
class TestHomePage:

    @allure.story("Page load")
    @allure.severity(allure.severity_level.BLOCKER)
    def test_title_is_displayed(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        assert page.title_text() == "ClipIQ"

    @allure.story("Upload mode toggle")
    @allure.severity(allure.severity_level.NORMAL)
    def test_default_mode_is_file(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        assert page.is_file_mode_active()

    @allure.story("Upload mode toggle")
    @allure.severity(allure.severity_level.NORMAL)
    def test_switch_to_url_mode(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        page.switch_to_url_mode()
        assert not page.is_file_mode_active()

    @allure.story("Upload mode toggle")
    @allure.severity(allure.severity_level.MINOR)
    def test_switch_back_to_file_mode(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        page.switch_to_url_mode()
        page.switch_to_file_mode()
        assert page.is_file_mode_active()
