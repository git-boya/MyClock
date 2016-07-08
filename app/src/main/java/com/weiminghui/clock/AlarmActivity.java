package com.weiminghui.clock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;

public class AlarmActivity extends Activity {
	public static MediaPlayer mMediaPlayer;
	private static AlarmActivity instance ;
	private AlarmReceiver receiver ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		instance = this ;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		//注册广播
		receiver = new AlarmReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		registerReceiver(receiver , new IntentFilter(Intent.ACTION_SCREEN_OFF));
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("闹钟");
		alert.setMessage("时间到了！");
		alert.setPositiveButton("知道了", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				closeClock();
			}
		});
		alert.setNegativeButton("再睡5分钟", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				// 实例化Intent
				Intent intent = new Intent(AlarmActivity.this, AlarmReceiver.class);
				// 设置Intent action属性
				intent.setAction("com.weiminghui.clock.service");
				// 实例化PendingIntent
				PendingIntent pi = PendingIntent.getBroadcast(AlarmActivity.this, 1999,
						intent, 0);
				// 获得系统时间
				long time = System.currentTimeMillis();
				am.set(AlarmManager.RTC_WAKEUP, time+1000 * 60 * 5 , pi);//5秒后闹铃
				closeClock();
			}
		});
		alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
//					if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_MUTE || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_HOME){
						if(mMediaPlayer != null){
							closeClock();
							return true ;
						}

//					}
				}
				return false;
			}
		});
//		alert.setCancelable(false);
//		alert.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		AlertDialog a = alert.create();
		a.show();
		//放在show下面
//		a.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		startAlarm();
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				closeClock();
//			}
//		} , 1000* 60 * 5);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
					|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_MUTE
					|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
					|| event.getKeyCode() == KeyEvent.KEYCODE_HOME){
				if(mMediaPlayer != null){
					closeClock();
					return true ;
				}

			}
		}
		return super.dispatchKeyEvent(event);
	}
//	@Override
//	public void onAttachedToWindow() {
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
//		super.onAttachedToWindow();
//	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK ||
				keyCode == KeyEvent.KEYCODE_HOME ||
				keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
				keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			closeClock();
			return true ;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onDestroy() {
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null ;
		}
		unregisterReceiver(receiver);
		super.onDestroy();

	}

	public static void closeClock(){
		instance.resumeOtherMusic();
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null ;
		}

		if(instance != null){
			instance.finish();// 关闭Activity
		}



	}
	private void startAlarm() {
		pauseOtherMusic();
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		try {
			AssetFileDescriptor fileDescriptor = getAssets().openFd("a_new_day.mp3");
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setLooping(true);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.reset();
			mMediaPlayer.setVolume(1, 1);
			mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                     fileDescriptor.getStartOffset(),
                     fileDescriptor.getLength());
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//暂停其他音乐
	private void pauseOtherMusic(){
		AudioManager am = (AudioManager)this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		am.requestAudioFocus(null , AudioManager.STREAM_MUSIC , AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}
	//恢复其他音乐
	private void resumeOtherMusic(){
		AudioManager am = (AudioManager)this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		am.abandonAudioFocus(null);
	}
}
