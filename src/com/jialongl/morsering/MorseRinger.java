package com.jialongl.morsering;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

public class MorseRinger extends BroadcastReceiver {
	private final int sampleRate = 8000;
	private final double freqOfTone = 440;

	private Context c = null;

	// Think of "DIT" as "DIT_DURATION" --- to save screen space and read/write effort
	private int DIT = 80;
	private int DAH = 280; //3*DIT;

	private int DD_GAP =      80;  // DIT_DAH_GAP, 1*DIT
	private int  L_GAP =     310;  // LETTER_GAP, 3*DIT
	private int  W_GAP = 7 * DIT;  // WORD_GAP

	private int[][] morseDurations = {
		{DIT, DD_GAP, DAH},                                 // ._
		{DAH, DD_GAP, DIT, DD_GAP, DIT, DD_GAP, DIT},       // _...
		{DAH, DD_GAP, DIT, DD_GAP, DAH, DD_GAP, DIT},       // _._.
		{DAH, DD_GAP, DIT, DD_GAP, DIT},                    // _..
		{DIT},                                              // .
		{DIT, DD_GAP, DIT, DD_GAP, DAH, DD_GAP, DIT},       // .._.
		{DAH, DD_GAP, DAH, DD_GAP, DIT},                    // __.
		{DIT, DD_GAP, DIT, DD_GAP, DIT, DD_GAP, DIT},       // ....
		{DIT, DD_GAP, DIT},                                 // ..
		{DIT, DD_GAP, DAH, DD_GAP, DAH, DD_GAP, DAH},       // .___
		{DAH, DD_GAP, DIT, DD_GAP, DAH},                    // _._
		{DIT, DD_GAP, DAH, DD_GAP, DIT, DD_GAP, DIT},       // ._..
		{DAH, DD_GAP, DAH},                                 // __
		{DAH, DD_GAP, DIT},                                 // _.
		{DAH, DD_GAP, DAH, DD_GAP, DAH},                    // ___
		{DIT, DD_GAP, DAH, DD_GAP, DAH, DD_GAP, DIT},       // .__.
		{DAH, DD_GAP, DAH, DD_GAP, DIT, DD_GAP, DAH},       // __._
		{DIT, DD_GAP, DAH, DD_GAP, DIT},                    // ._.
		{DIT, DD_GAP, DIT, DD_GAP, DIT},                    // ...
		{DAH},                                              // _
		{DIT, DD_GAP, DIT, DD_GAP, DAH},                    // .._
		{DIT, DD_GAP, DIT, DD_GAP, DIT, DD_GAP, DAH},       // ..._
		{DIT, DD_GAP, DAH, DD_GAP, DAH},                    // .__
		{DAH, DD_GAP, DIT, DD_GAP, DIT, DD_GAP, DAH},       // _.._
		{DAH, DD_GAP, DIT, DD_GAP, DAH, DD_GAP, DAH},       // _.__
		{DAH, DD_GAP, DAH, DD_GAP, DIT, DD_GAP, DIT},       // __..
	};

	@Override
	public void onReceive(Context c, Intent i) {
		this.c = c;
		String phoneState = i.getStringExtra(TelephonyManager.EXTRA_STATE);
		String contactName = null;

		if (TelephonyManager.EXTRA_STATE_RINGING.equals(phoneState)) {
			String incomingNumber = i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			contactName = contactNameByPhoneNumber(incomingNumber);
			if (contactName != null && !contactName.isEmpty())
				playMorseWithString(contactName);
		}
	}

	private String contactNameByPhoneNumber(String phoneNumber) {
		ContentResolver cr = c.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
		if (cursor == null)
			return null;

		String contactName = null;
		if(cursor.moveToFirst())
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));

		if(cursor != null && !cursor.isClosed())
			cursor.close();

		return contactName;
	}

	private void playMorseWithString(String s) {
		int   LONG_ENOUGH = 128;
		int[] durations = new int[LONG_ENOUGH];
		int   dIndex = 0;

		s = s.replaceAll("[^a-zA-Z ]", "").toLowerCase(Locale.ENGLISH);

		int sLength = s.length();
		for (int sIndex = 0; sIndex < sLength; sIndex++) {
			char ch = s.charAt(sIndex);
			if (ch == ' ') {
				durations[dIndex - 1] = W_GAP;
			} else {
				dIndex = copyArrayToArrayAt(morseDurations[ch - 'a'], durations, dIndex);
				durations[dIndex] = L_GAP;
				dIndex++;
			}
		}

		int duration = 0;
		for (int i = 0; i < dIndex; i++)
			duration += durations[i];

		int nSamples = duration * sampleRate / 1000;
		double sample[] = new double[nSamples];
		byte generatedSnd[] = new byte[2 * nSamples];

		int sampleIndex = 0;
		int nextSampleIndex;
		for (int i = 0; i < dIndex; i++) {
			nextSampleIndex = sampleIndex + sampleRate * durations[i] / 1000;
			for (; sampleIndex < nextSampleIndex; sampleIndex++)
				sample[sampleIndex] = Math.sin(2 * Math.PI * sampleIndex / (sampleRate/freqOfTone));

			i++;
			nextSampleIndex = sampleIndex + sampleRate * durations[i] / 1000;
			for (; sampleIndex < nextSampleIndex; sampleIndex++)
				sample[sampleIndex] = .0d;
		}

		int i = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[i++] = (byte) (val & 0x00ff);
			generatedSnd[i++] = (byte) ((val & 0xff00) >>> 8);
		}

		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
				AudioTrack.MODE_STATIC);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}

	private int copyArrayToArrayAt(int[] src, int[] dest, int startingIndex) {
		int sLength = src.length;
		int dLength = dest.length;
		for (int i = 0; i < sLength; i++) {
			if (startingIndex + i >= dLength)
				break;

			dest[startingIndex + i] = src[i];
		}
		return startingIndex + sLength;
	}
}
