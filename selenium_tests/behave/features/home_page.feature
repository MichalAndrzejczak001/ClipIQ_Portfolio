Feature: Home Page
  As a user
  I want the home page to load with the correct content
  So that I can choose how to submit content for analysis

  Scenario: ClipIQ title is displayed on the home page
    Given I am on the home page
    Then the page title "ClipIQ" is displayed

  Scenario: Default upload mode is file upload
    Given I am on the home page
    Then the file upload mode is active

  Scenario: Switching to URL mode shows the URL input
    Given I am on the home page
    When I switch to URL mode
    Then the file upload mode is not active

  Scenario: Switching back to file mode restores the drop zone
    Given I am on the home page
    When I switch to URL mode
    And I switch to file mode
    Then the file upload mode is active
