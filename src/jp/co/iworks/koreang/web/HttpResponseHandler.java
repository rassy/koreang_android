package jp.co.iworks.koreang.web;

public interface HttpResponseHandler {
	void onSuccess(String result);
	void onFailure(Throwable e);
}
