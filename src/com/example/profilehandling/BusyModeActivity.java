package com.example.profilehandling;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

public class BusyModeActivity extends Activity {

	TextView callDetails;
	private ITelephony telephonyService;
	String number;
	PopupWindow pw;
	View layout;
	ImageView contactImage;
	private Window wind;
	@Override
	protected void onResume() {
		super.onResume();
		wind = this.getWindow();
	    wind.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
	    wind.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    wind.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.busy_mode);
		number = getIntent().getStringExtra("Number");
		callDetails = (TextView)findViewById(R.id.callDetails);
		contactImage = (ImageView)findViewById(R.id.contactImage);
		String contactName = getContactDisplayNameByNumber(number);
		if(contactName.length() > 0) {
			callDetails.setText(contactName);
		} else {
			callDetails.setText(number);
		}
		LayoutInflater inflater = (LayoutInflater) getSystemService(CreateProfile.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.busy_mode_sms, null,false);
	}

	public void handleButtonClick(View v){
		switch (v.getId()) {
		case R.id.mute:
			finish();
			break;
		case R.id.rejectcall:
			rejectCall();
			finish();
			break;
		case R.id.reject_text:
			initiatePopupWindow(callDetails);
			break;
		default:
			break;
		}
	}

	public void rejectCall() {
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		try {
			audioManager.setStreamMute(AudioManager.STREAM_RING, true);
			TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			Class c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(tm);
			//telephonyService.silenceRinger();
			telephonyService.endCall();
		} catch(Exception e) {
			Log.i("BusyModeActivity", e.getMessage());
		}
		audioManager.setStreamMute(AudioManager.STREAM_RING, false);
	}

	public void sendSMS(String phoneNumber,String message) {
		Log.i("Inside sendSMS", "Num : "+phoneNumber);

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, null, null);
	}

	private void initiatePopupWindow(View v) {
		try {
			pw = new PopupWindow(BusyModeActivity.this);
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
			int height = (int) (metrics.heightPixels * 0.40);
			pw.setWidth(width);
			pw.setHeight(height);
			pw.setOutsideTouchable(false);
			pw.setContentView(layout);
			final EditText busySms = (EditText)layout.findViewById(R.id.busysms);
			Button send = (Button)layout.findViewById(R.id.send);
			send.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					rejectCall();
					if(busySms.getText().length() > 0) {
						sendSMS(number,busySms.getText().toString());
					}
					pw.dismiss();
					BusyModeActivity.this.finish();
				}
			});
			Button reject = (Button)layout.findViewById(R.id.rejectcall);
			reject.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					rejectCall();
					pw.dismiss();
					BusyModeActivity.this.finish();
				}
			});
			pw.showAtLocation(v, Gravity.CENTER, 0, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//to get conctaname from phone number
	public String getContactDisplayNameByNumber(String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = "";

		ContentResolver contentResolver = getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {ContactsContract.Contacts._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				Long contactId = contactLookup.getLong(contactLookup.getColumnIndex(ContactsContract.Contacts._ID));
				//InputStream input = openDisplayPhoto(contactId);
				InputStream input = openPhoto(contactId);
				if(input != null) {
					Drawable d = Drawable.createFromStream(input, "imagename");
					contactImage.setImageDrawable(d);
				}
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	/*public InputStream openDisplayPhoto(long contactId) {
	     Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri displayPhotoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO);
	     try {
	         AssetFileDescriptor fd =
	             getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
	         return fd.createInputStream();
	     } catch (IOException e) {
	    	 Log.i("IOException", e.getMessage());
	         return null;
	     }
	 }*/

	public InputStream openPhoto(long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = getContentResolver().query(photoUri,
				new String[] {Contacts.Photo.PHOTO}, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					return new ByteArrayInputStream(data);
				}
			}
		} finally {
			cursor.close();
		}
		return null;
	}
}
