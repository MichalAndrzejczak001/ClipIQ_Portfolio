Feature: History Page
  As a user
  I want to browse and manage my past analyses
  So that I can revisit and clean up previously submitted content

  Scenario: Empty state is shown when there is no history
    Given I am on the history page
    Then the empty state is visible

  Scenario: Search input is visible on the history page
    Given I am on the history page
    Then the search input is visible

  Scenario: Navigating from home to history shows the heading
    Given I am on the home page
    When I navigate to the history page
    Then I should see the heading "Historia analiz"

  Scenario: Navigating from history back to the home page
    Given I am on the history page
    When I go to the new analysis page
    Then the page title "ClipIQ" is displayed

  Scenario: A registered analysis appears in the history
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    And I submit the form
    And the analysis page loads
    And I open the history page
    Then the history contains "dQw4w9WgXcQ"
    And the empty state is not visible

  Scenario: Search filters history entries by URL fragment
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.youtube.com/watch?v=AAAAAAAAAAA"
    And I submit the form
    And the analysis page loads
    And I am on the home page
    And I switch to URL mode
    And I enter the URL "https://www.tiktok.com/@user/video/9999999999"
    And I submit the form
    And the analysis page loads
    And I open the history page
    And I search for "tiktok"
    Then the history contains "tiktok"
    And the history does not contain "AAAAAAAAAAA"

  Scenario: Searching with no matches shows a message
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.youtube.com/watch?v=CCCCCCCCCCC"
    And I submit the form
    And the analysis page loads
    And I open the history page
    And I search for "no-such-entry-exists"
    Then the no results message is shown

  Scenario: Removing a history entry clears it from the list
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.youtube.com/watch?v=BBBBBBBBBBB"
    And I submit the form
    And the analysis page loads
    And I open the history page
    And I remove the entry "BBBBBBBBBBB"
    Then the history does not contain "BBBBBBBBBBB"
    And the empty state is visible
