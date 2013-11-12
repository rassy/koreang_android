package jp.co.iworks.koreang.web;

import static jp.co.iworks.koreang.Const.URL_RESERVAION_CANCEL;
import static jp.co.iworks.koreang.Const.URL_RESERVATION_INDEX_BY_UUID;
import static jp.co.iworks.koreang.Const.URL_RESERVATION_REGIST;
import static jp.co.iworks.koreang.Const.URL_TEACHER_INDEX;
import static jp.co.iworks.koreang.Const.URL_TEACHER_PROFILE_INDEX;
import static jp.co.iworks.koreang.Const.URL_TIMETABLE_INDEX;
import static jp.co.iworks.koreang.Const.URL_USER_REGIST;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.iworks.koreang.dto.Teacher;
import jp.co.iworks.koreang.util.CommonUtils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


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
	public void getTeacherById(String teacherId, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("teacher_id", teacherId));
		new HttpRequest(mContext).get(CommonUtils.getUrl(URL_TEACHER_PROFILE_INDEX), parameters, new HttpResponseHandler(){

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject json = new JSONObject(result);
					JSONObject info = json.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONObject data = json.getJSONObject("data");
						Teacher teacher = new Teacher();
						teacher.setId(data.getString("teacher_id"));
						teacher.setNickname(data.getString("nickname"));
						teacher.setUrl(data.getString("url"));
						handler.onRespond(teacher);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					handler.onRespond(e);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				handler.onRespond(e);
			}
		});
	}
	public void getTimeTableList(String teacherId, String targetDate, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("teacher_id", teacherId));
		parameters.add(new BasicNameValuePair("target_date", targetDate));
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
	public void postReservation(String teacherId, String timeTableId, String targetDate, String price, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("teacher_id", teacherId));
		parameters.add(new BasicNameValuePair("time_table_id", timeTableId));
		parameters.add(new BasicNameValuePair("target_date", targetDate));
		parameters.add(new BasicNameValuePair("price", price));
		new HttpRequest(mContext).post(CommonUtils.getUrl(URL_RESERVATION_REGIST), parameters, new HttpResponseHandler(){

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
	/**
	 * 自分の予約一覧を取得する
	 * @param handler ハンドラ
	 */
	public void getReservationList(String targetDate, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("target_date", targetDate));
		new HttpRequest(mContext).get(CommonUtils.getUrl(URL_RESERVATION_INDEX_BY_UUID), parameters, new HttpResponseHandler(){

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
	/**
	 * 予約をキャンセルする
	 * @param reservationId 予約ID
	 * @param handler ハンドラ
	 */
	public void postReservationCancel(String reservationId, final APIResponseHandler handler) {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("reservation_id", reservationId));
		new HttpRequest(mContext).post(CommonUtils.getUrl(URL_RESERVAION_CANCEL), parameters, new HttpResponseHandler() {
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
}