/**
 * Using Jersy framework , we can parse the JSOn file to retrieve the necessary 
 * data from the JSON file
 * 
 * @author venkata subba rao cheedella, Cletan Sequeira
 * 
 * 
 */

package com.example.instagrampics1;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
	
	
	/**
	 * Retrieve user id from the json file
	 * @param filename json file where user details of an Instagram user can be retrieved
	 * @return returns user id of an user
	 * @throws IOException if file operations are not successful
	 * @throws ParseException when parsing a JSON is invalid
	 */
	public static String getUserIDfromJSONFile(String filename) throws IOException, ParseException
	{
		String user_id = "";
		FileReader filereader = new FileReader(filename);
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(filereader);
		filereader.close();
		JSONArray dataArray = (JSONArray)jsonObj.get("data");
		if(dataArray.size() != 0)
		{
			JSONObject dataobj = (JSONObject)dataArray.get(0);
			user_id = (String)dataobj.get("id");
		}	
		return user_id;
	}
		
	/**
	 * returns a list of user images from JSON file
	 * @param filename JSON file where user uploaded image urls can be retrieved
	 * @return list of image urls
	 * @throws IOException if the file operations are not successful
	 * @throws ParseException if parsing a file is invalid
	 */
	public static List<Map<String, String>> getListOfUserImages(String filename) throws IOException, ParseException
	{
		FileReader filereader = new FileReader(filename);
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(filereader);
		filereader.close();
		JSONArray dataArray = (JSONArray)jsonObj.get("data");
		List<Map<String, String>> user_images_info = new ArrayList<>();
		for(int i=0; i< dataArray.size(); i++)
		{
			JSONObject dataobj = (JSONObject)dataArray.get(i);
			Map<String, String> user_image_info = new HashMap<String, String>();
			if((JSONObject)dataobj.get("location") != null)
			{
				JSONObject location = (JSONObject)dataobj.get("location");
				String latitude = String.valueOf(location.get("latitude"));
				String longitude = String.valueOf(location.get("longitude"));
				user_image_info.put("latitude", latitude);
				user_image_info.put("longitude", longitude);
			}			
			JSONObject images_array = (JSONObject)dataobj.get("images");			
			JSONObject thumbnail_img = (JSONObject)images_array.get("thumbnail");
			String low_res_img = (String)thumbnail_img.get("url");					
			user_image_info.put("img_url", low_res_img);
			user_images_info.add(user_image_info);		
		}
		return user_images_info;
	}
}
