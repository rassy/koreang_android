package jp.co.iworks.koreang.web;

import static jp.co.iworks.koreang.Const.BASE_HOST;
import static jp.co.iworks.koreang.Const.PREF_NAME_APP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import jp.co.iwork.koreang.util.CommonUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class HttpRequest {
	private CommonUtils mUtils;
	private String uuid;
	
	public HttpRequest(Context context) {
		mUtils = new CommonUtils(context);
		uuid = mUtils.getSharedPrefsValue(PREF_NAME_APP, "uuid");
	}
	public void get(String url, List<BasicNameValuePair> parameters, final HttpResponseHandler handler) {
		String newUrl = url;
		if (parameters != null) {
			for (NameValuePair param : parameters) {
				if (newUrl.indexOf("?")>=0) {
					newUrl += "&";
				} else {
					newUrl += "?";
				}
				try {
					newUrl +=  param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		Log.d(this.toString(), "url=" + newUrl);
		final HttpGet request = new HttpGet(newUrl);
		try {
			execute(request, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void post(String url, List<BasicNameValuePair> parameters, final HttpResponseHandler handler) {
		HttpPost request = new HttpPost(url);
		try {
			if (parameters != null) {
				request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			}
			execute(request, handler);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	private void execute(final HttpUriRequest request, final HttpResponseHandler handler) {
		final Handler mainHandler = new Handler();
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		
		
		BasicClientCookie cookie = new BasicClientCookie("CakeCookie[uuid]", uuid);
		cookie.setDomain(BASE_HOST);
		cookie.setPath("/");
		httpClient.getCookieStore().addCookie(cookie);
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					final String result = httpClient.execute(request, new ResponseHandler<String>() {
	
						@Override
						public String handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							Log.d(this.toString(), response.toString());
							switch(response.getStatusLine().getStatusCode()) {
							case HttpStatus.SC_OK:
								return EntityUtils.toString(response.getEntity(), "UTF-8");
							case HttpStatus.SC_NOT_FOUND:
								throw new RuntimeException("Data not found.");
							default:
								Log.e(this.toString(), String.valueOf(response.getStatusLine().getStatusCode()));
								throw new RuntimeException("Unexpected error.");
							}
						}
					});
					mainHandler.post(new Runnable(){
						@Override
						public void run() {
							handler.onSuccess(result);		
						}
					});
					
				} catch (final Exception e) {
					mainHandler.post(new Runnable(){
						@Override
						public void run() {
							handler.onFailure(e);		
						}
					});
				} finally {
					httpClient.getConnectionManager().shutdown();
				}
			}
		}).start();
	}
}
