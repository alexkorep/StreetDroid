package org.levasoft.streetdroid;

public interface ITopic {
	String getTitle();

	String getAuthor();

	String getBlog();

	String getBlogUrl();

	String getContent();
	
	String getDateTime();
	
	IComment[] getComments();
	
	boolean getDownloadComplete();
	
	VotingDetails getVotingDetails();
	
	Site getSite();
}
