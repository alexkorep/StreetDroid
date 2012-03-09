package org.levasoft.streetdroid;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TopicActivity extends Activity implements ITopicDownloadCallback, IVotingCallback {

	static final String BUNDLE_VAR_TOPIC_URL = "topic_url";

	private static final int MENU_ITEM_VOTE_UP = 0;
	private static final int MENU_ITEM_VOTE_DOWN = 1;
	
	WebView m_webview = null;
	private TopicFormatter m_formatter = null;

	private ITopic m_topic = null;

	private ProgressDialog m_progressDialog;

	private int m_webviewScrollX = 0;
	private int m_webviewScrollY = 0;
	private boolean m_downloadComplete = false;
	
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        View rowView = findViewById(R.id.view_topic_title_row);
        rowView.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.title_text_bg));
        
        ImageView refreshButton = (ImageView) findViewById(R.id.refresh_icon);
        refreshButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                	reloadTopic(m_topic.getTopicUrl());
	                    break;
	                }
                }
                return true;
            }

        });

        m_formatter = new TopicFormatter(this);

        // Configure webview
        m_webview = (WebView) findViewById(R.id.webview);
        m_webview.getSettings().setJavaScriptEnabled(true);
        m_webview.getSettings().setBuiltInZoomControls(true);
		m_webview.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				if (m_downloadComplete) {
					stopRotatingRefreshIcon();
					m_webview.scrollTo(m_webviewScrollX, m_webviewScrollY);
				}
			}
		});
        
        // Load topic data
        //
        Bundle bun = getIntent().getExtras();
        assert bun != null;
		final String topicUrl = bun.getString(BUNDLE_VAR_TOPIC_URL);
		reloadTopic(topicUrl);
    }
    
    private void reloadTopic(String topicUrl) {
		m_topic = TopicDataProvider.INSTANCE.getFullTopic(topicUrl, this);
		if (m_topic == null) {
			Toast.makeText(this, getString(R.string.error_cant_find_topic), 200).show();
		}

        startRotatingRefreshIcon();
    	m_downloadComplete = false;
    	showTopic();
    }
    
    private void showTopic() {
        final String topicText = m_formatter.format(m_topic);
        m_webview.loadDataWithBaseURL("file:///android_asset/", topicText, "text/html", "UTF-8", null);
        m_webview.scrollTo(m_webviewScrollX, m_webviewScrollY);
        
        TextView titleView = (TextView) findViewById(R.id.view_topic_title_text);
        final String title = StringEscapeUtils.unescapeHtml4(m_topic.getTitle());
        titleView.setText(title);
	}

	public void onTopicDownloadComplete(ITopic topic) {
		// Remember scroll position
		//
		m_webviewScrollX = m_webview.getScrollX();
		m_webviewScrollY = m_webview.getScrollY();
		
		// Put topic into webview
		m_topic = topic;
    	m_downloadComplete = true;
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
	
	public void startRotatingRefreshIcon() {
		// TODO remove copy-paste with TopicListAdapter
		RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(1400);
		ImageView iv = (ImageView) findViewById(R.id.refresh_icon);
		iv.setImageDrawable(getResources().getDrawable(
				R.drawable.icon_titlebar_refresh_active));
		iv.startAnimation(anim);
	}

	public void stopRotatingRefreshIcon() {
		ImageView iv = (ImageView) findViewById(R.id.refresh_icon);
		iv.setImageDrawable(getResources().getDrawable(
				R.drawable.icon_titlebar_refresh));
		iv.clearAnimation();
	}
}