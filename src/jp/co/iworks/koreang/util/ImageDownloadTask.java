package jp.co.iworks.koreang.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

public class ImageDownloadTask {
	private ImageView iv;
	private String url;
	private Bitmap bitmap;

	public ImageDownloadTask(ImageView iv, String url){
		this.iv = iv;
		this.url = url;
	}

	public void execute(){
		final Handler mHandler = new Handler();
		new Thread(new Runnable() {
			public void run() {
				bitmap = getBitmap(url);
				if(bitmap == null) return;
				// ポスト処理
				mHandler.post(new Runnable() {
					public void run() {
						// 画像のセット
						iv.setImageBitmap(bitmap);
					}
				});
			}
		}).start();
	}

	/*
	 * 画像のダウンロード
	 */
	public Bitmap getBitmap(String url){
		Bitmap bitmap = null;
		try{
			BufferedInputStream in = new BufferedInputStream((InputStream) (new URL(url)).getContent());
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (Exception ex){
			return null;
		}
		return bitmap;
	}
}
