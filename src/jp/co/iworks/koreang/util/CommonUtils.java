package jp.co.iworks.koreang.util;

import static jp.co.iworks.koreang.Const.BASE_URL;
import static jp.co.iworks.koreang.Const.BASE_HOST;
import static jp.co.iworks.koreang.Const.ENV_RELEASE;
import static jp.co.iworks.koreang.Const.ENV;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * ユーティリティークラス
 * @author tryumura
 *
 */
public class CommonUtils {
	// コンテキスト
	private Context mContext;
	private AlertDialog.Builder errorDialog;
	
	public static Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
	/**
	 * コンストラクタ
	 * @param context
	 */
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
    
    public String getUUID() {
    	return UUID.randomUUID().toString();
    }
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
             
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
             
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void LOG(String message) {
    	if (ENV != ENV_RELEASE) {
    		Log.d("Koreang", message);
    	}
    }
}
