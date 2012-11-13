package org.levasoft.streetdroid;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Class to read preferences from Android shared preferences storage and save changed preferences 
 * back to the storage.
 * Since StreetDroid can work with multiple sites, we need to keep settings for each site.
 * Each site has following attributes:
 * - Site URL, e.g. "example.com"
 * - Site title
 * - username and password to login to the site, if user provided them.
 * 
 * Class implemented as a singletone.
 *
 */
public class PreferencesProvider {
	private static final String KEY_SITE_NUM = "site_num";			// Storage key to store total number of sites
	private static final String KEY_SITE_URL = "site_url";			// Storage key prefix to keep site url
	private static final String KEY_SITE_TITLE = "site_title";		// Storage key prefix to keep site title
	private static final String KEY_SITE_USERNAME = "site_username";	// Storage key prefix to keep username
	private static final String KEY_SITE_PASSWORD = "site_password";	// Storage key prefix to keep password

	// One and only instance
	public static PreferencesProvider INSTANCE = new PreferencesProvider();
	
	private Context m_context;

	// Quick list of supported sites. User can add any of those sites without entering URL manually. 
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
	
	// List of sites
	private ArrayList<Site> m_sites = new ArrayList<Site>(); 
	
	/**
	 * Private constructor
	 */
	private PreferencesProvider() {
	}

	/**
	 * Context setter
	 * @param context
	 */
	public void SetContext(Context context) {
		m_context = context;
	}
	
	/**
	 * Returns the list of sites. If they are not yet loaded, loads them from preferences storage.
	 * @return
	 */
	public Site[] getSites() {
		if (m_sites.size() == 0 && m_context != null) {
			loadSites();
		}
		return m_sites.toArray(new Site[0]);
	}

	/**
	 * Loads sites from preferences storage.
	 */
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

	/**
	 * Saves sites into preferences storage.
	 */
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

	/**
	 * Deletes site from the list and saves the updated list into the storage.
	 * @param siteId - id of site to delete
	 */
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

	/**
	 * Adds site to the list and saves the updated list into the storage.
	 * @param siteUrl - url of the site to add
	 */
	public void addSite(String siteUrl) {
		m_sites.add(new Site(siteUrl));
		Collections.sort(m_sites);
		saveSites();
	}

	/**
	 * Updates the site title and saves the updated list into the storage.
	 * That happens as soon as we parse site title from the RSS.
	 * @param m_websiteUrl - site URL
	 * @param siteTitle - new title
	 */
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

	/**
	 * Sets site username/password and saves the updated list into the storage.
	 * @param siteId - site id
	 * @param username - new username
	 * @param password - new password
	 */
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

	/**
	 * Returns site by id.
	 * TODO it's now O(N), consider using BST container to improve it.
	 */
	public Site getSiteById(int siteId) {
		for (int i = 0; i < m_sites.size(); ++i) {
			final Site site = m_sites.get(i);
			if (site.getId() == siteId) {
				return site;
			}
		}
		return null;
	}

	/**
	 * Returns the quick list of default sites 
	 */
	public String[] getDefaultSites() {
		return defaultSites;
	}

	/**
	 * Finds site in the list by URL
	 * @param siteUrl site URL
	 * TODO it's now O(N), consider using BST container to improve it.
	 */
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
