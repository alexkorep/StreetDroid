package org.levasoft.streetdroid;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TopicListActivity extends Activity {
	//http://mnmlist.ru/rss/index/
	/**
	 * Web view behavior class
	 */
	private class TopicWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

	WebView webview = null;
	private TopicDataProvider m_dataProvider = null;
	private TopicFormatter m_formatter = null; 
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_dataProvider = new TopicDataProvider();
        m_formatter = new TopicFormatter(this);

        // Configure webview
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new TopicWebViewClient());
        webview.getSettings().setBuiltInZoomControls(true);
        
        // Load topic data
        //
        
        ITopic[] topics = m_dataProvider.getTopicList();
        showTopics(topics);
    }
    
    private void showTopics(ITopic[] topics) {
        //final String topicText = m_formatter.format(topics);
        //webview.loadData(topicText, "text/html", "UTF-8");
	}

	public void onTopicDownloadComplete(ITopic topic) {
		//showTopic(topic);
	}	
	
}
