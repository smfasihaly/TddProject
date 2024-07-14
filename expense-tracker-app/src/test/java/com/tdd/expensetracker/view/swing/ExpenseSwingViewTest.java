package com.tdd.expensetracker.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.jdatepicker.impl.JDatePickerImpl;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

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
		window.label(JLabelMatcher.withText("Description"));
		window.textBox("descriptionTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Amount"));
		window.textBox("amountTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Date"));
		window.textBox("dateTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Category"));
		window.comboBox("categoryComboBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.list("expenseList");
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testWhenAllFieldsAreNonEmptyThenAddButtonShouldBeEnabled() {

		setFieldValues("decription", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireEnabled();
	}

	@Test
	public void testWhenAnyRequiredFieldsAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture descriptionTextBox = window.textBox("descriptionTextBox");
		JTextComponentFixture amountTextBox = window.textBox("amountTextBox");
		JTextComponentFixture datetextbox = window.textBox("dateTextBox");
		JComboBoxFixture categoryComboBox = window.comboBox("categoryComboBox");

		// empty Description
		setFieldValues("", "1000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank(descriptionTextBox, amountTextBox, datetextbox, categoryComboBox);

		// empty Amount
		setFieldValues("decription", "", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank(descriptionTextBox, amountTextBox, datetextbox, categoryComboBox);

		// empty date
		setFieldValues("decription", "1000", null, existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();

		resetAllFieldsToBlank(descriptionTextBox, amountTextBox, datetextbox, categoryComboBox);

		// empty category
		setFieldValues("decription", "1000", LocalDate.now(), null);
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
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
	public void testAddExpenseButtonShouldBeVisibleAndUpdateExpenseAndCancelButtonShouldBeHiddenAfterUpdatingExpense() {

		JTextComponentFixture descriptionTextBox = window.textBox("descriptionTextBox");

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		descriptionTextBox.setText("updated description");

		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Expense"));
		updateButton.click();
		updateButton.requireNotVisible();

		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		cancelButton.requireNotVisible();

		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Expense"));
		addButton.requireVisible();
		addButton.requireDisabled();

	}

	@Test
	public void testFormShouldBeResetAfterUpdatingExpense() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("descriptionTextBox").setText("updated description");
		window.button(JButtonMatcher.withText("Update Expense")).click();

		window.textBox("descriptionTextBox").requireText("");
		window.textBox("amountTextBox").requireText("");
		window.textBox("dateTextBox").requireText("");
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
	}

	@Test
	public void testFormResetAfterCancelingUpdate() {
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Cancel")).click();

		window.textBox("descriptionTextBox").requireText("");
		window.textBox("amountTextBox").requireText("");
		window.textBox("dateTextBox").requireText("");
		window.comboBox("categoryComboBox").requireNoSelection();
		window.button(JButtonMatcher.withText("Add Expense")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Expense")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();

	}

	@Test
	public void testSelectedExpenseShouldPopulateAllFieldsWhenUpdateSeletedButtonIsClicked() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));

		window.list("expenseList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		JTextComponentFixture descriptionTextBox = window.textBox("descriptionTextBox");
		JTextComponentFixture amountTextBox = window.textBox("amountTextBox");
		JTextComponentFixture datetextbox = window.textBox("dateTextBox");
		JComboBoxFixture categoryComboBox = window.comboBox("categoryComboBox");

		descriptionTextBox.requireText(expense.getDescription());
		amountTextBox.requireText(Double.toString(expense.getAmount()));
		datetextbox.requireText(expense.getDate().toString());
		categoryComboBox.requireSelection(expense.getCategory().toString());

	}

	//View Interface
	@Test
	public void testsShowAllExpensesShouldAddExpenseDescriptionsToTheList() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> expenseSwingView.showAllExpense(asList(expense, expense2)));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(expense.toString(), expense2.toString());

	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> expenseSwingView.showError("error message", expense));
		window.label("errorMessageLabel").requireText("error message: " + expense);
	}

	@Test
	public void testExpenseAddedShouldAddTheExpenseToTheListAndResetTheErrorLabel() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> expenseSwingView.expenseAdded(expense));
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(expense.toString());
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testExpenseDeleteShouldRemoveTheExpenseFromTheListAndResetTheErrorLabel() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense expense2 = new Expense("2", 50d, "testExpense2", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> {
			expenseSwingView.expenseAdded(expense);
			expenseSwingView.expenseAdded(expense2);
		});

		// execute
		GuiActionRunner.execute(() -> expenseSwingView
				.expenseDeleted(new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory)));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(expense2.toString());
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testExpenseUpdateShouldUpdateTheExpenseFromTheListAndResetTheErrorLabel() {

		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		GuiActionRunner.execute(() -> {

			expenseSwingView.expenseAdded(expense);
		});

		Expense updatedExpense = new Expense("1", 50d, "testExpense2", LocalDate.now(), existingCategory);
		// execute
		GuiActionRunner.execute(() -> expenseSwingView.expenseUpdated(updatedExpense));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(updatedExpense.toString());
		window.label("errorMessageLabel").requireText(" ");
	}

	// interaction with Controller
	@Test
	public void testAddButtonShouldDelegateToExpenseControllerNewExpense() {

		setFieldValues("testExpense", "5000", LocalDate.now(), existingCategory);
		window.button(JButtonMatcher.withText("Add Expense")).click();
		verify(expenseController).newExpense(new Expense(5000d, "testExpense", LocalDate.now(), existingCategory));
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
		verify(expenseController).deleteExpense(expense2);

	}

	@Test
	public void testUpdateButtonShouldDelegateToExpenseControllerUpdateExpense() throws InterruptedException {

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
		verify(expenseController).updateExpense(updatedExpense);

	}

	private void setFieldValues(String description, String amount, LocalDate date, Category category) {
		JDatePickerImpl datePicker = (JDatePickerImpl) window.robot().finder().findByName("datePicker");

		window.textBox("descriptionTextBox").enterText(description);
		window.textBox("amountTextBox").enterText(amount);
		if (category != null) {
			window.comboBox("categoryComboBox").selectItem(0);
		} else {
			window.comboBox("categoryComboBox").clearSelection();
		}
		if (date != null) {
			GuiActionRunner.execute(() -> {

				datePicker.getModel().setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
				datePicker.getModel().setSelected(true);
			});
		}
	}

	private void resetAllFieldsToBlank(JTextComponentFixture descriptionTextBox, JTextComponentFixture amountTextBox,
			JTextComponentFixture datetextbox, JComboBoxFixture categoryComboBox) {
		descriptionTextBox.setText("");
		amountTextBox.setText("");
		descriptionTextBox.setText("");
		categoryComboBox.clearSelection();
		JDatePickerImpl datePicker = (JDatePickerImpl) window.robot().finder().findByName("datePicker");
		GuiActionRunner.execute(() -> {

			datePicker.getModel().setValue(null);
		});
	}
}