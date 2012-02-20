package org.levasoft.streetdroid;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TopicActivity extends Activity implements ITopicDownloadCallback {
	/**
	 * Web view behavior class
	 */
	class TopicWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

	static final String BUNDLE_VAR_TOPIC_ID = "topic_id";
	
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
        
        //Bundle bun = getIntent().getExtras();
        // assert bun != null;
		final int topicId = 358; //bun.getInt(BUNDLE_VAR_TOPIC_ID, 0);
        ITopic topic = m_dataProvider.getFullTopic(topicId, this);
        showTopic(topic);
    }
    
    private void showTopic(ITopic topic) {
        final String topicText = m_formatter.format(topic);
        webview.loadData(topicText, "text/html", "UTF-8");
	}

	public void onTopicDownloadComplete(ITopic topic) {
		showTopic(topic);
	}
}