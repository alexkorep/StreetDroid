package org.levasoft.streetdroid;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

interface ITopicListItem {
	View getView(Context context, ViewGroup parent);

	ITopic getTopic();

	void updateRotatingRefreshIcon();
}

class TopicListItemTitle implements ITopicListItem {
	private TopicListActivity m_activity;
	private View m_rowView;

	TopicListItemTitle(TopicListActivity activity) {
		m_activity = activity;
	}

	@Override
	public View getView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_rowView = inflater.inflate(R.layout.topic_list_title_layout, parent, false);
        m_rowView.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.title_text_bg));
		
        TextView titleView = (TextView) m_rowView.findViewById(R.id.topic_list_title);
        titleView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                	m_activity.selectTopicListType();
	                    break;
	                }
                }
                return true;
            }

        });
        
        TopicListType topicListType = m_activity.getListType();
        final String titleText = m_activity.getSiteUrl() + " - " +  
        		(topicListType == TopicListType.TOPIC_LIST_GOOD ?
	        		m_activity.getString(R.string.topic_list_type_good) :
	        		m_activity.getString(R.string.topic_list_type_all));
        titleView.setText(titleText);
        
        // Reload data listener
        //
        ImageView refreshButton = (ImageView) m_rowView.findViewById(R.id.refresh_icon);
        refreshButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
	                case MotionEvent.ACTION_DOWN: {
	                	m_activity.loadData();
	                    break;
	                }
                }
                return true;
            }

        });
        
        updateRotatingRefreshIcon();

		return m_rowView;
	}

	@Override
	public ITopic getTopic() {
		// return null since it's just a header
		return null;
	}
	
	public void startRotatingRefreshIcon() {
		if (m_rowView == null) {
			return;
		}

		RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(1400);
		ImageView iv = (ImageView) m_rowView.findViewById(R.id.refresh_icon);
		iv.setImageDrawable(m_activity.getResources().getDrawable(
				R.drawable.icon_titlebar_refresh_active));
		iv.startAnimation(anim);
	}

	public void stopRotatingRefreshIcon() {
		if (m_rowView == null) {
			return;
		}
		
		ImageView iv = (ImageView) m_rowView.findViewById(R.id.refresh_icon);
		iv.setImageDrawable(m_activity.getResources().getDrawable(
				R.drawable.icon_titlebar_refresh));
		iv.clearAnimation();
	}

	@Override
	public void updateRotatingRefreshIcon() {
        if (m_activity.isDataReloading()) {
        	startRotatingRefreshIcon();
        } else {
        	stopRotatingRefreshIcon();
        }
	}
	
}

class TopicListItemTopic implements ITopicListItem {

	private final ITopic m_topic;

	TopicListItemTopic(ITopic topics) {
		m_topic = topics;
	}
	
	@Override
	public View getView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.topic_row_layout, parent, false);
        TextView titleView = (TextView) rowView.findViewById(R.id.topic_title);
        final String title = StringEscapeUtils.unescapeHtml4(m_topic.getTitle());
        titleView.setText(title);
        
        TextView detailsView = (TextView) rowView.findViewById(R.id.topic_details);
        final String details = m_topic.getAuthor() + " " + m_topic.getDateTime();
        detailsView.setText(details);

        // TODO add image
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.site_icon);
        //imageView.setImageResource(R.drawable.no);
        
        return rowView;
	}

	@Override
	public ITopic getTopic() {
		return m_topic;
	}

	@Override
	public void updateRotatingRefreshIcon() {
		// TODO Auto-generated method stub
		
	}

}

public class TopicListAdapter extends ArrayAdapter<ITopicListItem> {
	private final TopicListActivity m_activity;
	

    public TopicListAdapter(TopicListActivity activity, ITopic[] topics) {
        super(activity, R.layout.topic_row_layout, getTopicList(activity, topics));
        m_activity = activity;
    }
    
    private static ITopicListItem[] getTopicList(TopicListActivity activity, ITopic[] topics) {
    	ArrayList<ITopicListItem> items = new ArrayList<ITopicListItem>();
    	
    	items.add(new TopicListItemTitle(activity));
    	for (int i = 0; i < topics.length; i++) {
    		items.add(new TopicListItemTopic(topics[i]));
    	}
    	return items.toArray(new ITopicListItem[0]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ITopicListItem item = getItem(position);
        return item.getView(m_activity, parent);
    }

	public ITopic getTopic(int position) {
		return getItem(position).getTopic();
	}
	
	public void updateRotatingRefreshIcon() {
		// We assume first item always exists and is a title
		getItem(0).updateRotatingRefreshIcon();
	}
}
