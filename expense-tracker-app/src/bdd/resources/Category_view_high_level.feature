Feature: Category View High Level
  Specifications of the behavior of the Category View

  Background: 
    Given The database contains a few category
    And The Category View is shown

  Scenario: Add a new Category
    Given The user provides category data in the text fields
    And The user clicks the "Add Category" button
    Then The list contains the new category

  Scenario: Add a new category with a existing name
    Given The user provides category data in the text fields, specifying existing name
    When The user clicks the "Add Category" button
    Then An error is shown containing the name of the category

  Scenario: Delete a category
    Given The user selects a category from the list
    When The user clicks the "Delete Selected" button
    Then The category is removed from the list

  Scenario: Delete a not existing category
    Given The user selects a category from the list
    But The category is in the meantime removed from the database
    When The user clicks the "Delete Selected" button
    Then An error is shown containing the name of the category
    And The category is removed from the list
    
  Scenario: Update a category
    Given The user selects a category from the list
    When The user clicks the "Update Selected" button
    And The selected category is populated in textfields
    And The user provides Updated category data in the text fields
    And The user clicks the "Update Category" button
    Then The list contains the updated category
    
    Scenario: Update a with a with a existing name
    Given The user selects a category from the list
    When The user clicks the "Update Selected" button
    And The selected category is populated in textfields
    And The user provides Updated category data in the text fields, specifying existing name
    And The user clicks the "Update Category" button
    Then An error is shown containing the name of the existing category
    
    