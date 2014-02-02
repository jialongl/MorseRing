package com.jialongl.morsering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;

public class Preferences extends Activity {
	private EditText phoneNumberTF;
	private EditText ditTF;
	private EditText dahTF;
	private EditText ddgapTF;
	private EditText lgapTF;
	private EditText wgapTF;

	public void testFakeCall (View v) {
		Intent i = new Intent();
		i.setAction("android.intent.action.PHONE_STATE");
		i.putExtra(TelephonyManager.EXTRA_STATE, TelephonyManager.EXTRA_STATE_RINGING);
		i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, phoneNumberTF.getText().toString());
		i.putExtra("dit", ditTF.getText().toString());
		i.putExtra("dah", dahTF.getText().toString());
		i.putExtra("ddgap", ddgapTF.getText().toString());
		i.putExtra("lgap", lgapTF.getText().toString());
		i.putExtra("wgap", wgapTF.getText().toString());
		v.getContext().sendBroadcast(i);
	}

	public void savePreferences (View v) {
		Context c = v.getContext();
		String packageName = c.getPackageName();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(packageName + ".DIT",    Integer.parseInt(ditTF.getText().toString()));
		editor.putInt(packageName + ".DAH",    Integer.parseInt(dahTF.getText().toString()));
		editor.putInt(packageName + ".DD_GAP", Integer.parseInt(ddgapTF.getText().toString()));
		editor.putInt(packageName + ".L_GAP",  Integer.parseInt(lgapTF.getText().toString()));
		editor.putInt(packageName + ".W_GAP",  Integer.parseInt(wgapTF.getText().toString()));
		editor.commit();
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		phoneNumberTF = (EditText) findViewById(R.id.phoneNumberTF);
		ditTF = (EditText) findViewById(R.id.ditTF);
		dahTF = (EditText) findViewById(R.id.dahTF);
		ddgapTF = (EditText) findViewById(R.id.ddgapTF);
		lgapTF = (EditText) findViewById(R.id.lgapTF);
		wgapTF = (EditText) findViewById(R.id.wgapTF);
	}
}
