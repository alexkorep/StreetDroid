package org.levasoft.streetdroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class StreetDroidPreferenceActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {     
	    super.onCreate(savedInstanceState);        
	    addPreferencesFromResource(R.layout.preference);        
	}
}
