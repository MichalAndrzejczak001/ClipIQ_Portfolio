Feature: Analysis Registration
  As a ClipIQ user
  I want to register media files and URLs for analysis
  So that they can be transcribed and summarized

  Scenario: Register a valid MP3 file
    Given a multipart file named "audio.mp3" with type "audio/mpeg"
    When the user posts to "/register/file"
    Then the response status is 200
    And the response body contains field "analysisUuid"

  Scenario: Register a valid MP4 file
    Given a multipart file named "video.mp4" with type "video/mp4"
    When the user posts to "/register/file"
    Then the response status is 200
    And the response body contains field "analysisUuid"

  Scenario: Register a file with unsupported extension returns 400
    Given a multipart file named "clip.avi" with type "video/x-msvideo"
    When the user posts to "/register/file"
    Then the response status is 400

  Scenario: Register a valid YouTube URL
    Given a JSON body with url "https://youtube.com/watch?v=dQw4w9WgXcQ"
    When the user posts to "/register/url"
    Then the response status is 200
    And the response body contains field "analysisUuid"

  Scenario: Register a valid TikTok URL
    Given a JSON body with url "https://tiktok.com/@user/video/123456789"
    When the user posts to "/register/url"
    Then the response status is 200
    And the response body contains field "analysisUuid"

  Scenario: Register an invalid domain URL returns 400
    Given a JSON body with url "https://evil.com/malware"
    When the user posts to "/register/url"
    Then the response status is 400

  Scenario: Register a URL with path traversal returns 400
    Given a JSON body with url "https://youtube.com/../etc/passwd"
    When the user posts to "/register/url"
    Then the response status is 400
