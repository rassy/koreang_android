package jp.co.iworks.koreang;

import java.util.ArrayList;

import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTableActivity extends Activity {

	private String teacher_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_table_view);
		
		setupDisplay();
	}
	
	private void setupDisplay() {
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
		
		final TextView txtTargetDate = (TextView)findViewById(R.id.txtTargetDate);
		
		ListView listView = (ListView)findViewById(R.id.listTime);
		final ArrayAdapter<TimeTable> adapter = new TimeTableAdapter(this, R.layout.list_table_row);
		listView.setAdapter(adapter);
		
		new WebAPI(this).getTimeTableList(teacher_id, new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				try {
					JSONObject json = new JSONObject(result.toString());
					JSONObject results = json.getJSONObject("result");
					String date = results.getString("date");
					txtTargetDate.setText(date + " の予定");
					
					JSONObject info = results.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = results.getJSONArray("list");
						
						for (int i=0; i<list.length(); i++) {
							JSONObject data = list.getJSONObject(i);
							String start_date = data.getString("start_date");
							String end_date = data.getString("end_date");
							boolean available = data.getBoolean("available");
							adapter.add(new TimeTable(start_date, end_date, available));
						}
					}
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
			
		});
		
	}

	private class TimeTable {
		private String timeFrom;
		private String timeTo;
		private boolean canReserve;
		
		public TimeTable(String timeFrom, String timeTo, boolean canReserve) {
			this.timeFrom = timeFrom;
			this.timeTo = timeTo;
			this.canReserve = canReserve;
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
			TimeTable timeTable = this.timeTables.get(position);
			((TextView)view.findViewById(R.id.txtTimeFrom)).setText(timeTable.getTimeFrom());
			((TextView)view.findViewById(R.id.txtTimeTo)).setText(timeTable.getTimeTo());
			Button btnReserve = (Button)view.findViewById(R.id.btnReserve);
			btnReserve.setEnabled(timeTable.canReserve);
			if (timeTable.canReserve) {
				btnReserve.setText("予約");
			} else {
				btnReserve.setText("×");
			}
			return view;
		}
	}
}
