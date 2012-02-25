package org.levasoft.streetdroid;

public class Site implements Comparable<Site> {
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

	@Override
	public int compareTo(Site another) {
		if (another == null) return 1;
		return m_url.compareTo(another.m_url);	
	}

}
