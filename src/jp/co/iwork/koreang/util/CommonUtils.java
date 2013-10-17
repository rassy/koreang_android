package jp.co.iwork.koreang.util;

import static jp.co.iworks.koreang.Const.BASE_URL;

import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CommonUtils {
	private Context mContext;
	private AlertDialog.Builder errorDialog;
	public CommonUtils(Context context) {
		mContext = context;
        errorDialog = new AlertDialog.Builder(context);
        errorDialog.setPositiveButton("OK", null);
	}

	public String getSharedPrefsValue(String preferenceName, String key) {
		SharedPreferences sharedPrefs = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		return sharedPrefs.getString(key, null);
	}
	public Map<String, ?> getSharedPrefsAll(String preferenceName) {
		SharedPreferences sharedPrefs = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		return sharedPrefs.getAll();
	}
	public void putSharedPrefsValue(String preferenceName, String key, String value) {
		SharedPreferences sharedPrefs = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
    /**
     * URLを取得します
     * @param path パス
     * @return ドメイン名+パス
     */
    public static String getUrl(String path) {
    	return getDomain() + path;
    }
    /**
     * ドメイン名を取得する
     * @return
     */
    private static String getDomain() {
    	return BASE_URL;
    }
    public void showErrorDialog(String message) {
    	errorDialog.setTitle("システムエラー");
    	errorDialog.setMessage(message);
    	errorDialog.show();
    }
}
