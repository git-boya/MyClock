package com.weiminghui.clock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
	public static String readFile(String fileName){
		File file = new File(fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}
		
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));// 构造一个BufferedReader类来读取文件
			String s = null;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				result = result + "\n" + s;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}
	public static boolean writeFile(Context activity , String fileName , String info){
		 try {  
	            FileOutputStream fout = activity.openFileOutput(fileName, Activity.MODE_PRIVATE);  
	            byte[] bytes = info.getBytes();  
	            fout.write(bytes);  
	            fout.close();  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	            return false;
	        }  
		 return true ;
	}
	/**把数据源HashMap转换成json
	 * @param map
	 */
	public static String hashMapToJson(HashMap map) {
		String string = "{";
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			string += "'" + e.getKey() + "':";
			string += "'" + e.getValue() + "',";
		}
		string = string.substring(0, string.lastIndexOf(","));
		string += "}";
		return string;
	}
	// 读取
	public static String readRMS(Context context ) {
		SharedPreferences sharedPrefrences = context.getSharedPreferences("clockData.json", Context.MODE_WORLD_READABLE);
		return sharedPrefrences.getString("clockData", "");
	}

	// 保存
	public static void saveRMS(Context context , String value) {
		// 得到编辑器对象
		SharedPreferences.Editor editor = context.getSharedPreferences("clockData.json", Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString("clockData", value);
		editor.commit();
	}

}
