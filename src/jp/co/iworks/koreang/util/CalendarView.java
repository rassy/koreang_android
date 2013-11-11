package jp.co.iworks.koreang.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import jp.co.iworks.koreang.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 指定した年月日のカレンダーを表示するクラス
 */
public class CalendarView extends LinearLayout {
	/**
	 * 日付をクリックしたときのイベントリスナー
	 * @author tryumura
	 *
	 */
    public interface OnClickListener {
    	void onClick(Button button, Calendar calendar);
    }
    @SuppressWarnings("unused")
    private static final String TAG = CalendarView.class.getSimpleName();
    
    private static final int WEEKDAYS = 7;
    private static int MAX_WEEK = 0;
    // 週の始まりの曜日を保持する
    private static final int BIGINNING_DAY_OF_WEEK = Calendar.SUNDAY;

    // 通常の背景色 
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    
    // 年月表示部分
    private TextView mTitleView; 
    
    // 週のレイアウト
    private LinearLayout mWeekLayout;
    private ArrayList<LinearLayout> mWeeks = new ArrayList<LinearLayout>();
    private OnClickListener onClickListener = null;
    
    /**
     * コンストラクタ
     * 
     * @param context context
     */
    public CalendarView(Context context) {
        this(context, null);
    }
    
    /**
     * コンストラクタ
     * 
     * @param context context
     * @param attrs attributeset
     */
    @SuppressLint("SimpleDateFormat")
    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        
        createTitleView(context);
        createWeekViews(context);
        createDayViews(context);
        
        setTitle();
        setWeeks();
        setDays(context);
    }
    public void setOnClickListener(OnClickListener listener) {
    	this.onClickListener = listener;
    }
    /**
     * 年月日表示用のタイトルを生成する
     * 
     * @param context context
     */
    private void createTitleView(Context context) {
        float scaleDensity = context.getResources().getDisplayMetrics().density;
        
        mTitleView = new TextView(context);
        mTitleView.setGravity(Gravity.CENTER_HORIZONTAL); // 中央に表示
        mTitleView.setTextSize((int)(scaleDensity * 14));
        mTitleView.setTypeface(null, Typeface.BOLD); // 太字
        mTitleView.setPadding(0, 0, 0, (int)(scaleDensity * 16));
        
        addView(mTitleView, new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * 曜日表示用のビューを生成する
     * 
     * @param context context
     */
    private void createWeekViews(Context context) {
        float scaleDensity = context.getResources().getDisplayMetrics().density;
        // 週表示レイアウト
        mWeekLayout = new LinearLayout(context);
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, BIGINNING_DAY_OF_WEEK); // 週の頭をセット
        
        for (int i = 0; i < WEEKDAYS; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 0, (int)(scaleDensity * 4), 0);
            
            LinearLayout.LayoutParams llp = 
                    new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            llp.weight = 1;
            
            mWeekLayout.addView(textView, llp);
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        addView(mWeekLayout, new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    
    /**
     * 日付表示用のビューを生成する
     * 
     * @param context context
     */
    private void createDayViews(Context context) {
        float scaleDensity = context.getResources().getDisplayMetrics().density;
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        if (dayOfWeek == 0) {
        	MAX_WEEK = 1;
        } else {
        	MAX_WEEK = 2;
        }
        // カレンダー部 最大2行必要
        for (int i = 0; i < MAX_WEEK; i++) {
            LinearLayout weekLine = new LinearLayout(context);
            mWeeks.add(weekLine);
            
            // 1週間分の日付ビュー作成
            for (int j = 0; j < WEEKDAYS; j++) {
                Button dayView = new Button(context);
                dayView.setGravity(Gravity.CENTER); 
                //dayView.setPadding(0, (int)(scaleDensity * 4), (int)(scaleDensity * 4), 0);
                LinearLayout.LayoutParams llp = 
                        new LinearLayout.LayoutParams(0, (int)(scaleDensity * 48));
                llp.weight = 1;
                weekLine.addView(dayView, llp);
            }
            
            this.addView(weekLine, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * 年月をタイトルに設定する
     * 
     * @param year 年の指定
     * @param month 月の指定
     */
    @SuppressLint("SimpleDateFormat")
    private void setTitle() {
        Calendar targetCalendar = Calendar.getInstance();
        
        // 年月フォーマット文字列
        String formatString = mTitleView.getContext().getString(R.string.format_month_year);
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        mTitleView.setText(formatter.format(targetCalendar.getTime()));
    }

    /**
     * 曜日を設定する
     */
    @SuppressLint("SimpleDateFormat")
    private void setWeeks() {
        Calendar week = Calendar.getInstance();
        week.set(Calendar.DAY_OF_WEEK, BIGINNING_DAY_OF_WEEK); // 週の頭をセット
        SimpleDateFormat weekFormatter = new SimpleDateFormat("E"); // 曜日を取得するフォーマッタ
        for (int i = 0; i < WEEKDAYS; i++) {
            TextView textView = (TextView) mWeekLayout.getChildAt(i);
            textView.setText(weekFormatter.format(week.getTime())); // テキストに曜日を表示
            int dayOfWeek = week.get(Calendar.DAY_OF_WEEK);
            if (Calendar.SUNDAY == dayOfWeek) {
            	textView.setTextColor(Color.RED);
            } else if (Calendar.SATURDAY == dayOfWeek) {
            	textView.setTextColor(Color.BLUE);
            }
            week.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * 日付を設定していくメソッド
     * 
     * @param year 年の指定
     * @param month 月の指定
     */
    private void setDays(Context context) {
        Calendar todayCalendar = Calendar.getInstance();
        
        // 最初の空白を取得
        int skipCount = todayCalendar.get(Calendar.DAY_OF_WEEK) - 1;

        int dayCounter = 0;
        
        for (int i = 0; i < MAX_WEEK; i++) {
            final LinearLayout weekLayout = mWeeks.get(i);
            weekLayout.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
            for (int j = 0; j < WEEKDAYS; j++) {
                final Button dayView = (Button) weekLayout.getChildAt(j);
                
                // 第一週かつskipCountが残っていれば
                if (i == 0 && skipCount > 0) {
                    dayView.setEnabled(false);
                    skipCount--;
                    continue;
                }
                
                if (dayCounter >= 7) {
                	dayView.setEnabled(false);
                	dayCounter++;
                	continue;
                }
                // 日付を設定
                final Calendar targetCalendar = Calendar.getInstance();
                targetCalendar.add(Calendar.DAY_OF_MONTH, dayCounter);
                
                dayView.setText(String.valueOf(targetCalendar.get(Calendar.DAY_OF_MONTH)));
                dayView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						for (int m=0; m<MAX_WEEK; m++) {
							LinearLayout otherLayouts = mWeeks.get(m);
							for (int n=0; n<WEEKDAYS; n++) {
								Button otherViews = (Button)otherLayouts.getChildAt(n);
								otherViews.setTypeface(null, Typeface.NORMAL);
								otherViews.setTextColor(Color.BLACK);
							}
						}
						dayView.setTypeface(null, Typeface.BOLD);
						dayView.setTextColor(Color.MAGENTA);
						if (onClickListener != null) {
							onClickListener.onClick(dayView, targetCalendar);
						}
					}
				});
                dayCounter++;
            }
        }
    }

}