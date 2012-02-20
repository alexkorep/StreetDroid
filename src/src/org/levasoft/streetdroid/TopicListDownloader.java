package org.levasoft.streetdroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;

import android.os.AsyncTask;
import android.os.Message;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;

public class TopicListDownloader extends AsyncTask<String, Integer, String> {

	private static final String FEED_URL = "http://%s/rss/index/";
	
	private Topic[] m_topics = null;

	private TopicDataProvider m_dateProvider;

	public TopicListDownloader(TopicDataProvider topicDataProvider) {
		m_dateProvider = topicDataProvider;
	}

	public void download() {
		final String websiteUrl = PreferencesProvider.INSTANCE.getWebsiteUrl();
		String url = String.format(FEED_URL, websiteUrl);
		execute(url);
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			HtmlCleaner cleaner = new HtmlCleaner();

			TagNode node = cleaner.clean(new URL(arg0[0]));
			
			TagNode[] topicNodes = node.getElementsByName("item", true);
			if (null == topicNodes || topicNodes.length == 0 || null == topicNodes[0]) {
				// feed is empty?
				// TODO do exception 
				return null;
			}
			
			final int topicCount = topicNodes.length;
			m_topics = new Topic[topicCount]; 
			for (int i = 0; i < topicCount; ++i) {
				TagNode topicNode = topicNodes[i];
				String str = topicNode.getText().toString();
				str = str + "l";
				
				// Getting topic id
				final String link = getSingleNoteText(topicNode, "link");
				URL url = new URL(link);
				final String path = url.getPath();
				final String filename = path.substring(path.lastIndexOf("/"), path.lastIndexOf("."));
				final int topicId = Integer.valueOf(filename);
				
				Topic topic = new Topic(topicId);
				topic.setTitle(getSingleNoteText(topicNode, "title"));
				topic.setContent(getSingleNoteText(topicNode, "description"));
				m_topics[i] = topic;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private String getSingleNoteText(TagNode topicNode, String elementName) {
		TagNode[] nodes = topicNode.getElementsByName(elementName, true);
		if (null == nodes || nodes.length == 0 || null == nodes[0]) {
			return "";
		}
		return nodes[0].getText().toString();
	}

	@Override
	protected void onPostExecute(String result) {
		m_dateProvider.onTopicListDownloadComplete(m_topics);
	}	

}

class RSSParser {
	
	// names of the XML tags
    static final String PUB_DATE = "pubDate";
    static final  String DESCRIPTION = "description";
    static final  String LINK = "link";
    static final  String TITLE = "title";
    static final  String ITEM = "item";
    final URL feedUrl ;

    protected RSSParser(String feedUrl){
        try {
            this.feedUrl = new URL(feedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected InputStream getInputStream() {
        try {
            return feedUrl.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Message> parse() {
        final Message currentMessage = new Message();
        RootElement root = new RootElement("rss");
        final List<Message> messages = new ArrayList<Message>();
        Element channel = root.getChild("channel");
        Element item = channel.getChild(ITEM);
        
        item.setEndElementListener(new EndElementListener(){
            public void end() {
                messages.add(currentMessage.copy());
            }
        });
        
        item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setTitle(body);
            }
        });
        item.getChild(LINK).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setLink(body);
            }
        });
        item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setDescription(body);
            }
        });
        item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setDate(body);
            }
        });
        try {
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, 
root.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return messages;
    }
}
