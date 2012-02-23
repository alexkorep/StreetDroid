package org.levasoft.streetdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.CommentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.htmlcleaner.Utils;
import org.levasoft.streetdroid.rss.AndroidSaxFeedParser;
import org.levasoft.streetdroid.rss.Message;


import android.os.AsyncTask;

public class TopicDownloader extends AsyncTask<String, Integer, String> {

	private static final String RSS_URL = "http://%s/rss/comments/%s/";

	private final TopicDataProvider m_dataProvider;
	private Topic m_topic;
	private String m_filename = "";

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

			m_filename  = arg0[0];
			TagNode node = cleaner.clean(new URL(m_filename));
			
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
		//TagNode[] nodes = commentsNode.getElementsByAttValue("class", "comment", false, false);
		//TagNode[] nodes = commentsNode.getChildTags();
		//TagNode[] nodes = commentsNode.getElementsByAttValue("class", "comment", true, false);
		//commentsNode.

		ArrayList<Comment> comments = new ArrayList<Comment>();
		
		//parseCommentNodes(nodes, serializer, 0, comments);
		parseCommentsFromRss(comments);
		adjustCommentLevels(comments, commentsNode);
		
		m_topic.setComments(comments.toArray(new IComment[0]));
	}

	private void adjustCommentLevels(ArrayList<Comment> comments,
			TagNode commentsNode) {
		int minLevel = 1000;
		for (int i = 0; i < comments.size(); ++i) {
			Comment comment = comments.get(i);
			final String id = comment.getId();
			final String commentAnchor = id.substring(id.indexOf("#comment") + 1); 
			TagNode aNode = commentsNode.findElementByAttValue("id", commentAnchor, true, false);
			final int level = findLevel(aNode);
			if (minLevel > level) {
				minLevel = level;
			}
			comment.setLevel(level);
		}
		
		// Adjust level to zero
		for (int i = 0; i < comments.size(); ++i) {
			Comment comment = comments.get(i);
			comment.setLevel(comment.getLevel() - minLevel);
		}
		
	}

	private int findLevel(TagNode aNode) {
		TagNode node = aNode;
		int level = 0;
		while (node != null) {
			final String classVal = node.getAttributeByName("class"); 
			if (classVal != null && classVal.contains("comment")) {
				level++;
			}
			node = node.getParent();
		}
		return level;
	}

	private void parseCommentsFromRss(ArrayList<Comment> o_comments) {
		URL url;
		try {
			url = new URL(m_filename);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		final String topicId = m_filename.substring(m_filename.lastIndexOf("/") + 1, m_filename.lastIndexOf("."));
		final String rssUrl = String.format(RSS_URL, url.getHost(), topicId);
		AndroidSaxFeedParser parser = new AndroidSaxFeedParser(rssUrl);
		List<Message> messages = null;
		try {
			messages = parser.parse();
		} catch (Exception e) {
			// Invalid datafeed, we need to handle it.
			return;
		}
		
		final int messagesCount = messages == null ? 0 : messages.size();
		if (messagesCount == 0) {
			return;
		}
		
		for (int i = 0; i < messages.size(); ++i) {
			Message msg = messages.get(i);
			Comment comment = new Comment(msg.getLink());
			comment.setAuthor(msg.getCreator());
			
			String description = msg.getDescription().replace("<![CDATA[", "").replace("]]>", ""); 
			comment.setText(description);
			comment.setDateTime(msg.getDate());
			o_comments.add(comment);
		}
	}

	/**
	 * Recursively parses comments from the given node list
	 * @param commentNodes comments nodes
	 * @param serializer
	 * @param level 
	 * @param o_comments output array of comments
	 */
	/*
	private void parseCommentNodes(TagNode[] commentNodes,
			SimpleHtmlSerializer serializer, int level, ArrayList<IComment> o_comments) {
		
		if (commentNodes == null) {
			// no child comment for this section?
			return;
		}

		
		//IComment[] comments = new IComment[nodes.length];
		for (int i = 0; i < commentNodes.length; ++i) {
			TagNode commentNode = commentNodes[i];
			if (commentNode == null) {
				continue;
			}
			
			final String commentId = commentNode.getAttributeByName("id");
			if (commentId == null) {
				continue;
			}
			
			Comment comment = new Comment(commentId);
			comment.setLevel(level);
			
			TagNode authorNode = getSingleElement(commentNode, "author");
			if (null != authorNode) {
				final String author = authorNode.getText().toString();
				comment.setAuthor(author);
				comment.setAuthorUrl(authorNode.getAttributeByName("href"));
			}
			
			TagNode dateNode = getSingleElement(commentNode, "date");
			if (null != dateNode) {
				comment.setDateTime(dateNode.getText().toString());
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
			
			o_comments.add(comment);
			
			// Add child comments
			TagNode childrenNode = getSingleElement(commentNode, "comment-children");
			if (childrenNode != null) {
				TagNode[] childNodes = childrenNode.getElementsByAttValue("class", "comment", false, false);
				parseCommentNodes(childNodes, serializer, level + 1, o_comments);
			}
		}
		
	}
	*/

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