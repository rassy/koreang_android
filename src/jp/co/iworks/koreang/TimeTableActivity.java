package jp.co.iworks.koreang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.co.iworks.koreang.util.CalendarView;
import jp.co.iworks.koreang.web.APIResponseHandler;
import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTableActivity extends Activity {

	private String teacher_id;
	private Map<String, JSONArray> timeList = new HashMap<String, JSONArray>();
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_table_list);
		
		setupDisplay();
	}
	
	private void setupDisplay() {
		Intent intent = getIntent();
		teacher_id = intent.getStringExtra("teacher_id");
		
		CalendarView calendarView = (CalendarView)findViewById(R.id.calendarView1);
		calendarView.setOnClickListener(new CalendarView.OnClickListener() {
			
			@Override
			public void onClick(Button button, Calendar calendar) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
					String target = sdf.format(calendar.getTime());
					setListViewByTargetDate(target);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		refreshData();
	}
	private void refreshData() {
		new WebAPI(this).getTimeTableList(teacher_id, new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				try {
					JSONObject json = new JSONObject(result.toString());
					JSONObject info = json.getJSONObject("result").getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = json.getJSONObject("result").getJSONArray("list");
						for (int i=0; i<list.length(); i++) {
							String targetDate = list.getJSONObject(i).getString("target_date");
							JSONArray availableList = list.getJSONObject(i).getJSONArray("list");
							timeList.put(targetDate, availableList);
						}
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
					String today = sdf.format(new Date());
					setListViewByTargetDate(today);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void setListViewByTargetDate(String targetDate)  throws JSONException {
		JSONArray results = timeList.get(targetDate);
		
		ListView listView = (ListView)findViewById(R.id.listTime);
		ArrayAdapter<TimeTable> adapter = new TimeTableAdapter(this, R.layout.time_table_list_row);
		listView.setAdapter(adapter);
			
		for (int i=0; i<results.length(); i++) {
			JSONObject data = results.getJSONObject(i);
			String time_table_id = data.getString("time_table_id");
			String start_date = data.getString("start_date");
			String end_date = data.getString("end_date");
			String price = data.getString("price");
			boolean available = data.getBoolean("available");
			adapter.add(new TimeTable(time_table_id, targetDate, start_date, end_date, available, price));
		}
	}
	private class TimeTable {
		private String id;
		private String targetDate;
		private String timeFrom;
		private String timeTo;
		private boolean canReserve;
		private String price;
		
		public TimeTable(String id, String targetDate, String timeFrom, String timeTo, boolean canReserve, String price) {
			this.id = id;
			this.targetDate = targetDate;
			this.timeFrom = timeFrom;
			this.timeTo = timeTo;
			this.canReserve = canReserve;
			this.price = price;
		}
		public String getId() {
			return this.id;
		}
		public String getTargetDate() {
			return this.targetDate;
		}
		public String getTimeFrom() {
			return this.timeFrom;
		}
		public String getTimeTo() {
			return this.timeTo;
		}
		public String getPrice() {
			return this.price;
		}
	}
	private class TimeTableAdapter extends ArrayAdapter<TimeTable> {

		private ArrayList<TimeTable> timeTables = new ArrayList<TimeTable>();
		private LayoutInflater inflater;
		private int layout;
		public TimeTableAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
			this.layout = textViewResourceId;
		}
		@Override
		public void add(TimeTable object) {
			super.add(object);
			this.timeTables.add(object);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = this.inflater.inflate(this.layout, null);
			}
			final TimeTable timeTable = this.timeTables.get(position);
			((TextView)view.findViewById(R.id.txtTimeFrom)).setText(timeTable.getTimeFrom());
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(timeTable.getTimeTo());
			Button btnReserve = (Button)view.findViewById(R.id.btnReserve);
			btnReserve.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AlertDialog.Builder confirm = new AlertDialog.Builder(TimeTableActivity.this);
					confirm.setTitle("確認");
					confirm.setMessage("通話が開始される際に" + timeTable.getPrice() + "pt消費されます。\n予約しても宜しいですか？");
					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
					confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							registReservation(timeTable);
						}
					});
					confirm.show();
				}
			});
			btnReserve.setEnabled(timeTable.canReserve);
			if (timeTable.canReserve) {
				btnReserve.setText(timeTable.getPrice().toString() + "pt で予約");
			} else {
				btnReserve.setText("予約済み");
			}
			return view;
		}
	}
	private void registReservation(final TimeTable timeTable) {
		showProgress();
		new WebAPI(TimeTableActivity.this).postReservation(teacher_id, timeTable.getId(), timeTable.getTargetDate(), timeTable.getPrice(), new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				hideProgress();
				
				AlertDialog.Builder alert = new AlertDialog.Builder(TimeTableActivity.this);
				try {
					JSONObject json = new JSONObject(result.toString());
					boolean status = json.getJSONObject("result").getJSONObject("info").getBoolean("status");
					if (status) {
						alert.setTitle("完了");
						alert.setMessage("予約が正常に完了しました。");
						alert.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								refreshData();
							}					
						});
					} else {
						alert.setTitle("失敗");
						alert.setMessage("既に予約されているため予約出来ませんでした。");
						alert.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								refreshData();
							}
						});
					}
					alert.show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void showProgress() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Now Loading...");
		}
		progressDialog.show();
	}
	/**
	 * 読み込み中ダイアログを非表示にする
	 */
	private void hideProgress() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
}
