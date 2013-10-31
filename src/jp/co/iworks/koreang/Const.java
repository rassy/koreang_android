package jp.co.iworks.koreang;

public class Const {
	public static final String PREF_NAME_APP = "Koreang";
	public static final String PREF_NAME_COOKIE = "Koreang_Cookie";
	
	public static final String ENV_DEVELOP = "49.212.216.205";
	public static final String ENV_RELEASE = "";
	public static final String ENV_LOCAL = "192.168.108.222";
	public static final String BASE_SCHEME = "http://";
	public static final String BASE_HOST = ENV_LOCAL;
	public static final String BASE_URL = BASE_SCHEME + BASE_HOST;
	public static final int WEBVIEW_REQUEST_CODE = 1234;
	
	public static final String SIP_SERVER = "192.168.108.222";
	public static final int SIP_REQUEST_CODE = 5678;
	
	public static final int TIMETABLE_REQUEST_CODE = 9012;
	/** URL関連 */
	public static final String URL_USER_REGIST = "/users/regist.json";
	public static final String URL_TEACHER_INDEX = "/teachers/index.json";
	public static final String URL_TIMETABLE_INDEX = "/timeTables/index.json";
	public static final String URL_RESERVATION_REGIST = "/reservations/regist.json";
}
