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
	private static final String KEY_SITE_TITLE = "site_title";
	private static final String KEY_SITE_USERNAME = "site_username";
	private static final String KEY_SITE_PASSWORD = "site_password";

	public static PreferencesProvider INSTANCE = new PreferencesProvider();
	
	private Context m_context;
	
	private String[] defaultSites = {
			"avtoturistu.ru",
			"babiki.ru",
			"cookorama.net",
			"cs-force.ru",
			"debosh.net",
			"dslrfilm.ru",
			"freehabr.ru",
			"jnet.kz",
			"kachkanar.net",
			"livestreet.ru",
			"luntiki.ru",
			"magov.net",
			"mnmlist.ru",
			"nepropadu.ru",
			"steampunker.ru",
			"turometr.ru",
			"ugolock.ru",
	};
	
	private ArrayList<Site> m_sites = new ArrayList<Site>(); 
	
	private PreferencesProvider() {
	}

	public void SetContext(Context context) {
		m_context = context;
	}
	
	public Site[] getSites() {
		if (m_sites.size() == 0 && m_context != null) {
			loadSites();
		}
		return m_sites.toArray(new Site[0]);
	}

	private void loadSites() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		final int siteNum = sharedPrefs.getInt(KEY_SITE_NUM, 0);
		m_sites.clear();
		for (int i = 0; i < siteNum; ++i) {
			final String websiteUrl = sharedPrefs.getString(KEY_SITE_URL + i, "");
			if (websiteUrl.length() != 0) {
				Site site = new Site(websiteUrl);
				site.setTitle(sharedPrefs.getString(KEY_SITE_TITLE + i, ""));
				site.setUsernamePassword(sharedPrefs.getString(KEY_SITE_USERNAME + i, ""), 
						sharedPrefs.getString(KEY_SITE_PASSWORD + i, ""));
				m_sites.add(site);
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
			editor.putString(KEY_SITE_TITLE + i, site.getTitle());
			editor.putString(KEY_SITE_USERNAME + i, site.getUsername());
			editor.putString(KEY_SITE_PASSWORD + i, site.getPassword());
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

	public void SetSiteTitle(String m_websiteUrl, String siteTitle) {
		// TODO use getSiteById
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getUrl().equalsIgnoreCase(m_websiteUrl)) {
				site.setTitle(siteTitle);
				saveSites();
				return;
			}
		}
	}

	public void setSiteUsernamePassword(int siteId, String username, String password) {
		// TODO use getSiteById
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getId() == siteId) {
				site.setUsernamePassword(username, password);
				saveSites();
				return;
			}
		}
	}

	public Site getSiteById(int siteId) {
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getId() == siteId) {
				return site;
			}
		}
		return null;
	}

	public String[] getDefaultSites() {
		return defaultSites;
	}

	public Site getSiteByUrl(String siteUrl) {
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getUrl() == siteUrl) {
				return site;
			}
		}
		return null;
	}
}
