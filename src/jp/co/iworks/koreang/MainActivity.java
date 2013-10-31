package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.iwork.koreang.util.CommonUtils;
import jp.co.iworks.koreang.phone.PhoneManager;
import jp.co.iworks.koreang.phone.PhoneRegistrationHandler;
import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private CommonUtils commonUtils;
	private ProgressDialog progressDialog = null;
	private AlertDialog.Builder errorDialog = null;
	private String user_id = null;
	private String uuid = null;
	private String nick_name = null;
	
// Activity LifeCycle
//////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        commonUtils = new CommonUtils(this);
        initializeApplication();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
	protected void onRestart() {
		super.onRestart();
		//initializeApplication();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        PhoneManager.getInstance().closeManager(this);
    }
    
//////////////////////////////////////////////////////////////////
    /**
     * アプリケーションを初期化します
     */
    private void initializeApplication() {
    	uuid = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "uuid");
    	user_id = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "user_id");
    	// uuidが端末に残っていなかったら新規登録
    	if (uuid == null || user_id == null) {
    		showEntryDialog();
    		return;
    	}
    	setupDisplay();
    }

    /**
     * 名前入力画面を表示する
     * OKを押したらuuidを生成してサーバーに送信
     */
    private void showEntryDialog() {
        // 初めてだったら名前登録画面を表示
    	final EditText txtName = new EditText(this);
    	if (nick_name != null) {
    		txtName.setText(nick_name);
    	}
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("ようこそ");
        alertDialog.setMessage("韓流語学アプリへようこそ。\nはじめにお名前を教えて下さい。");
        alertDialog.setView(txtName);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (txtName.getText() == null) return;
				
				// 入力値
				nick_name = txtName.getText().toString();
				
				// UUID生成
				uuid = commonUtils.getUUID();

				
				showProgress();
				
				// 端末登録ハンドラ
				APIResponseHandler handler = new APIResponseHandler(){
					
					@Override
					public void onRespond(Object result) {
						hideProgress();
						Log.d("MainActivity/initializeApplication", result.toString());
						try {
							JSONObject json = new JSONObject(result.toString());
							JSONObject info = json.getJSONObject("info");
							JSONObject user = json.getJSONObject("user");
							boolean status = info.getBoolean("status");
							if (status) {
								user_id = user.getString("id");
								commonUtils.putSharedPrefsValue(PREF_NAME_APP, "uuid", uuid);
								commonUtils.putSharedPrefsValue(PREF_NAME_APP, "user_id", user_id);
								showDialogMessage("登録成功", "ご登録ありがとうございます！", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										setupDisplay();
									}
								});
							} else {
								showDialogMessage("登録エラー", "登録に失敗しました。\nリトライして下さい。", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										showEntryDialog();
									}
								});
							}
						} catch (JSONException e) {
							e.printStackTrace();
							hideProgress();
							showDialogMessage("登録エラー", e.getMessage(), new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									showEntryDialog();
								}
							});
						}
					}
				};
        				
				// 端末登録
				try {
					new WebAPI(MainActivity.this).registByUuid(nick_name, uuid, handler);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
        alertDialog.show();
    }

    
    private void setupDisplay() {

		((TextView)findViewById(R.id.txtTitle)).setTextColor(Color.WHITE);
    	new WebAPI(this).getTeacherList(new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				Log.d("MainActivity/setupDisplay", result.toString());
				List<String> urlList = new ArrayList<String>();
				try {
					JSONObject json = new JSONObject(result.toString());
					JSONObject info = json.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = json.getJSONArray("list");
						final List<Teacher> teacherList = new ArrayList<Teacher>();
						for (int i=0; i<list.length(); i++) {
							JSONObject teacherJson = list.getJSONObject(i);
							String id = teacherJson.getString("id");
							String nickname = teacherJson.getString("nickname");
							String message = teacherJson.getString("message");
							String url = teacherJson.getString("url");
							if (url != null) {
								Log.d("URL", url);
								urlList.add(url);
							}

							Teacher teacher = new Teacher();
							teacher.setId(id);
							teacher.setNickname(nickname);
							teacher.setMessage(message);
							teacher.setUrl(url);
							teacherList.add(teacher);
						}
						GridView gridView = (GridView)findViewById(R.id.gvTeacher);
				    	gridView.setAdapter(new ImageGridViewAdapter(MainActivity.this, urlList));
				    	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								Teacher teacher = teacherList.get(position);
								openTimeTable(teacher);
							}
						});
				    	gridView.invalidate();
				    	Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.motion);
				    	gridView.setAnimation(animation);
				    	animation.start();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					showDialogMessage("システムエラー", e.getMessage(), null);
				}
			}
    	});
    	setupManager();
    }

    private void setupManager() {
    	
    	PhoneManager.getInstance().initializeManager(this, "888" + user_id, uuid, new PhoneRegistrationHandler() {

			@Override
			public void onRegistering(String localProfileUri) {
				Log.d(this.toString(), "SIP Registering. localProfileUri="+localProfileUri);
				((TextView)findViewById(R.id.txtTitle)).setTextColor(Color.YELLOW);
			}

			@Override
			public void onRegistrationDone(String localProfileUri, long expiryTime) {
				Log.d(this.toString(), "SIP Registration finished. localProfileUri="+localProfileUri);
				((TextView)findViewById(R.id.txtTitle)).setTextColor(Color.GREEN);
			}

			@Override
			public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
				Log.e(this.toString(), "SIP Registration failed. localProfileUri="+localProfileUri+",errorCode="+errorCode+",message="+errorMessage);
				((TextView)findViewById(R.id.txtTitle)).setTextColor(Color.RED);
			}

    	});
    }
    private void openTimeTable(Teacher teacher) {
    	Intent intent = new Intent(this, TimeTableActivity.class);
    	intent.putExtra("teacher_id", teacher.getId());
    	intent.putExtra("nickname", teacher.getNickname());
    	intent.putExtra("message", teacher.getMessage());
    	intent.putExtra("url", teacher.getUrl());
    	startActivity(intent);
    }
	/**
	 * 読み込み中ダイアログを表示する
	 */
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
	/**
	 * エラーダイアログを表示する
	 * @param message エラーメッセージ
	 * @param listener OKを押したときのハンドラ
	 */
	private void showDialogMessage(String title, String message, DialogInterface.OnClickListener listener) {
		if (errorDialog == null) {
			errorDialog = new AlertDialog.Builder(this);
		}
		errorDialog.setTitle(title);
		errorDialog.setMessage(message);
		errorDialog.setPositiveButton("OK", listener);
		errorDialog.show();
	}
	
	private class Teacher {
		private String id;
		private String uuid;
		private String email;
		private String nickname;
		private String url;
		private String message;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getNickname() {
			return nickname;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
	}
}
