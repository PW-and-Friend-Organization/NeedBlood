package com.cfc.needblood;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;

public class EventDetails extends Activity{

	public static final String TAG = "EventDetails";
	
	private ProgressDialog pDialog; // Progress Dialog
	JSONParser jParser = new JSONParser(); // JSON parser class
    private static final String url_event_details = "http://reducespam.org/bd/get_event_details.php";
    
    private static final String TAG_ID = "ID";
    private static final String TAG_DATE = "Date";
    private static final String TAG_ORGANISER = "Organiser";
    private static final String TAG_TIMESTART = "TimeStart";
    private static final String TAG_TIMEEND = "TimeEnd";
    private static final String TAG_LOCATION = "Location";
//    private static final String TAG_URGENCY = "Urgency";
//    private static final String TAG_STATE = "State";
    private static final String TAG_OTHERS = "Others";
    
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_EVENT = "events";
	
    private String event_id;
    private String organiser;
    private String date;
    private String location;
    private String start_time;
    private String end_time;
    private String others;
//    private String request_type;
    
//    private TextView txtTitle;
    private TextView txtOrganiser;
    private TextView txtEventDate;
    private TextView txtLocation;
    private TextView txtStartTime;
    private TextView txtEndTime;
    private TextView txtOther;
//    private TextView txtRequestType;
    
    private Button btnFBShare;
    
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private final String PENDING_ACTION_BUNDLE_KEY = "com.cfc.needblood:PendingAction";
    private PendingAction pendingAction = PendingAction.NONE;
    
    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    private UiLifecycleHelper uiHelper;


    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

		setContentView(R.layout.event_details);
		
		Log.d(TAG, "onCreate()");   
		
		event_id = ((NeedBloodApplication) getApplication()).getEventID();
		
//		txtTitle = (TextView) findViewById(R.id.detail_title);
        txtOrganiser = (TextView) findViewById(R.id.detail_organiser);
        txtEventDate = (TextView) findViewById(R.id.detail_event_date);
        txtLocation = (TextView) findViewById(R.id.detail_location);
        txtStartTime = (TextView) findViewById(R.id.detail_starttime);
        txtEndTime = (TextView) findViewById(R.id.detail_endtime);
        txtOther = (TextView) findViewById(R.id.detail_other);
//        txtRequestType = (TextView) findViewById(R.id.detail_request_type);

        btnFBShare = (Button) findViewById(R.id.fb_share);
        btnFBShare.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				onClickPostStatusUpdate();
			}
		});
        
		new GetEventDetails().execute();
	}
	
	/**
     * Background Async Task to Get complete product details
     * */
    class GetEventDetails extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventDetails.this);
            pDialog.setMessage("Loading event details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {
        	
        	int success;
           
        	// Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_ID, event_id));

            Log.d("All Events: ", params.toString());
            
            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = jParser.makeHttpRequest(url_event_details, "GET", params);
             
            // check your log for json response
            Log.d("Single Product Details", json.toString());

            // json success tag
            try {
				success = json.getInt(TAG_SUCCESS);
				
				if( success == 1){
					// successfully received product details
					JSONArray productObj = json.getJSONArray(TAG_EVENT); // JSON Array
	
				    // get first product object from JSON Array
					JSONObject event = productObj.getJSONObject(0);
				    
					organiser = event.getString(TAG_ORGANISER);
				    date = event.getString(TAG_DATE);
				    others = event.getString(TAG_OTHERS);
				    location = event.getString(TAG_LOCATION);
                    start_time = event.getString(TAG_TIMESTART);
                    end_time = event.getString(TAG_TIMEEND);
				    
				    Log.d(TAG, "Organiser: " + organiser);
				    Log.d(TAG, "date: " + date);
				    Log.d(TAG, "others: " + others);
				    Log.d(TAG, "state: " + location);
				}
					
			} catch (JSONException e) {
				e.printStackTrace();
			}
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
            
            // display product data in EditText
            txtOrganiser.setText(organiser);
            txtEventDate.setText(date);
            txtLocation.setText(location);
            txtStartTime.setText(start_time);
            txtEndTime.setText(end_time);
            txtOther.setText(others);
            
        }
    }
    
	private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(this)
                    .setTitle("Canceled")
                    .setMessage("permission_not_granted")
                    .setPositiveButton("ok", null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        updateUI();
    }

    private void updateUI() {
        Session session = Session.getActiveSession();   
    }

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                break;
            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
        }
    }
	
    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = "SUCCESS";
            alertMessage = "Successfully Posted\n" + message;
        } else {
            title = "ERROR";
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    
    private void onClickPostStatusUpdate() {
        performPublish(PendingAction.POST_STATUS_UPDATE);
    }

    private void postStatusUpdate() {
        if (hasPublishPermission()) {
        	final String message = "There is a blood donation drive on " + 
        			txtEventDate.getText().toString() +
        			"\nOrganise by " + formatStringCase(txtOrganiser.getText().toString()) +
        			"\nAt " + formatStringCase( txtLocation.getText().toString()) +
        			"\nHope to see you there!";
        	
            Request request = Request
                    .newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            showPublishResult(message, response.getGraphObject(), response.getError());
                        }
                    });
            request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }
    
    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action) {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
            } else {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
            }
        }
    }
    
    private String formatStringCase(String sMessage)
    {
    	String s = sMessage.toLowerCase(Locale.ENGLISH).toString();
    	final StringBuilder result = new StringBuilder(s.length());
    	String[] words = s.split("\\s");
    	for(int i=0,l=words.length;i<l;++i) {
    	  if(i>0) result.append(" ");      
    	  result.append(Character.toUpperCase(words[i].charAt(0)))
    	        .append(words[i].substring(1));

    	}
    	
    	return result.toString();
    }
}
