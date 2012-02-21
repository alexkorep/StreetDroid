package org.levasoft.streetdroid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

interface ITopicDownloadCallback {
	void onTopicDownloadComplete(ITopic topic);
}

interface ITopicListDownloadCallback {
	void onTopicListDownloadComplete(ITopic[] topics);
}

public class TopicDataProvider {
	public static final TopicDataProvider INSTANCE = new TopicDataProvider();
	
	private HashMap<String, Topic> m_topics  = new HashMap<String, Topic>();
	private ITopicDownloadCallback m_topicDownloadCallback = null;
	private ITopicListDownloadCallback m_topicListDownloadCallback = null;
	private TopicListDownloader m_topicListDownloader = null;
	
	private TopicDataProvider() {
	}


	ITopic getFullTopic(final String topicUrl, ITopicDownloadCallback topicDownloadCallback) {
		assert topicDownloadCallback != null;
		assert topicUrl != null;
		
		m_topicDownloadCallback = topicDownloadCallback;
		Topic topic = findOrCreate(topicUrl);
		if (topic.getStatus() == TopicStatus.STATUS_INITIAL || 
				topic.getStatus() == TopicStatus.STATUS_BRIEF) {
			new TopicDownloader(this).download(topicUrl);
		}
		
		return topic;
	}


	private Topic findOrCreate(String topicUrl) {
		Topic topic = m_topics.get(topicUrl);
		if (null == topic) {
			topic = new Topic(topicUrl);
			m_topics.put(topicUrl, topic);
		}
		return topic;
	}


	public void onDownloadComplete(Topic topic) {
		// Overwrite existing topic
		// TODO make sure it's safe
		
		topic.setStatus(TopicStatus.STATUS_COMPLETE);
		m_topics.put(topic.getTopicUrl(), topic);
		m_topicDownloadCallback.onTopicDownloadComplete(topic);
	}


	public ITopic[] getTopicList(ITopicListDownloadCallback topicListDownloadCallback) {
		m_topicListDownloadCallback = topicListDownloadCallback;
		
		// Let's cancel previous download
		cancelDownload();
		
		// Start new download
		m_topicListDownloader = new TopicListDownloader(this); 
		m_topicListDownloader.download();
		return getTopicList();
	}
	
	private void cancelDownload() {
		if (m_topicListDownloader == null) {
			return;
		}
		
		m_topicListDownloader.cancel(true);
	}


	private ITopic[] getTopicList() {
		ITopic[] topics = new ITopic[m_topics.size()];
		int i = 0;
		for (Map.Entry<String, Topic> entry : m_topics.entrySet()) {
		    topics[i++] = entry.getValue();
		}
		return topics;
	}


	public void onTopicListDownloadComplete(Topic[] topics) {
		m_topics.clear();
		for (int i = 0; i < topics.length; ++i) {
			Topic topic = topics[i];
			m_topics.put(topic.getTopicUrl(), topic);
		}
		
		m_topicListDownloadCallback.onTopicListDownloadComplete(getTopicList());
		m_topicListDownloader = null;
	}
}
