package jp.co.iworks.koreang;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CommonUtils {
	private Context mContext;
	public CommonUtils(Context context) {
		mContext = context;
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
	
}
