package jp.co.iworks.koreang.web;

import jp.co.iwork.koreang.util.CommonUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import static jp.co.iworks.koreang.Const.PREF_NAME_COOKIE;

public class WebViewActivity extends Activity {
	private CommonUtils commonUtils;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		commonUtils = new CommonUtils(this);		
		LinearLayout layout = new LinearLayout(this);
		setContentView(layout, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		WebView webView = new WebView(this);
		layout.addView(webView);
		webView.setWebViewClient(new MyWebViewClient(this));
		webView.loadUrl(url);
	}
	private class MyWebViewClient extends WebViewClient {

		private ProgressDialog progressDialog;
		private Activity activity;
		public MyWebViewClient(Activity activity) {
			super();
			progressDialog = null;
			this.activity = activity;
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			super.shouldOverrideUrlLoading(view, url);
			setCookie(url);
			Uri uri = Uri.parse(url);
			String scheme = uri.getScheme();
			int port = uri.getPort();
			if (scheme.equals("koreang") && port == 80) {
				String action = uri.getQueryParameter("action");
				if (action.equals("close")) {
					String error = uri.getQueryParameter("error");
					if (error != null) {
						this.activity.setResult(99);
					} else {
						this.activity.setResult(RESULT_OK);
					}
					this.activity.finish();
					return true;
				}
			}
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			setCookie(url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(view.getContext());
				progressDialog.setMessage("Now Loading...");
			}
			progressDialog.show();
		}
		/**
		 * クッキーを永続化する
		 * @param url
		 */
		private void setCookie(String url) {
			String cookie = CookieManager.getInstance().getCookie(url);
			if (cookie != null) {
				Log.d(this.toString(), cookie);
				String[] cookies = cookie.split(";");
				for (String keyValue : cookies) {
					keyValue = keyValue.trim();
					String[] cookieSet = keyValue.split("=");
					commonUtils.putSharedPrefsValue(PREF_NAME_COOKIE, cookieSet[0], cookieSet[1]);
				}
			}
		}
	}
	
}
