package GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import modeling.tools.TextInOut;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TrafficModelingHistory {
	String trafficFileName;
	String tempFileName;
	String modelFileName;
	
	public TrafficModelingHistory(String traffic, String temp, String model)
	{
		this.trafficFileName = traffic;
		this.tempFileName = temp;
		this.modelFileName = model;
	}
	
	public void store(String filename)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String data = gson.toJson(this);
		try {
			TextInOut.writeFile(data, filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static TrafficModelingHistory load(String filename)
	{
		String data = "";
		try {
			ArrayList<String> temp = TextInOut.readFile(filename);
			for (String str:temp)
				data += str + "\r\n";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		TrafficModelingHistory obj = gson.fromJson(data, TrafficModelingHistory.class);
		return obj;
	}
}
