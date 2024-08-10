Feature: Expense View High Level
  Specifications of the behavior of the Expense View

  Background: 
    Given The database contains a few expense
    And The Expense View is shown

  Scenario: Add a new Expense
    Given The user provides expense data in the text fields
    And The user clicks the "Add Expense" button
    Then The list contains the new expense

  Scenario: Add a new expense with a future date
    Given The user provides expense data in the text fields, specifying a future date
    When The user clicks the "Add Expense" button
    Then An error is shown containing the name of the expense

  Scenario: Delete a expense
    Given The user selects a expense from the list
    When The user clicks the "Delete Selected" button
    Then The expense is removed from the list

  Scenario: Delete a not existing expense
    Given The user selects a expense from the list
    But The expense is in the meantime removed from the database
    When The user clicks the "Delete Selected" button
    Then An error is shown containing the name of the expense
    And The expense is removed from the list
    
  Scenario: Update a expense
    Given The user selects a expense from the list
    When The user clicks the "Update Selected" button
    And The selected expense is populated in textfields
    And The user provides Updated expense data in the text fields
    And The user clicks the "Update Expense" button
    Then The list contains the updated expense
    
    Scenario: Update a with a future date
    Given The user selects a expense from the list
    When The user clicks the "Update Selected" button
    And The selected expense is populated in textfields
    And The user provides Updated expense data in the text fields with future date
    And The user clicks the "Update Expense" button
    Then An error is shown containing the name of the expense
    #
    