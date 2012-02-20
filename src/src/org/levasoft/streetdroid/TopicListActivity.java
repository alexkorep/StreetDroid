package org.levasoft.streetdroid;

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
        
        PreferencesProvider.INSTANCE.SetContext(this);

        // Load topic data
        //
        ITopic[] topics = m_dataProvider.getTopicList(this);
        showTopics(topics);
    }

	private void showTopics(ITopic[] topics) {
        final String topicListText = m_formatter.formatTopicList(topics);
        webview.loadData(topicListText, "text/html", "UTF-8");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Настройки");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, StreetDroidPreferenceActivity.class));
                return true;
        }
        return false;
    }

	public void onTopicListDownloadComplete(ITopic[] topics) {
        showTopics(topics);
	}
}