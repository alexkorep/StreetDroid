package org.levasoft.streetdroid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.TreeMap;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.levasoft.streetdroid.rss.AndroidSaxFeedParser;
import org.levasoft.streetdroid.rss.Message;

import android.os.AsyncTask;

/**
 * Downloads topic text from topic HTML page and topic comments from comments RSS.
 *
 */
public class TopicDownloader extends AsyncTask<String, Integer, String> {

	private static final String RSS_URL = "http://%s/rss/comments/%s/";

	// We call its method when downloading is complete.
	// TODO introduce an interface implemented by data provider, use it instead.
	private final TopicDataProvider m_dataProvider; 
	
	private Topic m_topic;			// Topic object
	private String m_filename = ""; // Topic URL to be used for downloading 

	public TopicDownloader(TopicDataProvider dataProvider) {
		m_dataProvider = dataProvider;
	}

	/**
	 * Downloads topic context. Doesn't block current thread, launches
	 * a new download thread which calls TopicDataProvider.onDownloadComplete when it's done.
	 * @param site - site this topic belongs to
	 * @param topicUrl - topic URL
	 */
	public void download(Site site, String topicUrl) {
		m_topic = new Topic(topicUrl, site);
		execute(topicUrl);
	}

	/**
	 * Method to be executed in the worker thread.
	 */
	@Override
	protected String doInBackground(String... arg0) {
		try {
			HtmlCleaner cleaner = new HtmlCleaner();
			SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());

			m_filename  = arg0[0];
			
			final String page = PageDownloadManager.INSTANCE.download(m_filename);
			final TagNode node = cleaner.clean(page);
			
			TagNode topicNode = getSingleElement(node, "topic");
			if (topicNode == null) {
				// Throw some error
				return "Error loading topic (invalid formatting?)";
			}
			
			TagNode contentNode = getSingleElement(topicNode, "content"); 
			if (null != contentNode) {
				String content = serializer.getAsString(contentNode, "UTF-8", true);
				m_topic.setContent(content);
			}
			
			parseComments(node, serializer);
			
			final String topicPageText = serializer.getAsString(node);
			// TODO handle parsing errors
			m_topic.getVotingDetails().parseVotingDetails(topicPageText);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (StreetDroidException e) {
			// TODO Auto-generated catch block
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
			// TODO Throw some error
			// Error loading comments (invalid formatting?)
		}

		ArrayList<Comment> comments = new ArrayList<Comment>();
		
		parseCommentsFromRss(comments);
		adjustCommentLevels(comments, commentsNode);
		
		m_topic.setComments(comments.toArray(new IComment[0]));
	}

	/**
	 * Since RSS doesn't contain comment level information, we need to extract that
	 * data from HTML and merge with RSS data.
	 * @param comments
	 * @param commentsNode
	 */
	private void adjustCommentLevels(ArrayList<Comment> comments,
			TagNode commentsNode) {
		
		BitSet levels = new BitSet();
		for (int i = 0; i < comments.size(); ++i) {
			Comment comment = comments.get(i);
			final String id = comment.getId();
			final String commentAnchor = id.substring(id.indexOf("#comment") + 1); 
			TagNode aNode = commentsNode.findElementByAttValue("id", commentAnchor, true, false);
			if (aNode == null ) {
				aNode = commentsNode.findElementByAttValue("name", commentAnchor, true, false);
			}
			
			final int level = findLevel(aNode);
			levels.set(level);
			comment.setLevel(level);
		}
		
		// Adjust level to zero
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		int levelNo = 0;
		for (int i = 0; i < levels.size(); ++i) {
			if (levels.get(i)) {
				map.put(i, levelNo);
				levelNo++;
			}
		}
		
		for (int i = 0; i < comments.size(); ++i) {
			Comment comment = comments.get(i);
			final int level = comment.getLevel();
			final int newLevel = map.get(level);
			comment.setLevel(newLevel);
		}
		
	}

	private int findLevel(TagNode aNode) {
		TagNode node = aNode;
		int level = 0;
		while (node != null) {
			level++;
			node = node.getParent();
		}
		return level;
	}

	/**
	 * Parses comments from RSS
	 * @param o_comments
	 */
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
			comment.setDateTime(msg.getDateFormatted());
			o_comments.add(comment);
		}
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

	/**
	 * Method to be called when worker thread is done.
	 */
	@Override
	protected void onPostExecute(String result) {
		m_dataProvider.onDownloadComplete(m_topic);
	}	

	/**
	 * Not used: we don't report downloading progress to user.
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
	}
}