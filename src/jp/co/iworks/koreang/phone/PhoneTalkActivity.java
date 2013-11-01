package jp.co.iworks.koreang.phone;

import jp.co.iworks.koreang.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class PhoneTalkActivity extends FragmentActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.phone);
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.phoneContent, new PhoneCallingFragment());
		fragmentTransaction.commit();
	}
	public void onChangeStatus() {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.phoneContent, new PhoneTalkingFragment());
		fragmentTransaction.commit();
	}
}
