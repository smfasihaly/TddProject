package com.tdd.expensetracker.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.awt.event.KeyEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JTextField;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

public class ExpenseSwingViewTest extends AssertJSwingJUnitTestCase {

	private ExpenseSwingView expenseSwingView;
	private FrameFixture window;
	private Category existingCategory;

	@Mock
	private ExpenseController expenseController;
	@Mock
	private CategorySwingView categorySwingView;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		existingCategory = new Category("1", "name1", "description1");
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			expenseSwingView = new ExpenseSwingView();
			expenseSwingView.setExpenseController(expenseController);
			expenseSwingView.setCategoryView(categorySwingView);
			expenseSwingView.getComboCategoriesModel().addElement(existingCategory);
			expenseSwingView.getComboCategoriesModel().addElement(new Category("2", "Bills", "Utilities"));
			return expenseSwingView;
		});
		window = new FrameFixture(robot(), expenseSwingView);
		window.show();
		window.comboBox("categoryComboBox").clearSelection();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	// Test the initial state of all the UI controls
	@Test
	@GUITest
	public void testControlsInitialStates() {
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.isVisible()).isFalse();
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, false);
		assertThat(jdateChooser.isVisible()).isTrue();
		JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) jdateChooser.getDateEditor();
		assertThat(dateEditor.isEditable()).isFalse();
		window.label(JLabelMatcher.withText("Description"));
		window.textBox("descriptionTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Amount"));
		window.textBox("amountTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Date"));
		window.label(JLabelMatcher.withText("Category"));
		window.comboBox("categoryComboBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.list("expenseList");
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Open Category Form")).requireVisible().requireEnabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
		window.label("totalLabel").requireText("Total: 0");
		window.label("errorMessageLabel").requireText(" ");
	}

	// Test enabling Add button when all fields are non-empty
	@Test
	public void testWhenAllFieldsAreNonEmptyThenAddButtonShouldBeEnabled() {
		setFieldValues("description", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireEnabled();
	}

	// Test disabling Add button when any required field is blank
	@Test
	public void testWhenAnyRequiredFieldsAreBlankThenAddButtonShouldBeDisabled() {
		setFieldValues("", "1000", LocalDate.now(), existingCategory); // empty Description
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "", LocalDate.now(), existingCategory); // empty Amount
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "1000", null, existingCategory); // empty Date
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "1000", LocalDate.now(), null); // empty Category
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
	}

	// Test that Amount field only accepts numbers
	@Test
	public void testAmountFieldOnlyAcceptNumbers() {
		JTextComponentFixture amountTxtBox = window.textBox("amountTextBox");
		amountTxtBox.enterText("123456"); // digits only
		amountTxtBox.requireText("123456");
		amountTxtBox.setText(null);
		amountTxtBox.enterText("abcd"); // letters only
		amountTxtBox.requireText("");
		amountTxtBox.setText(null);
		amountTxtBox.enterText("12abcd345"); // mixed input (only digits should remain)
		amountTxtBox.requireText("12345");
		amountTxtBox.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE);
		amountTxtBox.pressAndReleaseKeys(KeyEvent.VK_DELETE);
	}

	// Test enabling Delete and Update buttons when an expense is selected
	@Test
	public void testDeleteSelectedAndUpdateSelectedButtonShouldBeEnabledOnlyWhenAExpenseIsSelected() {
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).requireEnabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireEnabled();
		window.list("expenseList").clearSelection();
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireDisabled();
	}

	// Test that selecting an expense populates the fields for update
	@Test
	public void testSelectedExpenseShouldPopulateAllFieldsWhenUpdateSeletedButtonIsClicked() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("descriptionTextBox").requireText("testExpense");
		window.textBox("amountTextBox").requireText(String.valueOf(5000d));
		window.comboBox("categoryComboBox").requireSelection(0);
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, true);
		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now());
		assertThat(jdateChooser.getDate()).isEqualTo(localDateToDate);
	}

	// Test disabling the Update button when required fields are blank
	@Test
	public void testWhenAnyRequiredFieldsAreBlankThenUpdateButtonShouldBeDisabled() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		resetAllFieldsToBlank();
		setFieldValues("", "1000", LocalDate.now(), existingCategory); // empty Description
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "", LocalDate.now(), existingCategory); // empty Amount
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "1000", null, existingCategory); // empty Date
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		resetAllFieldsToBlank();
		setFieldValues("description", "1000", LocalDate.now(), null); // empty Category
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
	}

	// Test visibility of Update and Cancel buttons when updating an expense
	@Test
	public void testUpdateExpenseAndCancelButtonShouldBeVisibleAndAddExpenseButtonShouldBeHiddenWhenUpdateSeletedButtonIsClicked() {
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Add Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Update Expense")).requireEnabled();
		window.button(JButtonMatcher.withText("Cancel")).requireEnabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireVisible();
	}

	// Test disabling the Update Expense button when fields are blank
	@Test
	public void testAnyRequiredFieldsAreBlankThenUpdateExpenseButtonShouldBeDisabled() {
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("descriptionTextBox").setText("");
		window.textBox("descriptionTextBox").enterText("  ");
		window.button(JButtonMatcher.withText("Update Expense")).requireDisabled();
	}

	// Test resetting the form after canceling an update
	@Test
	public void testFormResetAfterCancelingUpdate() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Cancel")).click();
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.getText()).isBlank();
		window.textBox("descriptionTextBox").requireText("");
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
	}

	// Test opening the Category Form when button is clicked
	@Test
	public void testOpenCategoryFormShouldOpenExpenseForm() {
		window.button(JButtonMatcher.withText("Open Category Form")).click();
		verify(categorySwingView).setVisible(true);
	}

	// Test showing all expenses in the list and updating total
	@Test
	public void testsShowAllExpensesShouldAddExpenseDescriptionsToTheListUpdateTotal() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.showAllExpense(asList(expense, expense2)));
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(expense), getDisplayString(expense2));
		window.label("totalLabel").requireText("Total: 5050.0");
	}

	// Test showing an error message in the error label
	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.showError("error message", expense);
		window.label("errorMessageLabel").requireText("error message: " + expense);
	}

	// Test showing an error when an expense is not found
	@Test
	public void testShowErrorExpenseNotFoundShouldShowTheMessageInTheErrorLabel() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.showErrorExpenseNotFound("error message", expense);
		window.label("errorMessageLabel").requireText("error message: " + expense);
	}

	// Test adding an expense and resetting the error label and total
	@Test
	public void testExpenseAddedShouldAddTheExpenseToTheListAndResetTheErrorLabelAndUpdateTotal() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(expense));
		window.label("errorMessageLabel").requireText(" ");
		window.label("totalLabel").requireText("Total: 5000.0");
	}

	// Test resetting the form after adding an expense
	@Test
	public void testFormShouldBeResetAfterAddingCategory() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
	}

	// Test removing an expense from the list and resetting the error label and
	// total
	@Test
	public void testExpenseDeleteShouldRemoveTheExpenseFromTheListAndResetTheErrorLabelAndUpdateTotal() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		expenseSwingView.expenseAdded(expense2);
		window.label("totalLabel").requireText("Total: 5050.0");
		expenseSwingView.expenseDeleted(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory));
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(expense2));
		window.label("errorMessageLabel").requireText(" ");
		window.label("totalLabel").requireText("Total: 50.0");
	}

	// Test updating an expense in the list and resetting the error label and total
	@Test
	public void testExpenseUpdateShouldUpdateTheExpenseFromTheListAndResetTheErrorLabelUpdateTotal() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		window.label("totalLabel").requireText("Total: 5000.0");
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		expenseSwingView.expenseUpdated(updatedExpense);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(updatedExpense));
		window.label("errorMessageLabel").requireText(" ");
		window.label("totalLabel").requireText("Total: 50.0");
	}

	// Test resetting the form after updating an expense
	@Test
	public void testFormShouldBeResetAfterUpdatingExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		expenseSwingView.expenseUpdated(updatedExpense);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.getText()).isBlank();
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
	}

	// Test visibility of buttons after updating an expense
	@Test
	public void testAddExpenseButtonShouldBeVisibleAndUpdateExpenseAndCancelButtonShouldBeHiddenAfterUpdatingExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseSwingView.expenseAdded(expense);
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		expenseSwingView.expenseUpdated(updatedExpense);
		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Expense"));
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> updateButton.requireNotVisible());
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
		window.button(JButtonMatcher.withText("Add Expense")).requireVisible().requireDisabled();
	}

	// Test showing all categories in the combo box
	@Test
	public void testsShowAllCategoryShouldAddCategoriesToTheCombox() {
		Category category = new Category("2", "Bills", "Utilities");
		GuiActionRunner.execute(() -> {
			expenseSwingView.getComboCategoriesModel().removeAllElements();
			expenseSwingView.showAllCategory(asList(existingCategory, category));
		});
		String[] comboBoxContent = window.comboBox().contents();
		assertThat(comboBoxContent).containsExactly(existingCategory.getName(), category.getName());
	}

	// Test adding an expense via the controller
	@Test
	public void testAddButtonShouldDelegateToExpenseControllerNewExpense() {
		setFieldValues("testExpense", "5000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).click();
		await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> verify(expenseController)
				.newExpense(new Expense(5000d, "testExpense", LocalDate.now(), existingCategory)));
	}

	// Test deleting an expense via the controller
	@Test
	public void testDeleteButtonShouldDelegateToExpenseControllerDeleteExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Expense> listStudentsModel = expenseSwingView.getListExpenseModel();
			listStudentsModel.addElement(expense);
			listStudentsModel.addElement(expense2);
		});
		window.list("expenseList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> verify(expenseController).deleteExpense(expense2));
	}

	// Test updating an expense via the controller
	@Test
	public void testUpdateButtonShouldDelegateToExpenseControllerUpdateExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Expense> listStudentsModel = expenseSwingView.getListExpenseModel();
			listStudentsModel.addElement(expense);
		});
		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("amountTextBox").setText(Double.toString(50d));
		window.button(JButtonMatcher.withText("Update Expense")).click();
		Expense updatedExpense = new Expense("1", 50d, "testExpense", LocalDate.now(), existingCategory);
		await().atMost(10, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(expenseController).updateExpense(updatedExpense));
	}

	// Utility methods to set and reset field values
	private void setFieldValues(String description, String amount, LocalDate date, Category category) {
		window.textBox("descriptionTextBox").enterText(description);
		window.textBox("amountTextBox").enterText(amount);
		if (category != null) {
			window.comboBox("categoryComboBox").selectItem(0);
		} else {
			window.comboBox("categoryComboBox").clearSelection();
		}
		if (date != null) {
			JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class,
					false);
			GuiActionRunner.execute(() -> {
				Date localDateToDate = java.sql.Date.valueOf(date);
				jdateChooser.setDate(localDateToDate);
			});
		}
	}

	private void resetAllFieldsToBlank() {
		window.textBox("descriptionTextBox").setText("");
		window.textBox("amountTextBox").setText("");
		window.textBox("descriptionTextBox").setText("");
		window.comboBox("categoryComboBox").clearSelection();
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class, false);
		GuiActionRunner.execute(() -> {
			jdateChooser.setDate(null);
		});
	}

	private String getDisplayString(Expense expense) {
		return expense.getId() + " | " + expense.getDescription() + " | " + expense.getAmount() + " | "
				+ expense.getDate() + " | " + expense.getCategory().getName();
	}
}