package org.levasoft.streetdroid;

import java.util.List;

import org.levasoft.streetdroid.rss.AndroidSaxFeedParser;
import org.levasoft.streetdroid.rss.Message;

import android.os.AsyncTask;


public class TopicListDownloader 
	extends AsyncTask<String, Integer, String>{

	private static final String FEED_URL = "http://%s/rss/index/";
	
	private Topic[] m_topics = null;
	private List<Message> m_messages = null;

	private final TopicDataProvider m_dateProvider;

	public TopicListDownloader(TopicDataProvider topicDataProvider) {
		m_dateProvider = topicDataProvider;
	}

	public void download() {
		// TODO pass it here, don't get from preferences
		final String websiteUrl = PreferencesProvider.INSTANCE.getWebsiteUrl().trim();
		String url = String.format(FEED_URL, websiteUrl);
		execute(url);
	}

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
			Topic topic = new Topic(msg.getLink());
			topic.setTitle(msg.getTitle());
			topic.setAuthor(msg.getCreator());
			topic.setDateTime(msg.getDate());
			
			String description = msg.getDescription().replace("<![CDATA[", "").replace("]]>", ""); 
			topic.setContent(description);
			topic.setStatus(TopicStatus.STATUS_BRIEF);
			topic.setBlog("", "");
			m_topics[i] = topic;
		}
		return null;
	}


	@Override
	protected void onPostExecute(String result) {
		m_dateProvider.onTopicListDownloadComplete(m_topics);
	}
	
}
