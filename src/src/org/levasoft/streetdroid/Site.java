package org.levasoft.streetdroid;

public class Site {
	private final String m_url;
	
	Site(String url) {
		m_url = url;
	}
	
	public String getUrl() {
		return m_url;
	}
	
	public int getId() {
		return m_url.hashCode();
	}
}
