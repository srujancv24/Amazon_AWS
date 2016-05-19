/**
 * 	implementation of RestfulService consumption. Save, the response in JSON
 * 
 * @author venkata subba rao cheedella, Cletan Sequeira
 * 
 * 
 */

package com.example.instagrampics1;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class RestfulService {
		
	private static String access_token = "2215670960.579ed75.10f5b84696fb4bc5b09eaafaabcd19e8";
	private static String url_instagram_srch_user = "https://api.instagram.com/v1/users/search?q=";	
	private static String url_instagram_media_url_p1 = "https://api.instagram.com/v1/users/";
	private static String url_instagram_media_url_p2 = "/media/recent/?access_token=";
	private static String url_instagram_locationid_url = "https://api.instagram.com/v1/locations/search?";
	private static Client client;
	private static WebTarget target;
	/**
	 * 
	 * @return returns a complete url to get the registered id for an user
	 */
	protected static String createSearchUserURL(String username)
	{
		String searchUserURL = url_instagram_srch_user+username+"&access_token=" + access_token;
		return searchUserURL;
	}
	
	/**
	 * Creates a Instagram end point for user provided coordinates
	 * @param lat latitude coordinate
	 * @param lng longitude coordinate
	 * @return returns the Instagram endpoint
	 */
	protected static String createURLTogetLocationIds(String lat, String lng)
	{
		String locationIdsURL = url_instagram_locationid_url + "lat=" + lat + "&lng=" + lng + "&access_token=" + access_token;
		return locationIdsURL;
	}
	
	/**
	 * 
	 * @param user_id user id of an Instagram User
	 * @return returns an Instagram endpoint to retrive uploaded images of user
	 */
	protected static String createInstagramMediaUserURL(String user_id)
	{
		String media_URL = url_instagram_media_url_p1+ user_id + url_instagram_media_url_p2 + access_token;
		return media_URL;
	}
	
	/**
	 * download data related to media files uploaded by the user in Instagram app
	 * @param _url	endpoint of instagram service
	 * @return filename of saved json
	 * @throws IOException if the file operations are not successful
	 */
	protected static String downloadJSONLocationIds(String _url) throws IOException
	{
		client = ClientBuilder.newClient();
		target = client.target(_url);
		String filename = "LocationIds.json";
		File file = new File(filename);
		if(!file.exists())
		{
			file.createNewFile();
		} 
		else
		{
			file.delete();
			file.createNewFile();
		}
		PrintWriter out = new PrintWriter(file);
		out.print(target.request(MediaType.APPLICATION_JSON).get(String.class));
		out.close();
		return filename;
	}
	
	/**
	 * Retrieved User id for a given user name
	 * @param _url	instagram end point
	 * @param user name of the selected user
	 * @return return the filename where we can retrieve the user id
	 * @throws IOException if the file operations are not successful
	 */
	protected static String downloadJSONForUserId(String _url, String user_id) throws IOException
	{
		client = ClientBuilder.newClient();
		target = client.target(_url);
		String file_name = user_id + ".json";
		File file = new File(file_name);
		if(!file.exists())
		{
			file.createNewFile();
		} 
		else
		{
			file.delete();
			file.createNewFile();
		}
		PrintWriter out = new PrintWriter(file);
		out.print(target.request(MediaType.APPLICATION_JSON).get(String.class));
		out.close();
		return file_name;
	}
	
	/**
	 * 
	 * @param _url instagram end point to retrieve media json of user
	 * @param user_name user name of the user
	 * @return filename of the saved json file
	 * @throws IOException if the file operations are not successful
	 */
	protected static String downloadUserImagesUrls(String _url, String user_name) throws IOException
	{
		client = ClientBuilder.newClient();
		target = client.target(_url);
		String file_name = user_name + "_images.json";	
		File file = new File(file_name);
		if(!file.exists())
		{
			file.createNewFile();
		} 
		else
		{
			file.delete();
			file.createNewFile();
		}
		PrintWriter out = new PrintWriter(file);
		out.print(target.request(MediaType.APPLICATION_JSON).get(String.class));
		out.close();
		return file_name;
	}
}
