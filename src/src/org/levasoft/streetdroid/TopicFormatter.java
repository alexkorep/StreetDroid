package org.levasoft.streetdroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import android.content.Context;

/**
 * Class to format topic for displaying on screen. Since we are displaying the full topic content in the web view, we need to
 * format topic as HTML text.
 * Topic is formatted using HTML template, which is located in assets/topic.html.
 * Mustache (http://mustache.github.com/) template engine is used to format the topic.
 *
 */
public class TopicFormatter {
	// Template file name, located in "assets" dir.
	private final static String templateFileName = "topic.html";
	
	// Context class, used to get input file stream from template file.
	private final Context m_context;
	
	// Template file content
	private static String m_template = "";
	
	/**
	 * Constructor
	 * @param context
	 */
	public TopicFormatter(Context context) {
		m_context = context;
	}
	
	/**
	 * Reads full input stream content into the string.
	 * @param inputStream - stream to read
	 * @return result string
	 * @throws IOException
	 */
	static private String readString(InputStream inputStream) throws IOException {
		final int bufferSize = 2048;			// Intermediate buffer size
		final String streamCharset = "UTF-8";	// Input stream character set 
		
		char[] buf = new char[bufferSize];
		Reader reader = new InputStreamReader(inputStream, streamCharset);
		StringBuilder stringBuilder = new StringBuilder();
		while (true) {
			int n = reader.read(buf);
			if (n < 0) {
				break;
			}
			stringBuilder.append(buf, 0, n);
		}
		return stringBuilder.toString();
	}

	
	/**
	 * Getting text content of the template file
	 * @param fileName - template file name, must be located in assets folder.
	 * @return template file content
	 */
	private String getTemplate(final String fileName) {
		try {
			m_template = readString(m_context.getAssets().open(fileName));
		} catch (IOException e) {
			// No error handling since this can never be removed/modified by user.
			e.printStackTrace();
		}
		
		return m_template;
	}

	/**
	 * Formats topic for displaying within the web view.
	 * @param topic - topic to format
	 * @return string representing formatted topic
	 */
	public String format(ITopic topic) {
		String template = getTemplate(templateFileName);
		Template tmpl = Mustache.compiler().escapeHTML(false).compile(template);
		final String htmlText = tmpl.execute(topic);
		return htmlText;
	}
}
