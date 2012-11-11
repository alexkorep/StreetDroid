package org.levasoft.streetdroid;

public class StreetDroidException extends Exception {
	private final int m_messageId;

	public StreetDroidException(int messageId) {
		m_messageId = messageId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
