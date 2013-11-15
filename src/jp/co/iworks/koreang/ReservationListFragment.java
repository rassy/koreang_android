package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.URL_RESERVAION_CANCEL;
import static jp.co.iworks.koreang.Const.URL_RESERVATION_INDEX_BY_UUID;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.co.iworks.koreang.util.CalendarView;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 予約済み一覧
 * @author tryumura
 * @since 2013/10/31
 */
public class ReservationListFragment extends Fragment {
	private View currentView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.reservation_list, container, false);
		currentView = view;
		setupDisplay();
		return view;
	}
	protected void setupDisplay() {
		CalendarView calendarView = (CalendarView) currentView.findViewById(R.id.calendar_reservation);
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
		
		calendarView.setOnClickListener(new CalendarView.OnClickListener() {
			
			@Override
			public void onClick(Button button, Calendar calendar) {
				String targetDate = formatter.format(calendar.getTime());
				setupAdapter(targetDate);
			}
		});
		setupAdapter(formatter.format(Calendar.getInstance().getTime()));
	}
	private void setupAdapter(final String targetDate) {
		ListView listView = (ListView)currentView.findViewById(R.id.listReservation);
		final ArrayAdapter<Reservation> adapter = new ReservationAdapter(getActivity(), R.layout.reservation_list_row);
		listView.setAdapter(adapter);

		RequestParams params = new RequestParams();
		params.add("target_date", targetDate);
		KoreangHttpClient.get(getActivity(), URL_RESERVATION_INDEX_BY_UUID, params, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				try {
					JSONObject info = response.getJSONObject("info");
					if (info.getBoolean("status")) {
						JSONArray list = response.getJSONArray("list");
						for(int i=0; i<list.length(); i++) {
							String id = list.getJSONObject(i).getString("id");
							SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
							Date startDate = formatterDate.parse(list.getJSONObject(i).getString("start_date"));
							Date endDate = formatterDate.parse(list.getJSONObject(i).getString("end_date"));
							String nickname = list.getJSONObject(i).getString("name");
							
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.MINUTE, 5);
							Date now = cal.getTime();
							if (now.after(startDate)) {
								continue;
							}
							adapter.add(new Reservation(id, targetDate, nickname, startDate, endDate));
						}
					} else if (info.getInt("code") == 404){
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
		});

	}

	private class Reservation {
		private String id;
		private String targetDate;
		private String nickname;
		private Date startDate;
		private Date endDate;

		public Reservation(String id, String targetDate, String nickname, Date startDate, Date endDate) {
			this.id = id;
			this.targetDate = targetDate;
			this.nickname = nickname;
			this.startDate = startDate;
			this.endDate = endDate;
		}
		public String getId() {
			return this.id;
		}
		public String getTargetDate() {
			return this.targetDate;
		}
		public String getNickname() {
			return this.nickname;
		}
		public Date getStartDate() {
			return this.startDate;
		}
		public Date getEndDate() {
			return this.endDate;
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
			SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm", Locale.JAPAN);
			final Reservation reservation = this.timeTables.get(position);
			((TextView)view.findViewById(R.id.txtTimeFrom)).setText(dateFormatter.format(reservation.getStartDate()));
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(dateFormatter.format(reservation.getEndDate()));
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
							cancel(reservation);
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
		private void cancel(final Reservation reservation) {
			RequestParams params = new RequestParams();
			params.add("reservation_id", reservation.getId());
			
			KoreangHttpClient.post(getActivity(), URL_RESERVAION_CANCEL, params, new JsonHttpResponseHandler(){

				@Override
				public void onSuccess(JSONObject response) {
					try {
						boolean status = response.getJSONObject("info").getBoolean("status");
						if (status) {
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setTitle("完了");
							builder.setMessage("キャンセルが完了しました");
							builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									setupAdapter(reservation.getTargetDate());
								}
							});
							builder.create().show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
			});

		}
	}
}
