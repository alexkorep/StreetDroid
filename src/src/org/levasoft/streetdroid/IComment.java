package org.levasoft.streetdroid;

/**
 * Topic comment interface 
 *
 */
public interface IComment {
	
	/**
	 * Returns comment ID, e.g. comment123
	 * This ID is used as a anchor on the HTML page, e.g. if id is comment123, then
	 * anchor can be http://example.com/blog/111.html#comment123
	 */
	public String getId();
	
	/**
	 * Returns comment author user name
	 */
	public String getAuthor();
	
	/**
	 * Returns comment HTLM text
	 */
	public String getText();
	
	/**
	 * Returns comment author profile URL, e.g. http://example.com/profile/username/
	 */
	public String getAuthorUrl();
	
	/**
	 * Returns comment level. Returns 0 for top-level comments, 1 for second level, etc.
	 */
	public int getLevel();
	
	/**
	 * Returns comment date and time in format as it should be displayed for output.
	 */
	public String getDateTime();
}
