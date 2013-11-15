package jp.co.iworks.koreang.phone;

import jp.co.iworks.koreang.R;
import android.net.sip.SipException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PhoneTalkingFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.phone_talking, container, false);
		Button btnEndTalking = (Button)view.findViewById(R.id.btnEndTalking);
		btnEndTalking.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					PhoneManager.getInstance().endTalk();
				} catch (SipException e) {
					e.printStackTrace();
				}
				getActivity().finish();
			}
		});
		return view;
	}

}
