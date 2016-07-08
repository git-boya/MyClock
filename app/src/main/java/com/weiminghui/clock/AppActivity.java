package com.weiminghui.clock;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

public class AppActivity extends Activity {
	
	private Button addClock;  //添加闹钟
    private TextView text;  
    private ListView listview;  //闹钟列表
    public MyAdapter adapter;  
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list_main);

        addClock = (Button)findViewById(R.id.add_clock);
        addClock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

                if (ClockService.clockDataArray.size() >= 5){
                    Toast.makeText(AppActivity.this , "最多5个设置5个闹钟" , Toast.LENGTH_LONG).show();
                    return ;
                }
				// TODO Auto-generated method stub
				Intent intent = new Intent(AppActivity.this , SetClockActivity.class);
				AppActivity.this.startActivity(intent);
			}
		});
        ClockService.loadClockData(this);
        startPushService();

        listview = (ListView)findViewById(R.id.listview);
        adapter = new MyAdapter(this);
        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
        return true ;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (featureId){
            case 0:
                showAbout();
                break;
        }
        return super.onMenuItemSelected(featureId, item);

    }

    public void startPushService(){
        Intent localIntent = new Intent();
        localIntent.setClass(this, ClockService.class); // 启动Service
        this.startService(localIntent);
    }

    private void showAbout(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about);
        dialog.setTitle("关于");

        TextView aboutText = (TextView) dialog.findViewById(R.id.about_text);
        aboutText.setText(Html.fromHtml("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " <p><strong>为魏明慧同学定制闹钟</strong></p>\n" +
                " <br><br><br>\n" +
                " <p>程序:李博雅</p>\n" +
                " <p>测试:魏明慧</p>\n" +
                "<p>版本:" + getVersionName() + "</p>" +
                " </body>\n" +
                "</html>"));


        Button aboutClose = (Button)dialog.findViewById(R.id.about_close);
        aboutClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialogWindow.setGravity(Gravity.CENTER);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
//        lp.x = 100; // 新位置X坐标
//        lp.y = 100; // 新位置Y坐标
        lp.width = width - 100; // 宽度
        lp.height = height - 200; // 高度
        lp.alpha = 0.7f; // 透明度
        dialogWindow.setAttributes(lp);
        dialog.show();
    }
	
	
	private class MyAdapter extends BaseAdapter {  
		  
        private Context context;  
        private LayoutInflater inflater;  
        public ArrayList<String> arr;  
        public MyAdapter(Context context) {  
            super();  
            this.context = context;  
            inflater = LayoutInflater.from(context);  
            
            
            
            
//            arr = new ArrayList<String>();
//            for(int i=0;i<ClockService.clockDataArray.size();i++){    //listview初始化3个子项
//                arr.add("");
//            }
        }  
        @Override  
        public int getCount() {  
            // TODO Auto-generated method stub  
            return ClockService.clockDataArray.size();
        }  
        @Override  
        public Object getItem(int arg0) {  
            // TODO Auto-generated method stub  
            return arg0;  
        }  
        @Override  
        public long getItemId(int arg0) {  
            // TODO Auto-generated method stub  
            return arg0;  
        }  
        @Override  
        public View getView(final int position, View view, ViewGroup arg2) {  
            // TODO Auto-generated method stub  
            if(view == null){  
                view = inflater.inflate(R.layout.list_item, null);
                ClockData clockData = ClockService.clockDataArray.get(position);
                RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.item_rl);
                final ToggleButton toggle = (ToggleButton)view.findViewById(R.id.toggleButton1);
                TextView text = (TextView)view.findViewById(R.id.item_text);
                text.setText("" + clockData.date);
                toggle.setChecked(clockData.toggle);
                if (clockData.toggle == true){
                    view.setBackgroundColor(Color.WHITE);
                }else{
                    view.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }


                toggle.setTag(position);
                rl.setTag(position);
                rl.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AppActivity.this , SetClockActivity.class);
                        intent.putExtra("index" ,(Integer) v.getTag());
                        AppActivity.this.startActivity(intent);
                        v.setEnabled(false);
                    }
                });
                toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        int tag = (Integer) buttonView.getTag();
                        if(isChecked){
                            //开启
                            ClockService.instance.addClock(tag);
                            ((RelativeLayout)buttonView.getParent()).setBackgroundColor(Color.WHITE);
                        }else{
//						关闭
                            ClockService.instance.cancelClock(tag);
                            ((RelativeLayout)buttonView.getParent()).setBackgroundColor(Color.parseColor("#DDDDDD"));
                        }
                        ClockData clock = ClockService.clockDataArray.get(tag);
                        clock.toggle = isChecked;
                        ClockService.clockDataArray.set(tag , clock);
                        ClockService.saveClockData();;
                    }
                });
            }

          
            return view;  
        }  
    }
    private String getVersionName()
    {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        String version = "";
        try{
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            version = packInfo.versionName;
        }catch (Exception e){

        }

        return version;
    }
}
