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

	
//	public long getAmount() {
//		return amount;
//	}
//
//	public void setAmount(long amount) {
//		this.amount = amount;
//	}
//
//	public String getDescription() {
//		return description;
//	}
//
//	public void setDescription(String description) {
//		this.description = description;
//	}
//
//	public LocalDate getDate() {
//		return date;
//	}
//
//	public void setDate(LocalDate date) {
//		this.date = date;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

//	public void setCategory(Category category) {
//		// TODO Auto-generated method stub
//		this.category = category;
//		
//	}

	public Category getCategory() {
		return category;
	}




}
