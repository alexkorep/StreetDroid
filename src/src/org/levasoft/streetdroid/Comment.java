package org.levasoft.streetdroid;

public class Comment implements IComment {

	private final String m_commentId;
	private String m_author = "";
	private String m_text = "";
	private String m_authorUrl = "";
	private int m_level = 0;
	private String m_dateTime = "";

	public Comment(String commentId) {
		m_commentId = commentId;
	}

	public void setAuthor(String author) {
		m_author = author;
	}

	public void setText(String text) {
		m_text = text;
	}

	public String getId() {
		return m_commentId;
	}

	public String getAuthor() {
		return m_author;
	}

	public String getText() {
		return m_text;
	}

	public String getAuthorUrl() {
		return m_authorUrl;
	}

	public void setAuthorUrl(String authorUrl) {
		m_authorUrl = authorUrl;
	}

	public int getLevel() {
		return m_level;
	}

	public void setLevel(int level) {
		m_level = level;
	}

	public void setDateTime(String dateTime) {
		m_dateTime = dateTime; 
	}

	public String getDateTime() {
		return m_dateTime;
	}

}
