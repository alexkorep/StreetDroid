package org.levasoft.streetdroid;


class TopicStatus {
	private TopicStatus() {
	}
	
	public static final TopicStatus STATUS_INITIAL 		= new TopicStatus(); // Initial status after object creation
	public static final TopicStatus STATUS_BRIEF 		= new TopicStatus();	// Contains information from RSS (title, author, brief text)
	public static final TopicStatus STATUS_DOWNLOADING 	= new TopicStatus(); // Full topic downloading is in progress
	public static final TopicStatus STATUS_COMPLETE 	= new TopicStatus();	// Full topic download complete
}

public class Topic implements ITopic {
	private String m_title = "Topic title";
	private String m_author = "Topic author";
	private String m_blog = "Blog name placeholder";
	private String m_blogUrl = "http://google.com";
	private String m_content = "Topic text comes here.";
	private TopicStatus m_status = TopicStatus.STATUS_INITIAL;
	private final String m_topicUrl;
	
	public Topic(String topicUrl) {
		m_topicUrl  = topicUrl;
	}

	public String getTitle() {
		return m_title;
	}

	public String getBlog() {
		return m_blog;
	}

	public String getBlogUrl() {
		return m_blogUrl;
	}

	public String getContent() {
		return m_content;
	}

	public String getAuthor() {
		return m_author;
	}

	public TopicStatus getStatus() {
		return m_status;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setAuthor(String author) {
		m_author = author;
	}

	public String getTopicUrl() {
		return m_topicUrl;
	}

	public void setStatus(TopicStatus status) {
		m_status = status;
	}

	public void setBlog(String blog, String blogUrl) {
		m_blog = blog;
		m_blogUrl = blogUrl;
	}
}
