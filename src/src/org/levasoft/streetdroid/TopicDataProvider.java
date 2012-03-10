package org.levasoft.streetdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	private TopicDownloader m_topicDownloader;
	
	private TopicDataProvider() {
	}


	ITopic getFullTopic(final String topicUrl, ITopicDownloadCallback topicDownloadCallback) {
		assert topicDownloadCallback != null;
		assert topicUrl != null;
		
		m_topicDownloadCallback = topicDownloadCallback;
		Topic topic = m_topics.get(topicUrl);
		if (topic == null) {
			return null;
		}
		
		if (topic.getStatus() == TopicStatus.STATUS_INITIAL || 
				topic.getStatus() == TopicStatus.STATUS_BRIEF) {
			
			if (m_topicDownloader != null) {
				m_topicDownloader.cancel(true);
			}
			
			m_topicDownloader = new TopicDownloader(this);
			m_topicDownloader.download(topic.getSite(), topicUrl);
		}
		
		return topic;
	}

	public void onDownloadComplete(Topic topic) {
		topic.setStatus(TopicStatus.STATUS_COMPLETE);
		Topic completeTopic = m_topics.get(topic.getTopicUrl());
		if (completeTopic == null) {
			m_topics.put(topic.getTopicUrl(), topic);
			completeTopic = topic;
		} else {
			if (topic.getContent().length() != 0) {
				completeTopic.setContent(topic.getContent());
			}
			
			completeTopic.setAuthor(topic.getAuthor());
			completeTopic.setBlog(topic.getBlog(), topic.getBlogUrl());
			completeTopic.setComments(topic.getComments());
		}
		m_topicDownloadCallback.onTopicDownloadComplete(completeTopic);
	}


	public ITopic[] getTopicList(Site site, TopicListType topicListType, ITopicListDownloadCallback topicListDownloadCallback) {
		m_topicListDownloadCallback = topicListDownloadCallback;
		
		// Let's cancel previous download
		cancelDownload();
		
		// let's clear topic list
		m_topics.clear();
		
		// Start new download
		m_topicListDownloader = new TopicListDownloader(site, this); 
		m_topicListDownloader.download(topicListType);
		return getTopicList();
	}
	
	private void cancelDownload() {
		if (m_topicListDownloader == null) {
			return;
		}
		
		m_topicListDownloader.cancel(true);
	}


	private ITopic[] getTopicList() {
		ArrayList<Topic> topics = new ArrayList<Topic>();
		for (Map.Entry<String, Topic> entry : m_topics.entrySet()) {
			topics.add(entry.getValue());
		}
		
		Collections.sort(topics);
		return topics.toArray(new ITopic[0]);
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


	public void clearTopicList() {
		m_topics.clear();
	}
}
