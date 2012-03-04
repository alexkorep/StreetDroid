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

public class PageDownloadManager {

	public static final PageDownloadManager INSTANCE = new PageDownloadManager();

	private final BasicHttpContext m_localContext;
	private final DefaultHttpClient m_httpclient;
	private final BasicCookieStore m_cookieStore; 

	private PageDownloadManager() {
	    m_cookieStore = new BasicCookieStore();
	    m_localContext = new BasicHttpContext();
	    m_localContext.setAttribute(ClientContext.COOKIE_STORE, m_cookieStore);		
		m_httpclient = new DefaultHttpClient();
		
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public HttpClient getHttpClient() {
		return m_httpclient;
	}

	public HttpContext getLocalContext() {
		return m_localContext;
	}

	public CookieStore getCookieStore() {
		return m_cookieStore;
	}
}
