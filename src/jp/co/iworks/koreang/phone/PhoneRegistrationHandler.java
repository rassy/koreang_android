package jp.co.iworks.koreang.phone;

public interface PhoneRegistrationHandler {
	void onRegistering(String localProfileUri);
	void onRegistrationDone(String localProfileUri, long expiryTime);
	void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage);
}
