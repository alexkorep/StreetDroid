package org.levasoft.streetdroid;

import java.security.KeyStore.LoadStoreParameter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

public class TopicActivity extends Activity implements ITopicDownloadCallback, IVotingCallback {

	static final String BUNDLE_VAR_TOPIC_URL = "topic_url";

	private static final int MENU_ITEM_VOTE_UP = 0;
	private static final int MENU_ITEM_VOTE_DOWN = 1;
	
	WebView m_webview = null;
	private TopicFormatter m_formatter = null;

	private ITopic m_topic = null;

	private ProgressDialog m_progressDialog; 
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_formatter = new TopicFormatter(this);

        // Configure webview
        m_webview = (WebView) findViewById(R.id.webview);
        m_webview.getSettings().setJavaScriptEnabled(true);
        m_webview.getSettings().setBuiltInZoomControls(true);
        
        // Load topic data
        //
        
        Bundle bun = getIntent().getExtras();
        assert bun != null;
		final String topicUrl = bun.getString(BUNDLE_VAR_TOPIC_URL);
		m_topic = TopicDataProvider.INSTANCE.getFullTopic(topicUrl, this);
		if (m_topic == null) {
			Toast.makeText(this, getString(R.string.error_cant_find_topic), 200).show();
		}
        showTopic();
    }
    
    private void showTopic() {
    	
		final int x = m_webview.getScrollX();
		final int y = m_webview.getScrollY();

        final String topicText = m_formatter.format(m_topic);
        m_webview.loadDataWithBaseURL("file:///android_asset/", topicText, "text/html", "UTF-8", null);
        
        m_webview.scrollTo(x, y);
	}

	public void onTopicDownloadComplete(ITopic topic) {
		m_topic = topic;
		showTopic();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ITEM_VOTE_UP, MENU_ITEM_VOTE_UP, R.string.topic_vote_up);
        menu.add(Menu.NONE, MENU_ITEM_VOTE_DOWN, MENU_ITEM_VOTE_DOWN, R.string.topic_vote_down);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_VOTE_UP:
            case MENU_ITEM_VOTE_DOWN: {
        		m_progressDialog = new ProgressDialog(this);
        		m_progressDialog.setMessage(getString(R.string.topic_vote_progress));
        		m_progressDialog.setIndeterminate(false);
        		m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		m_progressDialog.show();
        		
				TopicVoter topicVoter = new TopicVoter(item.getItemId() == MENU_ITEM_VOTE_UP ? 1 : -1, m_topic, this);
				topicVoter.execute(0);
                return true;
            }
        }
        return false;
    }

	@Override
	public void OnVotingResult(String message) {
		m_progressDialog.dismiss();
		Toast.makeText(this, message, 200).show();
	}
	
}