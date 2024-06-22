package com.tdd.expensetracker.model;

public class Category {

	private String id;
	private String name;
	private String description;

	public Category(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public Category() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}



}
