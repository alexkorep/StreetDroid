package org.levasoft.streetdroid;  
  
  
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
import android.widget.Toast;  
  
public class SiteList extends ListActivity {  
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
                conMenu.add(0, ITEM_CONTEXTMENU_SITECONFIG, ITEM_CONTEXTMENU_SITECONFIG, R.string.menu_popup_site_config);
                /* Add as many context-menu-options as you want to. */
			}
        });
        //registerForContextMenu(list);
    }  

    private void reloadSiteList() {
        m_adapter = new SiteListAdapter(this);
        setListAdapter(m_adapter);
	}

    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);  

        Site[] sites = PreferencesProvider.INSTANCE.getSiteList();
    	final String url = sites[position].getUrl();
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();  
        
		Bundle bun = new Bundle();
		bun.putString(TopicListActivity.BUNDLE_VAR_SITE_URL, url);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtras(bun);
		intent.setClassName(this, TopicListActivity.class.getName());
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
        menu.add(Menu.NONE, 0, 0, "Добавить");
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0: {
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
								PreferencesProvider.INSTANCE.addSite(siteUrl);
								reloadSiteList();
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
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ITEM_CONTEXTMENU_DELETEITEM: {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();;
				Site site = PreferencesProvider.INSTANCE.getSiteList()[info.position]; 
				PreferencesProvider.INSTANCE.deleteSite(site.getId());
				reloadSiteList();
				return true;
			}
			case ITEM_CONTEXTMENU_SITECONFIG: {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();;
				Site site = PreferencesProvider.INSTANCE.getSiteList()[info.position]; 
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

}  