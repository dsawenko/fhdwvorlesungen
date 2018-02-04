package de.sawenko.fhdw.vorlesungen.model;

public class Lecturer {
	
	private final String abbreviation;
	private final String surname;
	private final String name;
	private final String email;
	private final String function;
	
	public Lecturer(String abbreviation, String surname, String name, String email, String function) {
		this.abbreviation = abbreviation;
		this.surname = surname;
		this.name = name;
		this.email = email;
		this.function = function;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getSurname() {
		return surname;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getFunction() {
		return function;
	}

	@Override
	public String toString() {
		return name + " " + surname;
	}
	
	
	
}
