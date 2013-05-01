package com.cfc.needblood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class MainActivity extends Activity {
    
	private static final String TAG = "MainActivity";
    
	private static final String urlCheckUser = "http://reducespam.org/bd/check_user.php";
    private static final String c_user_id = "user_id";
	private static final String c_blood_type = "blood_type";
	private ProgressDialog pDialog; // Progress Dialog
    private static final String TAG_SUCCESS = "success"; // JSON Node names
    private static final String TAG_USERPROFILE = "UserProfile";
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONArray jUserProfile = null;
	
    private TextView textInstructionsOrLink;
    private Button buttonLoginLogout;
    private Button buttonHome;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
 
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		Log.d(TAG, "onCreate");
		
        setContentView(R.layout.main);
        buttonLoginLogout = (Button)findViewById(R.id.buttonLoginLogout);
        buttonHome = (Button)findViewById(R.id.button_goto_homepage);
        textInstructionsOrLink = (TextView)findViewById(R.id.instructionsOrLink);

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, null, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this));
            }
        }
     	
        buttonHome.setVisibility(View.GONE);
        buttonHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StartHomePage();
			}
		});
        
//        updateView();
        if (session.isOpened()) {
        	String user_name = ((NeedBloodApplication) getApplication()).getUsername();
        	if( user_name == "0" || user_name == null ){
        		textInstructionsOrLink.setText("");
        	}else{
        		textInstructionsOrLink.setText("Log in as " + user_name );
        	}
        	buttonHome.setVisibility(View.VISIBLE);
        	buttonLoginLogout.setText(R.string.logout);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { 
                	onClickLogout(); 
                }
            });
        } else {
        	textInstructionsOrLink.setText(R.string.instructions);
        	buttonHome.setVisibility(View.GONE);
        	buttonLoginLogout.setText(R.string.login);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { 
                	onClickLogin(); 
                }
            });
        }
        
        // If has register and login previously, can bypass this page.
        SharedPreferences mPrefs = getSharedPreferences("login", 0);
    	String mString = mPrefs.getString("login", "0");

    	if( mString.compareTo("1") == 0 ){
        	mPrefs = getSharedPreferences("user_id", 0);
        	((NeedBloodApplication) getApplication()).setUserID( mPrefs.getString("user_id", "0"));
        	mPrefs = getSharedPreferences("user_name", 0);
        	((NeedBloodApplication) getApplication()).setUsername( mPrefs.getString("user_name", "0"));
        	mPrefs = getSharedPreferences("blood_type", 0);
        	((NeedBloodApplication) getApplication()).setBloodType( mPrefs.getString("blood_type", "0"));
        	
        	StartHomePage();
        	return;
        }
    	
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        //Session.getActiveSession().addCallback(statusCallback);
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
        	String user_name = ((NeedBloodApplication) getApplication()).getUsername();
        	if( user_name == "0" || user_name == null ){
        		textInstructionsOrLink.setText("");
        	}else{
        		textInstructionsOrLink.setText("Log in as " + user_name );
        	}
        	buttonHome.setVisibility(View.VISIBLE);
        	buttonLoginLogout.setText(R.string.logout);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { 
                	onClickLogout(); 
                }
            });
        } else {
        	textInstructionsOrLink.setText(R.string.instructions);
        	buttonHome.setVisibility(View.GONE);
        	buttonLoginLogout.setText(R.string.login);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { 
                	onClickLogin(); 
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult " + requestCode + " " + resultCode + "" + data.toString());
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    private void updateView() {
    	
        final Session session = Session.getActiveSession();
        Log.d(TAG, "updateView");
        if (session.isOpened()) {
        	Log.d(TAG, "updateView session is opened");
        	
        	String user_name = ((NeedBloodApplication) getApplication()).getUsername();
        	if( user_name == "0" || user_name == null ){
        		textInstructionsOrLink.setText("");
        	}else{
        		textInstructionsOrLink.setText("Log in as " + user_name );
        	}
        	buttonHome.setVisibility(View.VISIBLE);
        	buttonLoginLogout.setText(R.string.logout);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
            

    		// If the session is open, make an API call to get user data
	        // and define a new callback to handle the response
	        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	                // If the response is successful
	                if (session == Session.getActiveSession()) {
	                    if (user != null) {
	                    	// Set the value to the Global
 	                    	((NeedBloodApplication) getApplication()).setUserID( user.getId() ) ;//user id
	                    	((NeedBloodApplication) getApplication()).setUsername( user.getName() );//user's profile name
	                    	
	                    	// Check DB -> If User Exist, move straight to home, else go to PostRegister
	            	        new CheckUser().execute();
	                    }
	                }
	            }
	        });
	        Request.executeBatchAsync(request);
	        
        } else {
        	Log.d(TAG, "updateView session is closed");
            textInstructionsOrLink.setText(R.string.instructions);
            buttonHome.setVisibility(View.GONE);
        	buttonLoginLogout.setText(R.string.login);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
        	Log.d(TAG, "onClickLogin session not active");
        	session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        	
        	List<String> permissions = new ArrayList<String>();
        	permissions.add("publish_action");
        	session.openForPublish(new OpenRequest(this).setPermissions(permissions));
        } else {
        	Log.d(TAG, "onClickLogin session active " + session.isOpened() + " " + session.isClosed());
        	Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
    	Log.d(TAG, "onClickLogout");
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
        
        ((NeedBloodApplication) getApplication()).ClearAll();
        
        SharedPreferences.Editor mEditor = getSharedPreferences("login", 0).edit();
	    mEditor.putString("login", "0").commit();
	    
	    updateView();
    }

    private void StartHomePage()
    {
    	// to by pass this page on next login
    	SharedPreferences mPrefs = getSharedPreferences("login", 0);
    	String mString = mPrefs.getString("login", "0");
        if( mString == "0" ){
	    	SharedPreferences.Editor mEditor = mPrefs.edit();
	    	mEditor.putString("login", "1").commit();
	    	mEditor.putString("user_id", ((NeedBloodApplication) getApplication()).getUserID() ).commit();
	    	mEditor.putString("user_name", ((NeedBloodApplication) getApplication()).getUsername() ).commit();
	    	mEditor.putString("blood_type", ((NeedBloodApplication) getApplication()).getBloodType() ).commit();
        }
        
    	Intent i = new Intent(this, HomePage.class);
    	startActivity(i);
    }
    
    private void StartPostRegister()
    {
    	Intent i = new Intent(this, PostRegister.class);
        startActivity(i);
    }
    
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	Log.d(TAG, "SessionStatusCallback updateView");
        	updateView();
        }
    }
    
	/**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class CheckUser extends AsyncTask<String, String, String> {
 
    	private boolean bUser = false;
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading user information. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
        	Log.d(TAG, "CheckUser doInBackground() start");
        	
        	String g_user_id = ((NeedBloodApplication) getApplication()).getUserID();
        	//String g_blood_type = ((NeedBloodApplication) getApplication()).getBloodType();
        	
        	List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(c_user_id, g_user_id));
            //params.add(new BasicNameValuePair(c_blood_type, g_blood_type));
            
            
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(urlCheckUser, "POST", params);
 
            // Check your log cat for JSON reponse
            Log.d("All Records: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                	jUserProfile = json.getJSONArray(TAG_USERPROFILE);
 
                    // looping through All Products
                    for (int i = 0; i < jUserProfile.length(); i++) {
                        JSONObject c = jUserProfile.getJSONObject(i);
 
                        // Storing each json item in variable
                        String user_id = c.getString(c_user_id);
                        String blood_type = c.getString(c_blood_type);
 
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
 
                        // adding each child node to HashMap key => value
                        map.put(c_user_id, user_id);
                        map.put(c_blood_type, blood_type);
 
                        bUser = true;
                    }
                } else {
                    // no user profile found
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            Log.d(TAG, "CheckUser doInBackground() end");
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
        	Log.d(TAG, "ValidateLogin onPostExecute()");
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            
            if( bUser )
            {
            	Log.d(TAG, "User return TRUE, move to StartHomePage()");   
            	//StartHomePage(); // disable this so that user wont experience double home page due to 
            					   // auto loading from this.
            	String user_name = ((NeedBloodApplication) getApplication()).getUsername();
            	if( user_name == "0" || user_name == null ){
            		textInstructionsOrLink.setText("");
            	}else{
            		textInstructionsOrLink.setText("Log in as " + user_name );
            	}
            	
            }
            else
            {
            	Log.d(TAG, "User return FALSE, move to StartPostRegister()");   
            	StartPostRegister();
            }
        }
    }
}
