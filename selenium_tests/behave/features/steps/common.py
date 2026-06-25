from behave import given, when, then
from pages.history_page import HistoryPage
from pages.home_page import HomePage


@given('I am on the home page')
def step_open_home_page(context):
    context.home_page = HomePage(context.driver, context.base_url).open()


@given('I am on the history page')
def step_open_history_page_given(context):
    context.history_page = HistoryPage(context.driver, context.base_url).open()


@when('I switch to URL mode')
def step_switch_to_url_mode(context):
    context.home_page.switch_to_url_mode()


@when('I switch to file mode')
def step_switch_to_file_mode(context):
    context.home_page.switch_to_file_mode()


@when('I enter the URL "{url}"')
def step_enter_url(context, url):
    context.home_page.enter_url(url)


@when('I submit the form')
def step_submit_form(context):
    context.home_page.submit()


@then('the page title "{title}" is displayed')
def step_assert_page_title(context, title):
    assert context.home_page.title_text() == title


@then('the file upload mode is active')
def step_assert_file_mode_active(context):
    assert context.home_page.is_file_mode_active()


@then('the file upload mode is not active')
def step_assert_file_mode_not_active(context):
    assert not context.home_page.is_file_mode_active()
