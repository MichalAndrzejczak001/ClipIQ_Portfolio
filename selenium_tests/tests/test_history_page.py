import allure
from pages.home_page import HomePage
from pages.analysis_page import AnalysisPage
from pages.history_page import HistoryPage


@allure.epic("ClipIQ")
@allure.feature("History Page")
class TestHistoryPage:

    @allure.story("Empty state")
    @allure.severity(allure.severity_level.NORMAL)
    def test_empty_state_when_no_history(self, driver, base_url):
        page = HistoryPage(driver, base_url).open()
        assert page.is_empty_state_visible()

    @allure.story("Empty state")
    @allure.severity(allure.severity_level.MINOR)
    def test_search_input_visible(self, driver, base_url):
        page = HistoryPage(driver, base_url).open()
        assert page.search_input_visible()

    @allure.story("Navigation")
    @allure.severity(allure.severity_level.NORMAL)
    def test_navigate_from_home_to_history(self, driver, base_url):
        home = HomePage(driver, base_url).open()
        home.go_to_history()
        history = HistoryPage(driver, base_url)
        assert history.title_text() == "Historia analiz"

    @allure.story("Navigation")
    @allure.severity(allure.severity_level.MINOR)
    def test_navigate_from_history_to_home(self, driver, base_url):
        history = HistoryPage(driver, base_url).open()
        history.go_to_new_analysis()
        home = HomePage(driver, base_url)
        assert home.title_text() == "ClipIQ"

    @allure.story("Registered analysis appears in history")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_registered_analysis_appears_in_history(self, driver, base_url):
        with allure.step("Register a YouTube URL from the home page"):
            home = HomePage(driver, base_url).open()
            home.switch_to_url_mode()
            home.enter_url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            home.submit()
            AnalysisPage(driver).wait_for_redirect()

        with allure.step("Open history and verify the new entry is listed"):
            history = HistoryPage(driver, base_url).open()
            assert history.contains_entry("dQw4w9WgXcQ")
            assert not history.is_empty_state_visible()

    @allure.story("Search")
    @allure.severity(allure.severity_level.NORMAL)
    def test_search_filters_entries(self, driver, base_url):
        with allure.step("Register two analyses with different URLs"):
            home = HomePage(driver, base_url).open()
            home.switch_to_url_mode()
            home.enter_url("https://www.youtube.com/watch?v=AAAAAAAAAAA")
            home.submit()
            AnalysisPage(driver).wait_for_redirect()

            home_again = HomePage(driver, base_url).open()
            home_again.switch_to_url_mode()
            home_again.enter_url("https://www.tiktok.com/@user/video/9999999999")
            home_again.submit()
            AnalysisPage(driver).wait_for_redirect()

        with allure.step("Search narrows the list to the matching entry"):
            history = HistoryPage(driver, base_url).open()
            history.search("tiktok")
            assert history.contains_entry("tiktok")
            assert not history.contains_entry("AAAAAAAAAAA")

    @allure.story("Search")
    @allure.severity(allure.severity_level.MINOR)
    def test_search_with_no_matches_shows_message(self, driver, base_url):
        with allure.step("Register an analysis"):
            home = HomePage(driver, base_url).open()
            home.switch_to_url_mode()
            home.enter_url("https://www.youtube.com/watch?v=CCCCCCCCCCC")
            home.submit()
            AnalysisPage(driver).wait_for_redirect()

        with allure.step("Search for a term that matches nothing"):
            history = HistoryPage(driver, base_url).open()
            history.search("no-such-entry-exists")
            assert history.has_no_results_message()

    @allure.story("Remove entry")
    @allure.severity(allure.severity_level.NORMAL)
    def test_remove_entry_clears_it_from_history(self, driver, base_url):
        with allure.step("Register an analysis"):
            home = HomePage(driver, base_url).open()
            home.switch_to_url_mode()
            home.enter_url("https://www.youtube.com/watch?v=BBBBBBBBBBB")
            home.submit()
            AnalysisPage(driver).wait_for_redirect()

        with allure.step("Remove it from the history list"):
            history = HistoryPage(driver, base_url).open()
            assert history.contains_entry("BBBBBBBBBBB")
            history.remove_entry("BBBBBBBBBBB")

        with allure.step("Entry is gone and empty state is shown again"):
            assert not history.contains_entry("BBBBBBBBBBB")
            assert history.is_empty_state_visible()
