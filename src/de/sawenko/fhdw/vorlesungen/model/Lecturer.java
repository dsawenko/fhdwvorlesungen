package de.sawenko.fhdw.vorlesungen.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Model representing an item of the ScoreKeeperUserData table in DynamoDB for the ScoreKeeper
 * skill.
 */
@DynamoDBTable(tableName = "VorlesungenLecturers")
public class Lecturer {

	private String abbreviation;
	private String surname;
	private String name;
	private String email;
	private String function;
	private String sayAs;

	@DynamoDBHashKey(attributeName = "abbreviation")
    public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@DynamoDBAttribute(attributeName = "surname") 
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@DynamoDBAttribute(attributeName = "name") 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@DynamoDBAttribute(attributeName = "email") 
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@DynamoDBAttribute(attributeName = "function") 
	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}
	
	@DynamoDBAttribute(attributeName = "sayAs") 
	public String getSayAs() {
		return sayAs;
	}

	public void setSayAs(String sayAs) {
		this.sayAs = sayAs;
	}

	@Override
	public String toString() {
		return name + " " + surname;
	}
	

}
