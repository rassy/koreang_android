package jp.co.iworks.koreang.phone;

import static jp.co.iworks.koreang.Const.URL_USER_TICKET_INDEX;

import java.text.NumberFormat;

import jp.co.iworks.koreang.R;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

public class PhoneTalkActivity extends FragmentActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.phone);
		KoreangHttpClient.get(this, URL_USER_TICKET_INDEX, null, new JsonHttpResponseHandler(){

  			@Override
  			public void onSuccess(JSONObject response) {
  				super.onSuccess(response);
  				TextView txtTicketBalance = (TextView)findViewById(R.id.txtTicketBalance);
  				try {
  	    			JSONObject info = response.getJSONObject("info");
  	    			boolean status = info.getBoolean("status");
  	    			
  	    			if (status) {
  	    				JSONObject ticket = response.getJSONObject("result");
  	    				int quantity = ticket.getInt("quantity");
  	    				NumberFormat nf = NumberFormat.getNumberInstance();        	
  	    				txtTicketBalance.setText(nf.format(quantity));
  	    				return;
  	    			}
  				} catch (JSONException e) {
  					e.printStackTrace();
      			}
  				txtTicketBalance.setText("0");
  			}
        });		
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
