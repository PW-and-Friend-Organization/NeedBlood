package com.cfc.needblood;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NeedBloodApplication extends Application{
	
	// user
	private String user_id = "";
	private String user_name = "";
	private String blood_type = "";
	private int push_notification = 1;	// Enable/Disable push notification
	
	public void ClearAll()
	{
		user_id = "";
		user_name = "";
		blood_type = "";
	}
	
	public String getUserID(){
		return user_id;
	}
	
	public void setUserID(String user_id){
		this.user_id = user_id;
	}
	
	public String getUsername(){
		return user_name;
	}
	
	public void setUsername(String user_name){
		this.user_name = user_name;
	}
	
	public String getBloodType()
	{
		return blood_type;
	}
	
	public void setBloodType(String blood_type)
	{
		this.blood_type = blood_type;
	}
	
	public int getPushNotification() {
		return push_notification;
	}

	public void setPushNotification(int push_notification) {
		this.push_notification = push_notification;
	}
	
	// Event
	private String event_id;
	
	public String getEventID(){
		return event_id;
	}
	
	public void setEventID(String event_id){
		this.event_id = event_id;
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
