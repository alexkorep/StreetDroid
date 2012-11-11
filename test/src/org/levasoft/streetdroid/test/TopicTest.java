package org.levasoft.streetdroid.test;

import org.levasoft.streetdroid.Site;
import org.levasoft.streetdroid.Topic;

import junit.framework.TestCase;

/**
 * Topic-related test cases
 * @author alexey
 *
 */
public class TopicTest extends TestCase {
	/**
	 * Tests extracting image from the topic text
	 */
	public void testFrontUrl() {
		Topic topic = new Topic("", new Site(""));
		topic.setContent("a;sgjasdlg <img width=12 src=\"http://hello.com\" height=\"6\">");
		final String url = topic.getFrontImageUrl();
		assertEquals(url, "http://hello.com");
	}
}
