package jp.co.iworks.koreang;

import jp.co.iworks.koreang.dto.Teacher;
import jp.co.iworks.koreang.util.ImageDownloadTask;
import jp.co.iworks.koreang.util.RoundedCornerImageView;
import jp.co.iworks.koreang.web.APIResponseHandler;
import jp.co.iworks.koreang.web.WebAPI;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
		new WebAPI(this).getTeacherById(teacher_id, new APIResponseHandler(){
			@Override
			public void onRespond(Object result) {
				if (result != null && result instanceof Teacher) {
					setupProfile((Teacher)result);
				}
			}
		});
	}
	private void setupProfile(final Teacher teacher) {
		RoundedCornerImageView iv = (RoundedCornerImageView)findViewById(R.id.iv_profile);
		new ImageDownloadTask(iv, teacher.getUrl()).execute();
		
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
