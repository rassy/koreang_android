package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.URL_TEACHER_PROFILE_INDEX;
import static jp.co.iworks.koreang.Const.URL_USER_TICKET_INDEX;

import java.text.NumberFormat;

import jp.co.iworks.koreang.dto.Teacher;
import jp.co.iworks.koreang.util.RoundedCornerImageView;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TeacherProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		setupDisplay();
	}
	private void setupDisplay() {
		Intent intent = getIntent();
		String teacher_id = intent.getStringExtra("teacher_id");
		RequestParams params = new RequestParams();
		params.add("teacher_id", teacher_id);
        KoreangHttpClient.get(this, URL_USER_TICKET_INDEX, null, new JsonHttpResponseHandler(){

  			@Override
  			public void onSuccess(JSONObject response) {
  				super.onSuccess(response);
  				TextView txtTicketBalance = (TextView)findViewById(R.id.txtTicketBalance);
  				try {
  	    			JSONObject info = response.getJSONObject("info");
  	    			boolean status = info.getBoolean("status");
  	    			
  	    			if (status) {
  	    				JSONObject ticket = response.getJSONObject("result");
  	    				int quantity = ticket.getInt("quantity");
  	    				NumberFormat nf = NumberFormat.getNumberInstance();        	
  	    				txtTicketBalance.setText(nf.format(quantity));
  	    				return;
  	    			}
  				} catch (JSONException e) {
  					e.printStackTrace();
      			}
  				txtTicketBalance.setText("0");
  			}
        });
		KoreangHttpClient.get(this, URL_TEACHER_PROFILE_INDEX, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				try {
					JSONObject info = response.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONObject data = response.getJSONObject("data");
						Teacher teacher = new Teacher();
						teacher.setId(data.getString("teacher_id"));
						teacher.setNickname(data.getString("nickname"));
						teacher.setUrl(data.getString("url"));
						setupProfile(teacher);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void setupProfile(final Teacher teacher) {
		RoundedCornerImageView iv = (RoundedCornerImageView)findViewById(R.id.iv_profile);
		ImageLoader.getInstance().displayImage(teacher.getUrl(), iv);
		
		TextView txtNickname = (TextView)findViewById(R.id.txtProfileNickName);
		txtNickname.setText(teacher.getNickname());
		Button btnSchedule = (Button)findViewById(R.id.btnOpenTimeTable);
		btnSchedule.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TeacherProfileActivity.this, TimeTableActivity.class);
				intent.putExtra("teacher_id", teacher.getId());
				startActivity(intent);
			}
		});
	}
}
