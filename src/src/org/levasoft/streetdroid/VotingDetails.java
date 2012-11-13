package org.levasoft.streetdroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to extract information needed for voting for the topic from the 
 * page HTML.
 *
 */
public class VotingDetails {
	// Regular expression used for extracting LiveStreet security key from HTML text
	private static final String REGEXP_SECURITY_KEY = "LIVESTREET_SECURITY_KEY.*'([0-9a-f]*)'";
	
	// Regular expression used for extracting topic id from HTML text
	private static final String REGEXP_VOTE_TOPIC_ID = "vote\\((\\d+),this,1,'topic'\\);";
	
	private String m_securityKey = "";
	private String m_topicId = "";;


	/**
	 * Parses information needed for voting from the page HTML text
	 * @param topicText - page HTML text
	 * @throws StreetDroidException
	 */
	public void parseVotingDetails(String topicText) throws StreetDroidException {
		parseSecurityKey(topicText);
		parseVoteTopicId(topicText);
	}
	
	/**
	 * Extracts current topic ID from the page HTML text
	 * @param topicText - page HTML text
	 * @throws StreetDroidException
	 */
	private void parseVoteTopicId(String topicText) throws StreetDroidException {
		Pattern pattern = Pattern.compile(REGEXP_VOTE_TOPIC_ID, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(topicText);
		boolean matchFound = matcher.find();
		if (!matchFound || matcher.groupCount() < 1) {
			throw new StreetDroidException(R.string.error_voting_page_parse);
		}
		
		m_topicId  = matcher.group(1);
	}

	/**
	 * Extracts LiveStreet security key from the page HTML text
	 * @param topicText - page HTML text
	 * @throws StreetDroidException
	 */
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

	/**
	 * @return Current topic id
	 */
	public String getVotingTopicId() {
		return m_topicId;
	}
	
	/**
	 * @return Current LiveStreet security key
	 */
	public String getVotingLSSecureKey() {
		return m_securityKey;
	}
}
