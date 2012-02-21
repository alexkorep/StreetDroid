package org.levasoft.streetdroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import android.content.Context;

public class TopicFormatter {
	private final Context m_context;
	private static String m_template = ""; 
	
	public TopicFormatter(Context context) {
		m_context = context;
	}
	
	static private String readString(InputStream inputStream) throws IOException {
		char[] buf = new char[2048];
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
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

	private String getTemplate(final String fileName) {
		try {
			m_template = readString(m_context.getAssets().open(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return m_template;
	}

	public String format(ITopic topic) {
		String template = getTemplate("topic.html");
		Template tmpl = Mustache.compiler().escapeHTML(false).compile(template);
		final String htmlText = tmpl.execute(topic);
		return htmlText;
	}

	public String formatTopicList(final ITopic[] topicList) {
		String template = getTemplate("topicList.html");
		Template tmpl = Mustache.compiler().escapeHTML(false).compile(template);
		final String htmlText = tmpl.execute(new Object() {
		    @SuppressWarnings("unused")
			Object topics = Arrays.asList(topicList);
		});
		return htmlText;
	}
}
