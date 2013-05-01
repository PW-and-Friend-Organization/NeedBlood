package com.cfc.needblood;

import static com.cfc.needblood.CommonUtilities.SENDER_ID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gcm.GCMRegistrar;

public class HomePage extends Activity {

	private static final String TAG = "HomePage";

	private static final String url_all_events = "http://reducespam.org/bd/get_latest_event.php";

	private Button btnRefresh;
	private Button btnSelectDate;
	private ListView lvEvents;

	ArrayList<HashMap<String, String>> eventsList;

	private ProgressDialog pDialog; // Progress Dialog
	JSONParser jParser = new JSONParser(); // Creating JSON Parser object
	JSONArray events; // products JSONArray

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_EVENT = "events";

	private static final String TAG_ID = "ID";
	private static final String TAG_DATE = "Date";
	private static final String TAG_ORGANISER = "Organiser";
	// private static final String TAG_TIMESTART = "TimeStart";
	// private static final String TAG_TIMEEND = "TimeEnd";
	private static final String TAG_LOCATION = "Location";
	// private static final String TAG_URGENCY = "Urgency";
	// private static final String TAG_STATE = "State";
	// private static final String TAG_DESC = "Others";

	private static final String TAG_MySQL_YEAR = "year";
	private static final String TAG_MySQL_MONTH = "month";
	private static final String TAG_MySQL_DAY = "day";

	private int mYear;
	private int mMonth;
	private int mDay;
	static final int DATE_DIALOG_ID = 1;

