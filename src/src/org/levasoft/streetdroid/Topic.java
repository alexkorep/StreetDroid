package org.levasoft.streetdroid;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class TopicStatus {
	private TopicStatus() {
	}
	
	public static final TopicStatus STATUS_INITIAL 		= new TopicStatus(); // Initial status after object creation
	public static final TopicStatus STATUS_BRIEF 		= new TopicStatus();	// Contains information from RSS (title, author, brief text)
	public static final TopicStatus STATUS_DOWNLOADING 	= new TopicStatus(); // Full topic downloading is in progress
	public static final TopicStatus STATUS_COMPLETE 	= new TopicStatus();	// Full topic download complete
}

public class Topic implements ITopic, Comparable<Topic> {
	private String m_title = "Topic title";
	private String m_author = "Topic author";
	private String m_blog = "Blog name placeholder";
	private String m_blogUrl = "http://google.com";
	private String m_content = "Topic text comes here.";
	private Date m_dateTime = new Date(1980, 01, 01);
	private TopicStatus m_status = TopicStatus.STATUS_INITIAL;
	private final String m_topicUrl;
	private IComment[] m_comments = new IComment[0];
	
	private static SimpleDateFormat RU_FORMATTER = 
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", new Locale("ru"));
	
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

	public IComment[] getComments() {
		return m_comments;
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

	public void setComments(IComment[] comments) {
		assert comments != null;
		m_comments = comments;
	}

	public String getDateTime() {
		return RU_FORMATTER.format(m_dateTime);
	}

	public void setDateTime(Date date) {
		m_dateTime = date;
	}

	public int compareTo(Topic another) {
		return another.m_dateTime.compareTo(m_dateTime);
	}
}
