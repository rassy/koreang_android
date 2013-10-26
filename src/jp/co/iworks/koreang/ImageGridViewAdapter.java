package jp.co.iworks.koreang;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageGridViewAdapter extends BaseAdapter {
	private List<String> urlList;
	private LayoutInflater inflater;

	static class ViewHolder{
		ImageView iv_image;
	}

	public ImageGridViewAdapter(Context context, List<String> urlList) {
		this.urlList = urlList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.image_grid_view, null);
			// 画像
			holder.iv_image = (ImageView) view.findViewById(R.id.imagegridview_iv_image);
			// 画像の非同期DL
			new ImageDownloadTask(holder.iv_image, urlList.get(position)).execute();
			// 登録
			view.setTag(holder);
		} else {
			holder = (ViewHolder)view.getTag();
		}
		return view;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public int getCount() {
		return urlList.size();
	}
}
