Feature: Chunker tests

  Scenario: Main success scenario

    Given there is a data file /to-chunk/source with the following content:
      """
      line1
      line2
      """

    And LLM replies by the following on the next request:
      """
      line1
      ----
      line2
      """

    When chunking is requested

    Then last HTTP POST request returns the following JSON:
      """
      [
        "/to-chunk/source"
      ]
      """