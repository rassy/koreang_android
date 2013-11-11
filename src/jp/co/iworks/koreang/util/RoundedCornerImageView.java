package jp.co.iworks.koreang.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedCornerImageView extends ImageView {
    private static final String TAG = "RoundedCornerImageView";
    private static final float RADIUS = 40.0f;
  
    public RoundedCornerImageView(Context context) {
        super(context);
    }
  
    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
  
    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
  
    @Override
    public void setImageDrawable(Drawable d) {
        Bitmap b = ((BitmapDrawable) d).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
         
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  
        canvas.drawARGB(0, 0, 0, 0);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
         
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, RADIUS, RADIUS, paint);
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(b, rect, rect, paint);
        paint.setXfermode(null);
         
        super.setImageDrawable(new BitmapDrawable(bitmap));
    }
}
