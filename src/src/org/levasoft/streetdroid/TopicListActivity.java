package org.levasoft.streetdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TopicListActivity extends Activity implements ITopicListDownloadCallback {
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

	public static final String BUNDLE_VAR_SITE_URL = "site_url";

	WebView webview = null;
	private TopicFormatter m_formatter = null;

	private String m_websiteUrl; 
	
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
        
        // TODO create separate class to avoid possible security hole
        webview.addJavascriptInterface(this, "jscontroller"); 
        
        PreferencesProvider.INSTANCE.SetContext(this);
        
        Bundle bun = getIntent().getExtras();
        assert bun != null;
		m_websiteUrl = bun.getString(BUNDLE_VAR_SITE_URL);

        // Load topic data
        //
        loadData();
    }

	private void loadData() {
        ITopic[] topics = TopicDataProvider.INSTANCE.getTopicList(m_websiteUrl, this);
        showTopics(topics);
	}

	private void showTopics(ITopic[] topics) {
        final String topicListText = m_formatter.formatTopicList(topics);
        webview.loadDataWithBaseURL("file:///android_asset/", topicListText, "text/html", "UTF-8", null);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Настройки");
        menu.add(Menu.NONE, 1, 1, "Обновить");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
                startActivity(new Intent(this, StreetDroidPreferenceActivity.class));
                return true;
            }
            case 1: {
            	loadData();
                return true;
            }
        }
        return false;
    }

	public void onTopicListDownloadComplete(ITopic[] topics) {
        showTopics(topics);
	}
	
	/**
	 * Should be called from JavaScript
	 * @param topicUrl
	 */
	public void loadTopic(String topicUrl) {
		Bundle bun = new Bundle();
		bun.putString(TopicActivity.BUNDLE_VAR_TOPIC_URL, topicUrl);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtras(bun);
		intent.setClassName(this, TopicActivity.class.getName());
		startActivity(intent);
	}
	
	public void deleteSite(int siteId) {
		
	}
}