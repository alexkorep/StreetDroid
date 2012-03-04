package org.levasoft.streetdroid;

import java.io.IOException;

import org.mobilelite.android.WebPage;
import org.mobilelite.annotation.Service;
import org.mobilelite.annotation.ServiceMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TopicListActivity extends Activity implements ITopicListDownloadCallback {
	public interface OnViewTopicListActionListener {
		public void onViewTopicListAction(String siteUrl);
	}

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

	public static final String BUNDLE_VAR_SITE_ID = "site_id";

	private TopicFormatter m_formatter = null;

	private WebPage m_webPage;

	private TopicListType m_topicListType;

	private Site m_site; 
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_formatter = new TopicFormatter(this);

        // Configure webview
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new TopicWebViewClient());
        webview.getSettings().setBuiltInZoomControls(true);
        
        m_webPage = new WebPage(webview);
        m_webPage.definePageBean("bean", new BusinessService(this));
        
        PreferencesProvider.INSTANCE.SetContext(this);
        
        Bundle bun = getIntent().getExtras();
        assert bun != null;
		final int siteId = bun.getInt(BUNDLE_VAR_SITE_ID);
		m_site = PreferencesProvider.INSTANCE.getSiteById(siteId);
		
		// We don't need to show topics from the previous website
		TopicDataProvider.INSTANCE.clearTopicList();

        // Load topic data
        //
		m_topicListType = TopicListType.TOPIC_LIST_GOOD; 
        loadData();
    }

	private void loadData() {
        ITopic[] topics = TopicDataProvider.INSTANCE.getTopicList(m_site, m_topicListType, this);
        showTopics(topics);
	}

	private void showTopics(ITopic[] topics) {
        final String topicListText = m_formatter.formatTopicList(topics);
        m_webPage.loadDataWithBaseURL("file:///android_asset/", topicListText, "text/html", "UTF-8", null);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, R.string.topic_list_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
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
	
	@Service
    private class BusinessService {
        private final TopicListActivity m_topicListActivity;

		public BusinessService(TopicListActivity topicListActivity) {
			m_topicListActivity = topicListActivity;
		}

        @SuppressWarnings("unused")
        @ServiceMethod
    	public void loadTopic(String topicUrl) {
    		Bundle bun = new Bundle();
    		bun.putString(TopicActivity.BUNDLE_VAR_TOPIC_URL, topicUrl);
    		
    		Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.putExtras(bun);
    		intent.setClassName(m_topicListActivity, TopicActivity.class.getName());
    		startActivity(intent);
    	}

        @SuppressWarnings("unused")
        @ServiceMethod
        /**
         * 
         * @param feedType: 0 - good topic, 1 - new topics
         */
    	public void loadTopicList(int feedType) {
        	switch (feedType) {
        	case 0: 
        		m_topicListType = TopicListType.TOPIC_LIST_GOOD;
        		break;
        	case 1:
        		m_topicListType = TopicListType.TOPIC_LIST_NEW;
        		break;
        	default:
        		// Not sure what to do
        		break;
        	}
        	loadData();
    	}
    }	
}