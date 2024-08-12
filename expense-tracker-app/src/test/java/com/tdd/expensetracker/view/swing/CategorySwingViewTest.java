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

	@Test
	public void testWhenNameFieldIsNonEmptyThenAddButtonShouldBeEnabled() {

		// Only name is required
		setFieldValues("name", "");
		window.button(JButtonMatcher.withText("Add Category")).requireEnabled();
	}
 
	@Test
	public void testWhenNameRequiredFieldIsBlankThenAddButtonShouldBeDisabled() {

		// empty Name
		setFieldValues("", "1000");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

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

	@Test
	public void testWhenNameRequiredFieldIsBlankThenUpdateButtonShouldBeDisabled() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		window.textBox("descriptionTextBox").enterText(" changed");
		window.button(JButtonMatcher.withText("Update Category")).requireEnabled();

		// setting required field empty and enter whitespaces
		window.textBox("nameTextBox").setText("");
		window.textBox("nameTextBox").enterText("  ");

		// empty Name
		window.button(JButtonMatcher.withText("Update Category")).requireDisabled();
	}

	@Test
	public void testFormResetAfterCancelingUpdate() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));

		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.button(JButtonMatcher.withText("Cancel")).click();

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Category")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();

	}

	@Test
	public void testExpenseTableAndCrossShouldBeHiddenWhenCrossLabelClicked() {
		// setting table and labels visible manually
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
	
	@Test
	public void testOpenExpenseFormShouldOpenExpenseForm() {
		window.button(JButtonMatcher.withText("Open Expense Form")).click();
		verify(expenseSwingView).setVisible(true);

	}

	// View Interface
	@Test
	public void testsShowAllCategorysShouldAddCategoryDescriptionsToTheList() {

		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");

		GuiActionRunner.execute(() -> categorySwingView.showAllCategory(asList(category, category2)));

		assertThat(window.list().contents()).containsExactly(getDisplayString(category), getDisplayString(category2));

	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.showError("error message", category);
		window.label("errorMessageLabel").requireText("error message: " + category);
	}
	
	@Test
	public void testShowErrorCategoryNotFoundShouldShowTheMessageInTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.showErrorCategoryNotFound("error message", category);
		window.label("errorMessageLabel").requireText("error message: " + category);
	}

	@Test
	public void testCategoryAddedShouldAddTheCategoryToTheListAndResetTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.categoryAdded(category);
		assertThat(window.list().contents()).containsExactly(getDisplayString(category));
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testFormShouldBeResetAfterAddingCategory() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.categoryAdded(category);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("descriptionTextBox").requireText(""));
		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("nameTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

	@Test
	public void testCategoryDeleteShouldRemoveTheCategoryFromTheListAndResetTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");

		GuiActionRunner.execute(() -> {
			categorySwingView.categoryAdded(category);
			categorySwingView.categoryAdded(category2);
		});

		// execute
		categorySwingView.categoryDeleted(new Category("1", "bills", "utilities"));

		assertThat(window.list().contents()).containsExactly(getDisplayString(category2));
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testCategoryUpdateShouldUpdateTheCategoryFromTheListAndResetTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.categoryAdded(category);

		Category updatedCategory = new Category("1", "testCategory2", "test");
		// execute
		categorySwingView.categoryUpdated(updatedCategory);

		assertThat(window.list().contents()).containsExactly(getDisplayString(updatedCategory));
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testFormShouldBeResetAfterUpdatingCategory() {

		Category category = new Category("1", "bills", "utilities");

		categorySwingView.categoryAdded(category);

		Category updatedCategory = new Category("1", "testCategory2", "test");
		// execute
		categorySwingView.categoryUpdated(updatedCategory);

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat("").isEqualTo(idTextBox.getText()));
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
	}

	@Test
	public void testCategoryUpdateShouldNotUpdateWhenCategoryNotFound() {

		Category category = new Category("1", "bills", "utilities");
		// This category has a different ID and should not be found in the list
		Category updatedCategory = new Category("2", "testCategory2", "test");

		categorySwingView.categoryAdded(category);
		categorySwingView.categoryUpdated(updatedCategory);

		// Verify that the original category remains unchanged in the list
		assertThat(window.list().contents()).containsExactly(getDisplayString(category));

	}

	@Test
	public void testAddCategoryButtonShouldBeVisibleAndUpdateCategoryAndCancelButtonShouldBeHiddenAfterUpdatingCategory() {

		Category category = new Category("1", "bills", "utilities");
		// This category has a different ID and should not be found in the list
		Category updatedCategory = new Category("2", "testCategory2", "test");

		categorySwingView.categoryAdded(category);
		categorySwingView.categoryUpdated(updatedCategory);

		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Category"));
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> updateButton.requireNotVisible());

		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		cancelButton.requireNotVisible();

		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Category"));
		addButton.requireVisible();
		addButton.requireDisabled();

	}

	@Test
	@GUITest
	public void testGetAllExpensesShouldShowTableTotalAndCrossAddExpensesToTable() {
		Category category = new Category("1", "bills", "utilities");
		Expense expense1 = new Expense(5000d, "Expense1", LocalDate.now(), category);
		Expense expense2 = new Expense(2500d, "Expense2", LocalDate.now(), category);

		List<Expense> expenses = asList(expense1, expense2);

		// Add category to the list model
		GuiActionRunner.execute(() -> {
			categorySwingView.getListCategoryModel().addElement(category);
			categorySwingView.getAllExpenses(expenses);
		});

		// Expected contents in the table
		Object[][] expectedContents = { { "5000.0", "Expense1", LocalDate.now().toString() },
				{ "2500.0", "Expense2", LocalDate.now().toString() } };

		// Assert the contents of the table
		window.table("expenseTable").requireVisible();
		window.label(JLabelMatcher.withText("X")).requireVisible();
		window.label("totalLabel").requireVisible();

		assertThat(window.table("expenseTable").contents()).isEqualTo(expectedContents);
		window.label("totalLabel").requireText("Total: 7500.0");
		// Attempt to get value at an invalid column index (e.g., column index 3) to
		// cover the null part of switch case
		// Ensure the invalid column index returns null
		assertThat(window.table("expenseTable").target().getModel().getValueAt(0, 3)).isNull();

	}

	// interaction with Controller
	@Test
	public void testAddButtonShouldDelegateToCategoryControllerNewCategory() {

		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(categoryController).newCategory(new Category("bills", "other")));
	}

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
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(categoryController).deleteCategory(category2));

	}

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
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(categoryController).updateCategory(updatedCategory));

	}

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
