package de.sawenko.fhdw.vorlesungen.storage;

import com.amazon.speech.speechlet.Session;

import de.sawenko.fhdw.vorlesungen.model.Lecturer;
import de.sawenko.fhdw.vorlesungen.model.Module;
import de.sawenko.fhdw.vorlesungen.model.User;

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
		User user = new User();
		user.setUserId(session.getUser().getUserId());

		user = (User) dynamoDbClient.loadItem(user);

		if (user == null) {
			return null;
		}

		return user.getCourse();
	}

	public Lecturer getLecturer(String abbreviation) {
		Lecturer lecturer = new Lecturer();
		lecturer.setAbbreviation(abbreviation);
		return dynamoDbClient.loadItem(lecturer);
	}
	
	public Module getModule(String abbreviation) {
		Module module = new Module();
		module.setAbbreviation(abbreviation);
		return dynamoDbClient.loadItem(module);
	}
	
	public int createNewUser(Session session) {
		// neuen accessCode ermitteln
		int accessCode = dynamoDbClient.getNewAccessCode();

		// User erstellen
		User user = new User();
		user.setUserId(session.getUser().getUserId());
		user.setAccessCode(accessCode);

		dynamoDbClient.saveItem(user);
		// accessCode zurückgeben
		return accessCode;
	}
	
	
}
