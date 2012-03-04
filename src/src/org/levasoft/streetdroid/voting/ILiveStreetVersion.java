package org.levasoft.streetdroid.voting;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.levasoft.streetdroid.ITopic;

public interface ILiveStreetVersion {
	HttpResponse doTopicVoteReqest(int vote, ITopic topic) throws UnsupportedEncodingException, ClientProtocolException, IOException;

	String parseVotingResponse(String responseString);
}
