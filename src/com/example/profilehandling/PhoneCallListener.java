package com.example.profilehandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class PhoneCallListener extends PhoneStateListener {

	private boolean isPhoneCalling = false;
	String LOG_TAG = "PhoneCallListener";
	ContentResolver cr;
	String profileMode;
	private ITelephony telephonyService;
	Context context;
	ArrayList<String> numbers = new ArrayList<String>();

	public PhoneCallListener(ContentResolver cr,String profile,Context context) {
		this.cr = cr;
		this.profileMode = profile;
		this.context = context;
	}
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		if (TelephonyManager.CALL_STATE_RINGING == state) {
			final String comingNumber = incomingNumber.substring(incomingNumber.length() - 10 , incomingNumber.length());
			SharedPreferences pre = context.getSharedPreferences("Mode", 0);
			profileMode = pre.getString("MODE", "");
			// phone ringing
			Log.i(LOG_TAG, "Mode :  " + profileMode);
			if(profileMode.equalsIgnoreCase("Meeting")) {
				numbers = new ArrayList<String>();
				readToFile();
				for(int i = 0; i < numbers.size(); i++) {
					Log.i(incomingNumber, numbers.get(i));
					String num = numbers.get(i);
					if(num.contains(comingNumber)) {
						Log.i("SMS   ", "number : " + incomingNumber);
						sendSMS(incomingNumber);
					}
				}
				AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				try {
					audioManager.setStreamMute(AudioManager.STREAM_RING, true);
					TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
					Class c = Class.forName(tm.getClass().getName());
					Method m = c.getDeclaredMethod("getITelephony");
					m.setAccessible(true);
					telephonyService = (ITelephony) m.invoke(tm);
					//telephonyService.silenceRinger();
					telephonyService.endCall();
					Log.i(LOG_TAG, "RINGING in Meeting mode, number: " + incomingNumber);
				} catch (Exception e) {
					Log.i("End call", e.getMessage());
				}
				audioManager.setStreamMute(AudioManager.STREAM_RING, false);
			} else if(profileMode.equalsIgnoreCase("Busy")) {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent busyMode = new Intent(context, BusyModeActivity.class);
						busyMode.putExtra("Number", comingNumber);
						busyMode.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(busyMode);
					}
				}, 2000);

				Log.i(LOG_TAG, "RINGING in Busy mode, number: " + incomingNumber);
			} else {
				Log.i(LOG_TAG, "RINGING Normal mode, number: " + incomingNumber);
			}
		}

		if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
			// active
			Log.i(LOG_TAG, "OFFHOOK");

			isPhoneCalling = true;
		}

		if (TelephonyManager.CALL_STATE_IDLE == state) {
			// run when class initial and phone call ended, need detect flag
			// from CALL_STATE_OFFHOOK
			Log.i(LOG_TAG, "IDLE number");

			if (isPhoneCalling) {

				Handler handler = new Handler();

				//Put in delay because call log is not updated immediately when state changed
				// The dialler takes a little bit of time to write to it 500ms seems to be enough
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// get start of cursor
						Log.i("CallLogDetailsActivity", "Getting Log activity...");
						String[] projection = new String[]{Calls.NUMBER};
						Cursor cur = cr.query(Calls.CONTENT_URI, projection, null, null, Calls.DATE +" desc");
						cur.moveToFirst();
						String lastCallnumber = cur.getString(0);
					}
				},500);

				isPhoneCalling = false;
			}

		}
	}

	public void readToFile() {
		try {
			PackageManager m = context.getPackageManager();
			String s = context.getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			File myFile = new File(s + "/contact.txt");

			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";

			while ((aDataRow = myReader.readLine()) != null) {
				numbers.add(aDataRow.toString());
			}
		}catch(Exception e) {

		}
	}

	public void sendSMS(String phoneNumber) {
		SharedPreferences pre = context.getSharedPreferences("SMS", 0);
		String message = pre.getString("MeetingSms", "");
		if(message.length() < 3) {
			message = "I'm busy right now. I'll call you later";
		}
		Log.i("Inside sendSMS", "Num : "+phoneNumber + message);
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, null, null);
	}
}
