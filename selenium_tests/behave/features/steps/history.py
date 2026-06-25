from behave import then, when
from pages.analysis_page import AnalysisPage
from pages.history_page import HistoryPage
from pages.home_page import HomePage


@when('I navigate to the history page')
def step_navigate_to_history(context):
    context.home_page.go_to_history()
    context.history_page = HistoryPage(context.driver, context.base_url)


@when('I open the history page')
def step_open_history(context):
    context.history_page = HistoryPage(context.driver, context.base_url).open()


@when('I go to the new analysis page')
def step_go_to_new_analysis(context):
    context.history_page.go_to_new_analysis()
    context.home_page = HomePage(context.driver, context.base_url)


@when('the analysis page loads')
def step_analysis_page_loads(context):
    AnalysisPage(context.driver).wait_for_redirect()


@when('I search for "{query}"')
def step_search(context, query):
    context.history_page.search(query)


@when('I remove the entry "{fragment}"')
def step_remove_entry(context, fragment):
    context.history_page.remove_entry(fragment)


@then('the empty state is visible')
def step_empty_state_visible(context):
    assert context.history_page.is_empty_state_visible()


@then('the empty state is not visible')
def step_empty_state_not_visible(context):
    assert not context.history_page.is_empty_state_visible()


@then('the search input is visible')
def step_search_input_visible(context):
    assert context.history_page.search_input_visible()


@then('I should see the heading "{heading}"')
def step_see_heading(context, heading):
    assert context.history_page.title_text() == heading


@then('the history contains "{fragment}"')
def step_history_contains(context, fragment):
    assert context.history_page.contains_entry(fragment)


@then('the history does not contain "{fragment}"')
def step_history_not_contains(context, fragment):
    assert not context.history_page.contains_entry(fragment)


@then('the no results message is shown')
def step_no_results(context):
    assert context.history_page.has_no_results_message()
