package de.sawenko.fhdw.vorlesungen.model;

/**
 * Created by Daniel on 10.05.2016.
 */
public class Module {

	private String abbreviation;
	private String abbreviationProfessor;

	public Module(String abbreviation, String abbreviationProfessor) {

		this.abbreviation = abbreviation;
		this.abbreviationProfessor = abbreviationProfessor;
		
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getAbbreviationProfessor() {
		return abbreviationProfessor;
	}

	public String getProfessor() {

		switch (abbreviationProfessor) {
		case "NUW":
			return "Professor Doktor Wilhelm Nüßer";
		case "PAL":
			return "Matthias Paul";
		case "JEN":
			return "Professor Doktor Thomas Jensen";
		case "MAA":
			return "Doktor Annalisa Mancini";
		case "HOZ":
			return "Professor Doktor Heiko Holzheuer";
		case "POT":
			return "Professor Doktor Oliver Pott";
		case "JAA":
			return "Andreas Jäsche";
		case "COX":
			return "Paul Cox";
		case "BEG":
			return "Professor Doktor Michael Bergsiek";
		case "DOM":
			return "Martin Dombrowski";
		case "RUL":
			return "Professor Doktor Ulrich Reus";
		}
		return "";
	}

	@Override
	public String toString() {

		switch (abbreviation) {
		case "NW":
			return "Netzwerke";
		case "SRM":
			return "Security Risk Management";
		case "VCM":
			return "Value Chain Management";
		case "UET":
			return "Unternehmensethik";
		case "AGI":
			return "Ausgewählte Gebiete der Informatik";
		case "EUU":
			return "Entrepreneurship und Unternehmensführung";
		case "BEV":
			return "Business English 5";
		case "PRAXIS":
			return "Praxischeck";

		}

		return abbreviation;

	}

}
