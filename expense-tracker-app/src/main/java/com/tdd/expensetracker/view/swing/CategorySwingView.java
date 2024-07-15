package com.tdd.expensetracker.view.swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.tdd.expensetracker.controller.CategoryController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.view.CategoryView;

public class CategorySwingView extends JFrame implements CategoryView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblDescription;
	private JTextField txtDescription;
	private JButton btnAddCategory;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane1;
	private JButton btnUpdateSelected;
	private JButton btnDeleteSelected;
	private JButton btnShowExpenses;
	/**
	 * @wbp.nonvisual location=397,377
	 */
	private JList<Category> categoryList;
	private JButton btnUpdateCategory;
	private JButton btnCancel;
	private final JLabel lblError = new JLabel(" ");

	private DefaultListModel<Category> listCategoryModel;
	private JTextField txtID;
	private CategoryController categoryController;
	private JTable expenseTable;
	private DefaultTableModel tableModel;
	private JLabel lblHideTable;

	DefaultListModel<Category> getListCategoryModel() {
		return listCategoryModel;
	}

	public void setCategoryController(CategoryController categoryController) {
		this.categoryController = categoryController;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CategorySwingView frame = new CategorySwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CategorySwingView() {
		listCategoryModel = new DefaultListModel<Category>();
//		Category categorysample = new Category("1", "name", "description");
//		categorysample.setExpenses(asList(new Expense(5000d, "name", LocalDate.now(), null)));
//		Category categorysample2 = new Category("2", "nam2e", "de2s2c2ription");
//		categorysample2.setExpenses(asList(new Expense(5000d, "name", LocalDate.now(), null),
//				new Expense(500d, "name1", LocalDate.now(), null),new Expense(50d, "name", LocalDate.now(), null)));
//		
//
//		listCategoryModel.addElement(categorysample);
//		listCategoryModel.addElement(categorysample2);
		setTitle("Category");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				if (btnAddCategory.isVisible()) {
					btnAddCategory.setEnabled(!txtName.getText().trim().isEmpty());
				}
				if (btnUpdateCategory.isVisible()) {
					btnUpdateCategory.setEnabled(!txtName.getText().trim().isEmpty());
				}

			}

		};

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtID = new JTextField();
		txtID.setName("idTextBox");
		txtID.setEditable(false);
		txtID.setVisible(false);
		GridBagConstraints gbc_txtID = new GridBagConstraints();
		gbc_txtID.insets = new Insets(0, 0, 5, 5);
		gbc_txtID.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtID.gridx = 0;
		gbc_txtID.gridy = 0;
		contentPane.add(txtID, gbc_txtID);
		txtID.setColumns(10);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		contentPane.add(lblName, gbc_lblName);

		txtName = new JTextField();
		txtName.addKeyListener(btnAddEnabler);
		txtName.setName("nameTextBox");
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 4;
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 1;
		contentPane.add(txtName, gbc_txtName);
		txtName.setColumns(10);

		lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.EAST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		contentPane.add(lblDescription, gbc_lblDescription);

		txtDescription = new JTextField();
		txtDescription.setName("descriptionTextBox");
		txtDescription.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtDescription = new GridBagConstraints();
		gbc_txtDescription.gridwidth = 4;
		gbc_txtDescription.insets = new Insets(0, 0, 5, 0);
		gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDescription.gridx = 1;
		gbc_txtDescription.gridy = 2;
		contentPane.add(txtDescription, gbc_txtDescription);
		txtDescription.setColumns(10);

		btnAddCategory = new JButton("Add Category");
		btnAddCategory.addActionListener(e -> {
			Category category = new Category(txtName.getText(), txtDescription.getText());
			categoryController.newCategory(category);
			resetFormState();
		});
		btnAddCategory.setEnabled(false);
		btnAddCategory.setName("categoryButton");
		GridBagConstraints gbc_btnAddCategory = new GridBagConstraints();
		gbc_btnAddCategory.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddCategory.gridwidth = 5;
		gbc_btnAddCategory.gridx = 0;
		gbc_btnAddCategory.gridy = 3;
		contentPane.add(btnAddCategory, gbc_btnAddCategory);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> resetFormState());
		btnCancel.setVisible(false);
		btnCancel.setName("cancelButton");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 4;
		contentPane.add(btnCancel, gbc_btnCancel);

		btnUpdateCategory = new JButton("Update Category");
		btnUpdateCategory.addActionListener(e -> {
			Category category = new Category(txtID.getText(), txtName.getText(), txtDescription.getText());
			categoryController.updateCategory(category);
			resetFormState();
		});
		btnUpdateCategory.setEnabled(false);
		btnUpdateCategory.setVisible(false);
		btnUpdateCategory.setName("updateCategoryButton");
		GridBagConstraints gbc_btnUpdateCategory = new GridBagConstraints();
		gbc_btnUpdateCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpdateCategory.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateCategory.gridx = 3;
		gbc_btnUpdateCategory.gridy = 4;
		contentPane.add(btnUpdateCategory, gbc_btnUpdateCategory);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 5;
		contentPane.add(scrollPane, gbc_scrollPane);

		categoryList = new JList<Category>(listCategoryModel);
		categoryList.addListSelectionListener(e -> {
			btnDeleteSelected.setEnabled(categoryList.getSelectedIndex() != -1);
			btnUpdateSelected.setEnabled(categoryList.getSelectedIndex() != -1);
			btnShowExpenses.setEnabled(categoryList.getSelectedIndex() != -1);
		});
		categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoryList.setName("categoryList");
		GridBagConstraints gbc_categoryList = new GridBagConstraints();
		gbc_categoryList.insets = new Insets(0, 0, 5, 0);
		gbc_categoryList.gridwidth = 4;
		gbc_categoryList.fill = GridBagConstraints.BOTH;
		gbc_categoryList.gridx = 0;
		gbc_categoryList.gridy = 3;
		scrollPane.setViewportView(categoryList);

		btnShowExpenses = new JButton("Show Expenses");
		btnShowExpenses.addActionListener(e -> {
			Category selectedCategory = categoryList.getSelectedValue();
			if (selectedCategory != null) {
				List<Expense> listExpense = selectedCategory.getExpenses();

				// Create the table model with the new expense data
				Object[][] expenseData = new Object[listExpense.size()][3];
				for (int i = 0; i < listExpense.size(); i++) {
					expenseData[i][0] = listExpense.get(i).getDescription();
					expenseData[i][1] = listExpense.get(i).getAmount();
					expenseData[i][2] = listExpense.get(i).getDate();
				}

				tableModel = new DefaultTableModel( expenseData, new String[] { "Description", "Amount", "Date" });

				// Set the new table model to the expense table
				expenseTable.setModel(tableModel);

				scrollPane1.setVisible(true);
				lblHideTable.setVisible(true);
			}

		});
		
		lblHideTable = new JLabel("X");
		lblHideTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			 scrollPane1.setVisible(false);
			 lblHideTable.setVisible(false);
			 
			}
		});
		lblHideTable.setForeground(new Color(237, 51, 59));
		lblHideTable.setVisible(false);
		GridBagConstraints gbc_lblHideTable = new GridBagConstraints();
		gbc_lblHideTable.insets = new Insets(0, 0, 5, 5);
		gbc_lblHideTable.gridx = 0;
		gbc_lblHideTable.gridy = 7;
		contentPane.add(lblHideTable, gbc_lblHideTable);
		btnShowExpenses.setEnabled(false);
		GridBagConstraints gbc_btnShowExpenses = new GridBagConstraints();
		gbc_btnShowExpenses.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowExpenses.insets = new Insets(0, 0, 5, 5);
		gbc_btnShowExpenses.gridx = 2;
		gbc_btnShowExpenses.gridy = 7;
		contentPane.add(btnShowExpenses, gbc_btnShowExpenses);

		btnUpdateSelected = new JButton("Update Selected");
		btnUpdateSelected.addActionListener(e -> {

			Category selectedCategory = categoryList.getSelectedValue();
			txtID.setText(selectedCategory.getId());
			txtName.setText(selectedCategory.getName());
			txtDescription.setText(selectedCategory.getDescription());

			btnAddCategory.setVisible(false);
			btnUpdateCategory.setEnabled(true);
			btnUpdateCategory.setVisible(true);
			btnCancel.setVisible(true);
		});
		btnUpdateSelected.setEnabled(false);
		GridBagConstraints gbc_btnDeleteSelected = new GridBagConstraints();
		gbc_btnDeleteSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDeleteSelected.gridx = 3;
		gbc_btnDeleteSelected.gridy = 7;
		contentPane.add(btnUpdateSelected, gbc_btnDeleteSelected);

		btnDeleteSelected = new JButton("Delete Selected");
		btnDeleteSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				categoryController.deleteCategory(categoryList.getSelectedValue());
			}
		});
		btnDeleteSelected.setEnabled(false);
		btnDeleteSelected.setName("deleteSelectedButton");
		GridBagConstraints gbc_btnDeleteSelected1 = new GridBagConstraints();
		gbc_btnDeleteSelected1.insets = new Insets(0, 0, 5, 0);
		gbc_btnDeleteSelected1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDeleteSelected1.gridx = 4;
		gbc_btnDeleteSelected1.gridy = 7;
		contentPane.add(btnDeleteSelected, gbc_btnDeleteSelected1);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.insets = new Insets(0, 0, 0, 5);
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 8;
		lblError.setName("errorMessageLabel");
		contentPane.add(lblError, gbc_lblError);

		// Create the table and set its model
		expenseTable = new JTable(tableModel);
		expenseTable.setName("expenseTable");
		expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		expenseTable.setRowSelectionAllowed(true); // Allow row selection

		GridBagConstraints gbc_expenseTable = new GridBagConstraints();
		gbc_expenseTable.gridwidth = 4;
		gbc_expenseTable.insets = new Insets(0, 0, 0, 5);
		gbc_expenseTable.fill = GridBagConstraints.BOTH;
		gbc_expenseTable.gridx = 1;
		gbc_expenseTable.gridy = 8;
		scrollPane1 = new JScrollPane();
		scrollPane1.setVisible(false);
		scrollPane1.setName("scrollPaneExpenseTable");

		// Define GridBagConstraints for the scroll pane
		GridBagConstraints gbc_scrollPane1 = new GridBagConstraints();
		gbc_scrollPane1.gridwidth = 5;
		gbc_scrollPane1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane1.gridx = 0;
		gbc_scrollPane1.gridy = 8;
		scrollPane1.setViewportView(expenseTable);
		getContentPane().add(scrollPane1, gbc_scrollPane1);

	}

	protected void resetFormState() {
		txtID.setText("");
		txtDescription.setText("");
		txtName.setText("");

		btnAddCategory.setEnabled(false);
		btnAddCategory.setVisible(true);
		btnUpdateCategory.setVisible(false);
		btnCancel.setVisible(false);

	}

	@Override
	public void showAllCategory(List<Category> category) {

		category.stream().forEach(listCategoryModel::addElement);

	}

	@Override
	public void categoryAdded(Category category) {

		listCategoryModel.addElement(category);
		resetErrorLabel();

	}

	private void resetErrorLabel() {
		lblError.setText(" ");
	}

	@Override
	public void showError(String message, Category category) {
		lblError.setText(message + ": " + category);

	}

	@Override
	public void categoryDeleted(Category categoryToDelete) {
		listCategoryModel.removeElement(categoryToDelete);
		resetErrorLabel();

	}

	@Override
	public void categoryUpdated(Category categoryToUpdate) {
		int index = IntStream.range(0, listCategoryModel.size())
				.filter(i -> listCategoryModel.get(i).getId() == categoryToUpdate.getId()).findFirst().orElse(-1);
		listCategoryModel.set(index, categoryToUpdate);
	}

}
