package com.weiminghui.clock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 
 * @ClassName: SetClockActivity
 * @Description: 主界面
 * @author HuHood
 * @date 2013-11-25 下午4:01:19  
 *
 */
public class SetClockActivity extends Activity {

	private static final String TAG = SetClockActivity.class.getSimpleName();
	
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private Button mButton1;
	private Button mButton2;
	private Button mButtonCancel;
	private TextView mClockTime ;
	private ToggleButton powerOffClock ;
	private int mHour = -1;
	private int mMinute = -1;
	private int mYear = -1 ;
	private int mMonth = -1 ;
	private int mDay = -1 ;
	public static final long DAY = 1000L * 60 * 60 * 24;
	private Calendar calendar ;
	private int index = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.activity_main);
		powerOffClock = (ToggleButton)findViewById(R.id.powerOffClock);
		Intent intent = getIntent();
		index = intent.getIntExtra("index" , -1);
		if(index != -1){
			powerOffClock.setChecked(ClockService.clockDataArray.get(index).powerOffClock);
			calendar = ClockService.getCalendar(ClockService.clockDataArray.get(index).date);
		}else {
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
		}
		// 获取当前时间
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		if(mHour == -1 && mMinute == -1) {
			mYear = calendar.get(Calendar.YEAR);
			mMonth = calendar.get(Calendar.MONTH);
			mDay = calendar.get(Calendar.DAY_OF_MONTH);
			mHour = calendar.get(Calendar.HOUR_OF_DAY);
			mMinute = calendar.get(Calendar.MINUTE);

		}

		mTimePicker = (TimePicker)findViewById(R.id.timePicker);
		mTimePicker.setCurrentHour(mHour);
		mTimePicker.setCurrentMinute(mMinute);
		mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				
 				mHour = hourOfDay;
				mMinute = minute;
				calendar.set(Calendar.MINUTE, mMinute);
				calendar.set(Calendar.HOUR_OF_DAY, mHour);
				SimpleDateFormat sdf = new SimpleDateFormat(ClockService.TIME_FORM);
				mClockTime.setText("响铃时间:" + sdf.format(calendar.getTime()));
			}
		});

		mDatePicker = (DatePicker)findViewById(R.id.datePicker);
		mDatePicker.updateDate(mYear , mMonth , mDay);
		mDatePicker.getCalendarView().setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
				mYear = year;
				mMonth = month;
				mDay = dayOfMonth;
				calendar.set(Calendar.YEAR , mYear);
				calendar.set(Calendar.MONTH , mMonth);
				calendar.set(Calendar.DAY_OF_MONTH , mDay);
				SimpleDateFormat sdf = new SimpleDateFormat(ClockService.TIME_FORM);
				mClockTime.setText("响铃时间:" + sdf.format(calendar.getTime()));
			}
		});
		mClockTime = (TextView)findViewById(R.id.text_time);
		SimpleDateFormat sdf = new SimpleDateFormat(ClockService.TIME_FORM);
		mClockTime.setText("响铃时间:" + sdf.format(calendar.getTime()));

		mButton2 = (Button)findViewById( R.id.repeating_button);
		mButton2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (index == -1){
					ClockData clockData = new ClockData();
					clockData.date = ClockService.getDateStr(calendar);
					clockData.toggle = true ;
					clockData.powerOffClock = powerOffClock.isChecked();
//					ClockService.instance.addClock(ClockService.clockDataArray.size() , calendar , clockData.powerOffClock);
					ClockService.clockDataArray.add(clockData);

				}else{
					ClockData clockData = ClockService.clockDataArray.get(index);
					clockData.date = ClockService.getDateStr(calendar);
					clockData.powerOffClock = powerOffClock.isChecked();
					ClockService.clockDataArray.set(index , clockData);
//					ClockService.instance.setClock();
				}

				ClockService.saveClockData();


				backToAppActivity(null);

	            Toast.makeText(SetClockActivity.this, "设置闹铃成功! ", Toast.LENGTH_LONG).show();
			}
		});

		mButtonCancel = (Button)findViewById( R.id.cancel_button);
		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(index != -1){
					ClockService.clockDataArray.remove(index);
					ClockService.instance.cancelClock(index);
					ClockService.saveClockData();
				}
				backToAppActivity(null);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			backToAppActivity(null);
			return true ;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void backToAppActivity(Calendar calendar){
		Intent intent = new Intent(this , AppActivity.class);
		this.startActivity(intent);
		this.finish();
	}

}
