package jp.co.iworks.koreang.web;

import static jp.co.iworks.koreang.Const.URL_TEACHER_INDEX;
import static jp.co.iworks.koreang.Const.URL_USER_REGIST;
import static jp.co.iworks.koreang.Const.URL_TIMETABLE_INDEX;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.iwork.koreang.util.CommonUtils;
import jp.co.iworks.koreang.APIResponseHandler;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


public class WebAPI {
	private Context mContext;
	public WebAPI (Context context) {
		mContext = context;
	}
	public void registByUuid(String nickName, String uuid, final APIResponseHandler handler) throws UnsupportedEncodingException {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();

		parameters.add(new BasicNameValuePair("uuid", uuid));
		parameters.add(new BasicNameValuePair("nickname", nickName));
		
		new HttpRequest(mContext).post(CommonUtils.getUrl(URL_USER_REGIST), parameters, new HttpResponseHandler(){

			@Override
			public void onSuccess(String result) {
				handler.onRespond(result);
			}

			@Override
			public void onFailure(Throwable e) {
				handler.onRespond(e);
			}
		});
	}
	public void getTeacherList(final APIResponseHandler handler) {
		new HttpRequest(mContext).get(CommonUtils.getUrl(URL_TEACHER_INDEX), null, new HttpResponseHandler(){

			@Override
			public void onSuccess(String result) {
				handler.onRespond(result);
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(e);
			}
			
		});
	}
	public void getTimeTableList(String teacherId, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("teacher_id", teacherId));
		new HttpRequest(mContext).get(CommonUtils.getUrl(URL_TIMETABLE_INDEX), parameters, new HttpResponseHandler(){

			@Override
			public void onSuccess(String result) {
				handler.onRespond(result);
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(e);
			}
			
		});
	}
}