package jp.co.iworks.koreang.phone;

import static jp.co.iworks.koreang.Const.URL_RESERVATION_CONSUME;
import jp.co.iworks.koreang.R;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
//		am.setStreamMute(AudioManager.STREAM_MUSIC, false);
//		am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, AudioManager.FLAG_SHOW_UI);
		am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
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
	            startTalk();
//				try {
//					PhoneManager.getInstance().startTalk();
//					((PhoneTalkActivity)getActivity()).onChangeStatus();
//				} catch (SipException e) {
//					e.printStackTrace();
//				}
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
	private void startTalk() {
		String userName = PhoneManager.getInstance().getPeerUserName();
		String teacher_id = userName.substring(3);
		RequestParams params = new RequestParams();
		params.add("teacher_id", teacher_id);
		KoreangHttpClient.get(getActivity(), URL_RESERVATION_CONSUME, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				try {
					JSONObject info = response.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
		            	PhoneManager.getInstance().startTalk();
			            ((PhoneTalkActivity)getActivity()).onChangeStatus();
					} else {
						showConsumeError();
					}
				} catch(SipException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseBody, Throwable e) {
				super.onFailure(statusCode, headers, responseBody, e);
				Log.d(this.toString(), responseBody);
				showConsumeError();
			}
			
		});
	}
	private void showConsumeError() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle("エラー");
		dialog.setMessage("チケットの消費に失敗しました。\nリトライします。");
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startTalk();
			}
		});
		dialog.create().show();
	}
}
