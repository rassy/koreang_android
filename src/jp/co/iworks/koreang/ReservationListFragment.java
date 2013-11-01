package jp.co.iworks.koreang;

import java.util.ArrayList;

import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private View currentView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reservation_list, container, false);
		setupDisplay(view);
		currentView = view;
		return view;
	}
	protected void setupDisplay(View view) {
		final Context context = getActivity();
		ListView listView = (ListView)view.findViewById(R.id.listReservation);
		final ArrayAdapter<Reservation> adapter = new ReservationAdapter(context, R.layout.reservation_list_row);
		listView.setAdapter(adapter);
		((MainActivity)getActivity()).showProgress();
		
		new WebAPI(context).getReservationList(new APIResponseHandler(){

			@Override
			public void onRespond(Object result) {
				((MainActivity)getActivity()).hideProgress();
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
							String start_date = list.getJSONObject(i).getString("start_date");
							String end_date = list.getJSONObject(i).getString("end_date");
							String teacher_id = list.getJSONObject(i).getString("teacher_id");
							String nickname = list.getJSONObject(i).getString("nickname");
							adapter.add(new Reservation(id, time_table_id, teacher_id, nickname, start_date, end_date));
						}
					} else if (info.getInt("code") == 404){
						((MainActivity)getActivity()).showDialogMessage("予約なし", "現在予約はありません", null);
					} else {
						((MainActivity)getActivity()).showDialogMessage("エラー", "データが正しく取得できませんでした。", null);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					((MainActivity)getActivity()).showDialogMessage("エラー", e.getMessage(), null);
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
			((TextView)view.findViewById(R.id.txtTitle)).setText(reservation.getTimeFrom());
			((TextView)view.findViewById(R.id.txtNickname)).setText(reservation.getNickname());
			((Button)view.findViewById(R.id.btnReservationCancel)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new WebAPI(getActivity()).postReservationCancel(reservation.getId(), new APIResponseHandler(){

						@Override
						public void onRespond(Object result) {
							try {
								JSONObject json = new JSONObject(result.toString());
								boolean status = json.getJSONObject("info").getBoolean("status");
								if (status) {
									((MainActivity)getActivity()).showDialogMessage("完了", "キャンセルが完了しました", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											that.setupDisplay(that.currentView);
										}
									});
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						
					});
				}
			});
			return view;
		}
	}
}
