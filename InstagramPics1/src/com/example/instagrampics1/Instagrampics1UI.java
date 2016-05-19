/**
 * InstagramUserPics - Implementation of an application where it consumes Instagram API to retrieve 
 * images uploaded by an user. The response, JSON files is parsed to retrieve the image URLS of users.
 * The retreives image URLS are updated to a Dynamo Database. Dynamo DB is a nosql 
 * database, a service provided by Amazon Web Services. If the application user wants to retrieve
 * the image items from the dynamo db table, he can click a button on the UI.
 * 
 * Instagrampics1UI - It provides User Inteface to the application. Application UI prompts the application 
 * user to select the registered users of the application. Based on the user selection,the logical flow varies.
 * 
 * In the second tab of the application user can provided latitude and longitude coordinates to retrieve 
 * the images from Instagram API. 
 * 
 * @author venkata subba rao cheedella, Cletan Sequeira
 * 
 */



package com.example.instagrampics1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.parser.ParseException;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
@Theme("instagrampics1")

public class Instagrampics1UI extends UI {	

	private List<String> registered_users = new ArrayList<>();	
	private VerticalLayout tab_home;
	private TextField latitude;
	private TextField longitude;
	private String JSON_userinfo;
	private List<Map<String, String>> user_image_urls;
	private AmazonWebServices awsservices;
	private VerticalLayout tab_locationPicsSrch;
	private Button retrieveImagesFromDB;
	private String aws_acccess_key = "AKIAIY7DMAZYF5KMJN7A";
	private String aws_secret_key = "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";

	/**
	 * 
	 * @return here, it returns the registered users of InstagramUserPics app. The user should be valid
	 * Instagram user
	 */
	public List<String> getUsers()
	{
		registered_users.add("srujanchalasani");
		registered_users.add("ajesh1704");
		registered_users.add("harik01");
		registered_users.add("cletansequeira");
		return registered_users;
	}

	/**
	 * Display the images in URL format when an user clicks an images, selected image is popped-up
	 * in new window
	 * @param verlayout	it is a layout where the images should be displayed
	 * @param listofImages images that has to be rendered to UI
	 */
	private void displayPics(VerticalLayout verlayout, List<String> listofImages)
	{

		for(int i=0; i< listofImages.size(); i++)
		{			
			Link img_link = new Link(listofImages.get(i), new ExternalResource(listofImages.get(i)));			
			img_link.setTargetName("User Pics");
			verlayout.addComponent(img_link);
		}
	}	

