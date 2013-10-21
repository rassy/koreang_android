package jp.co.iworks.koreang.web;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;
import static jp.co.iworks.koreang.Const.URL_USER_CHECK_LOGIN_STATUS;
import static jp.co.iworks.koreang.Const.URL_USER_LOGIN_TOKEN;
import static jp.co.iworks.koreang.Const.URL_TIMETABLE_INDEX;

import jp.co.iwork.koreang.util.CommonUtils;
import jp.co.iworks.koreang.APIResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


public class WebAPI {
	private Context mContext;
	public WebAPI (Context context) {
		mContext = context;
	}
	/**
	 * ユーザー認証します
	 */
	public void authUser(final APIResponseHandler handler) {
		new HttpRequest(mContext).post(CommonUtils.getUrl(URL_USER_CHECK_LOGIN_STATUS + ".json"), null, new HttpResponseHandler(){
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject json = new JSONObject(result);
					if (json.getBoolean("status")) {
						JSONObject user = json.getJSONObject("User");
						handler.onRespond(user);
					} else {
						handler.onRespond(null);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					handler.onRespond(null);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(null);
			}
		});
	}
    /**
     * トークンを用いてログインする
     * @param token ログイントークン
     * @param handler 結果を受け取るハンドラ
     * @return
     */
	public void loginByToken(String token, final APIResponseHandler handler) {
    	new HttpRequest(mContext).get(CommonUtils.getUrl(URL_USER_LOGIN_TOKEN + "/" + token), null, new HttpResponseHandler(){
			@Override
			public void onSuccess(String result) {
				JSONObject json;
				try {
					json = new JSONObject(result);
					String userString = json.getString("User");
					JSONObject user = new JSONObject(userString);
					handler.onRespond(user);
				} catch (JSONException e) {
					e.printStackTrace();
					handler.onRespond(null);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(null);
			}
       	});
    }
	public void getTimeTable(final APIResponseHandler handler) {
		new HttpRequest(mContext).get(CommonUtils.getUrl(URL_TIMETABLE_INDEX), null, new HttpResponseHandler() {

			@Override
			public void onSuccess(String result) {
				JSONObject json;
				try {
					json = new JSONObject(result);
					handler.onRespond(json);
				} catch (JSONException e) {
					e.printStackTrace();
					handler.onRespond(null);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(null);
			}
			
		});
	}
}