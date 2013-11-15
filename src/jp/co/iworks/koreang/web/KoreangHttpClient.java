package jp.co.iworks.koreang.web;

import static jp.co.iworks.koreang.Const.BASE_HOST;
import static jp.co.iworks.koreang.Const.BASE_URL;
import jp.co.iworks.koreang.util.CommonUtils;

import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

public class KoreangHttpClient {

  private static AsyncHttpClient client = new AsyncHttpClient();

  private static void setCookie(Context context) {
	 	String uuid = CommonUtils.getUuid();
		BasicClientCookie cookie = new BasicClientCookie("CakeCookie[uuid]", uuid);
		cookie.setDomain(BASE_HOST);
		cookie.setPath("/");
		PersistentCookieStore cookieStore = new PersistentCookieStore(context);
		cookieStore.addCookie(cookie);
		client.setCookieStore(cookieStore);
  }
  public static void get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  setCookie(context);
      client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  setCookie(context);
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }
}
