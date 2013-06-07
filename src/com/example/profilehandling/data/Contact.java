package com.example.profilehandling.data;

import java.util.ArrayList;

public class Contact {

	private String name;
	private ArrayList<String> numbers = new ArrayList<String>();
	boolean selected = false;
	private Long contactId;

	public Contact() {

	}

	public Contact(String name,ArrayList<String> num){
		this.name = name;
		this.numbers = num;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getNumbers() {
		return numbers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNumbers(ArrayList<String> numbs) {
		this.numbers = numbs;
	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void setId(Long id) {
		this.contactId = id;
	}
	
	public Long getId() {
		return contactId;
	}
	
}
