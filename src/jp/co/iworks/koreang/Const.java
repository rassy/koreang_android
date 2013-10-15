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
	public static final String URL_USER_LOGIN = "/lang/users/login";
	public static final String URL_USER_CHECK_LOGIN_STATUS = "/lang/users/checkLoginStatus";
	public static final String URL_USER_INDEX = "/lang/users/index";
	public static final String URL_USER_LOGIN_TOKEN = "/lang/users/loginByToken";
	public static final int WEBVIEW_REQUEST_CODE = 1234;
	
	public static final String SIP_SERVER = "192.168.108.222";
	public static final int SIP_REQUEST_CODE = 5678;
}
