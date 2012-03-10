package org.levasoft.streetdroid.test;

import org.levasoft.streetdroid.Site;
import org.levasoft.streetdroid.Topic;

import junit.framework.TestCase;

public class TopicTest extends TestCase {
	public void testFrontUrl() {
		Topic topic = new Topic("", new Site(""));
		topic.setContent("a;sgjasdlg <img width=12 src=\"http://hello.com\" height=\"6\">");
		final String url = topic.getFtontImageUrl();
		assertEquals(url, "http://hello.com");
	}
}
