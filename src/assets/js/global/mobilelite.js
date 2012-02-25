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

(function() {

var MobileLiteEngine = function(mobileLite) {
};

MobileLiteEngine.prototype = {
	constructor: MobileLiteEngine,
	callbackQueue: {},
	guid: 1,
	addCallback: function(callback) {
		var callbackId = "" + this.guid++;
		this.callbackQueue[callbackId] = callback;
		return callbackId;
	},
	doCallback: function(result, callbackId) {
		if(this.callbackQueue[callbackId]) {
			var callback = this.callbackQueue[callbackId];
			delete this.callbackQueue[callbackId];
			if(callback instanceof Function)
				callback(result);
		}
	},
	createLiteProxy: function(obj) {
		window[obj.name] = {
			name: obj.name,
			engine: this
		};
		for (methodName in obj.methodNames) {
			methodName = obj.methodNames[methodName];
			window[obj.name][methodName] = function() {
				var args = Array.prototype.slice.call(arguments);
				var callback = null;
				if (args.length >0 && args[args.length - 1] instanceof Function) {
					callback = args[args.length - 1];
					args = args.slice(0, args.length - 1);
				}
				
				this.engine.invokeBeanAction(this.name, arguments.callee.methodName, args, callback);
			}
			window[obj.name][methodName].methodName = methodName;
		}
	},
	invokeBeanAction: function(beanName, methodName, args, callback) {
		if (callback) {
			callback = this.addCallback(callback);
		}
		_mobileLiteProxy_.invokeBeanAction(beanName, methodName, JSON.stringify(args), callback);
	}
};

function MobileLiteObject () {
}

MobileLiteObject.prototype = {
	engine: new MobileLiteEngine(),
	initBeans: function(beans) {
		//alert("initBeans:start");
		if(!window["_mobileLiteProxy_"]) {
			this.engine.invokeBeanAction = function (beanName, methodName, args, callback) {
				if (callback) {
					callback = this.addCallback(callback);
				}
				_mobileLiteProxy_.invokeBeanAction(beanName, methodName, args, callback);
			};
			var thatEngine = this.engine;
			window._mobileLiteProxy_ = {
				invokeBeanAction: function (beanName, methodName, args, callback) {
					var obj = {
						bean: beanName,
						method: methodName,
						params: args,
						callback: callback
					};
					
					//var encodeRequest = JSON.stringify(obj).replace(/\\/g, "%5c");
					var encodeRequest = JSON.stringify(obj);
					alert("mobilelite:" + encodeRequest);
				}
			};
		}
		for (bean in beans) {
			this.engine.createLiteProxy(beans[bean]);
		}
		//alert("initBeans:end");
	},
	doCallback: function(result, callback) {
		//var cbFun = eval('(' + callback + ')' );
		//cbFun(result);
		this.engine.doCallback(result, callback);
	},
	newInstance: function() {
		return new MobileLiteObject();
	}
};

var mobileLite = new MobileLiteObject();

if (typeof exports !== 'undefined') exports.mobileLite = mobileLite;
else window.mobileLite = mobileLite;

//_mobileLiteProxy_.onPageReady();
//mobileLite.initBeans([{"methodNames":["hello", "readConfig"], "name":"bean"}]);
}) ();

