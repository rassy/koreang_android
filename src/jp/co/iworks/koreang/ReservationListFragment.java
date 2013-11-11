package jp.co.iworks.koreang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.co.iworks.koreang.util.CalendarView;
import jp.co.iworks.koreang.web.APIResponseHandler;
import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 予約済み一覧
 * @author tryumura
 * @since 2013/10/31
 */
public class ReservationListFragment extends Fragment {

	private final ReservationListFragment that = this;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.reservation_list, container, false);
		CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendar_reservation);
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
		
		calendarView.setOnClickListener(new CalendarView.OnClickListener() {
			
			@Override
			public void onClick(Button button, Calendar calendar) {
				String targetDate = formatter.format(calendar.getTime());
				setupDisplay(view, targetDate);
			}
		});
		setupDisplay(view, formatter.format(new Date()));
		return view;
	}
	protected void setupDisplay(View view, String targetDate) {
		ListView listView = (ListView)view.findViewById(R.id.listReservation);
		final ArrayAdapter<Reservation> adapter = new ReservationAdapter(getActivity(), R.layout.reservation_list_row);
		listView.setAdapter(adapter);

		new WebAPI(getActivity()).getReservationList(targetDate, new APIResponseHandler(){

			@Override
			public void onRespond(Object result) {
				if (result == null) {
					return;
				}
				try {
					JSONObject json = new JSONObject(result.toString());
					JSONObject info = json.getJSONObject("info");
					if (info.getBoolean("status")) {
						JSONArray list = json.getJSONArray("list");
						for(int i=0; i<list.length(); i++) {
							String id = list.getJSONObject(i).getString("id");
							String time_table_id = list.getJSONObject(i).getString("time_table_id");
							SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm", Locale.JAPAN);
							SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
							Date startDate = formatterDate.parse(list.getJSONObject(i).getString("start_date"));
							String start_date = formatterTime.format(startDate);
							Date endDate = formatterDate.parse(list.getJSONObject(i).getString("end_date"));
							String end_date = formatterTime.format(endDate);
							String teacher_id = list.getJSONObject(i).getString("teacher_id");
							String nickname = list.getJSONObject(i).getString("name");
							adapter.add(new Reservation(id, time_table_id, teacher_id, nickname, start_date, end_date));
						}
					} else if (info.getInt("code") == 404){
						//((MainActivity)getActivity()).showDialogMessage("予約なし", "現在予約はありません", null);
					} else {
						//((MainActivity)getActivity()).showDialogMessage("エラー", "データが正しく取得できませんでした。", null);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					//((MainActivity)getActivity()).showDialogMessage("エラー", e.getMessage(), null);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

		});
	}

	private class Reservation {
		private String id;
		private String time_table_id;
		private String teacher_id;
		private String nickname;
		private String timeFrom;
		private String timeTo;

		public Reservation(String id, String time_table_id, String teacher_id, String nickname, String timeFrom, String timeTo) {
			this.id = id;
			this.time_table_id = time_table_id;
			this.teacher_id = teacher_id;
			this.nickname = nickname;
			this.timeFrom = timeFrom;
			this.timeTo = timeTo;
		}
		public String getId() {
			return this.id;
		}
		public String getTimeTableId() {
			return this.time_table_id;
		}
		public String getTeacherId() {
			return this.teacher_id;
		}
		public String getNickname() {
			return this.nickname;
		}
		public String getTimeFrom() {
			return this.timeFrom;
		}
		public String getTimeTo() {
			return this.timeTo;
		}
	}
	private class ReservationAdapter extends ArrayAdapter<Reservation> {

		private ArrayList<Reservation> timeTables = new ArrayList<Reservation>();
		private LayoutInflater inflater;
		private int layout;
		public ReservationAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layout = textViewResourceId;
		}
		@Override
		public void add(Reservation object) {
			super.add(object);
			this.timeTables.add(object);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = this.inflater.inflate(this.layout, null);
			}
			final Reservation reservation = this.timeTables.get(position);
			((TextView)view.findViewById(R.id.txtTimeFrom)).setText(reservation.getTimeFrom());
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(reservation.getTimeTo());
			((TextView)view.findViewById(R.id.txtNickname)).setText(reservation.getNickname());
			((Button)view.findViewById(R.id.btnReservationCancel)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder confirm = new AlertDialog.Builder(getActivity());
					confirm.setTitle("キャンセル確認");
					confirm.setMessage("キャンセルしても宜しいですか？");
					confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new WebAPI(getActivity()).postReservationCancel(reservation.getId(), new APIResponseHandler(){

								@Override
								public void onRespond(Object result) {
									try {
										JSONObject json = new JSONObject(result.toString());
										boolean status = json.getJSONObject("info").getBoolean("status");
										if (status) {
//											((MainActivity)getActivity()).showDialogMessage("完了", "キャンセルが完了しました", new DialogInterface.OnClickListener() {
//												
//												@Override
//												public void onClick(DialogInterface arg0, int arg1) {
//													that.setupDisplay(that.currentView);
//												}
//											});
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								
							});
						}
					});
					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
					
					AlertDialog alert = confirm.create();
					alert.show();
					
				}
			});
			return view;
		}
	}
}
