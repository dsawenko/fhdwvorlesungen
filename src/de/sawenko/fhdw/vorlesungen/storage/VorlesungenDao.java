package de.sawenko.fhdw.vorlesungen.storage;

import com.amazon.speech.speechlet.Session;

/**
 * Contains the methods to interact with the persistence layer for ScoreKeeper
 * in DynamoDB.
 */
public class VorlesungenDao {
	private final VorlesungenDynamoDbClient dynamoDbClient;

	public VorlesungenDao(VorlesungenDynamoDbClient dynamoDbClient) {
		this.dynamoDbClient = dynamoDbClient;
	}

	/**
	 * Reads and returns the {@link ScoreKeeperGame} using user information from the
	 * session.
	 * <p>
	 * Returns null if the item could not be found in the database.
	 * 
	 * @param session
	 * @return
	 */
	public String getCourse(Session session) {
		VorlesungenUserDataItem item = new VorlesungenUserDataItem();
		item.setUserId(session.getUser().getUserId());

		item = dynamoDbClient.loadItem(item);

		if (item == null) {
			return null;
		}

		return item.getCourse();
	}

	public int createNewUser(Session session) {
		// neuen accessCode ermitteln
		int accessCode = dynamoDbClient.getNewAccessCode();

		// User erstellen
		VorlesungenUserDataItem item = new VorlesungenUserDataItem();
		item.setUserId(session.getUser().getUserId());
		item.setAccessCode(accessCode);

		dynamoDbClient.saveItem(item);
		// accessCode zurückgeben
		return accessCode;
	}
}
