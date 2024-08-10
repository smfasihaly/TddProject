Feature: Category Application Frame
  Specifications of the behavior of the Category Application Frame

  Scenario: The initial state of the view
    Given The database contains category with the following values
      | bills | utilities |
      | food  | Personal  |
    When The Category View is shown
    Then The Category view list contains an element with the following values
      | bills | utilities |
      | food  | Personal  |

  Scenario: Add a new Category
    When The Category View is shown
    And The user enters the following values in the text fields
      | nameTextBox | descriptionTextBox |
      | food        | first category     |
    And The user clicks the "Add Category" button
    Then The Category view list contains an element with the following values
      | food | first category |

  Scenario: Add a new Category with existing name
    Given The database contains category with the following values
      | bills | utilities |
      | food  | Personal  |
    When The Category View is shown
    And The user enters the following values in the text fields
      | nameTextBox | descriptionTextBox |
      | food        | second category    |
    And The user clicks the "Add Category" button
    Then An error is shown containing the following values
      | food | Personal |

  Scenario: Delete a category
    Given The database contains category with the following values
      | bills | utilities |
      | food  | Personal  |
    When The Category View is shown
    And The user selects a category from the list
    When The user clicks the "Delete Selected" button
    Then The category is removed from the list

  Scenario: Update a category
    Given The database contains category with the following values
      | bills | utilities |
      | food  | Personal  |
    When The Category View is shown
    And The user selects a category from the list
    And The user clicks the "Update Selected" button
    And All values are populated
      | nameTextBox | descriptionTextBox |
      | bills       | utilities          |
    When The user enters the following values in the text fields
      | nameTextBox |
      | bills- Updated   |
    And The user clicks the "Update Category" button
    Then The Category view list contains an element with the following values
      | bills- Updated | utilities |
      | food           | Personal  |

  Scenario: Update a category with existing name
    Given The database contains category with the following values
      | bills          | utilities |
      | categoryName2 | Personal  |
    When The Category View is shown
    And The user selects a category from the list
    And The user clicks the "Update Selected" button
    And All values are populated
      | nameTextBox | descriptionTextBox |
      | bills       | utilities          |
    When The user enters the following values in the text fields
      | nameTextBox |
      |   categoryName2 |
    And The user clicks the "Update Category" button
    Then An error is shown containing the following values
      | categoryName2 | Personal |
