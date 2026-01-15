@regression @home
Feature: Home screen

  This scenario test the main features of the 'home' screen

  Background:
    When the user accept all cookies
    When there is a showcase into the top banner carousel on a home screen with the CTA button of "TEST" content type

  @smoke  @run_with_test_data @ignore
  Scenario: Validate that main elements of 'Home' screen are loaded
    And the user sees navigation bar tab is selected as "Home"
