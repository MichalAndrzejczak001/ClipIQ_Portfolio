Feature: Upload Flow
  As a user
  I want to submit a video URL for analysis
  So that I can receive AI-generated insights about the content

  Scenario: Submitting a valid YouTube URL redirects to the analysis page
    Given I am on the home page
    When I switch to URL mode
    And I enter a valid YouTube URL
    And I submit the form
    Then I am redirected to an analysis page

  Scenario: Submitting a valid TikTok URL redirects to the analysis page
    Given I am on the home page
    When I switch to URL mode
    And I enter a valid TikTok URL
    And I submit the form
    Then I am redirected to an analysis page

  Scenario: Submitting an unsupported domain shows a validation error
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.instagram.com/p/abc123"
    And I submit the form
    Then I should see the error "Nieprawidłowy adres URL"

  Scenario: Submitting a path traversal URL shows a validation error
    Given I am on the home page
    When I switch to URL mode
    And I enter the URL "https://www.youtube.com/watch?v=../../etc/passwd"
    And I submit the form
    Then I should see the error "Nieprawidłowy adres URL"
