package com.example.profilehandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.profilehandling.adapter.SimpleListViewAdapter;
import com.example.profilehandling.adapter.selectedContactAdapter;
import com.example.profilehandling.data.Contact;

public class CreateProfile extends Activity implements OnClickListener {

	Spinner profiles;
	private static final String TAG = "CreateProfile";
	String[] profileOptions;
	PopupWindow pw;
	LayoutInflater inflater;
	View layout;
	Button add,delete,unselect,sms;
	ListView contactList,contacts;
	ArrayList<Contact> contactDetails = new ArrayList<Contact>();
	Contact selectedContact;
	ArrayList<Contact> selectedContactDetails = new ArrayList<Contact>();
	selectedContactAdapter adapterData;
	Intent intent;
	boolean status = true; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_profile);
		profiles = (Spinner)findViewById(R.id.profiles);
		contacts = (ListView)findViewById(R.id.contacts);
		add = (Button)findViewById(R.id.add);
		delete = (Button)findViewById(R.id.delete);
		unselect = (Button)findViewById(R.id.unselect);
		sms = (Button)findViewById(R.id.sms);
		readToFile();
		adapterData = new selectedContactAdapter(CreateProfile.this, R.id.contactList, selectedContactDetails);
		contacts.setAdapter(adapterData);
		add.setOnClickListener(this);
		delete.setOnClickListener(this);
		unselect.setOnClickListener(this);
		sms.setOnClickListener(this);
		profiles.setOnItemSelectedListener(listener);
		profileOptions = getResources().getStringArray(R.array.spinner_items);
		inflater = (LayoutInflater) getSystemService(CreateProfile.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.select_contact, null,false);
		SharedPreferences pref = getSharedPreferences("Mode", 0);
		int profileMode = pref.getInt("Profile", 0);		
		profiles.setSelection(profileMode);
		//intent = new Intent(CreateProfile.this, PhoneCallService.class);
		/*intent.putExtra("Profile", profileOptions[profileMode]);
		startService(intent);*/
	}
	OnItemSelectedListener listener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			if(profileOptions[pos].equalsIgnoreCase("Meeting") && selectedContactDetails.isEmpty()) {
				Toast.makeText(CreateProfile.this, "Please add contacts", Toast.LENGTH_LONG).show();
				SharedPreferences pref = getSharedPreferences("Mode", 0);
				int profileMode = pref.getInt("Profile", 0);		
				profiles.setSelection(profileMode);
			} else {
				SharedPreferences pref = getSharedPreferences("Mode", 0);
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt("Profile", pos);
				editor.putString("MODE", profileOptions[pos]);
				editor.commit();
				if(isMyServiceRunning()) {
					//stopService(intent);
				} else {
					intent = new Intent(CreateProfile.this, PhoneCallService.class);
					intent.putExtra("Profile", profileOptions[pos]);
					startService(intent);
				}
			}
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			if(profileOptions[pos].equalsIgnoreCase("Normal")) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else if(profileOptions[pos].equalsIgnoreCase("Busy")) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			} else if(profileOptions[pos].equalsIgnoreCase("Meeting")) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (PhoneCallService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_profile, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add:
			selectContacts();
			break;
		case R.id.delete:
			deleteSelectedItems();
			writeToFile();
			break;
		case R.id.unselect:
			unSelectItems();
			break;
		case R.id.sms:
			editSMS();
			break;
		default:
			break;
		}
	}

	public void editSMS() {
		Intent edit = new Intent(CreateProfile.this,SMSListActivity.class);
		startActivity(edit);
	}
	public void unSelectItems() {
		ArrayList<Contact> allContacts = adapterData.contact;
		for(int i = 0; i < allContacts.size(); i++) {
			Contact ct = allContacts.get(i);
			if(ct.isSelected()){
				ct.setSelected(false);
			}
		}
		adapterData.notifyDataSetChanged();
	}

	public void selectContacts() {
		initiatePopupWindow(contacts);
	}

	public void deleteSelectedItems() {
		ArrayList<Contact> allContacts = adapterData.contact;
		ArrayList<Contact> markedContacts = new ArrayList<Contact>();
		for(int i = 0; i < allContacts.size(); i++) {
			Contact ct = allContacts.get(i);
			if(ct.isSelected()){
				ct.setSelected(false);
				markedContacts.add(ct);
			}
		}
		selectedContactDetails.removeAll(markedContacts);
		adapterData.notifyDataSetChanged();
	}

	private void initiatePopupWindow(View v) {
		try {
			pw = new PopupWindow(CreateProfile.this);
			pw.setTouchable(true);
			pw.setFocusable(true);
			pw.setOutsideTouchable(true);
			pw.setTouchInterceptor(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
						pw.dismiss();
						return true;
					}
					return false;
				}
			});
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			int width = (int) (metrics.widthPixels * 0.75);
			int height = (int) (metrics.heightPixels * 0.75);
			pw.setWidth(width);
			pw.setHeight(height);
			pw.setOutsideTouchable(false);
			pw.setContentView(layout);
			contactList = (ListView) layout.findViewById(R.id.contactList);
			EditText contactName  = (EditText)layout.findViewById(R.id.contactName);
			contactName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
				@Override
				public void afterTextChanged(Editable s) {
					if(status && s.length() > 1) {
						new readContactsTask().execute(s.toString());
					}
				}
			});
			contactList.setOnItemClickListener(itemClick);
			pw.showAtLocation(v, Gravity.CENTER, 0, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readContact(String s) {
		Contact contactData;
		contactDetails = new ArrayList<Contact>();
		final ContentResolver cr = getContentResolver();
		final String sa1 = "%"+s+"%";
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",  new String[] { sa1 }, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				contactData = new Contact();
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					contactData.setName(name);
					contactData.setId(Long.parseLong(id));
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					ArrayList<String> numbs = new ArrayList<String>();
					while (pCur.moveToNext()) {
						String phone = pCur.getString(
								pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						numbs.add(phone);
					}
					contactData.setNumbers(numbs);
					pCur.close();
				}
				contactDetails.add(contactData);
			}
		}
	}

	OnItemClickListener itemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			pw.dismiss();
			selectedContact = contactDetails.get(pos);
			selectedContactDetails.add(selectedContact);
			writeToFile();
			adapterData.notifyDataSetChanged();
		}
	};
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			SharedPreferences pref = getSharedPreferences("Mode", 0);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("Profile", 0);
			editor.putString("MODE", profileOptions[0]);
			editor.commit();
			if(isMyServiceRunning()){
				//stopService(intent);
			}
			finish();
			break;
		case R.id.help:
			Intent help = new Intent(CreateProfile.this, HelpActivity.class);
			startActivity(help);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void writeToFile() {
		try {
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			Log.i("Dir", s);
			File myFile = new File(s + "/contact.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			File fileId = new File(s + "/contactIds.txt");
			fileId.createNewFile();
			FileOutputStream fOutId = new FileOutputStream(fileId);
			OutputStreamWriter idOutWriter = new OutputStreamWriter(fOutId);
			for(int i = 0; i < selectedContactDetails.size(); i++){
				Contact contact = selectedContactDetails.get(i);
				idOutWriter.append(contact.getId() +"\n");
				ArrayList<String> numbers = contact.getNumbers();
				for(int j = 0; j < numbers.size(); j++) {
					String str = numbers.get(j);
					str = str.replace("-", "");
					myOutWriter.append(str +"\n");
				}
			}

			myOutWriter.close();
			fOut.close();
			idOutWriter.close();
			fOutId.close();
		} catch(Exception e) {
			Log.i("File write", e.getMessage());
		}
	}

	public void readToFile() {
		selectedContactDetails = new ArrayList<Contact>();
		try {
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			File myFile = new File(s + "/contactIds.txt");

			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";

			while ((aDataRow = myReader.readLine()) != null) {
				selectedContactDetails.add(readContactById(aDataRow));
			}
		}catch(Exception e) {

		}
	}

	/*@Override
	public void onBackPressed() {

	}*/
	public class readContactsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			readContact(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			SimpleListViewAdapter adapter = new SimpleListViewAdapter(CreateProfile.this, R.id.contactList, contactDetails);
			contactList.setAdapter(adapter);
			status = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			status = false;
		}

	}

	public Contact readContactById(String s) {
		Contact contactData;
		final ContentResolver cr = getContentResolver();
		final String sa1 = s;
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, ContactsContract.Contacts._ID + " = ?",  new String[] { sa1 }, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				contactData = new Contact();
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					contactData.setName(name);
					contactData.setId(Long.parseLong(id));
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					ArrayList<String> numbs = new ArrayList<String>();
					while (pCur.moveToNext()) {
						String phone = pCur.getString(
								pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						numbs.add(phone);
					}
					contactData.setNumbers(numbs);
					pCur.close();
				}
				return contactData;
			}

		}
		return null;
	}

}
