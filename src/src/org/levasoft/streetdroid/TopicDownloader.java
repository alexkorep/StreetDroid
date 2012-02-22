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
			
			parseComments(node, serializer);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return null;
    }
	
	/**
	 * Parses comments from DOM
	 * @param node in, root document node
	 * @param serializer in, serializer, needs for comment text formatting.
	 */
	private void parseComments(TagNode node, SimpleHtmlSerializer serializer) {
		// Find comments section
		//
		TagNode commentsNode = getSingleElement(node, "comments");
		if (commentsNode == null) {
			// Throw some error
			// Error loading comments (invalid formatting?)
		}

		// Gets list of comment tags
		// TODO check if it loads comments from all levels or from top one
		TagNode[] nodes = node.getElementsByAttValue("class", "comment", true, false);
		
		if (nodes == null) {
			// no comments for this topic?
			return;
		}

		
		// TODO use dynamic container. Not all nodes will contain actual comment.
		IComment[] comments = new IComment[nodes.length];
		for (int i = 0; i < nodes.length; ++i) {
			TagNode commentNode = nodes[i];
			if (commentNode == null) {
				continue;
			}
			
			final String commentId = commentNode.getAttributeByName("id");
			if (commentId == null) {
				continue;
			}
			
			Comment comment = new Comment(commentId);
			
			TagNode authorNode = getSingleElement(commentNode, "author");
			if (null != authorNode) {
				final String author = authorNode.getText().toString();
				comment.setAuthor(author);
			}
			
			
			TagNode textNode = getSingleElement(commentNode, "text"); 
			if (null != textNode) {
				String text = "";
				try {
					text = serializer.getAsString(textNode, "UTF-8", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				comment.setText(text);
			}
			
			comments[i] = comment;
		}
		m_topic.setComments(comments);
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