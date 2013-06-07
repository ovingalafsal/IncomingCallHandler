package com.example.profilehandling;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallService extends Service {
	String profileMode;
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		profileMode = intent.getStringExtra("Profile");
		Log.i("Profile Mode:", profileMode);
		PhoneCallListener phoneListener = new PhoneCallListener(getContentResolver(),profileMode,this);
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
//		Intent in = new Intent(PhoneCallService.this,PhoneService.class);  
//		sendBroadcast(in);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i("Onstop", "Stop service");
		super.onDestroy();
	}
	

}
