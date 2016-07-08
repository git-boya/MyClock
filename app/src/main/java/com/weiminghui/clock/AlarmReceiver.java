package com.weiminghui.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @ClassName: AlarmReceiver  
 * @Description: 闹铃时间到了会进入这个广播，这个时候可以做一些该做的业务。
 * @author HuHood
 * @date 2013-11-25 下午4:44:30  
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	public final static String TAG = "AlarmReceiver";
	@Override
    public void onReceive(Context context, Intent intent) {
		Log.i(TAG , "action = " + intent.getAction());

		if(intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
			Intent localIntent = new Intent();
			localIntent.setClass(context, ClockService.class); // 启动Service
			context.startService(localIntent);
		}else if(intent.getAction() == "com.weiminghui.clock.service"){
			Toast.makeText(context, "闹铃响了, 可以做点事情了~~", Toast.LENGTH_LONG).show();
			Intent i=new Intent(context, AlarmActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}else if(intent.getAction() == Intent.ACTION_CLOSE_SYSTEM_DIALOGS || intent.getAction() == Intent.ACTION_SCREEN_OFF){
			AlarmActivity.closeClock();
		}



    }
	
	
}
