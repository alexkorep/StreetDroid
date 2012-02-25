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

import android.webkit.WebView;

public class WebPage {
	
	private BeanActionDispatcher dispatcher;

	public WebPage(WebView view) {
		//dispatcher = factory.get(view);
		dispatcher = new BeanActionDispatcher(view);
	}

	public void definePageBean(String name, Object bean) {
		dispatcher.definePageBean(name, bean);
	}
	
	public void loadUrl(String url) {
		dispatcher.loadUrl(url);
	}
	
	public void loadDataWithBaseURL(String baseUrl, String data, String miniType, String encoding, String historyUrl) {
		dispatcher.loadDataWithBaseURL(baseUrl, data, miniType, encoding, historyUrl);
	}
	
	public WebView getWebView() {
		return dispatcher.getWebView();
	}

}
