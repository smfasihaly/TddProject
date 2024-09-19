package com.tdd.expensetracker.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.controller.CategoryController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public class CategorySwingViewTest extends AssertJSwingJUnitTestCase {

	private CategorySwingView categorySwingView;
	private FrameFixture window;

	@Mock
	private CategoryController categoryController;

	@Mock
	private ExpenseSwingView expenseSwingView;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			categorySwingView = new CategorySwingView();
			categorySwingView.setCategoryController(categoryController);
			categorySwingView.setExpenseView(expenseSwingView);
			return categorySwingView;
		});
		window = new FrameFixture(robot(), categorySwingView);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	// Test initial states of UI controls
	@Test
	@GUITest
	public void testControlsInitialStates() {
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.isVisible()).isFalse();
		JScrollPane expenseTable = window.robot().finder().findByName("scrollPaneExpenseTable", JScrollPane.class,
				false);
		assertThat(expenseTable.isVisible()).isFalse();
		window.label(JLabelMatcher.withText("Name"));
		window.textBox("nameTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("Description"));
		window.textBox("descriptionTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
		window.list("categoryList");
		window.label(JLabelMatcher.withText("X")).requireNotVisible();
		window.label(JLabelMatcher.withText("Total: 0.0")).requireNotVisible();
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Show Expenses")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Category")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
		window.button(JButtonMatcher.withText("Open Expense Form")).requireVisible().requireEnabled();
		window.label("errorMessageLabel").requireText(" ");
	}

	// Test that the Add button is enabled when the name field is non-empty
	@Test
	public void testWhenNameFieldIsNonEmptyThenAddButtonShouldBeEnabled() {
		setFieldValues("name", "");
		window.button(JButtonMatcher.withText("Add Category")).requireEnabled();
	}

	// Test that the Add button is disabled when the name field is blank
	@Test
	public void testWhenNameRequiredFieldIsBlankThenAddButtonShouldBeDisabled() {
		setFieldValues("", "1000");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

	// Test enabling buttons when a category is selected
	@Test
	public void testDeleteSelectedAndUpdateSelectedButtonShouldBeEnabledOnlyWhenACategoryIsSelected() {
		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);
		JButtonFixture deleteSelectedButton = window.button(JButtonMatcher.withText("Delete Selected"));
		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		JButtonFixture showExpenseButton = window.button(JButtonMatcher.withText("Show Expenses"));
		deleteSelectedButton.requireEnabled();
		updateSelectedButton.requireEnabled();
		showExpenseButton.requireEnabled();
		window.list("categoryList").clearSelection();
		deleteSelectedButton.requireDisabled();
		updateSelectedButton.requireDisabled();
		showExpenseButton.requireDisabled();
	}

	// Test that selected category populates fields when Update is clicked
	@Test
	public void testSelectedCategoryShouldPopulateAllFieldsWhenUpdateSeletedButtonIsClicked() {
		Category category = new Category("1", "bills", "utilities");
		GuiActionRunner.execute(() -> {
			categorySwingView.getListCategoryModel().addElement(category);
		});
		window.list("categoryList").selectItem(0);
		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(category.getId()).isEqualTo(idTextBox.getText());
		window.textBox("nameTextBox").requireText(category.getName());
		window.textBox("descriptionTextBox").requireText(category.getDescription());
	}

	// Test visibility of Update and Cancel buttons when updating a category
	@Test
	public void testUpdateCategoryAndCancelButtonShouldBeVisibleAndAddCategoryButtonShouldBeHiddenWhenUpdateSeletedButtonIsClicked() {
		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);
		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();
		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Category"));
		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Category"));
		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		addButton.requireNotVisible();
		cancelButton.requireEnabled();
		updateButton.requireVisible();
		updateButton.requireEnabled();
	}

	// Test that the Update button is disabled when name field is blank
	@Test
	public void testWhenNameRequiredFieldIsBlankThenUpdateButtonShouldBeDisabled() {
		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);
		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();
		window.textBox("descriptionTextBox").enterText(" changed");
		window.button(JButtonMatcher.withText("Update Category")).requireEnabled();
		window.textBox("nameTextBox").setText("");
		window.textBox("nameTextBox").enterText("  ");
		window.button(JButtonMatcher.withText("Update Category")).requireDisabled();
	}

	// Test resetting the form after canceling the update
	@Test
	public void testFormResetAfterCancelingUpdate() {
		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Cancel")).click();
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.getText()).isBlank();
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Category")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
	}

	// Test hiding the expense table when the cross label is clicked
	@Test
	public void testExpenseTableAndCrossShouldBeHiddenWhenCrossLabelClicked() {
		JLabel crossLabel = window.robot().finder().findByName("crossLabel", JLabel.class, false);
		JScrollPane expenseTable = window.robot().finder().findByName("scrollPaneExpenseTable", JScrollPane.class,
				false);
		JLabel totalLabel = window.robot().finder().findByName("totalLabel", JLabel.class, false);
		GuiActionRunner.execute(() -> {
			crossLabel.setVisible(true);
			expenseTable.setVisible(true);
			totalLabel.setVisible(true);
		});
		window.label("crossLabel").click();
		assertThat(expenseTable.isVisible()).isFalse();
		assertThat(crossLabel.isVisible()).isFalse();
		assertThat(totalLabel.isVisible()).isFalse();
	}

	// Test that the expense form opens when "Open Expense Form" button is clicked
	@Test
	public void testOpenExpenseFormShouldOpenExpenseForm() {
		window.button(JButtonMatcher.withText("Open Expense Form")).click();
		verify(expenseSwingView).setVisible(true);
	}

	// Test displaying all categories in the list
	@Test
	public void testsShowAllCategorysShouldAddCategoryDescriptionsToTheList() {
		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");
		GuiActionRunner.execute(() -> categorySwingView.showAllCategory(asList(category, category2)));
		assertThat(window.list().contents()).containsExactly(getDisplayString(category), getDisplayString(category2));
	}

	// Test showing an error message in the label
	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.showError("error message", category);
		window.label("errorMessageLabel").requireText("error message: " + category);
	}

	// Test showing an error when a category is not found
	@Test
	public void testShowErrorCategoryNotFoundShouldShowTheMessageInTheErrorLabel() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.showErrorCategoryNotFound("error message", category);
		window.label("errorMessageLabel").requireText("error message: " + category);
	}

	// Test adding a category to the list and resetting the error label
	@Test
	public void testCategoryAddedShouldAddTheCategoryToTheListAndResetTheErrorLabel() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.categoryAdded(category);
		assertThat(window.list().contents()).containsExactly(getDisplayString(category));
		window.label("errorMessageLabel").requireText(" ");
	}

	// Test resetting the form after adding a category
	@Test
	public void testFormShouldBeResetAfterAddingCategory() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.categoryAdded(category);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat(idTextBox.getText()).isBlank();
		window.textBox("nameTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

	// Test deleting a category from the list and resetting the error label
	@Test
	public void testCategoryDeleteShouldRemoveTheCategoryFromTheListAndResetTheErrorLabel() {
		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");
		GuiActionRunner.execute(() -> {
			categorySwingView.categoryAdded(category);
			categorySwingView.categoryAdded(category2);
		});
		categorySwingView.categoryDeleted(new Category("1", "bills", "utilities"));
		assertThat(window.list().contents()).containsExactly(getDisplayString(category2));
		window.label("errorMessageLabel").requireText(" ");
	}

	// Test updating a category in the list and resetting the error label
	@Test
	public void testCategoryUpdateShouldUpdateTheCategoryFromTheListAndResetTheErrorLabel() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.categoryAdded(category);
		Category updatedCategory = new Category("1", "testCategory2", "test");
		categorySwingView.categoryUpdated(updatedCategory);
		assertThat(window.list().contents()).containsExactly(getDisplayString(updatedCategory));
		window.label("errorMessageLabel").requireText(" ");
	}

	// Test resetting the form after updating a category
	@Test
	public void testFormShouldBeResetAfterUpdatingCategory() {
		Category category = new Category("1", "bills", "utilities");
		categorySwingView.categoryAdded(category);
		Category updatedCategory = new Category("1", "testCategory2", "test");
		categorySwingView.categoryUpdated(updatedCategory);
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(idTextBox.getText()).isBlank());
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

	// Test that category is not updated if it is not found
	@Test
	public void testCategoryUpdateShouldNotUpdateWhenCategoryNotFound() {
		Category category = new Category("1", "bills", "utilities");
		Category updatedCategory = new Category("2", "testCategory2", "test");
		categorySwingView.categoryAdded(category);
		categorySwingView.categoryUpdated(updatedCategory);
		assertThat(window.list().contents()).containsExactly(getDisplayString(category));
	}

	// Test visibility of buttons after updating a category
	@Test
	public void testAddCategoryButtonShouldBeVisibleAndUpdateCategoryAndCancelButtonShouldBeHiddenAfterUpdatingCategory() {
		Category category = new Category("1", "bills", "utilities");
		Category updatedCategory = new Category("2", "testCategory2", "test");
		categorySwingView.categoryAdded(category);
		categorySwingView.categoryUpdated(updatedCategory);
		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Category"));
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> updateButton.requireNotVisible());
		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		cancelButton.requireNotVisible();
		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Category"));
		addButton.requireVisible();
		addButton.requireDisabled();
	}

	// Test showing all expenses in the table and calculating the total
	@Test
	@GUITest
	public void testGetAllExpensesShouldShowTableTotalAndCrossAddExpensesToTable() {
		Category category = new Category("1", "bills", "utilities");
		Expense expense1 = new Expense(5000d, "Expense1", LocalDate.now(), category);
		Expense expense2 = new Expense(2500d, "Expense2", LocalDate.now(), category);
		List<Expense> expenses = asList(expense1, expense2);
		GuiActionRunner.execute(() -> {
			categorySwingView.getListCategoryModel().addElement(category);
			categorySwingView.getAllExpenses(expenses);
		});
		Object[][] expectedContents = { { "5000.0", "Expense1", LocalDate.now().toString() },
				{ "2500.0", "Expense2", LocalDate.now().toString() } };
		window.table("expenseTable").requireVisible();
		window.label(JLabelMatcher.withText("X")).requireVisible();
		window.label("totalLabel").requireVisible();
		assertThat(window.table("expenseTable").contents()).isEqualTo(expectedContents);
		window.label("totalLabel").requireText("Total: 7500.0");
		assertThat(window.table("expenseTable").target().getModel().getValueAt(0, 3)).isNull();
	}

	// Test adding a category through the controller
	@Test
	public void testAddButtonShouldDelegateToCategoryControllerNewCategory() {
		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();
		await().atMost(10, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(categoryController).newCategory(new Category("bills", "other")));
	}

	// Test deleting a category through the controller
	@Test
	public void testDeleteButtonShouldDelegateToCategoryControllerDeleteCategory() {
		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Category> listStudentsModel = categorySwingView.getListCategoryModel();
			listStudentsModel.addElement(category);
			listStudentsModel.addElement(category2);
		});
		window.list("categoryList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> verify(categoryController).deleteCategory(category2));
	}

	// Test updating a category through the controller
	@Test
	public void testUpdateButtonShouldDelegateToCategoryControllerUpdateCategory() {
		Category category = new Category("1", "bills", "utilities");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Category> listStudentsModel = categorySwingView.getListCategoryModel();
			listStudentsModel.addElement(category);
		});
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("nameTextBox").setText("new Name");
		window.button(JButtonMatcher.withText("Update Category")).click();
		Category updatedCategory = new Category("1", "new Name", "utilities");
		await().atMost(10, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(categoryController).updateCategory(updatedCategory));
	}

	// Test fetching all expenses for a category through the controller
	@Test
	public void testShowExpenseButtonShouldDelegateToCategoryControllergetAllExpenses() {
		Category category = new Category("1", "bills", "utilities");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Category> listStudentsModel = categorySwingView.getListCategoryModel();
			listStudentsModel.addElement(category);
		});
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Show Expenses")).click();
		verify(categoryController).getAllExpenses(category);
	}

	private void setFieldValues(String name, String description) {
		window.textBox("nameTextBox").enterText(name);
		window.textBox("descriptionTextBox").enterText(description);
	}

	private String getDisplayString(Category category) {
		return category.getId() + " | " + category.getName() + " | " + category.getDescription();
	}
}
