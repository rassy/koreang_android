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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
				String target = sdf.format(calendar.getTime());
				setTimeTableAdapter(target);
			}
		});
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
		String target = sdf.format(Calendar.getInstance().getTime());
		setTimeTableAdapter(target);
	}
	private void setTimeTableAdapter(final String targetDate) {
		ListView listView = (ListView)findViewById(R.id.listTime);
		final ArrayAdapter<TimeTable> adapter = new TimeTableAdapter(this, R.layout.time_table_list_row);
		listView.setAdapter(adapter);

		new WebAPI(this).getTimeTableList(teacher_id, targetDate, new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				if (result == null) return;
				try {
					JSONObject json = new JSONObject(result.toString());
					JSONObject info = json.getJSONObject("result").getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = json.getJSONObject("result").getJSONArray("list");
						for (int i=0; i<list.length(); i++) {
							JSONObject data = list.getJSONObject(i);
							String time_table_id = data.getString("time_table_id");
							String start_date = data.getString("start_date");
							String end_date = data.getString("end_date");
							boolean available = data.getBoolean("available");
							boolean reserved = data.getBoolean("reserved");
							String price = data.getString("price");
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
							Date startDate = null;
							Date endDate = null;
							try {
								startDate = formatter.parse(start_date);
								endDate = formatter.parse(end_date);
								
								Date now = Calendar.getInstance().getTime();
								if (now.after(startDate)) {
									continue;
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
								continue;
							}
							adapter.add(new TimeTable(time_table_id, targetDate, startDate, endDate, available, reserved, price));
						}
					}
				} catch (JSONException e) {
					
				}
			}
		});
	}

	private class TimeTable {
		private String id;
		private String targetDate;
		private Date startDate;
		private Date endDate;
		private boolean available;
		private boolean reserved;
		private String price;
		
		public TimeTable(String id, String targetDate, Date startDate, Date endDate, boolean available, boolean reserved, String price) {
			this.id = id;
			this.targetDate = targetDate;
			this.startDate = startDate;
			this.endDate = endDate;
			this.reserved = reserved;
			this.available = available;
			this.price = price;
		}
		public String getId() {
			return this.id;
		}
		public String getTargetDate() {
			return this.targetDate;
		}
		public Date getStartDate() {
			return this.startDate;
		}
		public Date getEndDate() {
			return this.endDate;
		}
		public String getPrice() {
			return this.price;
		}
		public boolean getReserved() {
			return this.reserved;
		}
		public boolean getAvailable() {
			return this.available;
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
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.JAPAN);

			((TextView)view.findViewById(R.id.txtTimeFrom)).setText(formatter.format(timeTable.getStartDate()));
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(formatter.format(timeTable.getEndDate()));
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
			if (!timeTable.getAvailable()) {
				btnReserve.setText("×");
				btnReserve.setEnabled(false);
				return view;
			}
			if (!timeTable.getReserved()) {
				btnReserve.setText(timeTable.getPrice().toString() + "pt で予約");
				btnReserve.setEnabled(true);
			} else {
				btnReserve.setText("予約済み");
				btnReserve.setEnabled(false);
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
								setTimeTableAdapter(timeTable.getTargetDate());
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
								setTimeTableAdapter(timeTable.getTargetDate());
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
