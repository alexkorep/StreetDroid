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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mobilelite.annotation.Service;
import org.mobilelite.annotation.ServiceMethod;

public class ServiceBeanDefinition {

	private String name;
	
	private List<String> methodNames = new ArrayList<String>();
	
	public static ServiceBeanDefinition newInstance(String name, Object bean) {
		if (bean.getClass().isAnnotationPresent(Service.class)) {
			// is service object, getting its bean definition
			return new ServiceBeanDefinition(name, bean);
		} else {
			return null;
		}
	}
	
	private ServiceBeanDefinition(String name, Object bean) {
		this.name = name;
		initMethodNames(bean);
	}
	
	private void initMethodNames(Object bean) {
		@SuppressWarnings("rawtypes")
		Class clazz = bean.getClass();
		while(clazz != null) {
			Method[] methods = clazz.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].isAnnotationPresent(ServiceMethod.class)) {
					// is service method, need to be exposed to definition
					String methodName = methods[i].getName();
					if (!methodNames.contains(methodName)) {
						methodNames.add(methods[i].getName());
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	public String getName() {
		return name;
	}

	public List<String> getMethodNames() {
		return Collections.unmodifiableList(methodNames);
	}

}
