package com.weiminghui.clock;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ClockService extends Service {
    private static final long INITIAL_RETRY_INTERVAL = 1000 * 60 * 60 * 24 * 2; //60分钟后执行
    public static final String TIME_FORM = "yyyy-MM-dd HH:mm";
    public static ArrayList<ClockData> clockDataArray;
    public static ClockService instance = null ;
    public ClockService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this ;
        if(clockDataArray == null){
            loadClockData(this);
        }
//启用前台服务，主要是startForeground()
        Notification notification = new Notification(R.drawable.ic_launcher, "用电脑时间过长了！白痴！", System.currentTimeMillis());
        notification.setLatestEventInfo(this, "快去休息！！！", "一定护眼睛,不然遗传给孩子，老婆跟别人跑啊。", null);        //设置通知默认效果
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        startForeground(1, notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(clockDataArray == null){
            loadClockData(this);
        }

        setClock();
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Intent localIntent = new Intent();
        localIntent.setClass(this, ClockService.class); // 销毁时重新启动Service
        this.startService(localIntent);
    }

    public static void loadClockData(Context content) {
        if(clockDataArray != null){
            clockDataArray.clear();
            clockDataArray = null ;
        }
        clockDataArray = new ArrayList<ClockData>();
        String clockStr = Utils.readRMS(content);
        if (clockStr != null && !clockStr.equals("")) {
            try {
                JSONArray jsonArray = new JSONArray(clockStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String dateStr = json.getString("date");
                    boolean toggle = json.getBoolean("toggle");
                    boolean powerOffClock = json.isNull("powerOffClock") ? false :json.getBoolean("powerOffClock");
                    ClockData clockData = new ClockData();
                    clockData.date = dateStr;
                    clockData.toggle = toggle;
                    clockData.powerOffClock = powerOffClock;
                    clockDataArray.add(clockData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void saveClockData (){
        JSONArray jsonArray = new JSONArray();
        for(ClockData clockData : clockDataArray){

            JSONObject json = new JSONObject();
            try {
                json.put("date" , clockData.date);
                json.put("toggle" , clockData.toggle);
                json.put("powerOffClock" , clockData.powerOffClock);
                jsonArray.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Utils.saveRMS(ClockService.instance , jsonArray.toString());

    }
    public void addClock(int index , Calendar calendar , boolean powerOffClock){
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.weiminghui.clock.service");
        PendingIntent sender = PendingIntent.getBroadcast(this, index, intent, 0);
//        calendar.add(Calendar.MONTH , 1);
//                long firstTime = SystemClock.elapsedRealtime();    // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        // 选择的每天定时时间
        long selectTime = calendar.getTimeInMillis();


        //若当前时间大于设定时间则循环加2天
        boolean status = true ;
        while (status){
            if(systemTime > selectTime){
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                selectTime = calendar.getTimeInMillis();
            }else{
                status = false ;
            }
        }
        Date newDate = new Date(selectTime);
        SimpleDateFormat sdf=new SimpleDateFormat(TIME_FORM);

        if(index < clockDataArray.size()){
            ClockData clockData = clockDataArray.get(index);
            clockData.date = sdf.format(newDate);
            clockData.powerOffClock = powerOffClock;
            clockDataArray.set(index , clockData);
        }else{
            ClockData clockData = new ClockData();
            clockData.date = sdf.format(newDate);
            clockData.toggle = true ;
            clockData.powerOffClock = powerOffClock;
            clockDataArray.add(clockData);
            saveClockData();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        int alarmType = AlarmManager.RTC ; //关机无响铃
        if(powerOffClock){
            alarmType = AlarmManager.RTC_WAKEUP; //关机有响铃
        }
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.setRepeating(alarmType,
                selectTime, INITIAL_RETRY_INTERVAL , sender);
    }
    public void addClock(int index){
        if (clockDataArray.size() <= index){
            return ;
        }
        ClockData clockData = clockDataArray.get(index);
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORM);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = sdf.parse(clockData.date);

            calendar.setTime(date);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
           addClock(index , calendar , clockData.powerOffClock);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void setClock() {
        cancelAllClock();
        for (int i = 0; i < clockDataArray.size(); i++) {
            addClock(i);
        }

        saveClockData();
    }
    public static Calendar getCalendar (String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORM);
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null ;
    }
    public static String getDateStr(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORM);
        return sdf.format(calendar.getTime());
    }
    private void cancelAllClock() {
        for (int i = 0; i < 5; i++) {
            cancelClock(i);
        }
    }
    public void cancelClock(int index){
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.weiminghui.clock.service");
        PendingIntent sender = PendingIntent.getBroadcast(this, index, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(sender);
    }
}
