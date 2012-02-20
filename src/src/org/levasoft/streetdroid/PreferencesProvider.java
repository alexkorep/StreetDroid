package org.levasoft.streetdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesProvider {
	public static PreferencesProvider INSTANCE = new PreferencesProvider();
	
	private Context m_context;
	
	private PreferencesProvider() {
	}

	public void SetContext(Context context) {
		m_context = context;
	}
	
	public String getWebsiteUrl() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		final String websiteUrl = sharedPrefs.getString("website_url", "livestreet.ru");
		return websiteUrl;
	}
}
