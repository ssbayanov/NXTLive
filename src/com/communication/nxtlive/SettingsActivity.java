package com.communication.nxtlive;


import android.os.Bundle;

import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.preferences);
	}
}
