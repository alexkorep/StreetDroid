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

	//private static final String TOPIC_URL = "http://%s/%s";
	//private static final String SITE_URL = "mnmlist.ru";

	private final TopicDataProvider m_dataProvider;
	private Topic m_topic;

	public TopicDownloader(TopicDataProvider dataProvider) {
		m_dataProvider = dataProvider;
	}

	public void download(String  topicUrl) {
		m_topic = new Topic(topicUrl);
		//String url = String.format(TOPIC_URL, SITE_URL , topicUrl);
		execute(topicUrl);
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			HtmlCleaner cleaner = new HtmlCleaner();
			SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());

			TagNode node = cleaner.clean(new URL(arg0[0]));
			
			TagNode topicNode = getSingleElement(node, "topic");
			if (topicNode == null) {
				// Throw some error
				return "Error loading topic (invalid formatting?)";
			}
			
			TagNode titleNode = getSingleElement(topicNode, "title");
			if (null != titleNode) {
				String title = titleNode.getText().toString();
				m_topic.setTitle(title);
			}
			
			
			TagNode contentNode = getSingleElement(topicNode, "content"); 
			if (null != contentNode) {
				String content = serializer.getAsString(contentNode, "UTF-8", true);
				m_topic.setContent(content);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return null;
    }
	
	private TagNode getSingleElement(TagNode node, String elementClass) {
		TagNode[] nodes = node.getElementsByAttValue("class", elementClass, true, false);
		if (null == nodes || nodes.length == 0 || null == nodes[0]) {
			//throw new ParseExce
			// ToDo Throw parse exception
			return null;
		}
		TagNode resultNode = nodes[0];
		return resultNode;
	}

	@Override
	protected void onPostExecute(String result) {
		m_dataProvider.onDownloadComplete(m_topic);
	}	

	@Override
	 protected void onProgressUpdate(Integer... progress) {
		//onDownloadProgress(progress[0]);
     }
}