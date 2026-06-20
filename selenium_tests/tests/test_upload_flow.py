import allure
import pytest
from pages.home_page import HomePage
from pages.analysis_page import AnalysisPage


@allure.epic("ClipIQ")
@allure.feature("Upload Flow")
class TestUploadFlow:

    @allure.story("URL registration")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_valid_youtube_url_redirects_to_analysis(self, driver, base_url):
        with allure.step("Open home page and switch to URL mode"):
            page = HomePage(driver, base_url).open()
            page.switch_to_url_mode()

        with allure.step("Enter a valid YouTube URL and submit"):
            page.enter_url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            page.submit()

        with allure.step("Verify redirect to analysis page"):
            analysis = AnalysisPage(driver).wait_for_redirect()
            assert "/analysis/" in driver.current_url
            assert analysis.back_link_visible()

    @allure.story("URL registration")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_valid_tiktok_url_redirects_to_analysis(self, driver, base_url):
        with allure.step("Open home page and switch to URL mode"):
            page = HomePage(driver, base_url).open()
            page.switch_to_url_mode()

        with allure.step("Enter a valid TikTok URL and submit"):
            page.enter_url("https://www.tiktok.com/@user/video/1234567890")
            page.submit()

        with allure.step("Verify redirect to analysis page"):
            analysis = AnalysisPage(driver).wait_for_redirect()
            assert "/analysis/" in driver.current_url

    @allure.story("URL validation")
    @allure.severity(allure.severity_level.NORMAL)
    def test_invalid_domain_shows_error(self, driver, base_url):
        with allure.step("Open home page and switch to URL mode"):
            page = HomePage(driver, base_url).open()
            page.switch_to_url_mode()

        with allure.step("Enter URL with unsupported domain"):
            page.enter_url("https://www.instagram.com/p/abc123")
            page.submit()

        with allure.step("Verify error message is shown"):
            error = page.error_message()
            assert "Nieprawidłowy adres URL" in error

    @allure.story("URL validation")
    @allure.severity(allure.severity_level.NORMAL)
    def test_path_traversal_url_shows_error(self, driver, base_url):
        with allure.step("Open home page and switch to URL mode"):
            page = HomePage(driver, base_url).open()
            page.switch_to_url_mode()

        with allure.step("Enter URL with path traversal attempt"):
            page.enter_url("https://www.youtube.com/watch?v=../../etc/passwd")
            page.submit()

        with allure.step("Verify error is shown"):
            error = page.error_message()
            assert "Nieprawidłowy adres URL" in error

    @allure.story("Analysis page")
    @allure.severity(allure.severity_level.NORMAL)
    def test_analysis_page_shows_back_link(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        page.switch_to_url_mode()
        page.enter_url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        page.submit()

        analysis = AnalysisPage(driver).wait_for_redirect()
        assert analysis.back_link_visible()

    @allure.story("Analysis page")
    @allure.severity(allure.severity_level.MINOR)
    def test_analysis_page_displays_uuid(self, driver, base_url):
        page = HomePage(driver, base_url).open()
        page.switch_to_url_mode()
        page.enter_url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        page.submit()

        analysis = AnalysisPage(driver).wait_for_redirect()
        uuid_from_url = analysis.uuid_from_url()
        uuid_displayed = analysis.displayed_uuid()
        assert uuid_from_url == uuid_displayed
