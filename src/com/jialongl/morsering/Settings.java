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

public class Settings extends Activity {
	private EditText incomingNumberTF;
	private EditText ditTF;

	public void testFakeCall (View v) {
		Intent i = new Intent();
		i.setAction("android.intent.action.PHONE_STATE");
		i.putExtra(TelephonyManager.EXTRA_STATE, TelephonyManager.EXTRA_STATE_RINGING);
		i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, incomingNumberTF.getText().toString());
		v.getContext().sendBroadcast(i);
	}

	public void saveSettings (View v) {
		Context c = v.getContext();
		String packageName = c.getPackageName();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(packageName + ".DIT", Integer.parseInt(ditTF.getText().toString()));
		editor.putString(packageName + ".FAKE_INCOMING_NO", incomingNumberTF.getText().toString());
		editor.commit();
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		incomingNumberTF = (EditText) findViewById(R.id.incomingNumberTF);
		ditTF = (EditText) findViewById(R.id.ditTF);

		Context c = this.getApplicationContext();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
		String packageName = c.getPackageName();
		ditTF.setText(
			String.valueOf(
				pref.getInt(packageName + ".DIT", Defaults.DIT)
			)
		);
		incomingNumberTF.setText(
			String.valueOf(
				pref.getString(packageName + ".FAKE_INCOMING", "")
			)
		);
	}
}
