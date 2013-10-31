package jp.co.iworks.koreang.phone;

import jp.co.iworks.koreang.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PhoneTalkActivity extends Activity {

	private MediaPlayer mp = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.calling);
		
		mp = MediaPlayer.create(this, R.raw.tel3);
		mp.setLooping(true);
		
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.setStreamMute(AudioManager.STREAM_MUSIC, false);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, AudioManager.FLAG_SHOW_UI);
		
    	if (!mp.isPlaying()) {
    		mp.start();
    	}
		Intent intent = getIntent();
		
        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                    	Log.d("Ringing", "Ringing....");
                    	//incomingCall.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            PhoneManager.getInstance().initializeTalk(intent, listener);

        } catch (Exception e) {
        	e.printStackTrace();
//            if (incomingCall != null) {
//                incomingCall.close();
//            }
            if (mp.isPlaying()) {
            	mp.stop();
            }
        }
        
        Button btnReply = (Button)findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            try {
	            	PhoneManager.getInstance().startTalk();
		            
				} catch (SipException e) {
					e.printStackTrace();
				}
	            if (mp.isPlaying()) {
	            	mp.stop();
	            }
			}
		});
        
        Button btnReject = (Button)findViewById(R.id.btnReject);
        btnReject.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					PhoneManager.getInstance().endTalk();
				} catch (SipException e) {
					e.printStackTrace();
				}
				if (mp.isPlaying()) {
	            	mp.stop();
	            }
				finish();
			}
		});
	}
}
