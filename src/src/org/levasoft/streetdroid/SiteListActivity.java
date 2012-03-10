package org.levasoft.streetdroid;  
  
  
import java.util.ArrayList;

import org.levasoft.streetdroid.imageloader.ImageLoader;

import android.app.AlertDialog;
import android.app.ListActivity;  
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;  
import android.text.method.SingleLineTransformationMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;  
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;  
  
public class SiteListActivity extends ListActivity {  
	private static final int ITEM_CONTEXTMENU_DELETEITEM = 2;
	private static final int ITEM_CONTEXTMENU_SITECONFIG = 3;
	
	private SiteListAdapter m_adapter = null;
	
    public void onCreate(Bundle icicle) {  
        super.onCreate(icicle);  
        PreferencesProvider.INSTANCE.SetContext(this);
        reloadSiteList();
        
        ListView list = getListView();
        list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
        	 
			@Override
			public void onCreateContextMenu(ContextMenu conMenu, View arg1,
					ContextMenuInfo arg2) {
                //conMenu.setHeaderTitle("ContextMenu");
                conMenu.add(0, ITEM_CONTEXTMENU_DELETEITEM, ITEM_CONTEXTMENU_DELETEITEM, "Удалить");
                //conMenu.add(0, ITEM_CONTEXTMENU_SITECONFIG, ITEM_CONTEXTMENU_SITECONFIG, R.string.menu_popup_site_config);
			}
        });
        
        if (PreferencesProvider.INSTANCE.getSites().length == 0) {
        	addNewSiteDialog();
        }
    }
    
    @Override  
    protected void onDestroy () {
    	// Clear images cache
    	new ImageLoader(this).clearCache(); 
    	super.onDestroy();
    }

    private void reloadSiteList() {
        m_adapter = new SiteListAdapter(this);
        setListAdapter(m_adapter);
	}

    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);  

        Site site = m_adapter.getSite(position);
        if (null == site) {
        	// title clicked?
        	return;
        }
    	final int siteId = site.getId();
    	openSite(siteId);
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
        menu.add(Menu.NONE, 0, 0, "Добавить");
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0: {
				addNewSiteDialog();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();;
		final Site site = m_adapter.getSite(info.position);
		if (null == site) {
			// title selected
			return false;
		}
		
		switch (item.getItemId()) {
			case ITEM_CONTEXTMENU_DELETEITEM: {
				PreferencesProvider.INSTANCE.deleteSite(site.getId());
				reloadSiteList();
				return true;
			}
			case ITEM_CONTEXTMENU_SITECONFIG: {
				showSiteConfigDlg(site);
				return true;
			}
				
		}
		return false;
	}
	
	public void showSiteConfigDlg(final Site site) {
		LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.siteconfigdlg, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.site_config_prompt)
            .setView(textEntryView)
            .setPositiveButton(R.string.site_config_dlg_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// On OK handler
                	//
                    EditText edUsername = (EditText) textEntryView.findViewById(R.id.editUsername);
                    EditText edPassword = (EditText) textEntryView.findViewById(R.id.editPassword);
                    final String username = edUsername.getText().toString();
                    final String password = edPassword.getText().toString();
                    PreferencesProvider.INSTANCE.setSiteUsernamePassword(
                    		site.getId(), username, password);
               		m_adapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton(R.string.site_config_dlg_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            })
            .create();
        EditText edUsername = (EditText) textEntryView.findViewById(R.id.editUsername);
        EditText edPassword = (EditText) textEntryView.findViewById(R.id.editPassword);
        
        edUsername.setText(site.getUsername());
        edPassword.setText(site.getPassword());
        dialog.show();
	 }

	public void addNewSiteDialog() {
		ArrayList<String> sites = new ArrayList<String>(); 
		final String[] items = PreferencesProvider.INSTANCE.getDefaultSites();
		for (int i = 0; i < items.length; ++i) {
			sites.add(items[i]);
		}
		sites.add(getString(R.string.site_list_select_other));
		
		final String[] menuItems = sites.toArray(new String[0]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.site_list_select_list_title));
		builder.setItems(menuItems, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if (item == menuItems.length - 1) {
		    		showCustomSiteDialog();
		    	} else {
		    		addSite(menuItems[item]);
		    	}
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void showCustomSiteDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Новый сайт");
		alert.setMessage("Укажите URL сайта без http://, например livestreet.ru");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setTransformationMethod(new SingleLineTransformationMethod());
		alert.setView(input);

		alert.setPositiveButton("Добавить",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String siteUrl = input.getText().toString();
						addSite(siteUrl);
					}
				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	protected void addSite(String siteUrl) {
		PreferencesProvider.INSTANCE.addSite(siteUrl);
		reloadSiteList();
		
		Site site = PreferencesProvider.INSTANCE.getSiteByUrl(siteUrl);
		openSite(site.getId());
	}

	private void openSite(int siteId) {
		Bundle bun = new Bundle();
		bun.putInt(TopicListActivity.BUNDLE_VAR_SITE_ID, siteId);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtras(bun);
		intent.setClassName(this, TopicListActivity.class.getName());
		startActivity(intent);
	}
	
}  