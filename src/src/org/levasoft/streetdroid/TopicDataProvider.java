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

/**
 * Topic-related data provider class.
 * - Downloads data from topic full HTML page by request.
 * - Downloads topic list from RSS
 * Downloading is performed in asynchronously in separate thread and doesn't block the main thread.
 * Callbacks should be provided when requesting topic or topic list info in order to be notified when topic 
 * or topic list is completely downloaded.
 * 
 * Implemented as a singleton.
 *
 */
public class TopicDataProvider {
	// One and only instance
	public static final TopicDataProvider INSTANCE = new TopicDataProvider();
	
	// Topic list, stored as a hash map, topic URL is used as a map key.
	// TODO should it be volatile?
	private HashMap<String, Topic> m_topics  = new HashMap<String, Topic>();
	
	// Callback to be called when topic is completely downloaded and parsed from
	// HTML page
	private ITopicDownloadCallback m_topicDownloadCallback = null;
	
	// Callback to be called when topic list is completely downloaded and parsed from RSS.
	private ITopicListDownloadCallback m_topicListDownloadCallback = null;
	
	// Class to download and parse RSS with topic list
	private TopicListDownloader m_topicListDownloader = null;

	// Class to download and parse topic from the topic HTML page
	private TopicDownloader m_topicDownloader;
	
	// Constructor, private since it's a singleton
	private TopicDataProvider() {
	}


	/**
	 * Gets full topic content.
	 * If we don't have this topic downloaded, it starts downloading and immediately returns the topic in current status
	 * (we should have the brief topic content parsed from RSS by this moment). 
	 * @param topicUrl - Url to download topic from
	 * @param topicDownloadCallback - callback to be notified when topic has been completely downloaded.
	 * @return Topic object
	 */
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

	/**
	 * Is called by TopicDownloader when download is complete 
	 * @param topic
	 */
	public void onDownloadComplete(Topic topic) {
		topic.setStatus(TopicStatus.STATUS_COMPLETE);
		Topic oldTopic = m_topics.get(topic.getTopicUrl());
		
		// Update fields of just downloaded topic with fields of topic got from RSS
		if (topic.getAuthor().length() == 0) topic.setAuthor(oldTopic.getAuthor());
		if (topic.getContent().length() == 0) topic.setContent(oldTopic.getContent());
		if (topic.getTitle().length() == 0) topic.setTitle(oldTopic.getTitle());

		// Put the new topic into the container
		m_topics.put(topic.getTopicUrl(), topic);
		
		// Notify the callback
		m_topicDownloadCallback.onTopicDownloadComplete(topic);
	}


	/**
	 * Returns topic list for the given site. If we don't have it yet, then list is downloaded and parsed from RSS.
	 * @param site - site object.
	 * @param topicListType	- type of topics (all or good only). Different RSS URLs are used for different topic lists.
	 * @param topicListDownloadCallback - callback to be called when download is complete.
	 * @return
	 */
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
	
	/**
	 * Cancels all download processes. 
	 */
	private void cancelDownload() {
		if (m_topicListDownloader == null) {
			return;
		}
		
		m_topicListDownloader.cancel(true);
	}


	/**
	 * Returns cached topic list.
	 */
	private ITopic[] getTopicList() {
		ArrayList<Topic> topics = new ArrayList<Topic>();
		for (Map.Entry<String, Topic> entry : m_topics.entrySet()) {
			topics.add(entry.getValue());
		}
		
		Collections.sort(topics);
		return topics.toArray(new ITopic[0]);
	}


	/**
	 * Called by TopicListDownloader when it's done.
	 */
	public void onTopicListDownloadComplete(Topic[] topics) {
		m_topics.clear();
		for (int i = 0; i < topics.length; ++i) {
			Topic topic = topics[i];
			m_topics.put(topic.getTopicUrl(), topic);
		}
		
		m_topicListDownloadCallback.onTopicListDownloadComplete(getTopicList());
		m_topicListDownloader = null;
	}


	/**
	 * Clears internal topic list cache.
	 */
	public void clearTopicList() {
		m_topics.clear();
	}
}
