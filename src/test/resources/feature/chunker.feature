Feature: Chunker tests

  Scenario: Main success scenario

    Given there is a data file /source with the following content:
      """
      line1
      line2
      """

    When chunking is requested

    Then last HTTP POST request returns the following JSON:
      """
      [
        "/source"
      ]
      """