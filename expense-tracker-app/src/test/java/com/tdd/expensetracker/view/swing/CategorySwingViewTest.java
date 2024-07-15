package com.tdd.expensetracker.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
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

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {

		closeable = MockitoAnnotations.openMocks(this);

		GuiActionRunner.execute(() -> {
			categorySwingView = new CategorySwingView();
			categorySwingView.setCategoryController(categoryController);
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
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Selected")).requireDisabled();
		window.button(JButtonMatcher.withText("Update Category")).requireNotVisible();
		window.button(JButtonMatcher.withText("Cancel")).requireNotVisible();
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
	public void testFormShouldBeResetAfterAddingCategory() {

		setFieldValues("name", "description");

		window.button(JButtonMatcher.withText("Add Category")).click();

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
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
		updateButton.requireEnabled();
		cancelButton.requireEnabled();
		updateButton.requireVisible();

	}

	@Test
	public void testWhenNameRequiredFieldIsBlankThenUpdateButtonShouldBeDisabled() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));
		window.list("categoryList").selectItem(0);

		JButtonFixture updateSelectedButton = window.button(JButtonMatcher.withText("Update Selected"));
		updateSelectedButton.click();

		window.button(JButtonMatcher.withText("Update Category")).requireEnabled();

		window.textBox("nameTextBox").setText("");
		window.textBox("descriptionTextBox").setText("");

		// empty Name
		setFieldValues("", "1000");
		window.button(JButtonMatcher.withText("Update Category")).requireDisabled();
	}

	@Test
	public void testAddCategoryButtonShouldBeVisibleAndUpdateCategoryAndCancelButtonShouldBeHiddenAfterUpdatingCategory() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));

		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("nameTextBox").setText("updated name");

		JButtonFixture updateButton = window.button(JButtonMatcher.withText("Update Category"));
		updateButton.click();
		updateButton.requireNotVisible();

		JButtonFixture cancelButton = window.button(JButtonMatcher.withText("Cancel"));
		cancelButton.requireNotVisible();

		JButtonFixture addButton = window.button(JButtonMatcher.withText("Add Category"));
		addButton.requireVisible();
		addButton.requireDisabled();

	}

	@Test
	public void testFormShouldBeResetAfterUpdatingCategory() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));

		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("nameTextBox").setText("updated name");

		window.button(JButtonMatcher.withText("Update Category")).click();

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		assertThat("").isEqualTo(idTextBox.getText());
		window.textBox("nameTextBox").requireText("");
		window.textBox("descriptionTextBox").requireText("");
		window.button(JButtonMatcher.withText("Add Category")).requireDisabled();
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
	public void testExpenseTableAndCrossShouldBeVisibleWhenShowExpensesButtonClicked() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));

		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Show Expenses")).click();

		JScrollPane expenseTable = window.robot().finder().findByName("scrollPaneExpenseTable", JScrollPane.class,
				false);
		assertThat(expenseTable.isVisible()).isTrue();

		window.label(JLabelMatcher.withText("X")).requireVisible();
	}

	@Test
	public void testExpenseTableShouldBePopulatedWithExpensesWhenShowExpensesButtonClicked() {

		Category category = new Category("1", "bills", "utilities");
		category.setExpenses(asList(new Expense(5000d, "name", LocalDate.now(), category)));

		GuiActionRunner.execute(() -> categorySwingView.getListCategoryModel().addElement(category));
		
		 Object[][] expectedContents = {
		            { "name", "5000.0", LocalDate.now().toString() }
		        };
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Show Expenses")).click();
		assertThat(window.table("expenseTable").contents()).isEqualTo(expectedContents);
	}
	
	@Test
	public void testExpenseTableAndCrossShouldBeHiddenWhenCrossLabelClicked() {

		GuiActionRunner.execute(
				() -> categorySwingView.getListCategoryModel().addElement(new Category("1", "bills", "utilities")));

		window.list("categoryList").selectItem(0);
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Show Expenses")).click();
		
		JLabelFixture label = window.label(JLabelMatcher.withText("X"));
		label.click();

		JScrollPane expenseTable = window.robot().finder().findByName("scrollPaneExpenseTable", JScrollPane.class,
				false);
		assertThat(expenseTable.isVisible()).isFalse();
		label.requireNotVisible();
	}

	// View Interface
	@Test
	public void testsShowAllCategorysShouldAddCategoryDescriptionsToTheList() {

		Category category = new Category("1", "bills", "utilities");
		Category category2 = new Category("2", "groceries", "daily expense");

		GuiActionRunner.execute(() -> categorySwingView.showAllCategory(asList(category, category2)));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(category.toString(), category2.toString());

	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		GuiActionRunner.execute(() -> categorySwingView.showError("error message", category));
		window.label("errorMessageLabel").requireText("error message: " + category);
	}

	@Test
	public void testCategoryAddedShouldAddTheCategoryToTheListAndResetTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		GuiActionRunner.execute(() -> categorySwingView.categoryAdded(category));
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(category.toString());
		window.label("errorMessageLabel").requireText(" ");
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
		GuiActionRunner.execute(() -> categorySwingView.categoryDeleted(new Category("1", "bills", "utilities")));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(category2.toString());
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testCategoryUpdateShouldUpdateTheCategoryFromTheListAndResetTheErrorLabel() {

		Category category = new Category("1", "bills", "utilities");

		GuiActionRunner.execute(() -> {

			categorySwingView.categoryAdded(category);
		});

		Category updatedCategory = new Category("1", "testCategory2", "test");
		// execute
		GuiActionRunner.execute(() -> categorySwingView.categoryUpdated(updatedCategory));

		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(updatedCategory.toString());
		window.label("errorMessageLabel").requireText(" ");
	}

	// interaction with Controller
	@Test
	public void testAddButtonShouldDelegateToCategoryControllerNewCategory() {

		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();
		verify(categoryController).newCategory(new Category("bills", "other"));
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
		verify(categoryController).deleteCategory(category2);

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
		verify(categoryController).updateCategory(updatedCategory);

	}

	private void setFieldValues(String name, String description) {
		window.textBox("nameTextBox").enterText(name);
		window.textBox("descriptionTextBox").enterText(description);

	}

}
