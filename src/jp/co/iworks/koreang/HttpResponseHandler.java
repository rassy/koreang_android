package jp.co.iworks.koreang;

public interface HttpResponseHandler {
	void onSuccess(String result);
	void onFailure(Throwable e);
}
