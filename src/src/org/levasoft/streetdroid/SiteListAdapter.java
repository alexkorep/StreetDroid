package org.levasoft.streetdroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SiteListAdapter extends ArrayAdapter<Site> {
	private final Context m_context;

    public SiteListAdapter(Context context) {
        super(context, R.layout.siterowlayout, PreferencesProvider.INSTANCE.getSiteList());
        m_context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	final Site[] sites = PreferencesProvider.INSTANCE.getSiteList();
    	
        LayoutInflater inflater = (LayoutInflater) m_context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.siterowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.site_url);
        final String url = sites[position].getUrl();
        textView.setText(url);
        
        TextView descriptionView = (TextView) rowView.findViewById(R.id.site_description);
        final String title = sites[position].getTitle();
        descriptionView.setText(title);

        TextView usernameView = (TextView) rowView.findViewById(R.id.site_username);
        final String username = sites[position].getUsername();
        usernameView.setText(username);

        // TODO change icon
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.site_icon);
        //imageView.setImageResource(R.drawable.no);
        return rowView;
    }

}
