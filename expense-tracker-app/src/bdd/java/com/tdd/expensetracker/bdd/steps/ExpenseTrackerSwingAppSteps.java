package com.tdd.expensetracker.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.awaitility.Awaitility.await;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import com.toedter.calendar.JDateChooser;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ExpenseTrackerSwingAppSteps {

	// FrameFixture instance to represent the expense and category windows
	private FrameFixture expenseTrackerWindow;
	private Robot robot = BasicRobot.robotWithCurrentAwtHierarchy(); // Robot instance for simulating user interactions
																		// same robot used for both
	// windows

	@After
	public void tearDown() {
		// Cleanup method to close any open windows after each test
		if (expenseTrackerWindow != null)
			expenseTrackerWindow.cleanUp();

	}

	@When("The Expense View is shown")
	public void the_Expense_View_is_shown() {
		// Initialize robot and launch the ExpenseTrackerSwingApp application

		application("com.tdd.expensetracker.app.ExpenseTrackerSwingApp").start();
		 robot.waitForIdle();
		// Find and assign the expense window
		expenseTrackerWindow = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Expense".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot);
		
	}

	@Then("The Expense view list contains an element with the following values")
	public void the_Expense_view_list_contains_an_element_with_the_following_values(List<List<String>> values) {
		// Assert that the expense list contains the specified values
		values.forEach(v -> await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->assertThat(expenseTrackerWindow.list().contents())
				.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1), v.get(2), v.get(3)))));
	}

	@When("The user enters the following values in the text fields")
	public void the_user_enters_the_following_values_in_the_text_fields(List<Map<String, String>> values) {
		// Enter the specified values into the text fields in the expense window
		values.stream().flatMap(m -> m.entrySet().stream()).forEach(e -> {
			if (e.getKey().contains("TextBox")) {
				expenseTrackerWindow.textBox(e.getKey()).setText("");
				expenseTrackerWindow.textBox(e.getKey()).enterText(e.getValue());
			} else if (e.getKey().contains("expenseDateChooser")) {

				JDateChooser jdateChooser = expenseTrackerWindow.robot().finder().findByName(e.getKey(),
						JDateChooser.class, true);

				Date localDateToDate = java.sql.Date.valueOf(e.getValue());
				
				jdateChooser.setDate(localDateToDate);

			} else if ((e.getKey().contains("ComboBox"))) {
				expenseTrackerWindow.comboBox(e.getKey()).selectItem(0);
			}
		});
	}

	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		// Click the button with the specified text in the expense window
		expenseTrackerWindow.button(JButtonMatcher.withText(buttonText)).click();
	}

	@When("The user enters a date {int} days in the future in {string}")
	public void the_user_enters_a_date_days_in_the_future_in(Integer noOfDays, String datePicker) {
		// Enter a date a specified number of days in the future into the date picker
		JDateChooser jdateChooser = expenseTrackerWindow.robot().finder().findByName(datePicker, JDateChooser.class,
				true);
		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now().plusDays(noOfDays));
		SwingUtilities.invokeLater(() -> {
		jdateChooser.setDate(localDateToDate);
		});
	}

	@Then("An error is shown containing the following values")
	public void an_error_is_shown_containing_the_following_values(List<List<String>> values) {
		// Assert that an error message containing the specified values is shown
		assertThat(expenseTrackerWindow.label("errorMessageLabel").text()).contains(values.get(0));
	}

	@Given("The user selects a expense from the list")
	public void the_user_selects_a_expense_from_the_list() {

		expenseTrackerWindow.list("expenseList").selectItem(Pattern.compile(".*" + DatabaseSteps.EXPENSE_NAME1 + ".*"));
	}

	@Then("The expense is removed from the list")
	public void the_expense_is_removed_from_the_list() {
		// Assert that the expense has been removed from the list
		assertThat(expenseTrackerWindow.list().contents()).noneMatch(e -> e.contains(DatabaseSteps.EXPENSE_NAME1));
	}

	@Then("All values are populated")
	public void all_values_are_populated(List<Map<String, String>> values) {
		// Assert that all fields are populated with the specified values

		values.stream().flatMap(m -> m.entrySet().stream()).forEach(e -> {
			if (e.getKey().contains("TextBox"))
				expenseTrackerWindow.textBox(e.getKey()).requireText(e.getValue());
			else if (e.getKey().contains("expenseDateChooser")) {
				  JDateChooser jdateChooser = findDateChooser(e.getKey());
				Date localDateToDate = java.sql.Date.valueOf(e.getValue());
				assertThat(jdateChooser.getDate()).isEqualTo(localDateToDate);

			} else if ((e.getKey().contains("ComboBox"))) {
				expenseTrackerWindow.comboBox(e.getKey()).requireSelection(e.getValue());

			}
		});
	}

	@When("The Category View is shown")
	public void the_Category_View_is_shown() {

		// Show the category view by first showing the expense view and clicking the
		// button to open the category view
		the_Expense_View_is_shown();
		
		expenseTrackerWindow.button("openCatButton").click();

		// Find and assign the category window
		expenseTrackerWindow = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Category".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot);
	}

	@Then("The Category view list contains an element with the following values")
	public void the_Category_view_list_contains_an_element_with_the_following_values(List<List<String>> values) {
		// Assert that the category list contains the specified values
		values.forEach(v -> assertThat(expenseTrackerWindow.list().contents())
				.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1))));
	}

	@When("The user selects a category from the list")
	public void the_user_selects_a_category_from_the_list() {
		// Write code here that turns the phrase above into concrete actions
		expenseTrackerWindow.list().selectItem(Pattern.compile(".*" + DatabaseSteps.CATEGORY_NAME1 + ".*"));
	}

	@Then("The category is removed from the list")
	public void the_category_is_removed_from_the_list() {
		// Write code here that turns the phrase above into concrete actions
		assertThat(expenseTrackerWindow.list().contents()).noneMatch(e -> e.contains(DatabaseSteps.CATEGORY_NAME1));

	}

	@Given("The user provides expense data in the text fields")
	public void the_user_provides_expense_data_in_the_text_fields() {
		expenseTrackerWindow.textBox("descriptionTextBox").enterText("new expense test");
		expenseTrackerWindow.textBox("amountTextBox").enterText("10");
		expenseTrackerWindow.comboBox("categoryComboBox").selectItem(0);
		 JDateChooser jdateChooser = findDateChooser("expenseDateChooser");
		

		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now());
		SwingUtilities.invokeLater(() -> {
			jdateChooser.setDate(localDateToDate);
		});

	}

	private JDateChooser findDateChooser(String name) {
		JDateChooser jdateChooser = expenseTrackerWindow.robot().finder()
	                .find(new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
	                    @Override
	                    protected boolean isMatching(JDateChooser chooser) {
	                        return chooser.getName().equals(name) && chooser.isShowing();
	                    }
	                });
		return jdateChooser;
	}

	@Then("The list contains the new expense")
	public void the_list_contains_the_new_expense() {
		assertThat(expenseTrackerWindow.list().contents()).anySatisfy(e -> assertThat(e).contains("10",
				"new expense test", LocalDate.now().toString(), DatabaseSteps.CATEGORY_NAME1));

	}

	@Given("The user provides expense data in the text fields, specifying a future date")
	public void the_user_provides_expense_data_in_the_text_fields_specifying_a_future_date() {
		expenseTrackerWindow.textBox("descriptionTextBox").enterText(DatabaseSteps.EXPENSE_NAME1);
		expenseTrackerWindow.textBox("amountTextBox").enterText("10");
		expenseTrackerWindow.comboBox("categoryComboBox").selectItem(0);
			  JDateChooser jdateChooser = findDateChooser("expenseDateChooser");

		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now().plusDays(10));
		SwingUtilities.invokeLater(() -> {
			jdateChooser.setDate(localDateToDate);
		});
	}

	@Then("An error is shown containing the name of the expense")
	public void an_error_is_shown_containing_the_name_of_the_expense() {
		assertThat(expenseTrackerWindow.label("errorMessageLabel").text()).contains(DatabaseSteps.EXPENSE_NAME1);
	}

	@When("The selected expense is populated in textfields")
	public void the_selected_expense_is_populated_in_textfields() {

		expenseTrackerWindow.textBox("descriptionTextBox").requireText(DatabaseSteps.EXPENSE_NAME1);
		expenseTrackerWindow.textBox("amountTextBox").requireText(String.valueOf(DatabaseSteps.EXPENSE_AMOUNT1));
		expenseTrackerWindow.comboBox("categoryComboBox").requireSelection(0);
		JDateChooser jdateChooser = expenseTrackerWindow.robot().finder().findByName("expenseDateChooser",
				JDateChooser.class, true);
		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now());
		assertThat(jdateChooser.getDate()).isEqualTo(localDateToDate);

	}

	@When("The user provides Updated expense data in the text fields")
	public void the_user_provides_Updated_expense_data_in_the_text_fields() {
		expenseTrackerWindow.textBox("descriptionTextBox").enterText(" updated text");
	}

	@Then("The list contains the updated expense")
	public void the_list_contains_the_updated_expense() {
		// Write code here that turns the phrase above into concrete actions
		assertThat(expenseTrackerWindow.list().contents())
				.anySatisfy(e -> assertThat(e).contains(String.valueOf(DatabaseSteps.EXPENSE_AMOUNT1),
						DatabaseSteps.EXPENSE_NAME1 + " updated text", LocalDate.now().toString(),
						DatabaseSteps.CATEGORY_NAME1));

	}

	@When("The user provides Updated expense data in the text fields with future date")
	public void the_user_provides_Updated_expense_data_in_the_text_fields_with_future_date() {
		JDateChooser jdateChooser = expenseTrackerWindow.robot().finder().findByName("expenseDateChooser",
				JDateChooser.class, true);

		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now().plusDays(10));
		SwingUtilities.invokeLater(() -> {
		jdateChooser.setDate(localDateToDate);
		});
	}

	@Given("The user provides category data in the text fields")
	public void the_user_provides_category_data_in_the_text_fields() {
		expenseTrackerWindow.textBox("nameTextBox").enterText("New Category");
		expenseTrackerWindow.textBox("descriptionTextBox").enterText("desc");
	}

	@Then("The list contains the new category")
	public void the_list_contains_the_new_category() {
		assertThat(expenseTrackerWindow.list().contents())
				.anySatisfy(e -> assertThat(e).contains("New Category", "desc"));
	}

	@Given("The user provides category data in the text fields, specifying existing name")
	public void the_user_provides_category_data_in_the_text_fields_specifying_existing_name() {
		expenseTrackerWindow.textBox("nameTextBox").enterText(DatabaseSteps.CATEGORY_NAME1);
		expenseTrackerWindow.textBox("descriptionTextBox").enterText("desc");
	}

	@Then("An error is shown containing the name of the category")
	public void an_error_is_shown_containing_the_name_of_the_category() {
		assertThat(expenseTrackerWindow.label("errorMessageLabel").text()).contains(DatabaseSteps.CATEGORY_NAME1);
	}

	@When("The selected category is populated in textfields")
	public void the_selected_category_is_populated_in_textfields() {
		expenseTrackerWindow.textBox("nameTextBox").requireText(DatabaseSteps.CATEGORY_NAME1);
		expenseTrackerWindow.textBox("descriptionTextBox").requireText(DatabaseSteps.CATEGORY_DESCRIPTION1);
	}

	@When("The user provides Updated category data in the text fields")
	public void the_user_provides_Updated_category_data_in_the_text_fields() {
		expenseTrackerWindow.textBox("nameTextBox").enterText(" updated text");
	}

	@Then("The list contains the updated category")
	public void the_list_contains_the_updated_category() {
		assertThat(expenseTrackerWindow.list().contents()).anySatisfy(e -> assertThat(e)
				.contains(DatabaseSteps.CATEGORY_NAME1 + " updated text", DatabaseSteps.CATEGORY_DESCRIPTION1));
	}

	@When("The user provides Updated category data in the text fields, specifying existing name")
	public void the_user_provides_Updated_category_data_in_the_text_fields_specifying_existing_name() {
		expenseTrackerWindow.textBox("nameTextBox").setText("");
		expenseTrackerWindow.textBox("nameTextBox").enterText(DatabaseSteps.CATEGORY_NAME2);
	}

	@Then("An error is shown containing the name of the existing category")
	public void an_error_is_shown_containing_the_name_of_the_existing_category() {
		assertThat(expenseTrackerWindow.label("errorMessageLabel").text()).contains(DatabaseSteps.CATEGORY_NAME2);
	}

}
