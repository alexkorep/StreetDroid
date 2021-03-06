package org.levasoft.streetdroid;

/**
 * Class containing site information.
 * It's a simple storage class without business logic implementing only getters and setters.
 * Only exception is getId method which returns site URL hash code.
 *
 */
public class Site implements Comparable<Site> {
	private final String m_url;			// Site URL, e.g. "example.com"
	private String m_title = "";		// Site title
	private String m_username = "";		// Username to login to the site
	private String m_password = "";		// Password to login to the site.
	
	public Site(String url) {
		m_url = url;
	}
	
	public String getUrl() {
		return m_url;
	}
	
	public int getId() {
		return m_url.hashCode();
	}

	@Override
	public int compareTo(Site another) {
		if (another == null) return 1;
		return m_url.compareTo(another.m_url);	
	}

	public String getTitle() {
		return m_title ;
	}

	public void setTitle(String siteTitle) {
		m_title = siteTitle;
	}

	public String getUsername() {
		return m_username;
	}

	public String getPassword() {
		return m_password;
	}

	public void setUsernamePassword(String username, String password) {
		m_username = username;
		m_password = password;
	}

}
