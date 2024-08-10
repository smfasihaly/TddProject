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

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {

		existingCategory = new Category("1", "name1", "description1");
		closeable = MockitoAnnotations.openMocks(this);

		GuiActionRunner.execute(() -> {
			expenseSwingView = new ExpenseSwingView();
			expenseSwingView.setExpenseController(expenseController);

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

	@Test
	@GUITest
	public void testControlsInitialStates() {

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.isVisible()).isFalse();
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
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

	@Test
	public void testWhenAllFieldsAreNonEmptyThenAddButtonShouldBeEnabled() {

		setFieldValues("description", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireEnabled();
	}

	@Test
	public void testWhenAnyRequiredFieldsAreBlankThenAddButtonShouldBeDisabled() {

		// empty Description
		setFieldValues("", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty Amount
		setFieldValues("description", "", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty date
		setFieldValues("description", "1000", null, existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty category
		setFieldValues("description", "1000", LocalDate.now(), null);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

	}

	@Test
	public void testAmountFieldOnlyAcceptNumbers() {

		JTextComponentFixture amountTxtBox = window.textBox("amountTextBox");

		// Test entering only digits
		amountTxtBox.enterText("123456");
		amountTxtBox.requireText("123456");

		amountTxtBox.setText(null);

		// Test entering letters (should not be accepted)
		amountTxtBox.enterText("abcd");
		amountTxtBox.requireText("");

		amountTxtBox.setText(null);

		// Test entering a mix of digits and letters (only digits should be accepted)
		amountTxtBox.enterText("12abcd345");
		amountTxtBox.requireText("12345");

		// Test entering digits with backspace
		amountTxtBox.setText(null);
		amountTxtBox.enterText("12345");
		amountTxtBox.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE);
		amountTxtBox.requireText("1234");

		// Test entering digits with delete
		amountTxtBox.setText(null);
		amountTxtBox.enterText("12345");
		amountTxtBox.selectText(2, 3); // Select the third character
		amountTxtBox.pressAndReleaseKeys(KeyEvent.VK_DELETE);
		amountTxtBox.requireText("1245");
	}

	

	@Test
	public void testDeleteSelectedAndUpdateSelectedButtonShouldBeEnabledOnlyWhenAExpenseIsSelected() {

		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);

		JButtonFixture deleteSelectedButton = window.button(JButtonMatcher.withText("Delete Selected"));
		deleteSelectedButton.requireEnabled();

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.requireEnabled();

		window.list("expenseList").clearSelection();
		deleteSelectedButton.requireDisabled();
		updateSelectedButton.requireDisabled();
	}

	@Test
	public void testSelectedExpenseShouldPopulateAllFieldsWhenUpdateSeletedButtonIsClicked() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();
		
		window.textBox("descriptionTextBox").requireText("testExpense");
		window.textBox("amountTextBox").requireText(String.valueOf(5000d));
		window.comboBox("categoryComboBox").requireSelection(0);
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class,
				true);
		Date localDateToDate = java.sql.Date.valueOf(LocalDate.now());
		assertThat(jdateChooser.getDate()).isEqualTo(localDateToDate);
		

	}
	
	
	@Test
	public void testWhenAnyRequiredFieldsAreBlankThenUpdateButtonShouldBeDisabled() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		resetAllFieldsToBlank();
		// empty Description
		setFieldValues("", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty Amount
		setFieldValues("description", "", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty date
		setFieldValues("description", "1000", null, existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank();

		// empty category
		setFieldValues("description", "1000", LocalDate.now(), null);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

	}


	@Test
	public void testUpdateExpenseAndCancelButtonShouldBeVisibleAndAddExpenseButtonShouldBeHiddenWhenUpdateSeletedButtonIsClicked() {

		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Expense"));
		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Expense"));
		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));

		addButton.requireNotVisible();
		updateButton.requireEnabled();
		cancelButton.requireEnabled();
		updateButton.requireVisible();

	}

	@Test
	public void testAnyRequiredFieldsAreBlankThenUpdateExpenseButtonShouldBeDisabled() {

		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel()
				.addElement(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));
		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		JTextComponentFixture descriptionTextBox = window.textBox("descriptionTextBox");

		descriptionTextBox.setText("");
		descriptionTextBox.enterText("  ");
		window.button(JButtonMatcher.withText("Update Expense")).requireDisabled();
	}

	

	

	@Test
	public void testFormResetAfterCancelingUpdate() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Cancel")).click();

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("descriptionTextBox").requireText("");
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();

	}

	// View Interface
	@Test
	public void testsShowAllExpensesShouldAddExpenseDescriptionsToTheListUpdateTotal() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> expenseSwingView.showAllExpense(asList(expense, expense2)));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(expense), getDisplayString(expense2));
		window.label("totalLabel").requireText("Total: 5050.0");
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.showError("error message", expense);
		window.label("errorMessageLabel").requireText("error message: " + expense);
	}

	@Test
	public void testExpenseAddedShouldAddTheExpenseToTheListAndResetTheErrorLabelAndUpdateTotal() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.expenseAdded(expense);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(expense));
		window.label("errorMessageLabel").requireText(" ");
		window.label("totalLabel").requireText("Total: 5000.0");
	}
	
	@Test
	public void testFormShouldBeResetAfterAddingCategory() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.expenseAdded(expense);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();


	}
	

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

	@Test
	public void testExpenseUpdateShouldUpdateTheExpenseFromTheListAndResetTheErrorLabelUpdateTotal() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.expenseAdded(expense);
		window.label("totalLabel").requireText("Total: 5000.0");
		
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		// execute
		expenseSwingView.expenseUpdated(updatedExpense);

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(getDisplayString(updatedExpense));
		window.label("errorMessageLabel").requireText(" ");
		window.label("totalLabel").requireText("Total: 50.0");
	}
	
	@Test
	public void testFormShouldBeResetAfterUpdatingExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.expenseAdded(expense);
		
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		// execute
		expenseSwingView.expenseUpdated(updatedExpense);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("amountTextBox").requireText("");
		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
		assertThat(jdateChooser.getDate()).isNull();
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
	}
	
	@Test
	public void testAddExpenseButtonShouldBeVisibleAndUpdateExpenseAndCancelButtonShouldBeHiddenAfterUpdatingExpense() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseSwingView.expenseAdded(expense);
		
		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		// execute
		expenseSwingView.expenseUpdated(updatedExpense);

		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Expense"));

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> updateButton.requireNotVisible());

		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		cancelButton.requireNotVisible();

		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Expense"));
		addButton.requireVisible();
		addButton.requireDisabled();

	}

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

	// interaction with Controller
	@Test
	public void testAddButtonShouldDelegateToExpenseControllerNewExpense() {

		setFieldValues("testExpense", "5000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(expenseController)
				.newExpense(new Expense(5000d, "testExpense", LocalDate.now(), existingCategory)));
	}

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
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(expenseController).deleteExpense(expense2));

	}

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
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(expenseController).updateExpense(updatedExpense));
	}
	
	

	private void setFieldValues(String description, String amount, LocalDate date, Category category) {

		window.textBox("descriptionTextBox").enterText(description);
		window.textBox("amountTextBox").enterText(amount);
		if (category != null) {
			window.comboBox("categoryComboBox").selectItem(0);
		} else {
			window.comboBox("categoryComboBox").clearSelection();
		}
		if (date != null) {

			JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
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

		JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser",JDateChooser.class, false);
		GuiActionRunner.execute(() -> {

			jdateChooser.setDate(null);
		});
	}
	private String getDisplayString(Expense expense) {
		// TODO Auto-generated method stub
		return expense.getId() + " | " + expense.getDescription() +  " | " + expense.getAmount() + " | " + expense.getDate() + " | " + expense.getCategory().getName()  ;
	}
}