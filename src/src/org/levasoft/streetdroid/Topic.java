package org.levasoft.streetdroid;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class indicating the topic download status.
 * Topic could be downloaded from 2 different locations:
 * - From RSS article record. RSS record contains information only about topic title, brief content and author.
 * - From full topic HTML page. Topic title, author, publishing date, content and comments are parsed from that HTML page. 
 */
class TopicStatus {
	private TopicStatus() {
	}
	
	public static final TopicStatus STATUS_INITIAL 		= new TopicStatus();	// Initial status after object creation
	public static final TopicStatus STATUS_BRIEF 		= new TopicStatus();	// Contains information from RSS (title, author, brief text)
	public static final TopicStatus STATUS_DOWNLOADING 	= new TopicStatus();	// Full topic is being downloading from HTML page
	public static final TopicStatus STATUS_COMPLETE 	= new TopicStatus();	// Full topic download complete
}

/**
 * Class representing the topic
 *
 */
public class Topic implements ITopic, Comparable<Topic> {
	// Regular expression used to extract topic front image from topic HTML text
	private static final String REGEXP_IMAGE_URL = "<img.*src=\\\"([^\\\"]*)\\\".*>";
	
	private String m_title = "";	// Topic title
	private String m_author = "";	// Topic author username
	private String m_blog = "";		// Name of the blog this topic is published in
	private String m_blogUrl = "";	// Topic blog URL
	private String m_content = "";	// Topic HTML content
	private Date m_dateTime = new Date(1980, 01, 01);			// Topic publish date
	private TopicStatus m_status = TopicStatus.STATUS_INITIAL;	// Topic download status
	private final String m_topicUrl;							// Topic HTML page URL
	private IComment[] m_comments = new IComment[0];			// Comments to the topic
	
	private VotingDetails m_votingDetails = new VotingDetails();	// Information needed to vote for topic
	private final Site m_site;										// Website where the topic is published
	
	// Formatter to format topic publishing date/time
	// Russian date/time format is used here
	private static SimpleDateFormat RU_FORMATTER = 
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", new Locale("ru"));

	/**
	 * Constructor
	 * @param topicUrl - topic URL, e.g.
	 * http://example.com/blog/news/123.html 
	 * @param site - site class.
	 */
	public Topic(String topicUrl, Site site) {
		m_topicUrl  = topicUrl;
		m_site = site;
	}

	/**
	 * @see ITopic::getTitle
	 */
	@Override
	public String getTitle() {
		return m_title;
	}

	/**
	 * @see ITopic::getBlog
	 */
	@Override
	public String getBlog() {
		return m_blog;
	}

	/**
	 * @see ITopic::getBlogUrl
	 */
	@Override
	public String getBlogUrl() {
		return m_blogUrl;
	}

	/**
	 * @see ITopic::getContent
	 */
	@Override
	public String getContent() {
		return m_content;
	}

	/**
	 * @see ITopic::getAuthor
	 */
	@Override
	public String getAuthor() {
		return m_author;
	}

	/**
	 * Returns topic download status
	 */
	public TopicStatus getStatus() {
		return m_status;
	}

	/**
	 * @see ITopic::getComments
	 */
	@Override
	public IComment[] getComments() {
		return m_comments;
	}

	/**
	 * Sets topic title
	 * @param title - new title
	 */
	public void setTitle(String title) {
		m_title = title;
	}

	/**
	 * Sets topic content in HTML format
	 * @param content - topic content text
	 */
	public void setContent(String content) {
		m_content = content;
	}

	/**
	 * Sets topic author name
	 * @param author - topic author username
	 */
	public void setAuthor(String author) {
		m_author = author;
	}

	/**
	 * @see ITopic::getTopicUrl
	 */
	@Override
	public String getTopicUrl() {
		return m_topicUrl;
	}

	/**
	 * Sets topic download status
	 * @param status - new topic download status
	 */
	public void setStatus(TopicStatus status) {
		m_status = status;
	}

	/**
	 * Sets topis blog
	 * @param blog - blog name
	 * @param blogUrl - blog URL
	 */
	public void setBlog(String blog, String blogUrl) {
		m_blog = blog;
		m_blogUrl = blogUrl;
	}

	/**
	 * Sets topic comments
	 * @param comments - topic comment list
	 */
	public void setComments(IComment[] comments) {
		assert comments != null;
		m_comments = comments;
	}

	/**
	 * @see ITopic::getDateTime
	 * @note Date/time is formatted by SimpleDateFormat class
	 */
	public String getDateTime() {
		return RU_FORMATTER.format(m_dateTime);
	}

	/**
	 * Sets topic publish datetime
	 * @param date - topic publis datetime
	 */
	public void setDateTime(Date date) {
		m_dateTime = date;
	}

	/**
	 * Method to compare two topics
	 */
	public int compareTo(Topic another) {
		return another.m_dateTime.compareTo(m_dateTime);
	}

	/**
	 * @see ITopic::getDownloadComplete
	 */
	@Override
	public boolean getDownloadComplete() {
		return m_status == TopicStatus.STATUS_COMPLETE;
	}

	/**
	 * @see ITopic::getVotingDetails
	 */
	@Override
	public VotingDetails getVotingDetails() {
		return m_votingDetails;	
	}

	/**
	 * @see ITopic::getSite
	 */
	@Override
	public Site getSite() {
		return m_site;
	}

	/**
	 * @see ITopic::getFrontImageUrl
	 */
	@Override
	public String getFrontImageUrl() {
		if (m_content.length() == 0) {
			return "";
		}
		
		// TODO move all this into separate class
		//
		Pattern pattern = Pattern.compile(REGEXP_IMAGE_URL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(m_content);
		boolean matchFound = matcher.find();

		if (!matchFound || matcher.groupCount() < 1) {
			// not found
			return "";
		}
		
		return matcher.group(1);
	}
}
