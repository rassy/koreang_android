package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;
import static jp.co.iworks.koreang.Const.URL_USER_REGIST;
import jp.co.iworks.koreang.util.CommonUtils;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * ユーザー登録画面
 * @author tryumura
 *
 */
public class RegisterActivity extends Activity {
	// プログレス
	private ProgressDialog progressDialog = null;
	private AlertDialog.Builder errorDialog = null;
	private CommonUtils commonUtils = null;
	private String uuid = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		commonUtils = new CommonUtils(this);
		setupDisplay();
	}
	
	// 端末登録ハンドラ
	private JsonHttpResponseHandler userRegistHandler = new JsonHttpResponseHandler(){
		
		@Override
		public void onSuccess(JSONObject response) {
			super.onSuccess(response);
			hideProgress();
			try {
				JSONObject info = response.getJSONObject("info");
				JSONObject user = response.getJSONObject("user");
				boolean status = info.getBoolean("status");
				if (status) {
					String user_id = user.getString("id");
					commonUtils.putSharedPrefsValue(PREF_NAME_APP, "uuid", uuid);
					commonUtils.putSharedPrefsValue(PREF_NAME_APP, "user_id", user_id);
					showDialogMessage("登録成功", "ご登録ありがとうございます！", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setResult(RESULT_OK);
							finish();
						}
					});
				} else {
					showErrorDialog();
				}
			} catch (JSONException e) {
				showErrorDialog();
			}
		}
	};
	private void setupDisplay() {
		final RegisterActivity that = this;
		final EditText txtName = (EditText)findViewById(R.id.txtNickNameEntry);
		final Button btnRegist = (Button)findViewById(R.id.btnRegistUser);
		btnRegist.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (txtName.getText() == null) return;
				
				showProgress();
				
				// 入力値
				String nickName = txtName.getText().toString();
				// UUID生成
				that.uuid = commonUtils.generateUUID();
	
				RequestParams params = new RequestParams();
				params.put("nickname", nickName);
				params.put("uuid", uuid);
				
				// 端末登録
				KoreangHttpClient.post(that, URL_USER_REGIST, params, userRegistHandler);
			}
		});
	}
	private void showErrorDialog() {
		showDialogMessage("登録エラー", "登録に失敗しました。\nリトライして下さい。", null);
	}
	/**
	 * 読み込み中ダイアログを表示する
	 */
	private void showProgress() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("登録しています...");
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

}
