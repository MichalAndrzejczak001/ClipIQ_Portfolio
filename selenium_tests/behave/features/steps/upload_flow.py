from behave import then, when
from pages.analysis_page import AnalysisPage
from test_data import tiktok_url, youtube_url


@when('I enter a valid YouTube URL')
def step_enter_youtube_url(context):
    context.home_page.enter_url(youtube_url())


@when('I enter a valid TikTok URL')
def step_enter_tiktok_url(context):
    context.home_page.enter_url(tiktok_url())


@then('I am redirected to an analysis page')
def step_redirected_to_analysis(context):
    AnalysisPage(context.driver).wait_for_redirect()
    assert '/analysis/' in context.driver.current_url


@then('I should see the error "{message}"')
def step_see_error(context, message):
    assert message in context.home_page.error_message()
