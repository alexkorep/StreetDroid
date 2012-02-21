package org.levasoft.streetdroid;

import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TopicListActivity extends Activity implements ITopicListDownloadCallback {
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
        
        // TODO create separate class to avoid possible security hole
        webview.addJavascriptInterface(this, "jscontroller"); 
        
        PreferencesProvider.INSTANCE.SetContext(this);

        // Load topic data
        //
        loadData();
    }

	private void loadData() {
        ITopic[] topics = TopicDataProvider.INSTANCE.getTopicList(this);
        showTopics(topics);
	}

	private void showTopics(ITopic[] topics) {
        final String topicListText = m_formatter.formatTopicList(topics);
        //final String textEncoded = URLEncoder.encode(topicListText).replaceAll("\\+"," ");
        final String textEncoded = topicListText;
        webview.loadDataWithBaseURL("file:///android_asset/", textEncoded, "text/html", "UTF-8", null);
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
}