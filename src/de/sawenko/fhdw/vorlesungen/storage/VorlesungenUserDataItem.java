package de.sawenko.fhdw.vorlesungen.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Model representing an item of the ScoreKeeperUserData table in DynamoDB for the ScoreKeeper
 * skill.
 */
@DynamoDBTable(tableName = "VorlesungenUserData")
public class VorlesungenUserDataItem {

    private String userId;
    private int accessCode;
    private String course;


    @DynamoDBHashKey(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @DynamoDBHashKey(attributeName = "accessCode") 
    public int getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(int accessCode) {
		this.accessCode = accessCode;
	}

	@DynamoDBHashKey(attributeName = "course")
	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

}
