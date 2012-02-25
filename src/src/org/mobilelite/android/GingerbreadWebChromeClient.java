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

//import java.net.URLDecoder;

import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GingerbreadWebChromeClient extends LiteWebChromeClient {

	private BeanActionDispatcher dispatcher;

	public GingerbreadWebChromeClient(BeanActionDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		if (message != null && message.startsWith(MobileLiteConstants.PROTOCOL_MOBILELITE)) {
			Log.d("request", url);
			//String request = URLDecoder.decode(url.substring(MobileLiteConstants.PROTOCOL_MOBILELITE.length()));
			String request = message.substring(MobileLiteConstants.PROTOCOL_MOBILELITE.length());
			Log.d("decoded request", request);
			
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonParam = jsonParser.parse(request);
			
			if (jsonParam.isJsonNull() || jsonParam.isJsonPrimitive()) {
				Log.e("Invoke Bean Action", "request should be in format {bean:'beanName', method:'methodName', params:[], callback:'callback string' }");
				return true;
			}
			
			String beanName = null, methodName = null, callback = null;
			JsonElement params = null;
			boolean requestParsed = true;
			if (jsonParam.isJsonArray()) {
				JsonArray ja = jsonParam.getAsJsonArray();
				if (ja.size()  == 4) {
					beanName = ja.get(1).getAsString();
					methodName = ja.get(2).getAsString();
					params = ja.get(3);
					callback = ja.get(4).getAsString();
				} else {
					Log.e("Invoke Bean Action", "request should have 4 element");
					requestParsed = false;
				}
			} else if (jsonParam.isJsonObject()) {
				JsonObject jo = jsonParam.getAsJsonObject(); 
				if (jo.has(MobileLiteConstants.PARAM_KEY_BEAN)) 
					beanName = jo.get(MobileLiteConstants.PARAM_KEY_BEAN).getAsString();
				else {
					Log.e("Invoke Bean Action", "request should have 'bean' element");
					requestParsed = false;
				}

				if (jo.has(MobileLiteConstants.PARAM_KEY_METHOD)) 
					methodName = jo.get(MobileLiteConstants.PARAM_KEY_METHOD).getAsString();
				else {
					Log.e("Invoke Bean Action", "request should have 'method' element");
					requestParsed = false;
				}

				if (jo.has(MobileLiteConstants.PARAM_KEY_PARAMS)) 
					params = jo.get(MobileLiteConstants.PARAM_KEY_PARAMS);
				else {
					Log.e("Invoke Bean Action", "request should have 'params' element");
					requestParsed = false;
				}

				if (jo.has(MobileLiteConstants.PARAM_KEY_CALLBACK)) { 
					JsonElement callbackJson = null;
					callbackJson = jo.get(MobileLiteConstants.PARAM_KEY_CALLBACK);
					if (callbackJson.isJsonPrimitive())
						callback = callbackJson.getAsString();
				}
				else {
					Log.e("Invoke Bean Action", "request should have 'callback' element");
					requestParsed = false;
				}
			}
			
			if (requestParsed) {
				dispatcher._invokeBeanAction(beanName, methodName, params, callback);
			}
			
			result.confirm();
			return true;
		}
		return super.onJsAlert(view, url, message, result);
	}

}
