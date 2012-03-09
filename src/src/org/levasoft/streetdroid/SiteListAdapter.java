package org.levasoft.streetdroid;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

interface ISiteListItem {
	View getView(Context context, ViewGroup parent);

	Site getSite();
}

class SiteListItemTitle implements ISiteListItem {

	@Override
	public View getView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.site_list_title_layout, parent, false);
		rowView.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.title_text_bg));
		return rowView;
	}

	@Override
	public Site getSite() {
		// return null since it's just a header
		return null;
	}
}

class SiteListItemSite implements ISiteListItem {

	private final Site m_site;

	SiteListItemSite(Site site) {
		m_site = site;
	}
	
	@Override
	public View getView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.siterowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.site_url);
        final String url = m_site.getUrl();
        textView.setText(url);
        
        TextView descriptionView = (TextView) rowView.findViewById(R.id.site_description);
        final String title = m_site.getTitle();
        descriptionView.setText(title);

        TextView usernameView = (TextView) rowView.findViewById(R.id.site_username);
        final String username = m_site.getUsername();
        usernameView.setText(username);

        // TODO change icon
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.site_icon);
        //imageView.setImageResource(R.drawable.no);
        
        return rowView;
	}

	@Override
	public Site getSite() {
		return m_site;
	}
}

public class SiteListAdapter extends ArrayAdapter<ISiteListItem> {
	private final Context m_context;
	

    public SiteListAdapter(Context context) {
        super(context, R.layout.siterowlayout, getSiteList(context));
        m_context = context;
    }
    
    private static ISiteListItem[] getSiteList(Context context) {
    	ArrayList<ISiteListItem> items = new ArrayList<ISiteListItem>();
    	items.add(new SiteListItemTitle());
    	Site[] sites = PreferencesProvider.INSTANCE.getSites();
    	for (int i = 0; i < sites.length; i++) {
    		items.add(new SiteListItemSite(sites[i]));
    	}
    	return items.toArray(new ISiteListItem[0]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ISiteListItem item = getItem(position);
        return item.getView(m_context, parent);
    }

	public Site getSite(int position) {
		return getItem(position).getSite();
	}

}
