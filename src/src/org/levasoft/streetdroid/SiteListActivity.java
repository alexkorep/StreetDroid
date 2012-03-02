package org.levasoft.streetdroid;

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
        
        @Override  
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //webview.loadUrl("app://data#tips");
        }          
    }

	private TopicFormatter m_formatter = null;
	private WebPage m_webPage;
	private WebView m_webview; 
	
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
        m_webview.setWebViewClient(new TopicWebViewClient());
        m_webview.getSettings().setBuiltInZoomControls(true);
        
        // addJavascriptInterface doesn't work for 2.3, 
        // http://code.google.com/p/android/issues/detail?id=12987
        m_webPage = new WebPage(m_webview);
        m_webPage.definePageBean("bean", new BusinessService(this));
        
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
		final int x = m_webview.getScrollX();
		final int y = m_webview.getScrollY();
		
        final String siteListText = m_formatter.formatSiteList(sites);
        m_webPage.loadDataWithBaseURL("file:///android_asset/", siteListText, "text/html", "UTF-8", null);
        
        m_webview.scrollTo(x, y);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Обновить");
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
	
	@Service
    private class BusinessService {
        private SiteListActivity m_siteListActivity;

		public BusinessService(SiteListActivity siteListActivity) {
			m_siteListActivity = siteListActivity;
		}

        @SuppressWarnings("unused")
        @ServiceMethod
    	public void loadSite(String siteUrl) {
    		Toast.makeText(SiteListActivity.this, siteUrl, 200).show();

    		Bundle bun = new Bundle();
    		bun.putString(TopicListActivity.BUNDLE_VAR_SITE_URL, siteUrl);
    		Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.putExtras(bun);
    		intent.setClassName(m_siteListActivity, TopicListActivity.class.getName());
    		m_siteListActivity.startActivity(intent);
        }

        @SuppressWarnings("unused")
        @ServiceMethod
    	public void deleteSite(int siteId) {
        	PreferencesProvider.INSTANCE.deleteSite(siteId);
        	loadData();
        }

        @SuppressWarnings("unused")
        @ServiceMethod
    	public void addSite(String siteUrl) {
        	PreferencesProvider.INSTANCE.addSite(siteUrl);
        	loadData();
        }

	}	
}


