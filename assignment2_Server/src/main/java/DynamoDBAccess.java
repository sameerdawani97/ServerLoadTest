
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

public class DynamoDBAccess {

  public static String tableName = "Album";
  ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
//ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
  Region region = Region.US_WEST_2;
  DynamoDbClient ddb = DynamoDbClient.builder()
      //.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
      .credentialsProvider(credentialsProvider)
      .region(region)
      .build();

  public DynamoDBAccess() {

  }
  public DynamoDBAccess(String accessKey, String secretKey, String sessionToken) {
    System.setProperty("aws.accessKeyId", accessKey);
    System.setProperty("aws.secretAccessKey", secretKey);
    System.setProperty("aws.sessionToken", sessionToken);
  }

  public void putItemWithImageToDynamoDB(String primaryKeyValue, byte[] imageData, String artist, String title, String year) {

    long expirationTimestamp = System.currentTimeMillis() / 1000 + 3600; // 1 hour from now
    HashMap<String, AttributeValue> itemValues = new HashMap<>();
    itemValues.put("albumId", AttributeValue.builder().s(primaryKeyValue).build());
    itemValues.put("artist", AttributeValue.builder().s(artist).build());
    itemValues.put("title", AttributeValue.builder().s(title).build());
    itemValues.put("year", AttributeValue.builder().s(year).build());
    itemValues.put("imageData", AttributeValue.builder().b(SdkBytes.fromByteArray(imageData)).build());
    itemValues.put("ttl", AttributeValue.builder().n(String.valueOf(expirationTimestamp)).build());

    PutItemRequest request = PutItemRequest.builder()
        .tableName(tableName)
        .item(itemValues)
        .build();

      try {
        PutItemResponse response = ddb.putItem(request);

      } catch (ResourceNotFoundException e) {
        System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
        System.err.println("Be sure that it exists and that you've typed its name correctly!");
        System.exit(1);
      } catch (DynamoDbException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
  }

  public Map<String,AttributeValue> getItemFromDynamoDB(String key) {
    HashMap<String,AttributeValue> keyToGet = new HashMap<>();
    keyToGet.put("albumId", AttributeValue.builder()
        .s(key)
        .build());

    GetItemRequest request = GetItemRequest.builder()
        .key(keyToGet)
        .tableName(tableName)
        .build();

    try {

      Map<String,AttributeValue> returnedItem = ddb.getItem(request).item();
      if (!returnedItem.isEmpty()){
        return returnedItem;
      } else {
        return Collections.emptyMap();
      }

    } catch (DynamoDbException e) {
      //System.err.println(e.getMessage());
      return Collections.emptyMap();
    }
  }

}