package org.levasoft.streetdroid;

import java.net.URLEncoder;

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

	static final String BUNDLE_VAR_TOPIC_URL = "topic_url";
	
	WebView webview = null;
	private TopicFormatter m_formatter = null; 
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_formatter = new TopicFormatter(this);

        // Configure webview
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new TopicWebViewClient());
        webview.getSettings().setBuiltInZoomControls(true);
        
        // Load topic data
        //
        
        Bundle bun = getIntent().getExtras();
        assert bun != null;
		final String topicUrl = bun.getString(BUNDLE_VAR_TOPIC_URL);
        ITopic topic = TopicDataProvider.INSTANCE.getFullTopic(topicUrl, this);
        showTopic(topic);
    }
    
    private void showTopic(ITopic topic) {
        final String topicText = m_formatter.format(topic);
        webview.loadDataWithBaseURL("file:///android_asset/", topicText, "text/html", "UTF-8", null);
	}

	public void onTopicDownloadComplete(ITopic topic) {
		showTopic(topic);
	}
}