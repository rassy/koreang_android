package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_COOKIE;
import static jp.co.iworks.koreang.Const.URL_USER_LOGIN;
import static jp.co.iworks.koreang.Const.WEBVIEW_REQUEST_CODE;

import java.util.Map;

import jp.co.iwork.koreang.util.CommonUtils;
import jp.co.iworks.koreang.phone.PhoneManager;
import jp.co.iworks.koreang.phone.PhoneRegistrationHandler;
import jp.co.iworks.koreang.web.WebAPI;
import jp.co.iworks.koreang.web.WebViewActivity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	public PhoneManager phoneManager = null;
	private CommonUtils commonUtils;
	private ProgressDialog progressDialog = null;
	private String user_id = null;
	private String nick_name = null;
	private String password = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        commonUtils = new CommonUtils(this);
        initializeApplication();
    }
    @Override
	protected void onRestart() {
		super.onRestart();
		initializeApplication();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        phoneManager.closeManager();
    }
    /**
     * アプリケーションを初期化します
     */
    private void initializeApplication() {
    	if (phoneManager == null) {
    		phoneManager = new PhoneManager(this);
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
        // トークンからログインしたときのレスポンスハンドラ
     	final APIResponseHandler tokenHandler = new APIResponseHandler() {
     		@Override
     		public void onRespond(Object object) {
     			hideProgress();
     			try {
     				JSONObject user = (JSONObject)object;
     				user_id = user.getString("id");
     				nick_name = user.getString("nickname");
     				password = user.getString("password");
     				setupDisplay();
     			} catch (Exception e) {
     				Log.d(this.toString(), object.toString());
     				e.printStackTrace();
     				commonUtils.showErrorDialog(e.toString());
     			}
     		}
     	};
     	
     	// 通常認証時のレスポンスハンドラ
     	final APIResponseHandler authHandler = new APIResponseHandler() {

			@Override
			public void onRespond(Object result) {
				hideProgress();
				if (result == null) {
					openLoginPage();
					return;
				}
				try {
	 				JSONObject user = (JSONObject)result;
	 				Log.d("MainActivity/initializeApplication", user.toString(4));
	 				user_id = user.getString("id");
	 				nick_name = user.getString("nickname");
	 				password = user.getString("password");
	 				setupDisplay();
	 			} catch (Exception e) {
	 				e.printStackTrace();
	 				Log.d(this.toString(), result.toString());
	 				commonUtils.showErrorDialog(e.toString());
				}
			}
     	};
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
        	Uri uri = intent.getData();
        	if (uri!=null) {
        		String token = uri.getQueryParameter("token");
        		showProgress();
        		new WebAPI(this).loginByToken(token, tokenHandler);
        	}
        } else {
        	Map<String, ?> cookies = commonUtils.getSharedPrefsAll(PREF_NAME_COOKIE);
        	if (cookies.entrySet().size() > 0) {
            	showProgress();
        		new WebAPI(this).authUser(authHandler);	
        	} else {
        		alertDialog.show();
        	}
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void setupDisplay() {
    	if (user_id == null || password == null) {
    		commonUtils.showErrorDialog("user_id or password is not found.");
    		return;
    	}
    	TextView txtNickname = (TextView)findViewById(R.id.txtNickname);
    	txtNickname.setText(nick_name);
    	
    	final TextView txtSipStatus = (TextView)findViewById(R.id.txtSipStatus);
    	final TextView txtRetry = (TextView)findViewById(R.id.txtRetry);
		txtRetry.setVisibility(View.INVISIBLE);
		txtRetry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setupDisplay();
			}
		});
		Log.d("MainActivity/setupDisplay", "user_id="+user_id+" password="+password);
    	phoneManager.initializeManager(user_id, password, new PhoneRegistrationHandler() {

			@Override
			public void onRegistering() {
				txtSipStatus.setText("Registarating with SIP Server...");
				txtSipStatus.setTextColor(Color.YELLOW);
			}

			@Override
			public void onRegistrationDone() {
				txtSipStatus.setText("Ready");
				txtSipStatus.setTextColor(Color.GREEN);
			}

			@Override
			public void onRegistrationFailed() {
				txtSipStatus.setText("Registration failed.");
				txtSipStatus.setTextColor(Color.RED);
				txtRetry.setVisibility(View.VISIBLE);
			}
    		
    	});
    	
    	Button btnTestCall = (Button)findViewById(R.id.btnTestCall);
    	btnTestCall.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				phoneManager.initializeCall();
			}
		});
    }
    /**
     * WebViewでログインページを表示する
     */
    private void openLoginPage() {
    	Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
    	intent.putExtra("url", CommonUtils.getUrl(URL_USER_LOGIN));
    	startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
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
				initializeApplication();
			}
		}
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
}
