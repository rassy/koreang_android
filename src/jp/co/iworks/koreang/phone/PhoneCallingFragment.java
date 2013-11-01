package jp.co.iworks.koreang.phone;

import jp.co.iworks.koreang.R;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PhoneCallingFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.phone_calling, container, false);
		startCalling(view);
		return view;
	}
	
	private void startCalling(View view) {
		final MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.tel3);
		mp.setLooping(true);
		
		AudioManager am = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
		am.setStreamMute(AudioManager.STREAM_MUSIC, false);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, AudioManager.FLAG_SHOW_UI);
		
    	if (!mp.isPlaying()) {
    		mp.start();
    	}
		Intent intent = getActivity().getIntent();
		
        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                    	PhoneManager.getInstance().answerTalk();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            PhoneManager.getInstance().initializeTalk(intent, listener);

        } catch (Exception e) {
        	e.printStackTrace();
        	try {
				PhoneManager.getInstance().endTalk();
			} catch (SipException e1) {
				e1.printStackTrace();
			}
            if (mp.isPlaying()) {
            	mp.stop();
            }
        }
        
        Button btnReply = (Button)view.findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	            try {
	            	PhoneManager.getInstance().startTalk();
		            ((PhoneTalkActivity)getActivity()).onChangeStatus();
				} catch (SipException e) {
					e.printStackTrace();
				}
	            if (mp.isPlaying()) {
	            	mp.stop();
	            }
			}
		});
        
        Button btnReject = (Button)view.findViewById(R.id.btnReject);
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
				getActivity().finish();
			}
		});
	}
}
