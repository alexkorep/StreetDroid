/*
 * Copyright (C) 2011 Tony.Ni, Jim.Jiang http://mobilelite.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mobilelite.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mobilelite.annotation.Service;

import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class BeanActionDispatcher {
	
	/** The web view which need to communicate with js. */
	protected WebView webView;
	
	protected Map<String, ServiceBean> beans = new HashMap<String, ServiceBean>();
	
	protected Gson gson = new Gson();
	
	protected boolean webViewInitialized;
	
	public BeanActionDispatcher(WebView webView) {
		this.webView = webView;
		// don't init webview here for unit test reason
	}
	
	public void invokeBeanAction(String beanName, String methodName, String params, String callback) {
//		Log.d("BeanAction", beanName);
//		Log.d("methodName", methodName);
//		Log.d("params", params);
//		Log.d("callback", callback == null? "null" : callback);
		
		try {
			JsonParser jsonParser = new JsonParser();
			JsonElement je = jsonParser.parse(params);
			
			_invokeBeanAction(beanName, methodName, je, callback);
		} catch (JsonSyntaxException e) {
			Log.e("invokeBeanAction", "json parameter format error", e);
		}
	}

	/*
	void _invokeBeanAction(String beanName, String methodName, JsonElement jsonParams, String callback) {
		ServiceBean bean = beans.get(beanName);
		if (bean == null) {
			Log.e("invokeBeanAction", "service bean not exists");
		}
		try {
			Object result = bean.invoke(methodName, jsonParams);
			if (callback != null) {
//				Log.d("invokeBeanAction", "before gson to json: " + result);
				if (result != null) {
					result = gson.toJson(result);
				}
//				Log.d("invokeBeanAction", "after gson to json: " + result);
				//callback.replaceAll("\\", "%5c");
				webView.loadUrl("javascript:mobileLite.doCallback(" + result + ", " + callback + ")");

			}
		} catch (SecurityException e) {
		} catch (JsonSyntaxException e) {
		} catch (IllegalArgumentException e) {
		}
	}
	*/
	
	void _invokeBeanAction(String beanName, String methodName, JsonElement jsonParams, String callback) {
		ServiceBean bean = beans.get(beanName);
		if (bean == null) {
			Log.e("invokeBeanAction", "service bean not exists");
		}
		try {
			bean.invoke(webView, methodName, jsonParams, callback);
		} catch (SecurityException e) {
		} catch (JsonSyntaxException e) {
		} catch (IllegalArgumentException e) {
		}
	}

	public void definePageBean(String name, Object bean) {
		if (bean.getClass().isAnnotationPresent(Service.class)) {
			beans.put(name, new ServiceBean(name, bean));
		}
	}
	
	/*
	public void onPageReady() {
		//webView.loadUrl("javascript:liteEngine.initBeanProxy(" + gson.toJson(beans.keySet()) + ")");
		Log.d("onPageReady:", gson.toJson(getServiceBeanDefinitions()));
   		//Toast.makeText(webView.getContext(), "test", 200).show();
		webView.loadUrl("javascript:mobileLite.initBeans(" + gson.toJson(getServiceBeanDefinitions()) + ")");
	}
	*/
	
	List<ServiceBeanDefinition> getServiceBeanDefinitions() {
		List<ServiceBeanDefinition> defs = new ArrayList<ServiceBeanDefinition>();
		for (ServiceBean serviceBean : beans.values()) {
			defs.add(serviceBean.getServiceBeanDefinition());
		}
		return defs;
	}
	
	String getServiceBeanDefinitionJson() {
		return gson.toJson(getServiceBeanDefinitions());
	}
	
	public void loadUrl(String url) {
		initWebView();
		webView.loadUrl(url);
	}

	public void loadDataWithBaseURL(String baseUrl, String data, String miniType, String encoding, String historyUrl) {
		initWebView();
		webView.loadDataWithBaseURL(baseUrl, data, miniType, encoding, historyUrl);
	}
	
	private void initWebView() {
		if (webViewInitialized) return;
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new LiteWebViewClient(this));
		if (Build.VERSION.RELEASE.startsWith("2.3")) {
			//webView.setWebViewClient(new GingerbreadWebViewClient(this));
			webView.setWebChromeClient(new GingerbreadWebChromeClient(this));
		} else {
			//webView.setWebViewClient(new LiteWebViewClient(this));
			webView.addJavascriptInterface(this, "_mobileLiteProxy_");
			webView.setWebChromeClient(new LiteWebChromeClient());
		}
		webViewInitialized = true;
	}
	
	public WebView getWebView() {
		return webView;
	}
	
}
