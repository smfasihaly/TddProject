package com.tdd.expensetracker.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.view.ExpenseView;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

public class ExpenseSwingView extends JFrame implements ExpenseView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtDescription;
	private JTextField txtAmount;
	private JLabel lblDate;
	private JLabel lblCategory;
	private JButton btnAddExpense;
	private JScrollPane scrollPane;
	private JList<Expense> expenseList;
	private JButton btnDelete;
	private JLabel lblError;
	private JComboBox<Category> cbxCategory;
	private transient ExpenseController expenseController;
	private String dateFormatString = "yyyy-MM-dd";

	public void setExpenseController(ExpenseController expenseController) {
		this.expenseController = expenseController;
		expenseController.allCategory();
		cbxCategory.setSelectedIndex(-1);
	}

	private DefaultComboBoxModel<Category> comboBoxCategoriesModel;

	DefaultComboBoxModel<Category> getComboCategoriesModel() {
		return comboBoxCategoriesModel;
	}

	private JButton btnUpdateSelected;
	private JButton btnUpdateExpense;
	private JTextField txtID;
	private JButton btnCancel;
	private DefaultListModel<Expense> listExpenseModel;
	private JDateChooser dateChooser;
	private JLabel lblTotal;
	private JButton btnCategoryForm;
	private CategorySwingView categoryView;

	DefaultListModel<Expense> getListExpenseModel() {
		return listExpenseModel;
	}

	/**
	 * Create the frame.
	 */
	public ExpenseSwingView() {

		comboBoxCategoriesModel = new DefaultComboBoxModel<>();
		listExpenseModel = new DefaultListModel<>();

		// To update if new category added
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent arg0) {
				expenseController.allCategory();
				cbxCategory.setSelectedIndex(-1);
			}

		});

		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setEnableAddOrUpdateButton();

			}

		};

		setTitle("Expense");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 762, 510);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 30, 0, 0, 0, 0, 0, 0, 0, 30, 100, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtID = new JTextField();
		txtID.setName("idTextBox");
		txtID.setVisible(false);

		txtID.setEditable(false);
		GridBagConstraints gbc_txtID = new GridBagConstraints();
		gbc_txtID.insets = new Insets(0, 0, 5, 5);
		gbc_txtID.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtID.gridx = 1;
		gbc_txtID.gridy = 1;
		contentPane.add(txtID, gbc_txtID);
		txtID.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.anchor = GridBagConstraints.WEST;
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		contentPane.add(lblDescription, gbc_lblDescription);

		txtDescription = new JTextField();
		txtDescription.setName("descriptionTextBox");
		txtDescription.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtDescription = new GridBagConstraints();
		gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDescription.gridwidth = 3;
		gbc_txtDescription.insets = new Insets(0, 0, 5, 0);
		gbc_txtDescription.gridx = 1;
		gbc_txtDescription.gridy = 2;
		contentPane.add(txtDescription, gbc_txtDescription);
		txtDescription.setColumns(10);

		JLabel lblAmount = new JLabel("Amount");
		GridBagConstraints gbc_lblAmount = new GridBagConstraints();
		gbc_lblAmount.anchor = GridBagConstraints.WEST;
		gbc_lblAmount.insets = new Insets(0, 0, 5, 5);
		gbc_lblAmount.gridx = 0;
		gbc_lblAmount.gridy = 3;
		contentPane.add(lblAmount, gbc_lblAmount);

		txtAmount = new JTextField();
		txtAmount.setName("amountTextBox");
		txtAmount.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtAmount = new GridBagConstraints();
		gbc_txtAmount.gridwidth = 3;
		gbc_txtAmount.insets = new Insets(0, 0, 5, 0);
		gbc_txtAmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAmount.gridx = 1;
		gbc_txtAmount.gridy = 3;
		contentPane.add(txtAmount, gbc_txtAmount);
		txtAmount.setColumns(10);

		txtAmount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
					e.consume(); // Ignore non-digit characters
				}
			}
		});

		lblDate = new JLabel("Date");
		GridBagConstraints gbc_lblDate = new GridBagConstraints();
		gbc_lblDate.anchor = GridBagConstraints.WEST;
		gbc_lblDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblDate.gridx = 0;
		gbc_lblDate.gridy = 4;
		contentPane.add(lblDate, gbc_lblDate);

		dateChooser = new JDateChooser();
		dateChooser.setName("expenseDateChooser");
		dateChooser.setOpaque(false);
		dateChooser.setDateFormatString(dateFormatString);
		dateChooser.setSize(new Dimension(100, 50));

		JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
		editor.setEditable(false);
		editor.setBackground(new Color(255, 255, 255));

		GridBagConstraints gbc_dateChooser = new GridBagConstraints();
		gbc_dateChooser.gridwidth = 3;
		gbc_dateChooser.insets = new Insets(0, 0, 5, 0);
		gbc_dateChooser.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateChooser.gridx = 1;
		gbc_dateChooser.gridy = 4;

		contentPane.add(dateChooser, gbc_dateChooser);

		lblCategory = new JLabel("Category");
		GridBagConstraints gbc_lblCategory = new GridBagConstraints();
		gbc_lblCategory.anchor = GridBagConstraints.WEST;
		gbc_lblCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblCategory.gridx = 0;
		gbc_lblCategory.gridy = 5;
		contentPane.add(lblCategory, gbc_lblCategory);

		cbxCategory = new JComboBox<>(comboBoxCategoriesModel);
		cbxCategory.setBackground(new Color(255, 255, 255));
		cbxCategory.setName("categoryComboBox");
		cbxCategory.setSelectedIndex(-1);
		cbxCategory.addItemListener(e -> setEnableAddOrUpdateButton());
		cbxCategory.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Category) {
					Category category = (Category) value;
					setText(category.getName()); // Display the name of the category
				}
				return this;
			}

		});
		GridBagConstraints gbc_cbxCategory = new GridBagConstraints();
		gbc_cbxCategory.gridwidth = 3;
		gbc_cbxCategory.insets = new Insets(0, 0, 5, 0);
		gbc_cbxCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbxCategory.gridx = 1;
		gbc_cbxCategory.gridy = 5;

		contentPane.add(cbxCategory, gbc_cbxCategory);

		btnAddExpense = new JButton("Add Expense");
		btnAddExpense.setName("addButton");
		btnAddExpense.addActionListener(e -> new Thread(() -> {
			LocalDate date = getSelectedDateAsLocalDate();

			Category selectedCategory = comboBoxCategoriesModel.getElementAt(cbxCategory.getSelectedIndex());
			Expense expense = new Expense(Double.parseDouble(txtAmount.getText()), txtDescription.getText(), date,
					selectedCategory);
			expenseController.newExpense(expense);
		}).start());
		btnAddExpense.setEnabled(false);
		dateChooser.addPropertyChangeListener(e -> setEnableAddOrUpdateButton());
		GridBagConstraints gbc_btnAddExpense = new GridBagConstraints();
		gbc_btnAddExpense.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddExpense.gridwidth = 4;
		gbc_btnAddExpense.gridx = 0;
		gbc_btnAddExpense.gridy = 6;
		contentPane.add(btnAddExpense, gbc_btnAddExpense);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> resetFormState());
		btnCancel.setName("cancelButton");
		btnCancel.setVisible(false);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 7;
		contentPane.add(btnCancel, gbc_btnCancel);

		btnUpdateExpense = new JButton("Update Expense");
		btnUpdateExpense.setVisible(false);
		btnUpdateExpense.setEnabled(false);
		btnUpdateExpense.setName("updateExpenseButton");
		btnUpdateExpense.addActionListener(e -> new Thread(() -> {
			LocalDate date = getSelectedDateAsLocalDate();
			Category selectedCategory = comboBoxCategoriesModel.getElementAt(cbxCategory.getSelectedIndex());
			expenseController.updateExpense(new Expense(txtID.getText(), Double.parseDouble(txtAmount.getText()),
					txtDescription.getText(), date, selectedCategory));
		}).start());
		GridBagConstraints gbc_btnUpdateExpense = new GridBagConstraints();
		gbc_btnUpdateExpense.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateExpense.gridx = 2;
		gbc_btnUpdateExpense.gridy = 7;
		contentPane.add(btnUpdateExpense, gbc_btnUpdateExpense);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 8;
		contentPane.add(scrollPane, gbc_scrollPane);

		expenseList = new JList<>(listExpenseModel);
		expenseList.addListSelectionListener(e -> {
			btnDelete.setEnabled(expenseList.getSelectedIndex() != -1);
			btnUpdateSelected.setEnabled(expenseList.getSelectedIndex() != -1);
		});
		expenseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		expenseList.setName("expenseList");
		expenseList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Expense expense = (Expense) value;
				return super.getListCellRendererComponent(list, getDisplayString(expense), index, isSelected,
						cellHasFocus);
			}
		});
		scrollPane.setViewportView(expenseList);

		btnUpdateSelected = new JButton("Update Selected");
		btnUpdateSelected.addActionListener(e -> {
			btnAddExpense.setVisible(false);
			btnUpdateExpense.setVisible(true);
			btnCancel.setVisible(true);

			Expense selectedExpense = expenseList.getSelectedValue();
			txtAmount.setText(Double.toString(selectedExpense.getAmount()));
			cbxCategory.setSelectedItem(selectedExpense.getCategory());
			dateChooser.setDate(java.sql.Date.valueOf(selectedExpense.getDate()));
			txtID.setText(selectedExpense.getId());
			txtDescription.setText(selectedExpense.getDescription());

			setEnableAddOrUpdateButton();

		});

		btnUpdateSelected.setEnabled(false);
		btnUpdateSelected.setName("updateButton");
		GridBagConstraints gbc_btnUpdateSelected = new GridBagConstraints();
		gbc_btnUpdateSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpdateSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateSelected.gridx = 1;
		gbc_btnUpdateSelected.gridy = 10;
		contentPane.add(btnUpdateSelected, gbc_btnUpdateSelected);

		btnDelete = new JButton("Delete Selected");
		btnDelete.addActionListener(
				e -> new Thread(() -> expenseController.deleteExpense(expenseList.getSelectedValue())).start());
		btnDelete.setEnabled(false);
		btnDelete.setName("deleteButton");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridwidth = 2;
		gbc_btnDelete.gridx = 2;
		gbc_btnDelete.gridy = 10;
		contentPane.add(btnDelete, gbc_btnDelete);

		lblTotal = new JLabel("Total: 0");
		lblTotal.setName("totalLabel");
		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
		gbc_lblTotal.fill = GridBagConstraints.VERTICAL;
		gbc_lblTotal.anchor = GridBagConstraints.WEST;
		gbc_lblTotal.gridwidth = 2;
		gbc_lblTotal.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotal.gridx = 0;
		gbc_lblTotal.gridy = 11;
		contentPane.add(lblTotal, gbc_lblTotal);

		btnCategoryForm = new JButton("Open Category Form");
		btnCategoryForm.setName("openCatButton");
		btnCategoryForm.addActionListener(e -> {
			categoryView.setVisible(true);
			this.dispose();
			resetFormState();
			resetErrorLabel();

		});
		GridBagConstraints gbc_btnCategoryForm = new GridBagConstraints();
		gbc_btnCategoryForm.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCategoryForm.insets = new Insets(0, 0, 5, 5);
		gbc_btnCategoryForm.gridx = 2;
		gbc_btnCategoryForm.gridy = 11;
		contentPane.add(btnCategoryForm, gbc_btnCategoryForm);

		lblError = new JLabel(" ");
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		lblError.setFont(new Font("Dialog", Font.BOLD, 12));
		lblError.setBackground(new Color(255, 255, 255));
		lblError.setForeground(new Color(237, 51, 59));
		lblError.setName("errorMessageLabel");
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.insets = new Insets(0, 0, 5, 0);
		gbc_lblError.gridwidth = 4;
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 12;
		contentPane.add(lblError, gbc_lblError);

	}

	private void setTotalAmountLabel() {
		double sum = 0.0;
		Enumeration<Expense> elements = listExpenseModel.elements();
		while (elements.hasMoreElements()) {
			Expense expense = elements.nextElement();
			sum += expense.getAmount();
		}
		lblTotal.setText("Total: " + Double.toString(sum));
	}

	private String getDisplayString(Expense expense) {
		return expense.getId() + " | " + expense.getDescription() + " | " + expense.getAmount() + " | "
				+ expense.getDate() + " | " + expense.getCategory().getName();
	}

	private LocalDate getSelectedDateAsLocalDate() {
		Date SelectedDate = dateChooser.getDate();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
		String formattedDate = sdf.format(SelectedDate);

		// Parse the formatted date string to LocalDate
		return LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern(dateFormatString));
	}

	private void resetFormState() {
		SwingUtilities.invokeLater(() -> {
			txtID.setText("");
			txtDescription.setText("");
			txtAmount.setText("");
			dateChooser.setDate(null);
			cbxCategory.setSelectedItem(null);
			btnAddExpense.setEnabled(false);
			btnAddExpense.setVisible(true);
			btnUpdateExpense.setVisible(false);
			btnCancel.setVisible(false);
		});
	}

	private void setEnableAddOrUpdateButton() {
		if (btnAddExpense.isVisible()) {
			btnAddExpense.setEnabled(!txtDescription.getText().trim().isEmpty() && !txtAmount.getText().trim().isEmpty()
					&& dateChooser.getDate() != null && (cbxCategory.getSelectedItem() != null));
		}
		if (btnUpdateExpense.isVisible()) {
			btnUpdateExpense
					.setEnabled(!txtDescription.getText().trim().isEmpty() && !txtAmount.getText().trim().isEmpty()
							&& dateChooser.getDate() != null && (cbxCategory.getSelectedItem() != null));
		}
	}

	@Override
	public void showAllExpense(List<Expense> expense) {
		expense.stream().forEach(listExpenseModel::addElement);
		setTotalAmountLabel();
	}

	@Override
	public void expenseAdded(Expense expense) {
		SwingUtilities.invokeLater(() -> {
			listExpenseModel.addElement(expense);
			setTotalAmountLabel();
			resetErrorLabel();
		});
	}

	@Override
	public void showError(String message, Expense expense) {
		SwingUtilities.invokeLater(() -> lblError.setText(message + ": " + expense));

	}

	@Override
	public void expenseDeleted(Expense expense) {
		SwingUtilities.invokeLater(() -> {
			listExpenseModel.removeElement(expense);
			setTotalAmountLabel();
			resetErrorLabel();

		});
		resetFormState();
	}

	@Override
	public void expenseUpdated(Expense updatedExpense) {
		SwingUtilities.invokeLater(() -> {
			int index = IntStream.range(0, listExpenseModel.size())
					.filter(i -> listExpenseModel.get(i).getId().equals(updatedExpense.getId())).findFirst().orElse(-1);
			listExpenseModel.set(index, updatedExpense);
			setTotalAmountLabel();
			resetErrorLabel();
		});
		resetFormState();
	}

	private void resetErrorLabel() {
		lblError.setText(" ");
	}

	@Override
	public void showAllCategory(List<Category> categories) {
		comboBoxCategoriesModel.removeAllElements();
		categories.stream().forEach(comboBoxCategoriesModel::addElement);
		setTotalAmountLabel();

	}

	public void setCategoryView(CategorySwingView categoryView) {
		this.categoryView = categoryView;

	}

	@Override
	public void showErrorExpenseNotFound(String message, Expense expense) {
		SwingUtilities.invokeLater(() -> {
			lblError.setText(message + ": " + expense);
			listExpenseModel.removeElement(expense);
		});

	}

}
