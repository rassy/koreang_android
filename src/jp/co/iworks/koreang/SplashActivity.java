package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;
import jp.co.iworks.koreang.util.CommonUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * 初期化画面（スプラッシュ）
 * この画面がすべての起点になっている
 * この画面が破棄されるときはアプリが終了
 * @author tryumura
 *
 */
public class SplashActivity extends Activity {
	// お名前入力画面のリクエストコード
	private static final int REGIST_REQUEST_CODE = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setupApplication();
			}
		}, 3000);
	}

	/**
	 * アプリケーションのセットアップ
	 */
	private void setupApplication() {
		CommonUtils commonUtils = new CommonUtils(this);
    	String uuid = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "uuid");
    	String user_id = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "user_id");
    	
    	// ユーザー情報が保存されていたらSipManagerを初期化
    	if (uuid != null && user_id != null) {
    		Intent intent = new Intent(this, MainActivity.class);
    		startActivity(intent);
    		this.finish();
    	} else {
    		// ユーザー登録画面を起動
    		Intent intent = new Intent(this, RegisterActivity.class);
    		startActivityForResult(intent, REGIST_REQUEST_CODE);
    	}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		CommonUtils.LOG("requestCode=" + requestCode + ", resultCode=" + resultCode);
		if (requestCode == REGIST_REQUEST_CODE && resultCode == RESULT_OK) {
			// ユーザー登録が完了したらもう一度初期化する
			setupApplication();
		}
	}
	
}