	/**
	 * This is combination of uploading images to dynamo db and addition feature (future work on the project)
	 * The JSON file, response from the restful service is parsed. Image URLS of user is retrieved from JSON and
	 * saved to dynamo DB table.
	 * Addition work : JSON file is saved to S3, Scalable storage on cloud. This feature is working. The file is 
	 * successfully uploaded to S3 bucket. However, it do not have significance in the flow.
	 * @param AWSAccessKey Access key to access Amazon Web Services(AWS)
	 * @param AWSSecretKey Secrete Key to access AWS, dynamo db tables.
	 */
	private void uploadFileToS3(String AWSAccessKey, String AWSSecretKey)
	{
		awsservices = new AmazonWebServices(AWSAccessKey, AWSSecretKey);    
		String uploadstatus = awsservices.uploadFileToS3(JSON_userinfo);	
		String s3_bucket = uploadstatus.substring(uploadstatus.length()-8, uploadstatus.length());					// removes characters from response to display message, i.e. easy to understand
		tab_home.addComponent(new Label("File is uplaoded to AmazonS3 bucket and the container : " +s3_bucket));
		tab_home.addComponent(new Label(""));
		tab_home.addComponent(new Label("Please click below button to upload images of user to dynamoDb table"));
		Button uploadUserImageItems = new Button("UpLoad Images to DynamoDB");
		tab_home.addComponent(uploadUserImageItems);
		uploadUserImageItems.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				awsservices.createDynamoDBTable(tab_home);									// creation of dynamo db table
				awsservices.addItemsToDynamoDbTable(user_image_urls);						// add the image urls to the dynamodb table
				tab_home.addComponent(new Label("Images has been uploaded to DynamoDB"));	
				tab_home.addComponent(new Label(""));
				tab_home.addComponent(retrieveImagesFromDB);
			}
		});		
		retrieveImagesFromDB = new Button("Retrieve Images from DB");		
		retrieveImagesFromDB.addClickListener(new ClickListener() {			
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				List<String> user_images = awsservices.retrieveItemsFromDynamoDBTable(tab_home);	// retrieve all images from the Dynamo DB table					
				displayPics(tab_home, user_images);
			}
		});

	}

	/**
	 * When a user is selected from the drop down of list of registered users an API restful service call is made to
	 * Instagram end point. User ID is retrieved for an user. Using user id, access token (which is 
	 * retrieved from another API call) , we consume restful service and download JSON which contains the uploaded
	 * images of user. JSON file is parsed to retrieve images. The above mentioned functions are performed in 
	 * other class functions. Functions calls are made here
	 * @param user_name user name of the selected user
	 * @param layout necessary details are passed to the layout to render to UI
	 */
	private void comboBoxValueChangeEventHandler(String user_name, VerticalLayout layout)
	{
		// TODO Auto-generated method stub
		//String user_name = "ajesh1704";
		String search_user_id_url = RestfulService.createSearchUserURL(user_name);

		try {
			JSON_userinfo = RestfulService.downloadJSONForUserId(search_user_id_url, user_name);
			String user_id = JsonParser.getUserIDfromJSONFile(JSON_userinfo);
			Label user_id_text = new Label("UserID is retrieved for + " + user_name + " " + user_id);
			PopupView popup = new PopupView("Click for User_ID" , user_id_text);			
			//layout.addComponent(popup);			
			if(user_id != null)
				popup.setVisible(true);
			String user_images_url = RestfulService.createInstagramMediaUserURL(user_id);
			String userimages_json = RestfulService.downloadUserImagesUrls(user_images_url, user_name);
			if(userimages_json != null)
			{				
				tab_home.addComponent(new Label(""));
				layout.addComponent(new Label(user_name + "images JSON has been successfully downloaded"));
				String curr_dir = new File(".").getCanonicalPath();
				layout.addComponent(new Label("JsOn file resides at :   " + curr_dir));				
				tab_home.addComponent(new Label(""));
				user_image_urls = JsonParser.getListOfUserImages(userimages_json);					
				Button upload_file = new Button("Upload file to S3");
				layout.addComponent(upload_file);
				upload_file.addClickListener(new ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						// TODO Auto-generated method stub					
						if(aws_acccess_key.equals("AKIAIY7DMAZYF5KMJN7A") && aws_secret_key.equals("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"))			// Here you can find AWS access key and secret key, 
						{																																// They are created after creating an account in AWS portal. https://aws.amazon.com/
							uploadFileToS3(aws_acccess_key, aws_secret_key);
						}
						else
						{
							tab_home.addComponent(new Label("Please provide valid AWSAccessKey and AWSSecretKey"));
						}
					}
				});				
			}				
			else
			{
				layout.addComponent(new Label("Error at downlaoding JSON"));
			}			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * When an user provides the latitude and longitude coordinates, a restful service call is consumed to 
	 * retrieve the public data(images) uploaded by all users of Instagram. However, our application runs in 
	 * sandbox mode, it can retrieve only the registered users of InstagramUserPIcs application. In this case,
	 * our application users have uploaded only in one location. Those images are retrieved.
	 * 
	 * @param locationPicsSearchTab data that has to be rendered is directed to this layout
	 */
	private void runLocationPicsSearchTab(VerticalLayout locationPicsSearchTab)
	{
		locationPicsSearchTab.addComponent(new Label(""));
		locationPicsSearchTab.addComponent(new Label(""));
		locationPicsSearchTab.addComponent(new Label
				("Here you can search InstagramPics by providing Latitude and Longitude coordinates"));

		//Creating textfields for consuming latitude and longitude coordinates
		locationPicsSearchTab.addComponent(new Label(""));		
		HorizontalLayout latCoordinates = new HorizontalLayout();
		locationPicsSearchTab.addComponent(new Label("Please enter Latitude and Longitude coordinates to search images"));		
		latCoordinates.addComponent(new Label("Latitude Coordinates : "));
		latitude = new TextField();
		latCoordinates.addComponent(latitude);

		HorizontalLayout longCoordinates = new HorizontalLayout();
		longCoordinates.addComponent(new Label("Longitude Coordinates : "));	
		longitude = new TextField();
		longCoordinates.addComponent(longitude);		
		locationPicsSearchTab.addComponent(new Label(""));
		locationPicsSearchTab.addComponent(latCoordinates);
		locationPicsSearchTab.addComponent(new Label(""));
		locationPicsSearchTab.addComponent(longCoordinates);
		Button retrieve_Images = new Button("Download Images");
		retrieve_Images.addClickListener(new ClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				String lat = latitude.getValue();
				String longi = longitude.getValue();
				AmazonWebServices aws_services = new AmazonWebServices(aws_acccess_key, aws_secret_key);
				aws_services.initDynamoDBTable();														
				if(lat != "" && longi != "")
				{				
					List<String> loc_imgs_urls = aws_services.retrieveImagesForCoordinates(lat, longi);
					if(loc_imgs_urls.size() != 0)
					{
						List<String> loc_images = new ArrayList<>();
						for(int i=0; i< loc_imgs_urls.size(); i++)
						{
							//tab_locationPicsSrch.addComponent(new Label(loc_imgs_urls.get(i)));
							loc_images.add(loc_imgs_urls.get(i));
						}
						displayPics(tab_locationPicsSrch, loc_images);
					}
					else
					{
						tab_locationPicsSrch.addComponent(new Label("No images are uploaded at provided location"));
						tab_locationPicsSrch.addComponent(
								new Label("Please find the location below where user has uploaded images"));
						Map<String, String> locations = aws_services.retrieveAvailableLocations();
						tab_locationPicsSrch.addComponent(new Label(""));
						if(locations != null)
						{						
							Iterator<?> it = locations.entrySet().iterator();		
							while(it.hasNext())														// Retrieve all images that have location information
							{								
								Map.Entry<String, String> pair = (Entry<String, String>) it.next();
								String key = pair.getKey();
								String value = pair.getValue();
								tab_locationPicsSrch.addComponent(new Label("Latitude : " + key + "  Longitude : " + value));
							}
						}
					}
				}
				else
				{							
					Label info_status = new Label("Please provide Logitude and Latitude coordinates");
					tab_locationPicsSrch.addComponent(info_status);					
				}
			}
		});
		locationPicsSearchTab.addComponent(retrieve_Images);
	}

	/**
	 * 	This is start point of the application. 
	 * 	
	 */
	@Override
	protected void init(VaadinRequest request) {		
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);			
		TabSheet tabsheet = new TabSheet();								// Here UI has two tabs, one, to search pics for a given location and the other to retrieve images for an user
		tabsheet.addSelectedTabChangeListener(new SelectedTabChangeListener(){

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				// TODO Auto-generated method stub
				TabSheet tabsheet = event.getTabSheet();							
				VerticalLayout location_pics_srch = (VerticalLayout)tabsheet.getSelectedTab();
				if(location_pics_srch == tab_locationPicsSrch)
				{
					runLocationPicsSearchTab(location_pics_srch);	
				}								
			}			
		});
		tab_home = new VerticalLayout();			
		tab_locationPicsSrch = new VerticalLayout();
		tabsheet.addTab(tab_home, "Home");		
		tabsheet.addTab(tab_locationPicsSrch, "Location Pics Search");
		tab_home.addComponent(new Label(""));
		tab_home.addComponent(new Label(""));			
		ComboBox user_dropdown = new ComboBox("Select Registered User");				// dropdown menu to select the user
		ComboBox user_dropdown_detail = new ComboBox("Select Registered User");	
		user_dropdown.addItems(getUsers());	
		user_dropdown_detail.addItems(getUsers());
		user_dropdown.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				// TODO Auto-generated method stub
				String selected_user = (String)event.getProperty().getValue();	
				if(selected_user != null)
				{
					comboBoxValueChangeEventHandler(selected_user, tab_home);
					tab_home.addComponent(new Label(""));					
				}
				else
					layout.addComponent(new Label("Selected User is not registered"));
			}
		});		

		user_dropdown.setImmediate(true);			
		tab_home.addComponent(user_dropdown);			
		layout.addComponent(tabsheet);
	}
}