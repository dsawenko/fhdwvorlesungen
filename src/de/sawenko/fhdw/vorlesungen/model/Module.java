package de.sawenko.fhdw.vorlesungen.model;

public class Module {

	private final String abbreviation;
	private final String name;
	
	public Module(String abbreviation, String name) {
		this.abbreviation = abbreviation;
		if (name == null)
			this.name = "";
		else
			this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
