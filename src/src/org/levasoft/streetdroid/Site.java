package org.levasoft.streetdroid;

public class Site implements Comparable<Site> {
	private final String m_url;
	private String m_title = "";
	private String m_username = "";
	private String m_password = "";
	
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
