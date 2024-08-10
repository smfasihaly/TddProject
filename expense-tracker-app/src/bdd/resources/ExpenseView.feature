Feature: Expense Application Frame
  Specifications of the behavior of the Expense Application Frame

  Scenario: The initial state of the view
    Given The database contains the Expense with the following values
      | 1000.0 | first expense  | 2024-07-25 | bills | utilities |
      | 5000.0 | second expense | 2024-07-25 | bills | utilities |
    When The Expense View is shown
    Then The Expense view list contains an element with the following values
      | 1000.0 | first expense  | 2024-07-25 | bills |
      | 5000.0 | second expense | 2024-07-25 | bills |

  Scenario: Add a new Expense
    Given The database contains category with the following values
      | bills | utilities |
    When The Expense View is shown
    And The user enters the following values in the text fields
      | amountTextBox | descriptionTextBox | expenseDateChooser | categoryComboBox |
      |           100 | first expense      | 2024-07-25         | bills            |
    And The user clicks the "Add Expense" button
    Then The Expense view list contains an element with the following values
     | 100 | first expense | 2024-07-25 | bills |

  Scenario: Add a new Expense with Future Date
    Given The database contains category with the following values
      | bills | utilities |
    When The Expense View is shown
    And The user enters the following values in the text fields
      | amountTextBox | descriptionTextBox | categoryComboBox |
      |           100 | first expense      | bills            |
    And The user enters a date 10 days in the future in "expenseDateChooser"
    And The user clicks the "Add Expense" button
    Then An error is shown containing the following values
      | 100 | first expense | bills |

  Scenario: Delete a expense
    Given The database contains the Expense with the following values
      | 1000.0 | first expense  | 2024-07-25 | bills | utilities |
      | 5000.0 | second expense | 2024-07-25 | bills | utilities |
    And The Expense View is shown
    And The user selects a expense from the list
    When The user clicks the "Delete Selected" button
    Then The expense is removed from the list

  Scenario: Update a expense
    Given The database contains the Expense with the following values
      | 1000.0 | first expense  | 2024-07-25 | bills | utilities |
      | 5000.0 | second expense | 2024-07-25 | bills | utilities |
    And The Expense View is shown
    And The user selects a expense from the list
    When The user clicks the "Update Selected" button
    Then All values are populated
      | amountTextBox | descriptionTextBox | categoryComboBox |
      |        1000.0 | first expense      | bills            |
    When The user enters the following values in the text fields
      | descriptionTextBox |
      | first expenseUpdated            |
    And The user clicks the "Update Expense" button
    Then The Expense view list contains an element with the following values
      | 1000.0 | first expenseUpdated | 2024-07-25 | bills |
      | 5000.0 | second expense       | 2024-07-25 | bills |

  Scenario: Update a expense with  Future Date
    Given The database contains the Expense with the following values
      | 1000.0 | first expense  | 2024-07-25 | bills | utilities |
      | 5000.0 | second expense | 2024-07-25 | bills | utilities |
    And The Expense View is shown
    And The user selects a expense from the list
    When The user clicks the "Update Selected" button
    Then All values are populated
      | amountTextBox | descriptionTextBox | categoryComboBox |
      |        1000.0 | first expense      | bills            |
   When The user enters a date 10 days in the future in "expenseDateChooser"
    And The user clicks the "Update Expense" button
    Then An error is shown containing the following values
       | 1000.0 | first expense |  bills | utilities |
    

