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
	private HashMap<Integer, Topic> m_topics  = new HashMap<Integer, Topic>();
	private ITopicDownloadCallback m_topicDownloadCallback = null;
	private ITopicListDownloadCallback m_topicListDownloadCallback;
	
	public TopicDataProvider() {
	}


	ITopic getFullTopic(final int topicId, ITopicDownloadCallback topicDownloadCallback) {
		m_topicDownloadCallback = topicDownloadCallback;
		Topic topic = findOrCreate(topicId);
		if (topic.getStatus() == TopicStatus.STATUS_INITIAL || 
				topic.getStatus() == TopicStatus.STATUS_BRIEF) {
			new TopicDownloader(this).download(topicId);
		}
		
		return topic;
	}


	private Topic findOrCreate(Integer topicId) {
		Topic topic = m_topics.get(topicId);
		if (null == topic) {
			topic = new Topic(topicId);
			m_topics.put(topicId, topic);
		}
		return topic;
	}


	public void onDownloadComplete(Topic topic) {
		// Overwrite existing topic
		// TODO make sure it's safe
		
		topic.setStatus(TopicStatus.STATUS_COMPLETE);
		m_topics.put(topic.getTopicId(), topic);
		m_topicDownloadCallback.onTopicDownloadComplete(topic);
	}


	public ITopic[] getTopicList(ITopicListDownloadCallback topicListDownloadCallback) {
		m_topicListDownloadCallback = topicListDownloadCallback;
		new TopicListDownloader(this).download();
		return getTopicList();
	}
	
	private ITopic[] getTopicList() {
		ITopic[] topics = new ITopic[m_topics.size()];
		int i = 0;
		for (Map.Entry<Integer, Topic> entry : m_topics.entrySet()) {
		    topics[i++] = entry.getValue();
		}
		return topics;
	}


	public void onTopicListDownloadComplete(Topic[] topics) {
		m_topics.clear();
		for (int i = 0; i < topics.length; ++i) {
			Topic topic = topics[i];
			m_topics.put(topic.getTopicId(), topic);
		}
		
		m_topicListDownloadCallback.onTopicListDownloadComplete(getTopicList());
	}
}
