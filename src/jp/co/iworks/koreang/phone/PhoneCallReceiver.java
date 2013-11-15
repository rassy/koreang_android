package jp.co.iworks.koreang.phone;

import jp.co.iworks.koreang.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.koreang.INCOMMING_CALL")) {
			Log.d(this.toString(), intent.toString());
			Intent i = new Intent(context, PhoneTalkActivity.class);
			i.putExtras(intent.getExtras());
			((MainActivity)context).startActivityForResult(i, 1000);
		}
	}

}
