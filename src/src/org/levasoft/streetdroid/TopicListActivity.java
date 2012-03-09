package org.levasoft.streetdroid;  
  
  
import android.app.AlertDialog;
import android.app.ListActivity;  
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;  
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;  
import android.widget.ListView;  
  
public class TopicListActivity extends ListActivity implements ITopicListDownloadCallback {  
	public static final String BUNDLE_VAR_SITE_ID = "site_id";
	
	private TopicListAdapter m_adapter = null;
	private TopicListType m_topicListType;
	private Site m_site;

	private boolean m_isDataLoading = false;; 
	
    public void onCreate(Bundle icicle) {  
        super.onCreate(icicle);

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

    private void reloadTopicList(ITopic[] topics) {
        m_adapter = new TopicListAdapter(this, topics);
        setListAdapter(m_adapter);
	}

    void loadData() {
    	if (m_adapter == null) {
    		// Create an adapter for empty topic list
    		m_adapter = new TopicListAdapter(this, new ITopic[0]);
    	}
    	m_isDataLoading = true;
    	m_adapter.updateRotatingRefreshIcon();
        ITopic[] topics = TopicDataProvider.INSTANCE.getTopicList(m_site, m_topicListType, this);
        reloadTopicList(topics);
	}

    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);  

        ITopic selectedTopic = m_adapter.getTopic(position);
        if (selectedTopic == null) {
        	// Title item selected. Do nothing
        	return;
        }
        
		Bundle bun = new Bundle();
		bun.putString(TopicActivity.BUNDLE_VAR_TOPIC_URL, selectedTopic.getTopicUrl());
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtras(bun);
		intent.setClassName(this, TopicActivity.class.getName());
		startActivity(intent);
    }  
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (m_adapter != null) {
    		m_adapter.notifyDataSetChanged();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 0, 0, R.string.topic_list_type);
        menu.add(Menu.NONE, 1, 1, R.string.topic_list_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
            	selectTopicListType();
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
		reloadTopicList(topics);
		m_isDataLoading  = false;
    	m_adapter.updateRotatingRefreshIcon();
	}

	public TopicListType getListType() {
		return m_topicListType;
	}

	public String getSiteUrl() {
		return m_site.getUrl();
	}

	public boolean isDataReloading() {
		return m_isDataLoading;
	}

	public void selectTopicListType() {
		final CharSequence[] items = {
				getString(R.string.topic_list_type_good),
	        	getString(R.string.topic_list_type_all),
		};
		
		final TopicListType[] types = {
				TopicListType.TOPIC_LIST_GOOD,
				TopicListType.TOPIC_LIST_NEW,
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.topic_list_select_list_type));
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
		    	m_topicListType = types[item];
		    	loadData();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}  