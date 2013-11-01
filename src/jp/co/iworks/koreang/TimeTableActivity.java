package jp.co.iworks.koreang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTableActivity extends Activity {

	private String teacher_id;
	private JSONArray timeList = null;
	private String targetDate;
	private int currentIndex;
	private static Drawable next;
	private static Drawable next_push;
	private static Drawable back;
	private static Drawable back_push;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_table_list);
		
		setupDisplay();
	}
	
	private void setupDisplay() {
		currentIndex = 0;
		next = getResources().getDrawable(R.drawable.next);
		next_push = getResources().getDrawable(R.drawable.next_push);
		back = getResources().getDrawable(R.drawable.back);
		back_push = getResources().getDrawable(R.drawable.back_push);
		
		Intent intent = getIntent();
		teacher_id = intent.getStringExtra("teacher_id");
		String nickname = intent.getStringExtra("nickname");
		TextView txtTeacherName = (TextView)findViewById(R.id.txtTeacherName);
		txtTeacherName.setText(nickname);
		
		ImageView ivTeacher = (ImageView)findViewById(R.id.ivTeacher);
		String url = intent.getStringExtra("url");
		new ImageDownloadTask(ivTeacher, url).execute();
		
		String message = intent.getStringExtra("message");
		message = message.replace("\\n", "\n");
		TextView txtTeacherMessage = (TextView)findViewById(R.id.txtTeacherMessage);
		txtTeacherMessage.setText(message);
		
		TextView txtTicketBalance = (TextView)findViewById(R.id.txtTicketBalance);
		txtTicketBalance.setText("チケット残高　1,000pt.");
		
		final ImageButton btnNext = (ImageButton)findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentIndex<6) {
					currentIndex++;
					try {
						setListViewByIndex(currentIndex);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btnNext.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					btnNext.setBackgroundDrawable(next_push);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					btnNext.setBackgroundDrawable(next);
				}
				return false;
			}
		});
		final ImageButton btnPrevious = (ImageButton)findViewById(R.id.btnPrevious);
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentIndex > 0) {
					currentIndex--;
					try {
						setListViewByIndex(currentIndex);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btnPrevious.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					btnPrevious.setBackgroundDrawable(back_push);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					btnPrevious.setBackgroundDrawable(back);
				}
				return false;
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
						timeList = json.getJSONObject("result").getJSONArray("list");
					}
					setListViewByIndex(currentIndex);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	private void setListViewByIndex(int index)  throws JSONException {
		JSONObject results = timeList.getJSONObject(index);
		targetDate = results.getString("date");
		TextView txtTargetDate = (TextView)findViewById(R.id.txtTargetDate);
		txtTargetDate.setText(targetDate);
		
		ListView listView = (ListView)findViewById(R.id.listTime);
		ArrayAdapter<TimeTable> adapter = new TimeTableAdapter(this, R.layout.time_table_list_row);
		listView.setAdapter(adapter);

		JSONArray list = results.getJSONArray("list");
			
		for (int i=0; i<list.length(); i++) {
			JSONObject data = list.getJSONObject(i);
			String time_table_id = data.getString("id");
			String start_date = data.getString("start_date");
			String end_date = data.getString("end_date");
			boolean available = data.getBoolean("available");
			adapter.add(new TimeTable(time_table_id, start_date, end_date, available));
		}
	}
	private class TimeTable {
		private String id;
		private String timeFrom;
		private String timeTo;
		private boolean canReserve;
		
		public TimeTable(String id, String timeFrom, String timeTo, boolean canReserve) {
			this.id = id;
			this.timeFrom = timeFrom;
			this.timeTo = timeTo;
			this.canReserve = canReserve;
		}
		public String getId() {
			return this.id;
		}
		public String getTimeFrom() {
			return this.timeFrom;
		}
		public String getTimeTo() {
			return this.timeTo;
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
			((TextView)view.findViewById(R.id.txtTitle)).setText(timeTable.getTimeFrom());
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(timeTable.getTimeTo());
			Button btnReserve = (Button)view.findViewById(R.id.btnReserve);
			btnReserve.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d("TimeTableActivity/btnReserce.onclick", timeTable.getId() + ":" + timeTable.getTimeFrom() + " - " + timeTable.getTimeTo());
					showProgress();
					new WebAPI(TimeTableActivity.this).postReservation(teacher_id, timeTable.getId(), targetDate, new APIResponseHandler() {

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
			});
			btnReserve.setEnabled(timeTable.canReserve);
			if (timeTable.canReserve) {
				btnReserve.setText("予約");
			} else {
				btnReserve.setText("×");
			}
			return view;
		}
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
