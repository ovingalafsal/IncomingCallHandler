package com.example.profilehandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.example.profilehandling.adapter.SimpleListViewAdapter;
import com.example.profilehandling.data.Contact;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SMSListActivity extends Activity implements OnClickListener{

	ListView smsList;
	EditText editSms;
	Button saveSms,deleteSms;
	ArrayList<String> savedSMS = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_sms);
		smsList = (ListView)findViewById(R.id.smsList);
		editSms = (EditText)findViewById(R.id.editedsms);
		saveSms = (Button)findViewById(R.id.saveSms);
		deleteSms = (Button)findViewById(R.id.deleteSms);
		deleteSms.setOnClickListener(this);
		saveSms.setOnClickListener(this);
		SharedPreferences pre = getSharedPreferences("SMS", 0);
		editSms.setText(pre.getString("MeetingSms", ""));
		readToFile();
		if(savedSMS.size() < 7) {
			savedSMS = new ArrayList<String>();
			addDefaultSMS();
		}
		adapter = new ArrayAdapter<String>(SMSListActivity.this, android.R.layout.simple_list_item_1, savedSMS);
		smsList.setAdapter(adapter);
		smsList.setOnItemClickListener(listener);
	}

	public void writeToFile() {
		try {
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			File myFile = new File(s + "/sms.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			for(int i = 0; i < savedSMS.size(); i++){
				String str = savedSMS.get(i);
				myOutWriter.append(str +"\n");
			}

			myOutWriter.close();
			fOut.close();
		} catch(Exception e) {
			Log.i("File write", e.getMessage());
		}
	}

	public void readToFile() {
		try {
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			Log.i("Dir", s);
			File myFile = new File(s + "/sms.txt");

			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";

			while ((aDataRow = myReader.readLine()) != null) {
				savedSMS.add(aDataRow.toString());
			}
		}catch(Exception e) {

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveSms:
			String msg = editSms.getText().toString();
			if(msg.length() > 2 && !alreadyExist(msg)) {
				savedSMS.add(msg.toString());
				writeToFile();
				finish();
			} else {
				finish();
			}
			SharedPreferences pre = getSharedPreferences("SMS", 0);
			SharedPreferences.Editor editor = pre.edit();
			editor.putString("MeetingSms", msg);
			editor.commit();
			break;
		case R.id.deleteSms:
			msg = editSms.getText().toString();
			if(msg.length() > 2 && alreadyExist(msg)) {
				savedSMS.remove(msg.toString());
				writeToFile();
				editSms.setText("");
				adapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
	}
	
	OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			editSms.setText(savedSMS.get(pos));
		}
		
	};
	
	public boolean alreadyExist(String msg) {
		
		for(int i = 0; i < savedSMS.size(); i++) {
			if(savedSMS.get(i).equalsIgnoreCase(msg)) {
				return true;
			}
		}
		return false;
	}
	
	public void addDefaultSMS() {
		savedSMS.add("I'm in a meeting call me later");
		savedSMS.add("I'm busy right now. I'll call you later");
		savedSMS.add("I will be arriving at");
		savedSMS.add("Meeting is cancelled");
		savedSMS.add("See you at");
		savedSMS.add("See you in");
		savedSMS.add("I am late.I will be there at");
	}

}
