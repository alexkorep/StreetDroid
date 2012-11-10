package org.levasoft.streetdroid.test;


import org.levasoft.streetdroid.StreetDroidException;
import org.levasoft.streetdroid.VotingDetails;

import junit.framework.TestCase;

/**
 * Voting-related test cases
 * @author alexey
 *
 */
public class VotingDetailsTest extends TestCase {
	
	private VotingDetails m_votingDetails;


	protected void setUp() {
		m_votingDetails = new VotingDetails();
	}

	
	/**
	 * Tests extracting information required for voting from the page HTML (LiveStreet security key, topic id)
	 */
	public void testParseVotingDetails() {
		final String page = "		var LIVESTREET_SECURITY_KEY	= '62ff2cfc3090ab39af01b3a89aefac70';\n" +
				"<a href=\"#\" class=\"plus\" onclick=\"return ls.vote.vote(10221,this,1,'topic');\"></a>";
		try {
			m_votingDetails.parseVotingDetails(page);
		} catch (StreetDroidException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(m_votingDetails.getVotingTopicId(), "10221");
		assertEquals(m_votingDetails.getVotingLSSecureKey(), "62ff2cfc3090ab39af01b3a89aefac70");
	}
}