	// AsyncTask for registering GCM ID to our server
	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_page);

		Log.d(TAG, "onCreate()");

		Session session = Session.getActiveSession();
		if (session.isClosed()) {
			Log.d(TAG, "has logout.. move back to main");
			finish();
		}

		/* GCM codes */
		// Check if push notification is enabled
		SharedPreferences mPrefs = getSharedPreferences("login", 0);
		int pushNotif = mPrefs.getInt("push_notification", 1);
		((NeedBloodApplication) getApplication())
				.setPushNotification(pushNotif);

		// Do these only if push notification is enabled
		if (pushNotif == 1) {
			try {
				// Make sure device has proper dependencies
				GCMRegistrar.checkDevice(this);
				GCMRegistrar.checkManifest(this);

				// Get GCM registration ID from Google
				final String regId = GCMRegistrar.getRegistrationId(this);
				Log.d(TAG, "regId: " + regId);

				if (regId.equals("")) { // Not registered with GCM, register now
					GCMRegistrar.register(this, SENDER_ID);
					Log.d(TAG, "Registered on GCM");
				} else { // Device already registered on GCM
					if (GCMRegistrar.isRegisteredOnServer(this)) {
						// Check if registered on our SERVER
						Log.d(TAG, "Registered on our SERVER");
					} else { // If not registered on our server, try to register
								// on
								// another thread
						final Context context = this;
						mRegisterTask = new AsyncTask<Void, Void, Void>() {

							@Override
							protected Void doInBackground(Void... arg0) {
								// Register to our server
								String fbId = ((NeedBloodApplication)getApplication()).getUserID();
								String bloodType = ((NeedBloodApplication)getApplication()).getBloodType();
								if (ServerUtilities.register(context, regId, fbId, bloodType))
									updatePushNotifPref(1);
								else
									updatePushNotifPref(0);
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								// Destroy thread
								mRegisterTask = null;
							}
						};
						mRegisterTask.execute(null, null, null);
					}
				}

			} catch (UnsupportedOperationException e) {
				// Disable push notification if device doesn't support GCM
				((NeedBloodApplication) getApplication())
						.setPushNotification(0);
				pushNotif = 0;
				updatePushNotifPref(0);
				Log.d(TAG, "Device has no proper dependencies for GCM");
			}
		}
		/* END GCM codes */

		lvEvents = (ListView) findViewById(R.id.listview_event);
		lvEvents.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "lvEvents setOnItemClickListener()");
				// getting values from selected ListItem
				String event_id = ((TextView) view.findViewById(R.id.event_id))
						.getText().toString();
				((NeedBloodApplication) getApplication()).setEventID(event_id);

				Intent i = new Intent(getApplicationContext(),
						EventDetails.class);
				HomePage.this.startActivity(i);
			}
		});

		btnRefresh = (Button) findViewById(R.id.refresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "btnRefresh setOnClickListener()");
				new LoadAllEvents().execute();
			}
		});

		btnSelectDate = (Button) findViewById(R.id.select_date);
		btnSelectDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		updateDisplay();
	}

	/**
	 * Background Async Task to Load all product by making HTTP Request
	 * */
	class LoadAllEvents extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(HomePage.this);
			pDialog.setMessage("Loading events. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All products from url
		 * */
		protected String doInBackground(String... args) {
			Log.d(TAG, "LoadAllEvents doInBackground()");

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(TAG_MySQL_YEAR, Integer
					.toString(mYear)));
			params.add(new BasicNameValuePair(TAG_MySQL_MONTH, Integer
					.toString(mMonth + 1)));
			params.add(new BasicNameValuePair(TAG_MySQL_DAY, Integer
					.toString(mDay)));
			Log.d("All Events: ", params.toString());

			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_events, "GET",
					params);

			// Check your log cat for JSON reponse
			Log.d("All Events: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// products found
					// Getting Array of Products
					events = json.getJSONArray(TAG_EVENT);

					// Hashmap for ListView, clear a new set of data
					eventsList = new ArrayList<HashMap<String, String>>();

					// looping through All Products
					for (int i = 0; i < events.length(); i++) {
						JSONObject c = events.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_ID);
						String location = c.getString(TAG_LOCATION);
						String organiser = c.getString(TAG_ORGANISER);
						String date = c.getString(TAG_DATE);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_ID, id);
						map.put(TAG_LOCATION, location);
						map.put(TAG_ORGANISER, organiser);
						map.put(TAG_DATE, date);

						// adding HashList to ArrayList
						eventsList.add(map);
					}
				} else {
					// no products found
					// Launch Add New product Activity
					// Intent i = new Intent(getApplicationContext(),
					// NewProductActivity.class);
					// // Closing all previous activities
					// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// startActivity(i);
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
			// dismiss the dialog after getting all products
			pDialog.dismiss();

			if (eventsList.size() == 0) {
				Toast.makeText(HomePage.this,
						"Our server have not update for the selected date",
						Toast.LENGTH_LONG).show();

			}

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(HomePage.this,
							eventsList, R.layout.list_event, new String[] {
									TAG_ID, TAG_ORGANISER, TAG_LOCATION,
									TAG_DATE }, new int[] { R.id.event_id,
									R.id.event_title, R.id.event_desc,
									R.id.event_date });
					// updating listview
					lvEvents.setAdapter(adapter);
				}
			});

		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {

		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		}
	}

	private void updateDisplay() {
		btnSelectDate.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(mMonth + 1).append("-").append(mDay).append("-")
				.append(mYear).append(" "));
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	@Override
	protected void onDestroy() {
		// Delete mRegisterTask before exit
		if (mRegisterTask != null)
			mRegisterTask = null;

		// GCMRegistrar.onDestroy(this);

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) { // Switch case for which menu item clicked
		case R.id.menu_donation_guideline:
			i = new Intent(getApplicationContext(), DonationGuidelines.class);
			this.startActivity(i);
			break;
		case R.id.menu_about_us:
			i = new Intent(getApplicationContext(), About.class);
			this.startActivity(i);
			break;
		case R.id.menu_logout:
			onClickLogout();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void onClickLogout() {
		Log.d(TAG, "onClickLogout");
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}

		((NeedBloodApplication) getApplication()).ClearAll();

		SharedPreferences.Editor mEditor = getSharedPreferences("login", 0)
				.edit();
		mEditor.putString("login", "0").commit();

		finish();
	}

	/**
	 * Update preferences for push notification
	 * 
	 * @param push_notif
	 *            1 if push notification = true, 0 otherwise
	 */
	public void updatePushNotifPref(int push_notif) {
		SharedPreferences mPrefs = getSharedPreferences("login", 0);
		SharedPreferences.Editor mEditor = mPrefs.edit();
		mEditor.putInt("push_notification", push_notif).commit();
	}
}
