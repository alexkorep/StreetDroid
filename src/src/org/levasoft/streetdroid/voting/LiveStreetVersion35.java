package org.levasoft.streetdroid.voting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.levasoft.streetdroid.ITopic;
import org.levasoft.streetdroid.PageDownloadManager;

import android.util.Log;

public class LiveStreetVersion35 implements ILiveStreetVersion {

	private static final String VOTE_TOPIC_URL = "http://%s/include/ajax/voteTopic.php?JsHttpRequest=0-xml";
	private static final String VOTE_TOPIC_PARAM_VOTE = "value";
	private static final String VOTE_TOPIC_PARAM_TOPIC_ID = "idTopic";
	private static final String VOTE_TOPIC_PARAM_SECURITY_KEY = "security_ls_key";


	@Override
	public HttpResponse doTopicVoteReqest(int vote, ITopic topic) throws ClientProtocolException, IOException {
	    Log.d("StreetDroid", "Security Key = " + topic.getVotingDetails().getVotingLSSecureKey());
		final String url = String.format(VOTE_TOPIC_URL, topic.getSite().getUrl());
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(VOTE_TOPIC_PARAM_VOTE, String.valueOf(vote)));
        nameValuePairs.add(new BasicNameValuePair(VOTE_TOPIC_PARAM_TOPIC_ID, topic.getVotingDetails().getVotingTopicId()));
        nameValuePairs.add(new BasicNameValuePair(VOTE_TOPIC_PARAM_SECURITY_KEY, topic.getVotingDetails().getVotingLSSecureKey()));
        //nameValuePairs.add(new BasicNameValuePair("%24family[name]", "hash")); // WTF?
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    HttpResponse response = PageDownloadManager.INSTANCE.getHttpClient().execute(httpPost, PageDownloadManager.INSTANCE.getLocalContext());
	    return response;
	}

	@Override
	public String parseVotingResponse(String responseString) {
        JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(responseString);
	        JSONObject js = jsonObject.getJSONObject("js");
	        final String title = js.getString("sMsgTitle");
	        final String msg = js.getString("sMsg");
	        return title + ": " + msg;
		} catch (JSONException e) {
			e.printStackTrace();
			return "Ошибка парсинга JSON ответа с сервера: " + responseString;
		}
	}

}
