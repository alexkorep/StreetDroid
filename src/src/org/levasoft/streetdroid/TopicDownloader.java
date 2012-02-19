package org.levasoft.streetdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.htmlcleaner.CommentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.htmlcleaner.Utils;


import android.os.AsyncTask;

public class TopicDownloader extends AsyncTask<String, Integer, String> {

	private static final String TOPIC_URL = "http://%s/blog/%d.html";
	private static final String SITE_URL = "mnmlist.ru";

	private final TopicDataProvider m_dataProvider;
	private Topic m_topic;

	public TopicDownloader(TopicDataProvider dataProvider) {
		m_dataProvider = dataProvider;
	}

	public void download(int topicId) {
		m_topic = new Topic(topicId);
		String url = String.format(TOPIC_URL, SITE_URL , topicId);
		execute(url);
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			HtmlCleaner cleaner = new HtmlCleaner();
			SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());

			TagNode node = cleaner.clean(new URL(arg0[0]));
			
			TagNode[] topicNodes = node.getElementsByAttValue("class", "topic", true, false);
			if (null == topicNodes || null == topicNodes[0]) {
				//throw new ParseExce
				// ToDo Throw parse exception
				return null;
			}
			TagNode topicNode = topicNodes[0];
			
			TagNode[] titleNodes = topicNode.getElementsByAttValue("class", "title", true, false);
			if (null == titleNodes || null == titleNodes[0]) {
				return null;
			}
			String title = titleNodes[0].getText().toString();
			m_topic.setTitle(title);
			
			TagNode[] contentNodes = topicNode.getElementsByAttValue("class", "content", true, false);
			if (null == contentNodes || null == contentNodes[0]) {
				return null;
			}
			//String content = contentNodes[0].getText().toString();
			
			String content = serializer.getAsString(contentNodes[0], "UTF-8", true);
			m_topic.setContent(content);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return null;
    }
	
	@Override
	protected void onPostExecute(String result) {
		onDownloadComplete();
	}	

	@Override
	 protected void onProgressUpdate(Integer... progress) {
		onDownloadProgress(progress[0]);
     }

	private void onDownloadProgress(Integer integer) {
				
	}

	private void onDownloadComplete() {
		m_dataProvider.onDownloadComplete(m_topic);
	}
}