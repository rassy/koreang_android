package jp.co.iworks.koreang.phone;

import static jp.co.iworks.koreang.Const.SIP_REQUEST_CODE;
import static jp.co.iworks.koreang.Const.SIP_SERVER;
import jp.co.iwork.koreang.util.CommonUtils;
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
	public SipManager manager = null;
	private SipProfile profile = null;
	public SipAudioCall call = null;
	private PhoneCallReceiver phoneReceiver;
	private Context mContext;
	private CommonUtils commonUtils = null;
	public PhoneManager (Context context) {
		mContext = context;
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.koreang.INCOMMING_CALL");
		phoneReceiver = new PhoneCallReceiver();
		context.registerReceiver(phoneReceiver, filter);
		commonUtils = new CommonUtils(context);
	}
    /**
     * SIPマネージャを初期化する
     */
    public void initializeManager(String user_id, String password, final PhoneRegistrationHandler registrationHandler) {
        if(manager == null) {
        	manager = SipManager.newInstance(mContext);
        }
    	try {
			SipProfile.Builder builder = new SipProfile.Builder(user_id, SIP_SERVER);
			builder.setPassword(password);
			profile = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			commonUtils.showErrorDialog(e.toString());
			return;
		}
    	
    	Intent i = new Intent();
    	i.setAction("android.koreang.INCOMMING_CALL");
    	PendingIntent pi = PendingIntent.getBroadcast(mContext, SIP_REQUEST_CODE, i, Intent.FILL_IN_DATA);
    	
    	final Handler handler = new Handler();
    	try {
			manager.open(profile, pi, null);
	    	manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener(){
	
				@Override
				public void onRegistering(String arg0) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistering();
						}
					});
				}
	
				@Override
				public void onRegistrationDone(String arg0, long arg1) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistrationDone();
						}
					});
				}
	
				@Override
				public void onRegistrationFailed(String arg0, int arg1, String arg2) {
					Log.d(this.toString(), arg0);
					Log.d(this.toString(), arg2);
					handler.post(new Runnable() {
						@Override
						public void run() {
							registrationHandler.onRegistrationFailed();
						}
					});
				}
	    	});
		} catch (SipException e) {
			e.printStackTrace();
			commonUtils.showErrorDialog(e.toString());
		}
    }
    public void initializeCall(String sipAddress) {
    	try {
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
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    public void closeManager() {         
	    if (call != null) {
	        call.close();
	    }
        closeLocalProfile();
        if (phoneReceiver != null) {
            mContext.unregisterReceiver(phoneReceiver);
            Log.d("PhoneManager/closeManager", "Success to unregister receiver.");
        }
    }
    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (profile != null) {
                manager.close(profile.getUriString());
                Log.d("PhoneManager/closeLocalProfile", "Success to close local profile.");
            }
        } catch (Exception ee) {
            Log.d("PhoneManager/closeLocalProfile", "Failed to close local profile.", ee);
        }
    }
}
