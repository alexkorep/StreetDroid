package org.levasoft.streetdroid;

import java.util.List;

import org.levasoft.streetdroid.rss.AndroidSaxFeedParser;
import org.levasoft.streetdroid.rss.Message;

import android.os.AsyncTask;

/**
 * Class representing topic list type.
 * Different lists have different RSS URLs.
 * We have only 2 instance of this class: 
 * - TOPIC_LIST_GOOD - good topics (with rating higher than particular value)
 * - TOPIC_LIST_NEW - new topics (including topics with low rating)
 * Other instances cannot be created (prohibited by private constructor)
 *
 */
class TopicListType {
	private final String m_feedUrl; // RSS feed URL

	/**
	 * Constructor, private to avoid instance creation from outside of this class.
	 * @param feedUrl
	 */
	private TopicListType(String feedUrl) {
		m_feedUrl = feedUrl;
	}
	
	/**
	 * URL getter
	 */
	public String getFeedUrl() {
		return m_feedUrl;
	}
	
	public static final TopicListType TOPIC_LIST_GOOD	= new TopicListType("http://%s/rss/index/");	// Good topics
	public static final TopicListType TOPIC_LIST_NEW	= new TopicListType("http://%s/rss/new/");		// New Topics
}


/**
 * Downloads topic list from RSS.
 *
 */
public class TopicListDownloader 
	extends AsyncTask<String, Integer, String>{

	private Topic[] m_topics = null;
	private List<Message> m_messages = null;

	private final TopicDataProvider m_dateProvider;
	private Site m_site; // site object

	/**
	 * Constructor
	 * @param site - site to download topics for
	 * @param topicDataProvider - data provider, its method is called when download is complete
	 *		  TODO introduce and use topicDataProvider's interface instead.
	 */
	public TopicListDownloader(Site site, TopicDataProvider topicDataProvider) {
		m_dateProvider = topicDataProvider;
		m_site = site;
	}

	/**
	 * Download and parse topic list from RSS
	 * @param topicListType - type of the topic list. Different types - different RSS URLs.
	 */
	public void download(TopicListType topicListType) {
		String url = String.format(topicListType.getFeedUrl(), m_site.getUrl());
		execute(url);
	}

	/**
	 * Worker thread method.
	 */
	@Override
	protected String doInBackground(String... arg0) {
		final String url = arg0[0];
		AndroidSaxFeedParser parser = new AndroidSaxFeedParser(url);
		try {
			m_messages = parser.parse();
		} catch (Exception e) {
			// Invalid datafeed, we need to handle it.
			m_messages = null;
		}
		
		final int messagesCount = m_messages == null ? 0 : m_messages.size();
		m_topics = new Topic[messagesCount];
		if (messagesCount == 0) {
			return null;
		}
		
		for (int i = 0; i < m_messages.size(); ++i) {
			Message msg = m_messages.get(i);
			Topic topic = new Topic(msg.getLink(), m_site);
			topic.setTitle(msg.getTitle());
			topic.setAuthor(msg.getCreator());
			topic.setDateTime(msg.getDate());
			
			String description = msg.getDescription().replace("<![CDATA[", "").replace("]]>", ""); 
			topic.setContent(description);
			topic.setStatus(TopicStatus.STATUS_BRIEF);
			topic.setBlog("", "");
			m_topics[i] = topic;
		}
		
		final String siteTitle  = parser.getSiteTitle();
		PreferencesProvider.INSTANCE.SetSiteTitle(m_site.getUrl(), siteTitle);

		return null;
	}


	/**
	 * Called when downloading is done.
	 */
	@Override
	protected void onPostExecute(String result) {
		m_dateProvider.onTopicListDownloadComplete(m_topics);
	}
	
}
