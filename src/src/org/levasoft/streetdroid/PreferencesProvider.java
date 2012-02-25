package org.levasoft.streetdroid;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferencesProvider {
	private static final String KEY_SITE_NUM = "site_num";
	private static final String KEY_SITE_URL = "site_url";

	public static PreferencesProvider INSTANCE = new PreferencesProvider();
	
	private Context m_context;
	
	private String[] defaultSites = {
			"livestreet.ru",
			"mnmlist.ru",
			"turometr.ru",
			"avtoturistu.ru",
			"haycafe.ru",
			"luntiki.ru",
			"nepropadu.ru",
			"cookorama.net",
			"debosh.net",
			"babiki.ru",
			"cs-force.ru",
			"burnovoding.ru",
			"jnet.kz",			
	};
	
	private ArrayList<Site> m_sites = new ArrayList<Site>(); 
	
	private PreferencesProvider() {
	}

	public void SetContext(Context context) {
		m_context = context;
	}
	
	private String getWebsiteUrl() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		final String websiteUrl = sharedPrefs.getString("website_url", "livestreet.ru");
		return websiteUrl;
	}
	
	public Site[] getSiteList() {
		loadSites();
		return m_sites.toArray(new Site[0]);
	}

	private void loadSites() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		final int siteNum = sharedPrefs.getInt(KEY_SITE_NUM, 0);
		m_sites.clear();
		if (siteNum == 0) {
			for (int i = 0; i < defaultSites.length; ++i) {
				m_sites.add(new Site(defaultSites[i]));
			}
		} else {
			for (int i = 0; i < siteNum; ++i) {
				final String websiteUrl = sharedPrefs.getString(KEY_SITE_URL + i, "");
				if (websiteUrl.length() != 0) {
					m_sites.add(new Site(websiteUrl));
				}
			}
		}
		
		Collections.sort(m_sites);
	}

	private void saveSites() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_context);
        Editor editor = sharedPrefs.edit();
        editor.putInt(KEY_SITE_NUM, m_sites.size());
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			editor.putString(KEY_SITE_URL + i, site.getUrl());
		}
        editor.commit();
	}

	public void deleteSite(int siteId) {
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getId() == siteId) {
				m_sites.remove(i);
				saveSites();
				return;
			}
		}
		
	}

	public void addSite(String siteUrl) {
		m_sites.add(new Site(siteUrl));
		Collections.sort(m_sites);
		saveSites();
	}
}
