package jp.co.iworks.koreang;

import static jp.co.iworks.koreang.Const.URL_TICKET_INDEX;
import static jp.co.iworks.koreang.Const.URL_USER_TICKET_REGIST;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import jp.co.iworks.koreang.web.KoreangHttpClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TicketFragment extends Fragment {

	private View currentView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		currentView = inflater.inflate(R.layout.ticket_list, container, false);
		setupDisplay();
		return currentView;
	}
	private void setupDisplay() {
		ListView listView = (ListView)currentView.findViewById(R.id.listTicket);
		final ArrayAdapter<Ticket> adapter = new TicketAdapter(getActivity(), R.layout.ticket_list_row);
		listView.setAdapter(adapter);
		
		KoreangHttpClient.get(getActivity(), URL_TICKET_INDEX, null, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				try {
					JSONObject info = response.getJSONObject("info");
					boolean status = info.getBoolean("status");
					if (status) {
						JSONArray list = response.getJSONArray("list");
						for (int i=0; i<list.length(); i++) {
							JSONObject ticket = list.getJSONObject(i);
							String quantity = ticket.getString("quantity");
							String amount = ticket.getString("amount");
							String ticket_name = ticket.getString("ticket_name");
							adapter.add(new Ticket(ticket_name, quantity, amount));
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	private class Ticket {
		private String name;
		private String quantity;
		private String amount;
		
		public Ticket (String name, String quantity, String amount) {
			this.name = name;
			this.quantity = quantity;
			this.amount = amount;
		}
		public String getName() {
			return this.name;
		}
		public String getQuantity() {
			return this.quantity;
		}
		
		public String getAmount() {
			return this.amount;
		}
	}
	
	private class TicketAdapter extends ArrayAdapter<Ticket> {
		private ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		private LayoutInflater inflater;
		private int layout;
		public TicketAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layout = textViewResourceId;
		}
		@Override
		public void add(Ticket object) {
			super.add(object);
			this.tickets.add(object);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = this.inflater.inflate(this.layout, null);
			}

			final Ticket ticket = this.tickets.get(position);
			NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.JAPAN);
			nf.setMaximumFractionDigits(0);
			final int amount = Integer.parseInt(ticket.getAmount());
			final int quantity = Integer.parseInt(ticket.getQuantity());
			String amountString = nf.format(amount);
			((TextView)view.findViewById(R.id.txtTicketName)).setText(ticket.getName());
			((TextView)view.findViewById(R.id.txtTicketAmount)).setText(amountString);
			Button btnBuy = (Button)view.findViewById(R.id.btnBuy);
			btnBuy.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 本来はin-app-billing経由
					postTicket(quantity, amount);
				}
			});
			return view;
		}
		private void postTicket(int quantity, int amount) {
			RequestParams params = new RequestParams();
			params.add("quantity", String.valueOf(quantity));
			params.add("amount", String.valueOf(amount));

			KoreangHttpClient.post(getActivity(), URL_USER_TICKET_REGIST, params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONObject response) {
					super.onSuccess(response);
					try {
						JSONObject info = response.getJSONObject("info");
						if (info.getBoolean("status")) {
							AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
							dialog.setTitle("購入手続きが完了しました");
							dialog.setMessage("ご購入ありがとうございます！");
							dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									((MainActivity)getActivity()).updateTicket();
								}
							});
							dialog.create().show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
						dialog.setTitle("購入処理に失敗しました。");
						dialog.setMessage("再度購入ボタンを押して下さい。");
						dialog.setPositiveButton("OK", null);
						dialog.create().show();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseBody, Throwable e) {
					super.onFailure(statusCode, headers, responseBody, e);
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					dialog.setTitle("通信エラー");
					dialog.setMessage(e.getMessage());
					dialog.setPositiveButton("OK", null);
					dialog.create().show();
				}
			});
		}
	}
}
