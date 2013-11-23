package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.URL_TEACHER_PROFILE_INDEX;
import static jp.co.iworks.koreang.Const.URL_USER_TICKET_INDEX;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.iworks.koreang.util.RoundedCornerImageView;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.apache.http.Header;
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
		final String teacher_id = intent.getStringExtra("teacher_id");
		
		Button btnSchedule = (Button)findViewById(R.id.btnOpenTimeTable);
		btnSchedule.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TeacherProfileActivity.this, TimeTableActivity.class);
				intent.putExtra("teacher_id", teacher_id);
				startActivity(intent);
			}
		});
		
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
						RoundedCornerImageView iv = (RoundedCornerImageView)findViewById(R.id.iv_profile);
						ImageLoader.getInstance().displayImage(data.getString("resource_url"), iv);

						// 名前
						Object name = data.get("name");
						if (name != JSONObject.NULL){ 
							TextView txtNickname = (TextView)findViewById(R.id.txtProfileNickName);
							txtNickname.setText(name.toString());
						}
						
						Object birthdayString = data.get("birthday");
						if (birthdayString != JSONObject.NULL) {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
							TextView txtBirthday = (TextView)findViewById(R.id.txtBirthday);
							if (birthdayString != null) {
								
								try {
									Date birthday = formatter.parse(birthdayString.toString());
									txtBirthday.setText(formatter.format(birthday));
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}
						// 身長
						Object height = data.get("height");
						if (height != JSONObject.NULL) {
							TextView txtHeight = (TextView)findViewById(R.id.txtHeight);
							txtHeight.setText(height.toString());
						}
						
						// 血液型
						Object blood = data.get("blood");
						if (blood != JSONObject.NULL) {
							TextView txtBlood = (TextView)findViewById(R.id.txtBlood);
							txtBlood.setText(blood.toString());
						}

						// 家族構成
						Object family = data.get("family");
						if (family != JSONObject.NULL){ 
							TextView txtFamily = (TextView)findViewById(R.id.txtFamily);
							txtFamily.setText(family.toString());
						}

						// 好きなタイプ
						Object womanType = data.get("woman_type");
						if (womanType != JSONObject.NULL) {
							TextView txtWomanType = (TextView)findViewById(R.id.txtLikeType);
							txtWomanType.setText(womanType.toString());
						}

						// マイブーム
						Object myboom = data.get("myboom");
						if (myboom != JSONObject.NULL) {
							TextView txtMyBoom = (TextView)findViewById(R.id.txtMyBoom);
							txtMyBoom.setText(myboom.toString());
						}

						// 好きな食べ物
						Object food = data.get("food");
						if (food != JSONObject.NULL) {
							TextView txtFood = (TextView)findViewById(R.id.txtLikeFood);
							txtFood.setText(food.toString());
						}
						
						// 似ている芸能人
						Object lookLike = data.get("look_like");
						if (lookLike != JSONObject.NULL) {
							TextView txtLookLike = (TextView)findViewById(R.id.txtLookLike);
							txtLookLike.setText(lookLike.toString());
						}

						// ニックネーム
						Object adana = data.get("nickname");
						if (adana != JSONObject.NULL) {
							TextView txtAdana = (TextView)findViewById(R.id.txtAdana);
							txtAdana.setText(adana.toString());
						}
						
						// 好きな映画
						Object movie = data.get("movie");
						if (movie != JSONObject.NULL) { 
							TextView txtMovie = (TextView)findViewById(R.id.txtLikeMovie);
							txtMovie.setText(movie.toString());
						}
						
						// 好きなシーン
						Object scene = data.get("scene");
						if (scene != JSONObject.NULL){ 
							TextView txtScene = (TextView)findViewById(R.id.txtLikeMovieScene);
							txtScene.setText(scene.toString());
						}

						// 好きなしぐさ
						Object gesture = data.get("gesture");
						if (gesture != JSONObject.NULL) {						
							TextView txtGesture = (TextView)findViewById(R.id.txtLikeShigusa);
							txtGesture.setText(gesture.toString());
						}

						// ストレス発散法
						Object stress = data.get("stress");
						if (stress != JSONObject.NULL){ 
							TextView txtStress = (TextView)findViewById(R.id.txtStress);
							txtStress.setText(stress.toString());
						}
						
						// 何フェチ
						Object fetish = data.get("fetish");
						if (fetish != JSONObject.NULL){ 
							TextView txtFetish = (TextView)findViewById(R.id.txtFetch);
							txtFetish.setText(fetish.toString());
						}

						// SかMか
						Object sm = data.get("sm");
						if (sm != JSONObject.NULL){ 
							TextView txtSm = (TextView)findViewById(R.id.txtSM);
							txtSm.setText(sm.toString());
						}
						
						// 最後に一言
						Object message = data.get("message");
						if (message != JSONObject.NULL){ 
							TextView txtMessage = (TextView)findViewById(R.id.txtMessage);
							txtMessage.setText(message.toString());
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseBody, Throwable e) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseBody, e);
			}
		});
	}

}
