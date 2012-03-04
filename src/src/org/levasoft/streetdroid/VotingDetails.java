package org.levasoft.streetdroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VotingDetails {
	private static final String REGEXP_SECURITY_KEY = "LIVESTREET_SECURITY_KEY.*'([0-9a-f]*)'";
	private static final String REGEXP_VOTE_TOPIC_ID = "vote\\((\\d+),this,1,'topic'\\);";
	
	private String m_securityKey = "";
	private String m_topicId = "";;


	public void parseVotingDetails(String topicText) throws StreetDroidException {
		parseSecurityKey(topicText);
		parseVoteTopicId(topicText);
		
	}
	
	private void parseVoteTopicId(String topicText) throws StreetDroidException {
		Pattern pattern = Pattern.compile(REGEXP_VOTE_TOPIC_ID, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(topicText);
		boolean matchFound = matcher.find();
		if (!matchFound || matcher.groupCount() < 1) {
			throw new StreetDroidException(R.string.error_voting_page_parse);
		}
		
		m_topicId  = matcher.group(1);
	}

	private void parseSecurityKey(String topicText) throws StreetDroidException {
		Pattern pattern = Pattern.compile(REGEXP_SECURITY_KEY, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(topicText);
		boolean matchFound = matcher.find();

		if (!matchFound || matcher.groupCount() < 1) {
			// not found
			throw new StreetDroidException(R.string.error_voting_page_parse);
		}
		
		m_securityKey = matcher.group(1);
	}

	public String getVotingTopicId() {
		return m_topicId;
	}
	
	public String getVotingLSSecureKey() {
		return m_securityKey;
	}
}
