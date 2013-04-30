package com.cfc.needblood;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

	/**
	 * BASE URL of the web server. 
	 * TODO Change it to our web server address.
	 * 
	 * Note: this string is used as below.
	 * String serverUrl = SERVER_URL + "/register.php"; 
	 */
	static final String SERVER_URL = "http://192.168.1.4/GCM";

	/**
	 * Google API project id registered to use GCM. Currently using Alvin's
	 * Google Account.
	 */
	static final String SENDER_ID = "116032215270";

	/**
	 * Tag used on log messages. Change it whatever you like.
	 */
	static final String TAG = "GCM_NeedBlood";
}
