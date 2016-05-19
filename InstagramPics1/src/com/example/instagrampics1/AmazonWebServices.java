/**
 * Java interface to make handshakes and download/upload data
 * to Amazon Web Services
 * 
 * @author venkata subba rao cheedella, Cletan Sequeira
 */


package com.example.instagrampics1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AmazonWebServices {
	
	BasicAWSCredentials awsCreds;
	static AmazonDynamoDBClient dynamoDB;
	String tableName = "Instagram-Imgs-Tbl";					// Table, that is used to save the image urls of various users
	
	
	/**
	 * Constructor of AWS which creates an awsservice object which is used to downlaod/upload to various
	 * services like : S3, Dynamo DB 
	 * @param AWSAccessKeyId amazon web services access key
	 * @param AWSSecretKey amazon web services access key
	 */
	public AmazonWebServices(String AWSAccessKeyId, String AWSSecretKey) {
		// TODO Auto-generated constructor stub
		awsCreds = new BasicAWSCredentials(
				AWSAccessKeyId, AWSSecretKey);
	}	
	
	/**
	 * upload file to s3, scalable storage in the cloud
	 * @param filename file that has to be uploaded
	 * @return return the status of the upload
	 */
	protected String uploadFileToS3(String filename)
	{		
		AmazonS3 s3Client = new AmazonS3Client(awsCreds);						
		PutObjectResult upload_status = s3Client.putObject(new PutObjectRequest(
				"venkatacaproject", filename, new File(filename)));					
		return upload_status.toString();
	}	
	
	/**
	 * Initializing DynamoDb table. 
	 *	choose the region where the table is created.
	 * 
	 */
	protected void initDynamoDBTable()
	{
		dynamoDB = new AmazonDynamoDBClient(awsCreds);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
        
	}	
	
	/**
	 * create a table in the database initialized above. If the table is not present for a given name, a new
	 * table is created
	 * 
	 * @param verlayout to display the response for a creation request
	 */
	@SuppressWarnings("deprecation")
	protected void createDynamoDBTable(VerticalLayout verlayout)
	{
		try{
			initDynamoDBTable();						
            // Create table if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, tableName)) {
                verlayout.addComponent(new Label("Table " + tableName + " is already ACTIVE"));
            } else {
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                    //verlayout.addComponent(new Label("Created Table: " + createdTableDescription));

                // Wait for it to become active
                    verlayout.addComponent(new Label("Waiting for " + tableName + " to become ACTIVE..."));
                Tables.awaitTableToBecomeActive(dynamoDB, tableName);
            }

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            verlayout.addComponent(new Label("Instagram-Images-tableis created"));                                  
		}
		catch(AmazonServiceException ase)
		{
			verlayout.addComponent(new Label("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason."));
			verlayout.addComponent(new Label("Error Message:    " + ase.getMessage()));
			verlayout.addComponent(new Label("HTTP Status Code: " + ase.getStatusCode()));
			verlayout.addComponent(new Label("AWS Error Code:   " + ase.getErrorCode()));
			verlayout.addComponent(new Label("Error Type:       " + ase.getErrorType()));
			verlayout.addComponent(new Label("Request ID:       " + ase.getRequestId()));
		}catch (AmazonClientException ace)
		{
			verlayout.addComponent(new Label("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network."));
			verlayout.addComponent(new Label("Error Message: " + ace.getMessage()));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			verlayout.addComponent(new Label(e.toString()));
		}
	}
	
	/**
	 * table is updatedwith Images urls of an user.
	 * @param listofImages list of images of a new user
	 * @return returns the status of the update
	 */
	protected String addItemsToDynamoDbTable(List<Map<String, String>> listofImages)
	{		
		PutItemResult putItemResult = null;
		 // Add image items to the dynamo db table
        for(int i=0; i < listofImages.size(); i++)
        {
        	Map<String, AttributeValue> item = createImageObject(listofImages.get(i));
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);            
        }
        return putItemResult.toString();
	}
	
	
	/**
	 * Each image object is created as Map<String, Attribute>
	 * @param images_info image object has priamry key, iamge url and coordinates if image JSON has
	 * @return retruns a image object item type that can be inserted to the table
	 */
	private Map<String, AttributeValue> createImageObject(Map<String, String>  images_info)
	{		
		String image_url = images_info.get("img_url");
		int index_of_unique = image_url.length()/2;
		if(image_url.length() > 10)
		{
			index_of_unique = image_url.length()-(image_url.length()-10);
		}					
		Map<String, AttributeValue> new_item = null;
		if(index_of_unique != -1)
		{
			String unique_img_name = image_url.substring(index_of_unique);
			new_item = new HashMap<>();
			new_item.put("name", new AttributeValue(unique_img_name));
			new_item.put("image", new AttributeValue(image_url));
			if(images_info.get("latitude") != null)
			{
				new_item.put("latitude", new AttributeValue(images_info.get("latitude")));
				new_item.put("longitude", new AttributeValue(images_info.get("longitude")));
			}			
		}
		return new_item;
	}
	
	/**
	 * image items are retrieved from the table
	 * @param verlayout to display the retrieved message on the layout
	 * @return returns a list of images that are retrieved
	 */
	protected List<String> retrieveItemsFromDynamoDBTable(VerticalLayout verlayout)
	{
		HashMap<String, AttributeValue> scanFilter = new HashMap<String, AttributeValue>();
		List<Map<String, AttributeValue>> image_items = new ArrayList<>();
		List<String> image_urls = new ArrayList<>(); 
		ScanRequest scanRequest = new ScanRequest(tableName);
		ScanResult scanResult = dynamoDB.scan(scanRequest);        
		image_items = scanResult.getItems();   
		verlayout.addComponent(new Label("Images downloaded from DynamoDB"));
		//int count =1;
		for(int i=0; i< image_items.size(); i++)
		{
			scanFilter = (HashMap<String, AttributeValue>) image_items.get(i);
			AttributeValue image_url = scanFilter.get("image");
			String _url = image_url.toString();
			_url = _url.substring(4, _url.length()-2);
			image_urls.add(_url);
//			Link img_link = new Link("Images" + count, new ExternalResource(_url));			
//			img_link.setTargetName("User Pics");
//			verlayout.addComponent(img_link);
//			//verlayout.addComponent(new Label(_url));
//			count++;
		}	
		return image_urls;
	}
	
	/**
	 * retrieves all images and returns coordinates that are retrieved from images
	 * @return retruns a map of coordinates, key has latitude and value has longitude
	 */
	protected Map<String, String> retrieveAvailableLocations()
	{
		HashMap<String, AttributeValue> scanFilter = new HashMap<String, AttributeValue>();
		List<Map<String, AttributeValue>> image_items = new ArrayList<>();
		Map<String, String> locations = new HashMap<>();
		ScanRequest scanRequest = new ScanRequest(tableName);
		ScanResult scanResult = dynamoDB.scan(scanRequest);        
		image_items = scanResult.getItems();   				
		for(int i=0; i< image_items.size(); i++)
		{
			scanFilter = (HashMap<String, AttributeValue>) image_items.get(i);
			if(scanFilter.get("latitude") != null)
			{
				AttributeValue image_lat = scanFilter.get("latitude");
				AttributeValue image_lng = scanFilter.get("longitude");
				String image_lati = image_lat.toString();
				image_lati = image_lati.substring(4, image_lati.length()-2);
				String image_longi = image_lng.toString();
				image_longi = image_longi.substring(4, image_longi.length()-2);
				locations.put(image_lati, image_longi);
			}						
		}
		return locations;
	}
	
	/**
	 * Search images for a given coordinates and return a list of images
	 * @param lat latitude
	 * @param lng longitude 
	 * @return  a list of images that matches the coordinates
	 */
	protected List<String> retrieveImagesForCoordinates(String lat, String lng)
	{
		HashMap<String, AttributeValue> scanFilter = new HashMap<String, AttributeValue>();
		List<Map<String, AttributeValue>> image_items = new ArrayList<>();
		List<String> image_urls = new ArrayList<>(); 
		ScanRequest scanRequest = new ScanRequest(tableName);
		ScanResult scanResult = dynamoDB.scan(scanRequest);        
		image_items = scanResult.getItems();   				
		for(int i=0; i< image_items.size(); i++)
		{
			scanFilter = (HashMap<String, AttributeValue>) image_items.get(i);
			if(scanFilter.get("latitude") != null)
			{
				AttributeValue image_lat = scanFilter.get("latitude");
				AttributeValue image_lng = scanFilter.get("longitude");
				String image_lati = image_lat.toString();
				image_lati = image_lati.substring(4, image_lati.length()-2);				
				String image_longi = image_lng.toString();
				image_longi = image_longi.substring(4, image_longi.length()-2);
				if(image_lati.equals(lat) && image_longi.equals(lng))
				{
					AttributeValue image_url = scanFilter.get("image");
					String _url = image_url.toString();
					_url = _url.substring(4, _url.length()-2);
					image_urls.add(_url);
				}				
			}						
		}
		return image_urls;
	}
}
