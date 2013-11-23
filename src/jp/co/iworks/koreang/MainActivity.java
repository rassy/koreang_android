package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.PREF_NAME_APP;
import static jp.co.iworks.koreang.Const.URL_USER_TICKET_INDEX;

import java.text.NumberFormat;

import jp.co.iworks.koreang.phone.PhoneManager;
import jp.co.iworks.koreang.phone.PhoneRegistrationHandler;
import jp.co.iworks.koreang.util.CommonUtils;
import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener {

    private TabHost mTabHost;
    private String mLastTabId;
    private boolean setup = false;
    private CommonUtils commonUtils = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		commonUtils = new CommonUtils(this);
        String uuid = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "uuid");
    	String user_id = commonUtils.getSharedPrefsValue(PREF_NAME_APP, "user_id");

    	CommonUtils.setUuid(uuid);
    	CommonUtils.setUserId(user_id);
    	
		setupDisplay();
    	setupManager(user_id, uuid);
    }

	@Override
	protected void onResume() {
		super.onResume();
		updateTicket();
	}
	@Override
	public void onTabChanged(String tabId) {
		if (mLastTabId != tabId) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			if ("search".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new TeacherListFragment());
			} else if ("reservation".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new ReservationListFragment());
			} else if ("favorite".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new FavoriteListFragment());
			} else if ("ticket".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new TicketFragment());
			} else if ("settings".equals(tabId)) {
				fragmentTransaction.replace(R.id.realtabcontent, new SettingsFragment());
			}
			mLastTabId = tabId;
			fragmentTransaction.commit();
		}
	}
	   /*
     * android:id/tabcontent のダミーコンテンツ
     */
    private static class DummyTabFactory implements TabContentFactory {

        /* Context */
        private final Context mContext;

        DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            return v;
        }
    }
    private class MainTabContentView extends FrameLayout {
    	LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	public MainTabContentView(Context context) {
    		super(context);
    	}
    	
    	public MainTabContentView(Context context, String title, int icon) {
    		this(context);
    		View childView = this.inflater.inflate(R.layout.tabwidget, null);
    		ImageView iv1 = (ImageView)childView.findViewById(R.id.imageview);
    		TextView tv1 = (TextView)childView.findViewById(R.id.textview);
    		tv1.setText(title);
    		iv1.setImageResource(icon);
    		addView(childView);
    	}
    }
    private void setupDisplay() {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.setCurrentTab(1);      

        View childView1 = new MainTabContentView(this, "予約する", R.drawable.ic_teacher_search);
        TabSpec tabTeacher = mTabHost.newTabSpec("search");
        tabTeacher.setIndicator(childView1);
        tabTeacher.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabTeacher);                      

        View childView2 = new MainTabContentView(this, "確認する", R.drawable.ic_reservation);
        TabSpec tabReservation = mTabHost.newTabSpec("reservation");
        tabReservation.setIndicator(childView2);
        tabReservation.setContent(new DummyTabFactory(this)); 
        mTabHost.addTab(tabReservation);         
        
        View childView3 = new MainTabContentView(this, "お気に入り", R.drawable.ic_favorite);
        TabSpec tabFavorite = mTabHost.newTabSpec("favorite");
        tabFavorite.setIndicator(childView3);
        tabFavorite.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabFavorite);
        
        View childView5 = new MainTabContentView(this, "チケット", R.drawable.ic_ticket);
        TabSpec tabTicket = mTabHost.newTabSpec("ticket");
        tabTicket.setIndicator(childView5);
        tabTicket.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabTicket);
        
        View childView4 = new MainTabContentView(this, "その他", R.drawable.ic_settings);
        TabSpec tabProfile = mTabHost.newTabSpec("settings");
        tabProfile.setIndicator(childView4);
        tabProfile.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tabProfile);
        
        mTabHost.setOnTabChangedListener(this);
        updateTicket();
    }
    public void updateTicket() {
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
    }
	/**
	 * SipManagerのセットアップ
	 * @param user_id ユーザーID
	 * @param uuid UUID
	 */
    private void setupManager(String user_id, String uuid) {
    	final TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
    	PhoneManager.getInstance().initializeManager(this, "888" + user_id, uuid, new PhoneRegistrationHandler() {

			@Override
			public void onRegistering(String localProfileUri) {
				CommonUtils.LOG("SIP Registering. localProfileUri="+localProfileUri);
				txtTitle.setTextColor(Color.YELLOW);
			}

			@Override
			public void onRegistrationDone(String localProfileUri, long expiryTime) {
				CommonUtils.LOG("SIP Registration finished. localProfileUri="+localProfileUri);
				txtTitle.setTextColor(Color.GREEN);
				if (!setup) {
					setup = true;
					onTabChanged("search");
				}
			}

			@Override
			public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
				CommonUtils.LOG("SIP Registration failed. localProfileUri="+localProfileUri+",errorCode="+errorCode+",message="+errorMessage);
				txtTitle.setTextColor(Color.RED);
			}
    	});
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PhoneManager.getInstance().closeManager(this);
	}
}
