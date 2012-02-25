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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mobilelite.annotation.ServiceMethod;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

public class ServiceBean {

	private String name;
	@Expose(serialize = false, deserialize = false)
	private Object bean;

	public ServiceBean(String name, Object bean) {
		this.name = name;
		this.bean = bean;
	}

	public ServiceBeanDefinition getServiceBeanDefinition() {
		return ServiceBeanDefinition.newInstance(name, bean);
	}

	private Method[] getBeanMethods(String methodName, int paramNum) {
		List<Method> methods = new ArrayList<Method>();
		@SuppressWarnings("rawtypes")
		Class clazz = bean.getClass();
		while(clazz != null) {
			Method[] beanMethods = clazz.getDeclaredMethods();
			for (Method method : beanMethods) {
				if (method.isAnnotationPresent(ServiceMethod.class) && method.getName().equals(methodName)
						&& method.getParameterTypes().length == paramNum)
					methods.add(method);
			}
			clazz = clazz.getSuperclass();
		}
		return methods.toArray(new Method[] {});
	}

	@SuppressWarnings("unchecked")
	public void invoke(WebView webView, String methodName, JsonElement jsonParam, String callback) {
		Gson gson = new Gson();
		JsonElement je = jsonParam;

		JsonArray jaParams = null;
		if (je.isJsonArray()) {
			jaParams = je.getAsJsonArray();
		} else if (je.isJsonObject() || je.isJsonPrimitive()) {
			jaParams = new JsonArray();
			jaParams.add(je);
		} else if (je.isJsonNull()) {
			jaParams = new JsonArray();
		}

		Method[] methods = getBeanMethods(methodName, jaParams.size());
		for (Method method : methods) {
			@SuppressWarnings("rawtypes")
			Class[] paramClasses = method.getParameterTypes();
			List<Object> params = new ArrayList<Object>();
			try {
				for (int i = 0; i < paramClasses.length; i++) {
					params.add(gson.fromJson(jaParams.get(i), paramClasses[i]));
				}

				ServiceMethod serviceMethod = method.getAnnotation(ServiceMethod.class);
				if (serviceMethod.showDialog()|| serviceMethod.execAsync()) {
					executeMethodInDialog(bean, method, params, serviceMethod, webView, callback);
				} else {
					executeMethod(bean, method, params, webView, callback);
				}
				break;
			} catch (JsonParseException e) { 
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

	}

	private void executeMethodInDialog(final Object bean, final Method method, final List<Object> params, final ServiceMethod serviceMethod,
			final WebView webView, final String callback) {
		AsyncTask<Object, Integer, Object> asyncTask = new AsyncTask<Object, Integer, Object>() {
			ProgressDialog progDialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if(serviceMethod.showDialog()) {
					progDialog = ProgressDialog.show(webView.getContext(), serviceMethod.title(), serviceMethod.message(), true);
				}
			}

			@Override
			protected Object doInBackground(Object... arg0) {
				try {
					return method.invoke(bean, params.toArray());
				} catch (SecurityException e) {
				} catch (JsonSyntaxException e) {
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				super.onPostExecute(result);
				if(serviceMethod.showDialog()) {
					progDialog.dismiss();
				}
				if (callback != null) {
//					Log.d("invokeBeanAction", "before gson to json: " + result);
					if (result != null) {
						result = (new Gson()).toJson(result);
					}
//					Log.d("invokeBeanAction", "after gson to json: " + result);
					//callback.replaceAll("\\", "%5c");
					//webView.loadUrl("javascript:mobileLite.doCallback(" + result + ", " + callback + ")");

					final Object data = result;
					webView.post(new Runnable() {
						@Override
						public void run() {
							webView.loadUrl("javascript:mobileLite.doCallback(" + data + ", " + callback + ")");
						}
					});
				}
			}

		};
		asyncTask.execute();
	}

	private void executeMethod(Object bean, Method method, List<Object> params, final WebView webView, final String callback) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Object result = method.invoke(bean, params.toArray());
		if (callback != null) {
//			Log.d("invokeBeanAction", "before gson to json: " + result);
			if (result != null) {
				result = (new Gson()).toJson(result);
			}
//			Log.d("invokeBeanAction", "after gson to json: " + result);
			//callback.replaceAll("\\", "%5c");
			//webView.loadUrl("javascript:mobileLite.doCallback(" + result + ", " + callback + ")");

			final Object data = result;
			webView.post(new Runnable() {
				@Override
				public void run() {
					webView.loadUrl("javascript:mobileLite.doCallback(" + data + ", " + callback + ")");
				}
			});

		}
	}

}
