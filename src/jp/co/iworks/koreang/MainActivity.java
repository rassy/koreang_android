package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;

import java.io.UnsupportedEncodingException;

import jp.co.iwork.koreang.util.CommonUtils;
import jp.co.iworks.koreang.phone.PhoneManager;
import jp.co.iworks.koreang.phone.PhoneRegistrationHandler;
import jp.co.iworks.koreang.web.WebAPI;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener {

	private CommonUtils commonUtils;
	private ProgressDialog progressDialog = null;
	private AlertDialog.Builder errorDialog = null;
	private String user_id = null;
	private String uuid = null;
	private String nick_name = null;
	// TabHost
    private TabHost mTabHost;

    private String mLastTabId;

// Activity LifeCycle
//////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();        

        TabSpec tabTeacher = mTabHost.newTabSpec("teacher");
        tabTeacher.setIndicator("キャスト", getResources().getDrawable(R.drawable.ic_teacher));                  
        tabTeacher.setContent(new DummyTabFactory(this)); 
        mTabHost.addTab(tabTeacher);                      

        TabSpec tagReservation = mTabHost.newTabSpec("reservation");
        tagReservation.setIndicator("予約一覧", getResources().getDrawable(R.drawable.ic_list));                  
        tagReservation.setContent(new DummyTabFactory(this)); 
        mTabHost.addTab(tagReservation);         
        
        TabSpec tabFavorite = mTabHost.newTabSpec("favorite");
        tabFavorite.setIndicator("お気に入り", getResources().getDrawable(R.drawable.ic_favorite));
        tabFavorite.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabFavorite);
        
        TabSpec tabProfile = mTabHost.newTabSpec("profile");
        tabProfile.setIndicator("プロフィール", getResources().getDrawable(R.drawable.ic_profile));
        tabProfile.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabProfile);
        
        mTabHost.setOnTabChangedListener(this);
        
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
	@Override
	public void onTabChanged(String tabId) {
		if (mLastTabId != tabId) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			if ("teacher".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new TeacherListFragment());
			} else if ("reservation".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new ReservationListFragment());
			} else if ("favorite".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new FavoriteListFragment());
			} else if ("profile".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new ProfileFragment());
			}
			mLastTabId = tabId;
			fragmentTransaction.commit();
		}
	}
	   /*
     * android:id/tabcontent のダミーコンテンツ
     */
    private static class DummyTabFactory implements TabContentFactory {

        /* Context */
        private final Context mContext;

        DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            return v;
        }
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
    	} else {
    		setupManager();
    		onTabChanged("teacher");
    	}
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
										setupManager();
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

	/**
	 * 読み込み中ダイアログを表示する
	 */
	public void showProgress() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Now Loading...");
		}
		progressDialog.show();
	}
	/**
	 * 読み込み中ダイアログを非表示にする
	 */
	public void hideProgress() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
	/**
	 * エラーダイアログを表示する
	 * @param message エラーメッセージ
	 * @param listener OKを押したときのハンドラ
	 */
	public void showDialogMessage(String title, String message, DialogInterface.OnClickListener listener) {
		if (errorDialog == null) {
			errorDialog = new AlertDialog.Builder(this);
		}
		errorDialog.setTitle(title);
		errorDialog.setMessage(message);
		errorDialog.setPositiveButton("OK", listener);
		errorDialog.show();
	}
}
