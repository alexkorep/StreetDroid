package org.levasoft.streetdroid;

/**
 * Class representing the comment for a topic.
 * Used only for storing comment information, doesn't contain any business logic, 
 * has only getters and setters.
 *
 */
public class Comment implements IComment {

	private final String m_commentId;	// Comment ID as it used on topic page, e.g. "comment123"
	private String m_author = "";		// Comment author username
	private String m_text = "";			// Comment text
	private String m_authorUrl = "";	// Comment author URL, e.g. "http://example.com/profile/username/"
	private int m_level = 0;			// Comment level. 0 for top-level comments, 1 for 2nd level, etc.
	private String m_dateTime = "";		// Comment publishing date and time, already formatted.

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
