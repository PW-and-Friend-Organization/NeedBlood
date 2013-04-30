package com.cfc.needblood;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PostRegister extends Activity{
	
	private static final String TAG = "PostRegister";
	
	private static final String urlAddUser = "http://reducespam.org/bd/add_user.php";
    private static final String c_user_id = "user_id";
	private static final String c_blood_type = "blood_type";
	
	private Spinner SpinnerBloodType;
	public TextView User_Name;
	public Button	Submit;
	
	// Progress Dialog
    private ProgressDialog pDialog;
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
//    private static final String TAG_USERPROFILE = "UserProfile";
    
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONArray jUserProfile = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_register);
		
		Log.d(TAG, "onCreate()");   

		User_Name = (TextView) findViewById(R.id.profile_name_txt);
		User_Name.setText(((NeedBloodApplication)getApplication()).getUsername());
		
		SpinnerBloodType = (Spinner) findViewById(R.id.select_blood_type);
		SpinnerBloodType.setOnItemSelectedListener(new OnItemSelectedListener() {	
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
				Log.d(TAG, "SpinnerBloodType onItemSelected()"); 
				((NeedBloodApplication) getApplication()).setBloodType((String)parent.getSelectedItem());
			}			
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});	
		
		
		Submit = (Button) findViewById(R.id.post_reg_ok_btn);
		Submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((NeedBloodApplication) getApplication()).getBloodType() == "" )
				{
					Toast.makeText( getApplicationContext(), R.string.select_blood_type_title, Toast.LENGTH_SHORT).show();
				}
				else
				{
					// Insert user_id to DB and back to Main Activity
					new AddUser().execute();
				}
			}
		});      
	}
	
	private void StartMainActivity()
	{
		Log.d(TAG, "GOTO StartMainActivity()");   
//		Intent i = new Intent(this, MainActivity.class);
//		startActivity(i);
        finish();
	}
	
	/**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class AddUser extends AsyncTask<String, String, String> {
 
    	private boolean bUser = false;
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PostRegister.this);
            pDialog.setMessage("Adding User To Database...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
        	Log.d(TAG, "AddUser doInBackground() start");
        	
        	String g_user_id = ((NeedBloodApplication) getApplication()).getUserID();
        	String g_blood_type = ((NeedBloodApplication) getApplication()).getBloodType();
        	
        	List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(c_user_id, g_user_id));
            params.add(new BasicNameValuePair(c_blood_type, g_blood_type));
            
            
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(urlAddUser, "POST", params);
 
            // Check your log cat for JSON reponse
            Log.d("All Records: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // products found
                    // Getting Array of Products
//                	jUserProfile = json.getJSONArray(TAG_USERPROFILE);
// 
//                    // looping through All Products
//                    for (int i = 0; i < jUserProfile.length(); i++) {
//                        JSONObject c = jUserProfile.getJSONObject(i);
// 
//                        // Storing each json item in variable
//                        String user_id = c.getString(c_user_id);
//                        String blood_type = c.getString(c_blood_type);
// 
//                        // creating new HashMap
//                        HashMap<String, String> map = new HashMap<String, String>();
// 
//                        // adding each child node to HashMap key => value
//                        map.put(c_user_id, user_id);
//                        map.put(c_blood_type, blood_type);
// 
//                        bUser = true;
//                    }
                    bUser = true;
                } else {
                    // no user profile found

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            Log.d(TAG, "AddUser doInBackground() end");
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
        	Log.d(TAG, "AddUser onPostExecute()");
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            
            if( bUser )
            {
            	Log.d(TAG, "AddUser from user return TRUE");
            	StartMainActivity();
            }
        }
    }
}
