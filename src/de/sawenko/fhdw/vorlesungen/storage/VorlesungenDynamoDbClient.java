package de.sawenko.fhdw.vorlesungen.storage;

import java.util.Iterator;
import java.util.Random;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

/**
 * Client for DynamoDB persistance layer for the Score Keeper skill.
 */
public class VorlesungenDynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;

    public VorlesungenDynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    /**
     * Loads an item from DynamoDB by primary Hash Key. Callers of this method should pass in an
     * object which represents an item in the DynamoDB table item with the primary key populated.
     * 
     * @param tableItem
     * @return
     */
    public VorlesungenUserDataItem loadItem(final VorlesungenUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        VorlesungenUserDataItem item = mapper.load(tableItem);
        return item;
    }

    /**
     * Stores an item to DynamoDB.
     * 
     * @param tableItem
     */
    public void saveItem(final VorlesungenUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(tableItem);
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     * 
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }
    
    
    public int getNewAccessCode() {
    	return getNewAccessCode(10); // 10 Versuche einen neuen accessCode zu generieren
    }
    
    private int getNewAccessCode(int timeToLive) {
    	if (timeToLive <= 0)
    		return 0;
    	
    	DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
    	
    	int accessCode = randInt(0, 999999);

    	Table table = dynamoDB.getTable("WeatherData");
    	Index index = table.getIndex("PrecipIndex");

    	QuerySpec spec = new QuerySpec()
    	    .withKeyConditionExpression("accessCode = :v_accessCode")
    	    .withValueMap(new ValueMap()
    	        .withNumber(":v_accessCode", accessCode));

    	ItemCollection<QueryOutcome> items = index.query(spec);
    	Iterator<Item> iter = items.iterator(); 
    	
    	// Wenn accessCode bereits vorhanden, dann versuche es erneut
    	if (iter.hasNext()) {
    		// Verringere timeToLive um eins
    	    return getNewAccessCode(--timeToLive);
    	}
    	
    	// Wenn der accessCode noch nicht vergeben ist, dann gib ihn zurück
    	return accessCode;
    }
    
    /**
     * Returns a psuedo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimim value
     * @param max Maximim value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
