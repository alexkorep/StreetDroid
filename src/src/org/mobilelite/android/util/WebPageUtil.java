package org.mobilelite.android.util;

import org.mobilelite.android.WebPage;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

public final class WebPageUtil {

	private WebPageUtil() {}
	
	public static void showProgressDialogWhenLoadingUrl(Context context, WebPage webPage, String title, String message) {
		showProgressDialogWhenLoadingUrl(context, webPage.getWebView(), title, message);
	}
	
	public static void showProgressDialogWhenLoadingUrl(Context context, final WebView webView, String title, String message) {
		final ProgressDialog progDialog = ProgressDialog.show(context, title, message, true);
		new Thread(new Runnable() {
			
			private static final long INIT_TO_LOADING_TIMEOUT = 5000;
			private static final long RECHECK_INTERAL = 100;
			private static final int STATUS_INIT = 0;
			private static final int STATUS_LOADING = 1;
			private static final int STATUS_DONE = 2;
			
			long loadingPeriod = 0;
			int status = STATUS_INIT;
			
			@Override
			public void run() {
				Log.d(WebPageUtil.class.getSimpleName(), "progress BEFORE waiting: " + webView.getProgress());
				
				while (status != STATUS_DONE) {
					try {
						Thread.sleep(RECHECK_INTERAL);
						loadingPeriod += RECHECK_INTERAL;
					} catch (InterruptedException e) {
						Log.e("WebPageUtil-showProgressDialogWhenLoadingUrl", e.getMessage());
					}
					
					int currentProgress = webView.getProgress();
					Log.d(WebPageUtil.class.getSimpleName(), "progress DURING waiting: " + currentProgress);
					
					// check if progress is always 100% because of the extremely short period of time of loading
					if (currentProgress == 100) {
						if (status == STATUS_LOADING) {
							// done loading
							status = STATUS_DONE;
						} else if (status == STATUS_INIT && loadingPeriod > INIT_TO_LOADING_TIMEOUT) {
							status = STATUS_DONE;
						}
					} else {
						// during loading
						status = STATUS_LOADING;
					}
				}
				Log.d(WebPageUtil.class.getSimpleName(), "dismissed progDialog while the progress exceed 100: " + webView.getProgress());
				progDialog.dismiss();
			}
		}).start();
	}
	
}
