package com.tdd.expensetracker.model;

import java.time.LocalDate;

public class Expense {

	private String id;
	private long amount;
	private String description;
	private LocalDate date;
	private Category category;
	
	// Constructor
	public Expense(String id, long amount, String description, LocalDate date, Category category) {
		this.id = id;
		this.amount = amount;
		this.description = description;
		this.date = date;
		this.category = category;
	}

	public Expense() {
	}

	
	public long getAmount() {
		return amount;
	}

	public String getDescription() {
		return description;
	}

	public LocalDate getDate() {
		return date;
	}



	public String getId() {
		return id;
	}



	public Category getCategory() {
		return category;
	}

	




}
