Scenario: Work With Iframes - Not visible
Given the user is on the W3Schools HTML5 iframe page
Then button Try Yourself is not reachable

Scenario: Work With Iframes - Visible
Given the user is on the W3Schools HTML5 iframe page
When the user switches to iframe
Then button Try Yourself is reachable