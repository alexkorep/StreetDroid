package org.levasoft.streetdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;

import org.levasoft.streetdroid.voting.ILiveStreetVersion;
import org.levasoft.streetdroid.voting.LiveStreetVersion35;
import org.levasoft.streetdroid.voting.LiveStreetVersion40;

import android.os.AsyncTask;

interface IVotingCallback {
	void OnVotingResult(final String message);
}

public class TopicVoter extends AsyncTask<Integer, Integer, String> {
	private static final String POST_VAR_LOGIN = "login";
	private static final String POST_VAR_PASSWORD = "password";
	private static final String POST_VAR_REMEMBER = "remember";
	private static final String POST_VAR_REMEMBER_VALUE = "checked";
	private static final String POST_VAR_SUBMIT_LOGIN = "submit_login";

	private static String LOGIN_URL = "http://%s/login";

	
	private static ILiveStreetVersion[] m_versions = {
		new LiveStreetVersion35(),
		new LiveStreetVersion40()
	};
	
	// Cookie name which is set when
	private static String LOGGED_IN_COOKIE_NAME = "key";
	private final int m_vote;
	private final IVotingCallback m_callback;
	private final ITopic m_topic;
	
	TopicVoter(int vote, ITopic topic, IVotingCallback callback) {
		m_vote = vote;
		m_topic = topic;
		m_callback = callback;
	}
	
	private void login(final Site site) throws StreetDroidException, ClientProtocolException, IOException {
		if (isLoggedIn(site)) {
			// Already logged in
			return;
		}

		// Posting login request
	    //
		final String url = String.format(LOGIN_URL, site.getUrl());
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(POST_VAR_LOGIN, site.getUsername()));
        nameValuePairs.add(new BasicNameValuePair(POST_VAR_PASSWORD, site.getPassword()));
        nameValuePairs.add(new BasicNameValuePair(POST_VAR_REMEMBER, POST_VAR_REMEMBER_VALUE));
        nameValuePairs.add(new BasicNameValuePair(POST_VAR_SUBMIT_LOGIN, ""));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        
	    HttpResponse response = PageDownloadManager.INSTANCE.getHttpClient().execute(httpPost, PageDownloadManager.INSTANCE.getLocalContext());
	    StatusLine statusLine = response.getStatusLine();
	    final int status = statusLine.getStatusCode();
	    
        // Close the connection
        try {
			response.getEntity().getContent().close();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	    if(status != HttpStatus.SC_OK || !isLoggedIn(site)) {
	    	throw new StreetDroidException(R.string.login_error);
	    } 
	}

	private boolean isLoggedIn(Site site) {
		List<Cookie> cookies = PageDownloadManager.INSTANCE.getCookieStore().getCookies();
    	if (cookies.isEmpty()) {
    		return false;
    	}
    	
    	Iterator<Cookie> iterator = cookies.iterator();
    	while (iterator.hasNext()) {
    		Cookie cookie = iterator.next();
    		if (cookie.getName().equals(LOGGED_IN_COOKIE_NAME) &&
    			cookie.getDomain().contains(site.getUrl())) { // TODO: dangerous, some site names can be parts of other site names
    			// logged in OK
    			return true;
    		}
    	}

		return false;
	}

	
	private String doTopicVoteRequest(int vote, ITopic topic) throws ClientProtocolException, IOException {

		HttpResponse response = null;
		ILiveStreetVersion version = null;
		for (int i = 0; i < m_versions.length; i++) {
			response = m_versions[i].doTopicVoteReqest(vote, topic);
		    StatusLine statusLine = response.getStatusLine();
		    if (statusLine.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
		    	version = m_versions[i];
		    	break;
		    }
		    response.getEntity().getContent().close();
		}
		
		if (version == null) {
			return "Ошибка: Неизвестная версия LiveStreet";
		}
		
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getEntity().writeTo(out);
        out.close();
        final String responseString = out.toString();
        final String message = version.parseVotingResponse(responseString);
        return message;
	}

	@Override
	protected String doInBackground(Integer... arg0) {
		try {
			login(m_topic.getSite());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (StreetDroidException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return doTopicVoteRequest(m_vote, m_topic);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Неизвестная ошибка";
	}
	
	@Override
	protected void onPostExecute(String result) {
		m_callback.OnVotingResult(result);
	}	
}
