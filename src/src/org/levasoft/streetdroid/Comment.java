package org.levasoft.streetdroid;

public class Comment implements IComment {

	private final String m_commentId;
	private String m_author;
	private String m_text;

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

}
