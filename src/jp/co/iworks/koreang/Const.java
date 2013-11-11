package jp.co.iworks.koreang;

public class Const {

	public static final int ENV_RELEASE = 0;
	public static final int ENV_STAGING = 1;
	public static final int ENV_DEVELOP = 2;
	
	/** 
	 * 以下は環境によって変更する
	 * ENV_STAGING/ENV_RELEASE/ENV_DEVELOP
	 */
	public static final int ENV = ENV_STAGING;
	
	public static final String PREF_NAME_APP = "Koreang";
	public static final String PREF_NAME_COOKIE = "Koreang_Cookie";

	public static final String HOST_STAGING = "202.181.102.159"; // sakura
	public static final String HOST_RELEASE = "202.181.102.159"; // sakura
	public static final String HOST_DEVELOP = "192.168.108.222"; // vmware
	
	public static final String BASE_SCHEME = "http://";
	public static final int WEBVIEW_REQUEST_CODE = 1234;
	
	public static final int SIP_REQUEST_CODE = 5678;
	
	public static final int TIMETABLE_REQUEST_CODE = 9012;
	/** URL関連 */
	public static final String URL_USER_REGIST = "/users/regist.json";
	public static final String URL_TEACHER_INDEX = "/teachers/index.json";
	public static final String URL_TEACHER_PROFILE_INDEX = "/teacherProfiles/index.json";
	public static final String URL_TIMETABLE_INDEX = "/timeTables/index.json";
	public static final String URL_RESERVATION_REGIST = "/reservations/regist.json";
	public static final String URL_RESERVATION_INDEX_BY_UUID = "/reservations/indexByUuid.json";
	public static final String URL_RESERVAION_CANCEL = "/reservations/cancel.json";
	
	public static final String BASE_HOST = HOST_STAGING;
	
	// 変更不要
	public static final String BASE_URL = BASE_SCHEME + BASE_HOST;
	
	/**
	 * SIPサーバーとWEBサーバーを同居させない場合は変更
	 */
	public static final String SIP_SERVER = BASE_HOST; // SIPサーバー
}
