package jp.co.iworks.koreang.phone;

import static jp.co.iworks.koreang.Const.SIP_REQUEST_CODE;
import static jp.co.iworks.koreang.Const.SIP_SERVER;
import jp.co.iworks.koreang.util.CommonUtils;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Handler;
import android.util.Log;

public class PhoneManager {
	private SipManager manager = null;
	private SipProfile profile = null;
	public SipAudioCall call = null;
	private PhoneCallReceiver phoneReceiver;
	
	private CommonUtils commonUtils = null;

	private static PhoneManager instance = new PhoneManager();
	private PhoneManager() {
	}
	public static PhoneManager getInstance() {
		return instance;
	}
    /**
     * SIPマネージャを初期化する
     */
    public void initializeManager(Context context, String user_id, String password, final PhoneRegistrationHandler registrationHandler) {
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.koreang.INCOMMING_CALL");
		phoneReceiver = new PhoneCallReceiver();
		context.registerReceiver(phoneReceiver, filter);
		commonUtils = new CommonUtils(context);

        if(manager == null) {
        	manager = SipManager.newInstance(context);
        }
    	try {
			SipProfile.Builder builder = new SipProfile.Builder(user_id, SIP_SERVER);
			//builder.setPort(15060);
			builder.setPassword(password);
			profile = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			commonUtils.showErrorDialog(e.toString());
			return;
		}
    	try{
    		boolean isRegistered = manager.isRegistered(profile.getUriString());
    		if (isRegistered) {
    			return;
    		}
    	} catch (SipException e) {
    		e.printStackTrace();
    	}
    	Intent i = new Intent();
    	i.setAction("android.koreang.INCOMMING_CALL");
    	PendingIntent pi = PendingIntent.getBroadcast(context, SIP_REQUEST_CODE, i, Intent.FILL_IN_DATA);
    	
    	final Handler handler = new Handler();
    	try {
			manager.open(profile, pi, null);
	    	manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener(){
	
				@Override
				public void onRegistering(final String localProfileUri) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistering(localProfileUri);
						}
					});
				}
	
				@Override
				public void onRegistrationDone(final String localProfileUri, final long expiryTime) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistrationDone(localProfileUri, expiryTime);
						}
					});
				}
	
				@Override
				public void onRegistrationFailed(final String localProfileUri, final int errorCode, final String errorMessage) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistrationFailed(localProfileUri, errorCode, errorMessage);
						}
					});
				}
	    	});
		} catch (SipException e) {
			e.printStackTrace();
			commonUtils.showErrorDialog(e.toString());
		}
    }
    public void initializeCall(String sipAddress) throws SipException {
		SipAudioCall.Listener listener = new SipAudioCall.Listener() {

			@Override
			public void onCallEstablished(SipAudioCall call) {
				Log.d(this.toString(), "call established");
				call.startAudio();
				call.setSpeakerMode(true);
				call.toggleMute();
			}

			@Override
			public void onCallEnded(SipAudioCall call) {
				Log.d(this.toString(), "call ended");
				super.onCallEnded(call);
			}
		};
		call = manager.makeAudioCall(profile.getUriString(), sipAddress, listener, 30);
    	
    }
    public void answerTalk() {
    	try {
			call.answerCall(30);
		} catch (SipException e) {
			e.printStackTrace();
		}
    }
    public void initializeTalk(Intent intent, SipAudioCall.Listener listener) throws SipException {
    	call = manager.takeAudioCall(intent, listener);
    }
    public void startTalk() throws SipException {
		call.answerCall(30);
        call.startAudio();
        call.setSpeakerMode(true);
    }
    public void endTalk() throws SipException {
    	call.endCall();
    	call.close();

    }
    public String getPeerUserName() {
    	return call.getPeerProfile().getUserName();
    }
    public void closeManager(Context context) {         
	    if (call != null) {
	        call.close();
	    }
	    try {
	    	closeLocalProfile();
	    } catch (SipException e) {
	    	e.printStackTrace();
	    }
        if (phoneReceiver != null) {
            context.unregisterReceiver(phoneReceiver);
            Log.d("PhoneManager/closeManager", "Success to unregister receiver.");
        }
    }
    public void closeLocalProfile() throws SipException {
        if (manager == null) {
            return;
        }
        if (profile != null) {
            manager.close(profile.getUriString());
            Log.d("PhoneManager/closeLocalProfile", "Success to close local profile.");
        }
    }
}
