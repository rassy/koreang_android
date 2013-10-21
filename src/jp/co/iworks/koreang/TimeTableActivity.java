package jp.co.iworks.koreang;

import jp.co.iworks.koreang.web.WebAPI;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;

public class TimeTableActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new WebAPI(this).getTimeTable(new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				Log.d("TimeTableActivity/onCreate", result.toString());
			}
			
		});
		TableLayout tableLayout = new TableLayout(this);
		
	}

}
