package com.tdd.expensetracker.view.swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseSwingView extends JFrame implements ExpenseView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtDescription;
	private JTextField txtAmount;
	private JLabel lblDate;
	private JDatePickerImpl datePicker;
	private JLabel lblCategory;
	private JButton btnAddExpense;
	private JScrollPane scrollPane;
	private JList<Expense> expenseList;
	private JButton btnDelete;
	private JLabel lblError;
	private JComboBox<Category> cbxCategory;
	private ExpenseController expenseController;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExpenseSwingView frame = new ExpenseSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setExpenseController(ExpenseController expenseController) {
		this.expenseController = expenseController;
	}

	private DefaultComboBoxModel<Category> comboBoxCategoriesModel;

	DefaultComboBoxModel<Category> getComboCategoriesModel() {
		return comboBoxCategoriesModel;
	}

	private DefaultListModel<Expense> listExpenseModel;
	private JButton btnUpdateSelected;
	private JButton btnUpdateExpense;
	private JTextField txtID;
	private JButton btnCancel;

	DefaultListModel<Expense> getListExpenseModel() {
		return listExpenseModel;
	}

	/**
	 * Create the frame.
	 */
	public ExpenseSwingView() {

		comboBoxCategoriesModel = new DefaultComboBoxModel<Category>();
		listExpenseModel = new DefaultListModel<Expense>();
		// getComboCategoriesModel().addElement(new Category("2", "Bills",
		// "Utilities"));
		
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setEnableAddOrUpdateButton();

			}

		};
		
		setTitle("Expense");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0,
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
		gbc_txtID.gridy = 0;
		contentPane.add(txtID, gbc_txtID);
		txtID.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.anchor = GridBagConstraints.WEST;
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 1;
		contentPane.add(lblDescription, gbc_lblDescription);

		txtDescription = new JTextField();
		txtDescription.setName("descriptionTextBox");
		txtDescription.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtDescription = new GridBagConstraints();
		gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDescription.gridwidth = 3;
		gbc_txtDescription.insets = new Insets(0, 0, 5, 0);
		gbc_txtDescription.gridx = 1;
		gbc_txtDescription.gridy = 1;
		contentPane.add(txtDescription, gbc_txtDescription);
		txtDescription.setColumns(10);

		JLabel lblAmount = new JLabel("Amount");
		GridBagConstraints gbc_lblAmount = new GridBagConstraints();
		gbc_lblAmount.anchor = GridBagConstraints.WEST;
		gbc_lblAmount.insets = new Insets(0, 0, 5, 5);
		gbc_lblAmount.gridx = 0;
		gbc_lblAmount.gridy = 2;
		contentPane.add(lblAmount, gbc_lblAmount);

		txtAmount = new JTextField();
		txtAmount.setName("amountTextBox");
		txtAmount.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtAmount = new GridBagConstraints();
		gbc_txtAmount.gridwidth = 3;
		gbc_txtAmount.insets = new Insets(0, 0, 5, 0);
		gbc_txtAmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAmount.gridx = 1;
		gbc_txtAmount.gridy = 2;
		contentPane.add(txtAmount, gbc_txtAmount);
		txtAmount.setColumns(10);

		lblDate = new JLabel("Date");
		GridBagConstraints gbc_lblDate = new GridBagConstraints();
		gbc_lblDate.anchor = GridBagConstraints.WEST;
		gbc_lblDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblDate.gridx = 0;
		gbc_lblDate.gridy = 3;
		contentPane.add(lblDate, gbc_lblDate);

		Properties properties = new Properties();
		properties.put("text.day", "Today");
		properties.put("text.month", "Month");
		properties.put("text.year", "Year");

		UtilDateModel model = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
		//to populate date from calendar to textBox
		datePicker = new JDatePickerImpl(datePanel, new AbstractFormatter() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public String valueToString(Object value) throws ParseException {
				if (isVisible())
					setEnableAddOrUpdateButton();
				if (value != null) {
					if (value instanceof LocalDate) {
						LocalDate localDate = (LocalDate) value;
						value = convertToDateViaInstant(localDate); // Convert LocalDate to Calendar
					}

					Calendar cal = (Calendar) value;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

					return format.format(cal.getTime());
				}
				return "";
			}

			@Override
			public Object stringToValue(String text) throws ParseException {
				if (text.isEmpty()) {
					return null;
				}
				return LocalDate.parse(text);
			}
			
			//convert localDate to calendar
			private Calendar convertToDateViaInstant(LocalDate dateToConvert) {
				Date date = Date.from(dateToConvert.atStartOfDay(ZoneId.systemDefault()).toInstant());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				return cal;
			}
		});

		datePicker.setName("datePicker");
		datePicker.getJFormattedTextField().setBackground(new Color(255, 255, 255));
		datePicker.getJFormattedTextField().setName("dateTextBox");

		GridBagConstraints gbc_datePicker = new GridBagConstraints();
		gbc_datePicker.gridwidth = 3;
		gbc_datePicker.insets = new Insets(0, 0, 5, 0);
		gbc_datePicker.fill = GridBagConstraints.HORIZONTAL;
		gbc_datePicker.gridx = 1;
		gbc_datePicker.gridy = 3;
		contentPane.add(datePicker, gbc_datePicker);

		lblCategory = new JLabel("Category");
		GridBagConstraints gbc_lblCategory = new GridBagConstraints();
		gbc_lblCategory.anchor = GridBagConstraints.WEST;
		gbc_lblCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblCategory.gridx = 0;
		gbc_lblCategory.gridy = 4;
		contentPane.add(lblCategory, gbc_lblCategory);

		cbxCategory = new JComboBox<Category>(comboBoxCategoriesModel);
		cbxCategory.setName("categoryComboBox");
		cbxCategory.addItemListener(e -> setEnableAddOrUpdateButton());
		GridBagConstraints gbc_cbxCategory = new GridBagConstraints();
		gbc_cbxCategory.gridwidth = 3;
		gbc_cbxCategory.insets = new Insets(0, 0, 5, 0);
		gbc_cbxCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbxCategory.gridx = 1;
		gbc_cbxCategory.gridy = 4;

		contentPane.add(cbxCategory, gbc_cbxCategory);

		btnAddExpense = new JButton("Add Expense");
		btnAddExpense.addActionListener(e -> {
			LocalDate date = LocalDate.parse(datePicker.getJFormattedTextField().getText().toString());
			Category selectedCategory = comboBoxCategoriesModel.getElementAt(cbxCategory.getSelectedIndex());
			Expense expense = new Expense(Double.parseDouble(txtAmount.getText()), txtDescription.getText(), date,
					selectedCategory);
			expenseController.newExpense(expense);
		});
		btnAddExpense.setEnabled(false);
		btnAddExpense.setName("addButton");
		GridBagConstraints gbc_btnAddExpense = new GridBagConstraints();
		gbc_btnAddExpense.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddExpense.gridwidth = 4;
		gbc_btnAddExpense.gridx = 0;
		gbc_btnAddExpense.gridy = 5;
		contentPane.add(btnAddExpense, gbc_btnAddExpense);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> resetFormState());
		btnCancel.setName("cancelButton");
		btnCancel.setVisible(false);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 6;
		contentPane.add(btnCancel, gbc_btnCancel);

		btnUpdateExpense = new JButton("Update Expense");
		btnUpdateExpense.setVisible(false);
		btnUpdateExpense.setEnabled(false);
		btnUpdateExpense.setName("updateExpenseButton");
		btnUpdateExpense.addActionListener(e -> {
			LocalDate date = LocalDate.parse(datePicker.getJFormattedTextField().getText());

			Category selectedCategory = comboBoxCategoriesModel.getElementAt(cbxCategory.getSelectedIndex());
			expenseController.updateExpense(new Expense(txtID.getText(), Double.parseDouble(txtAmount.getText()),
					txtDescription.getText(), date, selectedCategory));

			resetFormState();

		});
		GridBagConstraints gbc_btnUpdateExpense = new GridBagConstraints();
		gbc_btnUpdateExpense.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateExpense.gridx = 2;
		gbc_btnUpdateExpense.gridy = 6;
		contentPane.add(btnUpdateExpense, gbc_btnUpdateExpense);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 7;
		contentPane.add(scrollPane, gbc_scrollPane);

		expenseList = new JList<Expense>(listExpenseModel);
		expenseList.addListSelectionListener(e -> {
			btnDelete.setEnabled(expenseList.getSelectedIndex() != -1);
			btnUpdateSelected.setEnabled(expenseList.getSelectedIndex() != -1);
		});
		expenseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		expenseList.setName("expenseList");
		scrollPane.setViewportView(expenseList);

		btnUpdateSelected = new JButton("Update Selected");
		btnUpdateSelected.addActionListener(e -> {
			btnAddExpense.setVisible(false);
			btnUpdateExpense.setVisible(true);
			btnCancel.setVisible(true);

			Expense selectedExpense = expenseList.getSelectedValue();
			txtID.setText(selectedExpense.getId());
			txtDescription.setText(selectedExpense.getDescription());
			txtAmount.setText(Double.toString(selectedExpense.getAmount()));
			cbxCategory.setSelectedItem(selectedExpense.getCategory());
			datePicker.getJFormattedTextField().setText(selectedExpense.getDate().toString());
			datePicker.getModel().setSelected(true);
		});

		btnUpdateSelected.setEnabled(false);
		btnUpdateSelected.setName("updateButton");
		GridBagConstraints gbc_btnUpdateSelected = new GridBagConstraints();
		gbc_btnUpdateSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateSelected.gridx = 1;
		gbc_btnUpdateSelected.gridy = 9;
		contentPane.add(btnUpdateSelected, gbc_btnUpdateSelected);

		btnDelete = new JButton("Delete Selected");
		btnDelete.addActionListener(e -> expenseController.deleteExpense(expenseList.getSelectedValue()));
		btnDelete.setEnabled(false);
		btnDelete.setName("deleteButton");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridwidth = 2;
		gbc_btnDelete.gridx = 2;
		gbc_btnDelete.gridy = 9;
		contentPane.add(btnDelete, gbc_btnDelete);

		lblError = new JLabel(" ");
		lblError.setName("errorMessageLabel");
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.gridwidth = 4;
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 10;
		contentPane.add(lblError, gbc_lblError);	
	}

	private void resetFormState() {
		txtID.setText("");
		txtDescription.setText("");
		txtAmount.setText("");
		datePicker.getJFormattedTextField().setText("");
		cbxCategory.setSelectedItem(null);
		btnAddExpense.setEnabled(false);
		btnAddExpense.setVisible(true);
		btnUpdateExpense.setVisible(false);
		btnCancel.setVisible(false);
	}

	protected void setEnableAddOrUpdateButton() {

		if (btnAddExpense.isVisible()) {
			btnAddExpense.setEnabled(!txtDescription.getText().trim().isEmpty() && !txtAmount.getText().trim().isEmpty()
					&& datePicker.getModel().getValue() != null && (cbxCategory.getSelectedItem() != null));
		}
		if (btnUpdateExpense.isVisible()) {
			btnUpdateExpense
					.setEnabled(!txtDescription.getText().trim().isEmpty() && !txtAmount.getText().trim().isEmpty()
							&& datePicker.getModel().getValue() != null && (cbxCategory.getSelectedItem() != null));
		}
	}

	@Override
	public void showAllExpense(List<Expense> expense) {
		expense.stream().forEach(listExpenseModel::addElement);
	}

	@Override
	public void expenseAdded(Expense expense) {

		listExpenseModel.addElement(expense);
		resetErrorLabel();

	}

	@Override
	public void showError(String message, Expense expense) {
		lblError.setText(message + ": " + expense);

	}

	@Override
	public void expenseDeleted(Expense expense) {
		listExpenseModel.removeElement(expense);
		resetErrorLabel();

	}

	@Override
	public void expenseUpdated(Expense updatedExpense) {
		int index = IntStream.range(0, listExpenseModel.size())
				.filter(i -> listExpenseModel.get(i).getId() == updatedExpense.getId()).findFirst().orElse(-1);
		listExpenseModel.set(index, updatedExpense);

	}

	private void resetErrorLabel() {
		lblError.setText(" ");
	}

}
