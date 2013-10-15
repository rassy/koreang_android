package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.BASE_URL;
import static jp.co.iworks.koreang.Const.PREF_NAME_APP;
import static jp.co.iworks.koreang.Const.PREF_NAME_COOKIE;
import static jp.co.iworks.koreang.Const.SIP_SERVER;
import static jp.co.iworks.koreang.Const.SIP_REQUEST_CODE;
import static jp.co.iworks.koreang.Const.URL_USER_CHECK_LOGIN_STATUS;
import static jp.co.iworks.koreang.Const.URL_USER_LOGIN;
import static jp.co.iworks.koreang.Const.URL_USER_LOGIN_TOKEN;
import static jp.co.iworks.koreang.Const.WEBVIEW_REQUEST_CODE;

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private CommonUtils commonUtils;
	private ProgressDialog progressDialog;
	private String user_name;
	private String nick_name;
	private String password;
	
	public SipManager manager = null;
	public SipProfile profile = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeApplication();
    }
    @Override
	protected void onRestart() {
		super.onRestart();
		initializeApplication();
    }
    /**
     * アプリケーションを初期化します
     */
    private void initializeApplication() {
    	if (commonUtils == null) {
    		commonUtils = new CommonUtils(this);
    	}
    	
        // 初めてだったら会員登録画面を表示
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("初期設定");
        alertDialog.setMessage("この端末では初めてのご利用なので、ログインまたは会員登録が必要です。");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ログインページを表示
				openLoginPage();		
			}
		});
        
        // ブラウザなど他から軌道したかどうかを確認
    	loginByToken(new APIResponseHandler() {
    		@Override
    		public void onRespond(Object object) {
    			// トークンで認証していない場合
    			if ((Boolean)object) {
    				setupDisplay();
    			} else {
	    			// ユーザー情報が保存されているか確認
	    	        if (commonUtils.getSharedPrefsValue(PREF_NAME_COOKIE, "koreang[sessionid]") != null) {
	    	        	authUser(new APIResponseHandler() {
	    	        		@Override
	    	        		public void onRespond(Object result) {
	    	        			if ((Boolean)result) {
	    	        				setupDisplay();
	    	        			} else {
	    	        				openLoginPage();
	    	        			}
	    	        		}
	    	        	});
	    	        	return;
	    	        } else {
	    	        	openLoginPage();
	    	        }
    			}
    		}
    	});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void setupDisplay() {
    	TextView txtNickname = (TextView)findViewById(R.id.txtNickname);
    	txtNickname.setText(nick_name);
    	
    	initializeManager();
    }
    /**
     * WebViewでログインページを表示する
     */
    private void openLoginPage() {
    	Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
    	intent.putExtra("url", getUrl(URL_USER_LOGIN));
    	startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
    }
    /**
     * Intentをチェックして他アプリから起動されたか確認する
     * @return
     */
    @SuppressLint("HandlerLeak")
	private void loginByToken(final APIResponseHandler handler) {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
        	Uri uri = intent.getData();
        	if (uri!=null) {
        		String token = uri.getQueryParameter("token");
        		Log.d(this.toString(), token);
        		new WebAPI(this).get(getUrl(URL_USER_LOGIN_TOKEN + "/" + token), null, new HttpResponseHandler(){
					@Override
					public void onSuccess(String result) {
						JSONObject json;
						try {
							Log.d(this.toString(), result);
							json = new JSONObject(result);
							String userString = json.getString("User");
							JSONObject user = new JSONObject(userString);
							user_name = user.getString("name");
							nick_name = user.getString("nickname");
							commonUtils.putSharedPrefsValue(PREF_NAME_APP, "user_name", user_name);
							handler.onRespond(true);
						} catch (JSONException e) {
							e.printStackTrace();
							handler.onRespond(e);
						}
					}

					@Override
					public void onFailure(Throwable e) {
						e.printStackTrace();
						handler.onRespond(false);
					}
        		});
        	}
        }
        handler.onRespond(false);
    }
    /**
     * URLを取得します
     * @param path パス
     * @return ドメイン名+パス
     */
    private String getUrl(String path) {
    	return getDomain() + path;
    }
    /**
     * ドメイン名を取得する
     * @return
     */
    private String getDomain() {
    	return BASE_URL;
    }

    /**
     * WebViewからのレスポンス
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (WEBVIEW_REQUEST_CODE == requestCode) {
			Log.d(this.toString(), "onActivityResult. resultCode=" + resultCode);
			if (resultCode == RESULT_OK) {
				
			}
		}
	}
	/**
	 * ユーザー認証します
	 */
	@SuppressLint("HandlerLeak")
	private void authUser(final APIResponseHandler handler) {
		showProgress();
		new WebAPI(this).post(getUrl(URL_USER_CHECK_LOGIN_STATUS + ".json"), null, new HttpResponseHandler(){
			@Override
			public void onSuccess(String result) {
				hideProgress();
				try {
					JSONObject json = new JSONObject(result);
					if (json.getBoolean("status")) {
						JSONObject user = json.getJSONObject("User");
						user_name = user.getString("name");
						nick_name = user.getString("nickname");
						password = user.getString("password");
						handler.onRespond(true);
					} else {
						handler.onRespond(false);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					user_name = "";
					handler.onRespond(false);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				hideProgress();
				handler.onRespond(false);
				openLoginPage();
			}
		});
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
//////////////////////////////////////////////////////////////////////
    /**
     * SIPマネージャを初期化する
     */
    public void initializeManager() {
        if(manager == null) {
          manager = SipManager.newInstance(this);
        }
        initializeLocalProfile();
    }
    public void initializeLocalProfile() {
    	try {
			SipProfile.Builder builder = new SipProfile.Builder(user_name, SIP_SERVER);
			builder.setPassword(password);
			profile = builder.build();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	Intent i = new Intent();
    	i.setAction("android.koreang.INCOMMING_CALL");
    	PendingIntent pi = PendingIntent.getBroadcast(this, SIP_REQUEST_CODE, i, Intent.FILL_IN_DATA);
    	try {
			manager.open(profile, pi, null);
	    	manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener(){
	
				@Override
				public void onRegistering(String arg0) {
					Log.d(this.toString(), "Registrating with SIP Server...");
				}
	
				@Override
				public void onRegistrationDone(String arg0, long arg1) {
					Log.d(this.toString(), "Ready");
				}
	
				@Override
				public void onRegistrationFailed(String arg0, int arg1, String arg2) {
					Log.d(this.toString(), "Registration failed.");
				}
	    	});
		} catch (SipException e) {
			e.printStackTrace();
		}
    }
}
