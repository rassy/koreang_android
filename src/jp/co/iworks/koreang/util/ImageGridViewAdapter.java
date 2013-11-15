package jp.co.iworks.koreang.util;

import java.io.File;
import java.util.List;

import jp.co.iworks.koreang.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;


public class ImageGridViewAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> urlList;
	private LayoutInflater inflater;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public ImageGridViewAdapter(Context context, List<String> urlList) {
		mContext = context;
		this.urlList = urlList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		        .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
		        .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
//		        .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//		        .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
		        .threadPoolSize(3) // default
		        .threadPriority(Thread.NORM_PRIORITY - 1) // default
		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        .memoryCacheSize(2 * 1024 * 1024)
		        .memoryCacheSizePercentage(13) // default
		        .discCache(new UnlimitedDiscCache(cacheDir)) // default
		        .discCacheSize(50 * 1024 * 1024)
		        .discCacheFileCount(100)
		        .discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
		        .imageDownloader(new BaseImageDownloader(context)) // default
		        //.imageDecoder(new BaseImageDecoder()) // default
		        .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
		        .writeDebugLogs()
		        .build();
		imageLoader.init(config);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ImageView imageView;
		View view = convertView;
		if (convertView == null) {
			view = (LinearLayout) inflater.inflate(R.layout.teacher_list_grid, parent, false);
		} else {
			view = (LinearLayout) convertView;
		}
		final LinearLayout layout = (LinearLayout)view;
		
		// 画像
		imageView = (ImageView) layout.findViewById(R.id.imagegridview_iv_image);

		imageLoader.displayImage(urlList.get(position), imageView);
		return view;
	}

	@Override
	public long getItemId(int position) {
		return position;
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
