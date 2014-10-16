package com.kamranj.sensortransmitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public class Logfiles {

	private static final Double DEFAULT_RSS_MISSING_VALUE = -110.0;
	
	private String TAG="LogSensor";
	private File logFile=null;
	public BufferedWriter bfwriter_gyro=null;
	public BufferedWriter bfwriter_magnet=null;
	public BufferedWriter bfwriter_accel=null;
	public BufferedWriter bfwriter_linearaccel=null;
	public BufferedWriter bfwriter_rotationvector=null;
	public BufferedWriter bfwriter_gravity=null;
	public BufferedWriter bfwriter_gamerotationvector=null;
	public BufferedWriter bfwriter_pressure=null;
	public BufferedWriter bfwriter_wifi=null;
	public BufferedWriter bfwriter_visiblemac=null;
	public BufferedWriter bfwriter_orientation=null;
	public BufferedWriter bfwriter_rotationmatrix=null;
	public BufferedWriter bfwriter_gps=null;
	public BufferedWriter bfwriter_netlocation=null;
	
	
/*	public boolean gyro_enable=false;
	public boolean magnet_enable=false;
	public boolean accel_enable=false;
	public boolean linearaccel_enable=false;
	public boolean rotationvector_enable=false;
	public boolean gravity_enable=false;
	public boolean gamerotationvector_enable=false;
	public boolean pressure_enable=false;
	public boolean wifi_enable=false;
	public boolean visiblemac_enable=false;
	public boolean orientation_enable=false;
	public boolean rotationmatrix_enable=false;
	public boolean gps_enable=false;
	*/
	
	public boolean use_date_time=false;
	public	String	foldername="SensorTransmitter";
////////////////////////////////////////////////////////
////////////IO related functions for saving log  ///
////////////////////////////////////////////////////////

	@SuppressLint("SimpleDateFormat")
	public void openLogFiles(){
		String FileName = "";
		boolean success = false;
		File root = Environment.getExternalStorageDirectory();
		File appDirectory = new File(root.toString()+"/"+foldername);
		if (!appDirectory.exists()) {
		    success = appDirectory.mkdir();
		}
		if (success){
			
		}else{
			
		}
		String currentDateandTime="";
		if (use_date_time){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			currentDateandTime = "_"+sdf.format(new Date());
		}else{
			currentDateandTime="";
		}
		//if (gyro_enable){
			FileName = "gyro";
			logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
			try {
				bfwriter_gyro=new BufferedWriter(new FileWriter(logFile));
			} catch (IOException e) {
				Log.e(TAG,e.toString());
			}
		//}

		//if (accel_enable){
		FileName = "accel";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_accel=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}

		//if (magnet_enable){
		FileName = "magnet";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_magnet=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}

		//if (linearaccel_enable){
		FileName = "linear_accel";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_linearaccel=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}

		//if (gravity_enable){
		FileName = "gravity";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_gravity=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (rotationvector_enable){
		FileName = "rotationvector";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_rotationvector=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}

		//if (gamerotationvector_enable){
		FileName = "gamerotationvector";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_gamerotationvector=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (pressure_enable){
		FileName = "pressure";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_pressure=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (wifi_enable){
		FileName = "wifi";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_wifi=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (wifi_enable){
		FileName = "visiblemac";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_visiblemac=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (orientation_enable){
		FileName = "orientation";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_orientation=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (rotationmatrix_enable){
		FileName = "rotationmatrix";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_rotationmatrix=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
		
		//if (gps_enable){
		FileName = "gps";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_gps=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
		Log.e(TAG,e.toString());
		}
		//}
		
		//if (gps_enable){
		FileName = "netlocation";
		logFile=new File(appDirectory.getAbsolutePath(), "/"+FileName+currentDateandTime+".csv");
		try {
			bfwriter_netlocation=new BufferedWriter(new FileWriter(logFile));
		} catch (IOException e) {
			Log.e(TAG,e.toString());
		}
		//}
	}

	public boolean closeLogFiles(ArrayList<String> visibleMAC, LinkedHashMap<String, ArrayList<Double>> rssSamples){
		try {
			bfwriter_gyro.close();
			bfwriter_magnet.close();
			bfwriter_accel.close();
			bfwriter_gravity.close();
			bfwriter_linearaccel.close();
			bfwriter_rotationvector.close();
			bfwriter_gamerotationvector.close();
			bfwriter_pressure.close();
			bfwriter_orientation.close();
			bfwriter_rotationmatrix.close();
			bfwriter_gps.close();
			bfwriter_netlocation.close();
			dump_wifi(visibleMAC,rssSamples);
			bfwriter_wifi.close();
			bfwriter_visiblemac.close();
			return true;
		} catch (IOException e) {
			Log.e(TAG,e.toString());
			return false;
		}
	}

	private void dump_wifi(ArrayList<String> visibleMAC, LinkedHashMap<String, ArrayList<Double>> rssSamples){

		ArrayList<Double> list = new ArrayList<Double>();

		list = rssSamples.get("Time");
		if (list!=null){

			int sizee=list.size();


			for (int j=0;j<visibleMAC.size();j++){
				list = rssSamples.get(visibleMAC.get(j));
				if (list==null){
					Log.d(TAG,"NULL");
					Log.d(TAG,visibleMAC.get(j));
				}
				for (int i=list.size();i<sizee;i++){
					list.add(DEFAULT_RSS_MISSING_VALUE);
				}
				rssSamples.put(visibleMAC.get(j), list);
			}	

			for (int i=0;i<sizee;i++){
				list = rssSamples.get("Time");
				try {
					bfwriter_wifi.write(Double.toString(list.get(i))+",");
					for (int j=0;j<(visibleMAC.size()-1);j++){
						list = rssSamples.get(visibleMAC.get(j));
						bfwriter_wifi.write(Double.toString(list.get(i))+",");
					}
					list = rssSamples.get(visibleMAC.get(visibleMAC.size()-1));
					bfwriter_wifi.write(Double.toString(list.get(i)));
					bfwriter_wifi.newLine();
				} catch (IOException e) {
					Log.d(TAG,e.toString());
				}
			}

			for (int j=0;j<visibleMAC.size();j++){
				try {
					bfwriter_visiblemac.write(visibleMAC.get(j));
					bfwriter_visiblemac.newLine();
				} catch (IOException e) {
					Log.d(TAG,e.toString());
				}
			}	
		}
	}

}
