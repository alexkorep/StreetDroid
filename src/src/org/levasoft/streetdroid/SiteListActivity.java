package org.levasoft.streetdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SiteListActivity extends Activity {
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
        Site[] sites = PreferencesProvider.INSTANCE.getSiteList();
        showSites(sites);
	}

	private void showSites(Site[] sites) {
        final String siteListText = m_formatter.formatSiteList(sites);
        webview.loadDataWithBaseURL("file:///android_asset/", siteListText, "text/html", "UTF-8", null);
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

	/**
	 * Should be called from JavaScript
	 * @param siteUrl
	 */
	public void loadSite(String siteUrl) {
		Bundle bun = new Bundle();
		bun.putString(TopicListActivity.BUNDLE_VAR_SITE_URL, siteUrl);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtras(bun);
		intent.setClassName(this, TopicListActivity.class.getName());
		startActivity(intent);
	}
}


