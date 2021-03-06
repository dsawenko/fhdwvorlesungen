package de.sawenko.fhdw.vorlesungen.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Model representing an item of the ScoreKeeperUserData table in DynamoDB for the ScoreKeeper
 * skill.
 */
@DynamoDBTable(tableName = "VorlesungenModules")
public class Module {

	private String abbreviation;
	private String name;
	private String sayAs;
	
	@DynamoDBHashKey(attributeName = "abbreviation")
    public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@DynamoDBAttribute(attributeName = "name") 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return name;
	}

}
