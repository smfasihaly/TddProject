package com.tdd.expensetracker.model;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Expense")
public class Expense {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "id", unique = true, nullable = false)
	private String id;

	@Column(name = "amount")
	private double amount;

	@Column(name = "description")
	private String description;

	@Column(name = "date")
	private LocalDate date;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	// Constructor
	public Expense(String id, Double amount, String description, LocalDate date, Category category) {
		this.id = id;
		this.amount = amount;
		this.description = description;
		this.date = date;
		this.category = category;
	}

	public Expense(Double amount, String description, LocalDate date, Category category) {
		this.id = "";
		this.amount = amount;
		this.description = description;
		this.date = date;
		this.category = category;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, category, date, description, id);
	}

	@Override
	public String toString() {
		return "Expense [id=" + id + ", amount=" + amount + ", description=" + description + ", date=" + date
				+ ", category=" + category + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Expense other = (Expense) obj;
		return Double.doubleToLongBits(amount) == Double.doubleToLongBits(other.amount)
				&& Objects.equals(category, other.category) && Objects.equals(date, other.date)
				&& Objects.equals(description, other.description) && Objects.equals(id, other.id);

	}

	public Expense() {
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public double getAmount() {
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
