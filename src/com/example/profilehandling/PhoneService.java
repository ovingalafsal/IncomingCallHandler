package com.example.profilehandling;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class PhoneService extends BroadcastReceiver {

	private ITelephony telephonyService;
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (!intent.getAction().equals("android.intent.action.PHONE_STATE")) 
			return;
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//			String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//			
//			Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
//		    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
//		    context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
//
//			Intent answer = new Intent(Intent.ACTION_MEDIA_BUTTON);
//			answer.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
//			context.sendOrderedBroadcast(answer, null);
//			Log.i("Test", "Reject incoming call from: " + number);
			try {
			audioManager.setStreamMute(AudioManager.STREAM_RING, true);
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			Class c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(tm);
			//telephonyService.silenceRinger();
			telephonyService.endCall();
			} catch(Exception e) {
				Log.i("Exceptions", e.getMessage());
			}
			audioManager.setStreamMute(AudioManager.STREAM_RING, false);
		}
		return;
	} 
}

