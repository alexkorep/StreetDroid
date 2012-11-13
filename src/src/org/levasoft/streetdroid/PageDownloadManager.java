package org.levasoft.streetdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Simple wrapper on HTTP classes to download HTML file from given URL.
 * Keeps HTTP context and cookies store inside.
 * Implemented as a singleton in order to keep cookies and HTTP 
 * context in a single place.
 *
 */
public class PageDownloadManager {

	// One and only instance
	public static final PageDownloadManager INSTANCE = new PageDownloadManager();

	private final BasicHttpContext m_localContext;
	private final DefaultHttpClient m_httpclient;
	private final BasicCookieStore m_cookieStore;

	/**
	 * Constructor.
	 * It's private since the class is singleton and is not supposed to have any
	 * instances other than INSTANCE.
	 */
	private PageDownloadManager() {
		m_cookieStore = new BasicCookieStore();
		m_localContext = new BasicHttpContext();
		m_localContext.setAttribute(ClientContext.COOKIE_STORE, m_cookieStore);		
		m_httpclient = new DefaultHttpClient();
	}

	/**
	 * Downloads the HTML page from the given URL
	 * @param url - URL to download file from
	 * @return downloaded file content.
	 * @note This is a blocking i/o operation and will block current thread until download complete. 
	 */
	public String download(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = m_httpclient.execute(httpGet, m_localContext);
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				return responseString;
			}
		} catch (ClientProtocolException e) {
			// TODO need to handle it
			e.printStackTrace();
		} catch (IOException e) {
			// TODO need to handle it
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * returns HttpClient object
	 */
	public HttpClient getHttpClient() {
		return m_httpclient;
	}

	/**
	 * Returns local HTTP context
	 */
	public HttpContext getLocalContext() {
		return m_localContext;
	}

	/**
	 * Returns cookie store
	 */
	public CookieStore getCookieStore() {
		return m_cookieStore;
	}
}
