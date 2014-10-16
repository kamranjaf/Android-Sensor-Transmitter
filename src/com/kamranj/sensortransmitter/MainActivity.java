package com.kamranj.sensortransmitter;



import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.util.InetAddressUtils;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


@SuppressLint("ShowToast")
public class MainActivity extends Activity implements SensorEventListener, BluetoothAdapter.LeScanCallback {

	private String TAG="LogSensor";
	
	private LocationManager locationManager; // used to request location updated and get gps status
	private MyLocationListener gpslocationListener; // listener for GPS location updates - added/removed in Start/Stop button
	private MyLocationListener2 netlocationListener;
	
	private static final Double DEFAULT_RSS_MISSING_VALUE = -110.0;
	
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayList<Integer> mrss;
    private ArrayList<byte[]> madvdata;
    
	ServerHandler	server;
	Logfiles filelogger;
	
	private int sensors_port_number=6000;
	private int gps_port_number=6001;
	private int wifi_port_number=6002;
	private int ble_port_number=6003;
	
	Switch switch_accel;
	Switch switch_gyro;
	Switch switch_magnet;
	Switch switch_baro;
	Switch switch_wifi;
	Switch switch_ble;
	Switch switch_gps;
	

	Timer timer;
	MyTimerTask myTimerTask;
	 
    Toast mytoast;
    
    
    private float[] gyro = new float[3];
    private float[] magnet = new float[3];
    private float[] accel = new float[3];
    private float[] gravity = new float[3];
    private float[] linear_accel = new float[3];
    private float[] rotation_vector = new float[3];
    private float[] gamerotation_vector = new float[3];
    private float[] orientation = new float[3];
    private float[] Imat = new float[3];
    private float[] R1=new float[9];
    private double[] gps_data=new double[3];
    private float gps_bearing;
    private float gps_speed;
    private float gps_accuracy;
    private double[] netlocation_data=new double[2];
    private float netlocation_accuracy;
    private float pressure;
    
    boolean send_flag_gyro=false;
    boolean send_flag_accel=false;
    boolean send_flag_magnet=false;
    boolean send_flag_gravity=false;
    boolean send_flag_linear_accel=false;
    boolean send_flag_rotation_vector=false;
    boolean send_flag_gamerotation_vector=false;
    boolean send_flag_pressure=false;
    boolean send_flag_orientation=false;
    boolean send_flag_rotationmatrix=false;

    boolean	accel_available;
    boolean	gyro_available;
    boolean	magnet_available;
    boolean	baro_available;
    boolean	gamerotation_available=false;
    boolean ble_available;
    
    boolean wifiscanner_enable=true;
    boolean blescanner_enable=true;
    boolean gps_enable=true;
    boolean accel_enable=true;
    boolean gyro_enable=true;
    boolean magnet_enable=true;
    boolean baro_enable=true;
    
    
    private int counter_gyro=0;
    private int counter_accel=0;
    private int counter_magnet=0;
    private int counter_gravity=0;
    private int counter_linear_accel=0;
    private int counter_rotation_vector=0;
    private int counter_gamerotation_vector=0;
    private int counter_orientation=0;
    private int counter_rotationmatrix=0;
    private int counter_wifi=0;
    
    private static final int samples_per_pack = 2;
    private static final int MAX_WIFI_APs = 200;
    private static final int MAX_BLE_APs = 100;
    
    private float[] gyro_buff = new float[samples_per_pack*4];
    private float[] accel_buff = new float[samples_per_pack*4];
    private float[] magnet_buff = new float[samples_per_pack*4];
    private float[] gravity_buff = new float[samples_per_pack*4];
    private float[] linear_accel_buff = new float[samples_per_pack*4];
    private float[] rotation_vector_buff = new float[samples_per_pack*4];
    private float[] gamerotation_vector_buff = new float[samples_per_pack*4];
    private float[] pressure_buff = new float[2];
    private float[] gps_buff = new float[7];
    private float[] orientation_buff = new float[samples_per_pack*4];
    private float[] rotationmatrix_buff = new float[samples_per_pack*10];
    private int[] wifi_rss_buff= new int[MAX_WIFI_APs];
    private String[] wifi_mac_buff= new String[MAX_WIFI_APs];
    private int[] ble_rss_buff= new int[MAX_BLE_APs];
    private String[] ble_mac_buff= new String[MAX_BLE_APs];
    private float wifi_t;
    private float ble_t;
    
    private boolean start=false;
    
    // Time variables
	long timestamp=0;
	float real_timestamp;
	long reference_time=0;
	
	long timestamp2=0;
	float real_timestamp2;
	long reference_time2=0;
	
	// Sensor manager
	private SensorManager mSensorManager = null;
	
	// Wi-Fi scanner part
    int counter=1;
	WiFiScanReceiver receiver;
	WifiManager wifiManager = null;
	private ArrayList<String> visibleMAC;
	private LinkedHashMap<String, ArrayList<Double>> rssSamples;	

			
	TextView my_text;
	ToggleButton togglebutton1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new ArrayList<BluetoothDevice>();
        mrss = new ArrayList<Integer>();
        madvdata = new ArrayList<byte[]>();
        
		server=new ServerHandler();
		filelogger=new Logfiles();
		
		
		mytoast=Toast.makeText(getApplicationContext(), "Speed", Toast.LENGTH_LONG);
		mytoast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
		
	    

		 togglebutton1 = (ToggleButton) findViewById(R.id.toggleButton1);
		
		  switch_accel = (Switch) findViewById(R.id.switch_accel);
		  switch_accel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	accel_enable=true;
		    }else{
		    	accel_enable=false;
		    	
		    	
		    	// Don't allow disabling accel for now
		    	switch_accel.setChecked(true);
		    	accel_enable=true;
		    }

		   }
		  });
		  
		  
		  switch_gyro =  (Switch) findViewById(R.id.switch_gyro);
		  switch_gyro.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	gyro_enable=true;
		    }else{
		    	gyro_enable=false;
		    }

		   }
		  
		  });
		  
		  switch_magnet = (Switch) findViewById(R.id.switch_magnet);
		  switch_magnet.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	magnet_enable=true;
		    }else{
		    	magnet_enable=false;
		    }

		   }
		  });
		  
		  
		  
		  switch_baro = (Switch) findViewById(R.id.switch_baro);
		  switch_baro.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	baro_enable=true;
		    }else{
		    	baro_enable=false;
		    }

		   }
		  });
		  
		  
		  
		  switch_wifi = (Switch) findViewById(R.id.switch_wifi);
		  switch_wifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	wifiscanner_enable=true;
		    }else{
		    	wifiscanner_enable=false;
		    }

		   }
		  });
		  
		  
		  
		  switch_ble = (Switch) findViewById(R.id.switch_ble);
		  switch_ble.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	blescanner_enable=true;
		    }else{
		    	blescanner_enable=false;
		    }

		   }
		  });
		  
		  
		  
		  switch_gps = (Switch) findViewById(R.id.switch_gps);
		  switch_gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		   @Override
		   public void onCheckedChanged(CompoundButton buttonView,
		     boolean isChecked) {
		    
		    if(isChecked){
		    	gps_enable=true;
		    }else{
		    	gps_enable=false;
		    }

		   }
		  });
		  
		  
  
		
		// get the location manager object to use when adding location listeners and getting gps status
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);   
        gpslocationListener = new MyLocationListener();
        netlocationListener = new MyLocationListener2();
        // listener for location updates from gps added in Start Button listener and removed in Stop Button listener
        
		
		visibleMAC = new ArrayList<String>();
		rssSamples = new LinkedHashMap<String, ArrayList<Double>>();
		// Initialize sensors
	    mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
	    
	    
	    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	    	ble_available = false;
        }else{
        	ble_available = true;
        }
	    
	    /* Check available Sensors */
	    List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
	    for (int i = 0; i< deviceSensors.size(); i++) {
	        if (deviceSensors.get(i).getType() == Sensor.TYPE_PRESSURE) {
	            baro_available = true;
	        }
	        if (deviceSensors.get(i).getType() == Sensor.TYPE_ACCELEROMETER) {
	            accel_available = true;
	        }
	        if (deviceSensors.get(i).getType() == Sensor.TYPE_MAGNETIC_FIELD) {
	            magnet_available = true;
	        }
	        if (deviceSensors.get(i).getType() == Sensor.TYPE_GYROSCOPE) {
	            gyro_available = true;
	        }
	        if (deviceSensors.get(i).getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
	            gamerotation_available = true;
	        }
	    }
	    
	    
	    
		if (accel_available){  
			switch_accel.setChecked(true);
			accel_enable=true;
		}else{
			switch_accel.setChecked(false);
			switch_accel.setEnabled(false);
			accel_enable=false;
		}
		
		if (magnet_available){  
			switch_magnet.setChecked(true);
			magnet_enable=true;
		}else{
			switch_magnet.setChecked(false);
			switch_magnet.setEnabled(false);
			magnet_enable=false;
		}
		
		if (gyro_available){  
			switch_gyro.setChecked(true);
			gyro_enable=true;
		}else{
			switch_gyro.setChecked(false);
			switch_gyro.setEnabled(false);
			gyro_enable=false;
		}

		if (baro_available){  
			switch_baro.setChecked(true);
			baro_enable=true;
		}else{
			switch_baro.setChecked(false);
			switch_baro.setEnabled(false);
			baro_enable=false;
		}
		
		if (ble_available){  
			switch_ble.setChecked(true);
			blescanner_enable=true;
		}else{
			switch_ble.setChecked(false);
			switch_ble.setEnabled(false);
			blescanner_enable=false;
		}
		

	    switch_gps.setChecked(true);
	    switch_wifi.setChecked(true);
		
	    
	    // Initialize Wifi
		try {
			wifiManager = (WifiManager) getBaseContext()
					.getSystemService(Context.WIFI_SERVICE);
			//wifiManager.setWifiEnabled(false);
		} catch (Exception e) {
			Log.e(TAG,e.toString());
		}

		// Register Broadcast Receiver
		receiver=new WiFiScanReceiver();
		
		my_text= (TextView) findViewById(R.id.textView_ip);
		my_text.setText("");
		
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	        while (en.hasMoreElements()) {
	            NetworkInterface intf = en.nextElement();
	            
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
	                	my_text.append(intf.getDisplayName()+"   :   ");
	                	my_text.append(inetAddress.getHostAddress()+"\r\n");
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        
	    }
		
		loadPref();
		
		//start_process();
	}
	
	public void start_process(){
		if (!start){
		// Initialize log files
			filelogger.openLogFiles();
			if (wifiscanner_enable){
				registerReceiver(receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				wifiManager.startScan();
			}
		initListeners();
		reference_time=0;
		reference_time2=System.nanoTime();
		counter=1;
		rssSamples.clear();
		visibleMAC.clear();
		mDevices.clear();
		mrss.clear();
		madvdata.clear();
		
		if (gps_enable){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslocationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, netlocationListener);
		}
		
		if (blescanner_enable){
			mBluetoothAdapter.startLeScan(this);
		    if(timer != null){
		        timer.cancel();
		       }
		       
		       //re-schedule timer here
		       //otherwise, IllegalStateException of
		       //"TimerTask is scheduled already" 
		       //will be thrown
		       timer = new Timer();
		       myTimerTask = new MyTimerTask();		   
		       timer.schedule(myTimerTask, 1000, 1000);
		}
		
		// initialize variables
		int ii;
		for (ii=0;ii<samples_per_pack*4;ii++){
			gyro_buff[ii]=-1000;
			accel_buff[ii]=-1000;
			magnet_buff[ii]=-1000;
			gravity_buff[ii]=-1000;
			linear_accel_buff[ii]=-1000;
			rotation_vector_buff[ii]=-1000; 
			gamerotation_vector_buff[ii]=-1000; 
			orientation_buff[ii]=-1000; 
		}
		for (ii=0;ii<samples_per_pack*10;ii++){
			rotationmatrix_buff[ii]=-1000;
		}
		pressure_buff[0]=-1000;
		pressure_buff[1]=-1000;
		
		
		// Start server
		server.MAIN_PORT=sensors_port_number;
		server.GPS_PORT=gps_port_number;
		server.WIFI_PORT=wifi_port_number;
		server.BLE_PORT=ble_port_number;
		
		
		server.Start();
		start=true;
		}
	}

	
	public void stop_process(){
		if (start){
	        
			filelogger.closeLogFiles(visibleMAC,rssSamples);
		mSensorManager.unregisterListener(MainActivity.this);
		if (wifiscanner_enable)
			unregisterReceiver(receiver);
		if(blescanner_enable){
			mBluetoothAdapter.stopLeScan(this);
			if(timer != null){
		        timer.cancel();
		    }
		}
    	if (gps_enable){
    		locationManager.removeUpdates(gpslocationListener);
    		locationManager.removeUpdates(netlocationListener);    	
    	}
		server.Stop();
		start=false;
		}
	}
	
	
    @Override
    public void onStop() {
    	super.onStop();
    	//wifiManager.setWifiEnabled(true);
    	stop_process();
    }
	
    
    
    @Override
    protected void onPause() {
        super.onPause();
       // wifiManager.setWifiEnabled(true);
       stop_process();
       if (accel_available){  
			switch_accel.setEnabled(true);
			accel_enable=true;
		}else{
			switch_accel.setChecked(false);
			switch_accel.setEnabled(false);
			accel_enable=false;
		}
		
		if (magnet_available){  
			switch_magnet.setEnabled(true);
			magnet_enable=true;
		}else{
			switch_magnet.setChecked(false);
			switch_magnet.setEnabled(false);
			magnet_enable=false;
		}
		
		if (gyro_available){  
			switch_gyro.setEnabled(true);
			gyro_enable=true;
		}else{
			switch_gyro.setChecked(false);
			switch_gyro.setEnabled(false);
			gyro_enable=false;
		}

		if (baro_available){  
			switch_baro.setEnabled(true);
			baro_enable=true;
		}else{
			switch_baro.setChecked(false);
			switch_baro.setEnabled(false);
			baro_enable=false;
		}
		
		if (ble_available){  
			switch_ble.setEnabled(true);
			blescanner_enable=true;
		}else{
			switch_ble.setChecked(false);
			switch_ble.setEnabled(false);
			blescanner_enable=false;
		}


	    switch_gps.setEnabled(true);
	    switch_wifi.setEnabled(true);
	    

	 togglebutton1.setChecked(false);
    }
    
    
    
    @Override
    public void onResume() {
    	super.onResume();
    	
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    	//wifiManager.setWifiEnabled(false);
    	// restore the sensor listeners when user resumes the application.
    	//initListeners();
		//registerReceiver(receiver, new IntentFilter(
		//		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		my_text.setText("");
		
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	        while (en.hasMoreElements()) {
	            NetworkInterface intf = en.nextElement();
	            
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
	                	my_text.append(intf.getDisplayName()+"   :   ");
	                	my_text.append(inetAddress.getHostAddress()+"\r\n");
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        
	    }
    }
    
    
    
    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.
 //   @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@SuppressLint("InlinedApi")
	public void initListeners(){
		
    	if (accel_enable){
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME);
        	//filelogger.accel_enable=true;
    	}
    	
    	if (gyro_enable){
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_GAME);
        	//filelogger.gyro_enable=true;
    	}
    	
    	if (magnet_enable){
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME);
        	//filelogger.magnet_enable=true;
    	}
    	
    	if(accel_enable && gyro_enable && magnet_enable){
        
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
        		//filelogger.rotationvector_enable=true;
        		//filelogger.rotationmatrix_enable=true;
        		//filelogger.orientation_enable=true;
    	}
    	
    	if (baro_enable)
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);
        
    	if (accel_enable && gyro_enable){
    			mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
    	
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);
    	}
    	
    }

    
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	
		
	}
	
	
	  @Override
	    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		  	byte[] adv_data = new byte[31];
		  	System.arraycopy(scanRecord, 0, adv_data, 0, 31);
		  	
		  	//if (adv_data[0]==0x30 && adv_data[1]==0x30)
	        mDevices.add(device);
	        mrss.add(rssi);
	        madvdata.add(adv_data);
	    }

	
	@Override
	public void onSensorChanged(SensorEvent event) {
	
		if(server.MAIN_PORT_CONNECTED==1){
			server.MAIN_PORT_CONNECTED=0;
			mytoast.setText(server.MAIN_PORT_LOCAL_ADDRESS+"\r\n    is connected to \r\n"+server.MAIN_PORT_REMOTE_ADDRESS);		    
			mytoast.show();
		}
		
		if(server.GPS_PORT_CONNECTED==1){
			server.GPS_PORT_CONNECTED=0;
			mytoast.setText(server.GPS_PORT_LOCAL_ADDRESS+"\r\n    is connected to \r\n"+server.GPS_PORT_REMOTE_ADDRESS);		    
			mytoast.show();
		}
		
		if(server.WIFI_PORT_CONNECTED==1){
			server.WIFI_PORT_CONNECTED=0;
			mytoast.setText(server.WIFI_PORT_LOCAL_ADDRESS+"\r\n    is connected to \r\n"+server.WIFI_PORT_REMOTE_ADDRESS);		    
			mytoast.show();
		}
		
		if(server.BLE_PORT_CONNECTED==1){
			server.BLE_PORT_CONNECTED=0;
			mytoast.setText(server.BLE_PORT_LOCAL_ADDRESS+"\r\n    is connected to \r\n"+server.BLE_PORT_REMOTE_ADDRESS);		    
			mytoast.show();
		}
		
		if (start){
       timestamp = event.timestamp;
        
       if (reference_time==0){
    	   reference_time=timestamp;
    	   }
       
       
        real_timestamp=(float)(timestamp-reference_time)/1000000000.0f;
        
        if (!accel_enable){
        	send_flag_accel=true;
        	send_flag_gravity=true;
        	send_flag_linear_accel=true;
        	send_flag_rotation_vector=true;
        	send_flag_gamerotation_vector=true;
        	send_flag_orientation=true;
        	send_flag_rotationmatrix=true;
        }
        
        if (!gyro_enable){
        	send_flag_gyro=true;
        	send_flag_gravity=true;
        	send_flag_linear_accel=true;
        	send_flag_rotation_vector=true;
        	send_flag_gamerotation_vector=true;
        	send_flag_orientation=true;
        	send_flag_rotationmatrix=true;
        }
        
        if (!magnet_enable){
        	send_flag_magnet=true;
        	send_flag_rotation_vector=true;
        }
        
        if(!gamerotation_available){
        	send_flag_gamerotation_vector=true;
        }
        
        if (send_flag_gyro && send_flag_accel && send_flag_magnet && send_flag_gravity && send_flag_linear_accel && 
				send_flag_rotation_vector && send_flag_gamerotation_vector && send_flag_orientation && send_flag_rotationmatrix){
        	
        server.Write_Sensors(gyro_buff,accel_buff,magnet_buff,gravity_buff,linear_accel_buff,rotation_vector_buff
    			,gamerotation_vector_buff,orientation_buff,pressure_buff,rotationmatrix_buff);
        

		send_flag_gyro=false;
		send_flag_accel=false;
		send_flag_magnet=false;
		send_flag_gravity=false;
		send_flag_linear_accel=false;
		send_flag_rotation_vector=false;
		send_flag_gamerotation_vector=false;
		send_flag_orientation=false;
		send_flag_rotationmatrix=false;
		
        }
        
		switch(event.sensor.getType()) {
		
	    case Sensor.TYPE_ACCELEROMETER:
	    	
	    /*	if (blescanner_enable){
	    		counter_ble++;
	    		if (counter_ble==50){
	    			counter_ble=0;
	    			Send_BLE_results();
	    		}
	    	}*/
	    	
	    	System.arraycopy(event.values, 0, accel, 0, 3);
	    	accel_buff[counter_accel*4]=real_timestamp;
	    	accel_buff[counter_accel*4+1]=accel[0];
	    	accel_buff[counter_accel*4+2]=accel[1];
	    	accel_buff[counter_accel*4+3]=accel[2];
	    	counter_accel++;
	    	if (counter_accel==samples_per_pack){
				counter_accel=0;
				send_flag_accel=true;
			}
	    	try
	    	{
	    		filelogger.bfwriter_accel.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_accel.write(Float.toString(accel[0])+",");
	    		filelogger.bfwriter_accel.write(Float.toString(accel[1])+",");
	    		filelogger.bfwriter_accel.write(Float.toString(accel[2]));
	    		filelogger.bfwriter_accel.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	 
	    case Sensor.TYPE_GYROSCOPE:
	    	System.arraycopy(event.values, 0, gyro, 0, 3);
	    	gyro_buff[counter_gyro*4]=real_timestamp;
	    	gyro_buff[counter_gyro*4+1]=gyro[0];
	    	gyro_buff[counter_gyro*4+2]=gyro[1];
	    	gyro_buff[counter_gyro*4+3]=gyro[2];
	    	counter_gyro++;
	    	if (counter_gyro==samples_per_pack){
				counter_gyro=0;
				send_flag_gyro=true;
			}
	    	try
	    	{
	    		filelogger.bfwriter_gyro.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_gyro.write(Float.toString(gyro[0])+",");
	    		filelogger.bfwriter_gyro.write(Float.toString(gyro[1])+",");
	    		filelogger.bfwriter_gyro.write(Float.toString(gyro[2]));
	    		filelogger.bfwriter_gyro.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	 
	    case Sensor.TYPE_MAGNETIC_FIELD:
	    	System.arraycopy(event.values, 0, magnet, 0, 3);
	    	magnet_buff[counter_magnet*4]=real_timestamp;
	    	magnet_buff[counter_magnet*4+1]=magnet[0];
	    	magnet_buff[counter_magnet*4+2]=magnet[1];
	    	magnet_buff[counter_magnet*4+3]=magnet[2];
	    	counter_magnet++;
	    	if (counter_magnet==samples_per_pack){
				counter_magnet=0;
				send_flag_magnet=true;
			}
	    	try
	    	{
	    		filelogger.bfwriter_magnet.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_magnet.write(Float.toString(magnet[0])+",");
	    		filelogger.bfwriter_magnet.write(Float.toString(magnet[1])+",");
	    		filelogger.bfwriter_magnet.write(Float.toString(magnet[2]));
	    		filelogger.bfwriter_magnet.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}

	        break;
	        
	    case Sensor.TYPE_GRAVITY:
	    	System.arraycopy(event.values, 0, gravity, 0, 3);
	    	gravity_buff[counter_gravity*4]=real_timestamp;
	    	gravity_buff[counter_gravity*4+1]=gravity[0];
	    	gravity_buff[counter_gravity*4+2]=gravity[1];
	    	gravity_buff[counter_gravity*4+3]=gravity[2];
	    	counter_gravity++;
	    	if (counter_gravity==samples_per_pack){
				counter_gravity=0;
				send_flag_gravity=true;
			}
	    	SensorManager.getRotationMatrix(R1, Imat, gravity, magnet);
	    	SensorManager.getOrientation(R1, orientation);
	    	if (!magnet_enable){
	    		orientation[0]=-1000;
	    	}
	    	orientation_buff[counter_orientation*4]=real_timestamp;
	    	orientation_buff[counter_orientation*4+1]=orientation[0];
	    	orientation_buff[counter_orientation*4+2]=orientation[1];
	    	orientation_buff[counter_orientation*4+3]=orientation[2];
	    	counter_orientation++;
	    	if (counter_orientation==samples_per_pack){
				counter_orientation=0;
				send_flag_orientation=true;
			}
	    	try
	    	{
	    		filelogger.bfwriter_gravity.write(Float.toString(real_timestamp)+",");
	    	filelogger.bfwriter_gravity.write(Float.toString(gravity[0])+",");
	    	filelogger.bfwriter_gravity.write(Float.toString(gravity[1])+",");
	    	filelogger.bfwriter_gravity.write(Float.toString(gravity[2]));
	    	filelogger.bfwriter_gravity.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	    	
	    	// writing orientation and rotation matrix
	    	try
	    	{
	    		filelogger.bfwriter_orientation.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_orientation.write(Float.toString(orientation[0]*(float)(180.0/3.1415))+",");
	    		filelogger.bfwriter_orientation.write(Float.toString(orientation[1]*(float)(180.0/3.1415))+",");
	    		filelogger.bfwriter_orientation.write(Float.toString(orientation[2]*(float)(180.0/3.1415)));
	    		filelogger.bfwriter_orientation.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	        
	    case Sensor.TYPE_LINEAR_ACCELERATION:
	    	System.arraycopy(event.values, 0, linear_accel, 0, 3);
	    	linear_accel_buff[counter_linear_accel*4]=real_timestamp;
	    	linear_accel_buff[counter_linear_accel*4+1]=linear_accel[0];
	    	linear_accel_buff[counter_linear_accel*4+2]=linear_accel[1];
	    	linear_accel_buff[counter_linear_accel*4+3]=linear_accel[2];
	    	counter_linear_accel++;
	    	if (counter_linear_accel==samples_per_pack){
				counter_linear_accel=0;
				send_flag_linear_accel=true;
			}
	    	
	    	
	    	int ii;
	    	if (!magnet_enable){
	    		for (ii=0;ii<6;ii++){
	    			R1[ii]=-1000;
	    		}
	    	}
	    	
	    	
	    	
	    	rotationmatrix_buff[counter_rotationmatrix*10]=real_timestamp;
	    	for (int k=0;k<9;k++){
	    		rotationmatrix_buff[counter_rotationmatrix*10+k+1]=R1[k];
	    	}
	    	counter_rotationmatrix++;
	    	if (counter_rotationmatrix==samples_per_pack){
				counter_rotationmatrix=0;
				send_flag_rotationmatrix=true;
			}
	    	
	    	try
	    	{
	    		filelogger.bfwriter_linearaccel.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_linearaccel.write(Float.toString(linear_accel[0])+",");
	    		filelogger.bfwriter_linearaccel.write(Float.toString(linear_accel[1])+",");
	    		filelogger.bfwriter_linearaccel.write(Float.toString(linear_accel[2]));
	    		filelogger.bfwriter_linearaccel.newLine();
	    		filelogger.bfwriter_rotationmatrix.write(Float.toString(real_timestamp)+",");
	    	for (int k=0;k<8;k++){
	    		filelogger.bfwriter_rotationmatrix.write(Float.toString(R1[k])+",");
	    	}
	    	filelogger.bfwriter_rotationmatrix.write(Float.toString(R1[8]));
	    	filelogger.bfwriter_rotationmatrix.newLine();
	    	
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	        
	        
	    case Sensor.TYPE_PRESSURE:
	    	pressure=event.values[0];
	    	pressure_buff[0]=real_timestamp;
	    	pressure_buff[1]=pressure;
	    	try
	    	{
	    		filelogger.bfwriter_pressure.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_pressure.write(Float.toString(pressure));
	    	filelogger.bfwriter_pressure.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	    	break;
	        
	    case Sensor.TYPE_ROTATION_VECTOR:
	    	System.arraycopy(event.values, 0, rotation_vector, 0, 3);
	    	rotation_vector_buff[counter_rotation_vector*4]=real_timestamp;
	    	rotation_vector_buff[counter_rotation_vector*4+1]=rotation_vector[0];
	    	rotation_vector_buff[counter_rotation_vector*4+2]=rotation_vector[1];
	    	rotation_vector_buff[counter_rotation_vector*4+3]=rotation_vector[2];
	    	counter_rotation_vector++;
	    	if (counter_rotation_vector==samples_per_pack){
				counter_rotation_vector=0;
				send_flag_rotation_vector=true;
			}
	    	try
	    	{
	    		filelogger.bfwriter_rotationvector.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_rotationvector.write(Float.toString(rotation_vector[0])+",");
	    		filelogger.bfwriter_rotationvector.write(Float.toString(rotation_vector[1])+",");
	    		filelogger.bfwriter_rotationvector.write(Float.toString(rotation_vector[2]));
	    		filelogger.bfwriter_rotationvector.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	        
	    case Sensor.TYPE_GAME_ROTATION_VECTOR:
	    	System.arraycopy(event.values, 0, gamerotation_vector, 0, 3);
	    	SensorManager.getRotationMatrixFromVector(R1, gamerotation_vector);
	    	SensorManager.getOrientation(R1, orientation);
	    	gamerotation_vector_buff[counter_gamerotation_vector*4]=real_timestamp;
	    	gamerotation_vector_buff[counter_gamerotation_vector*4+1]=orientation[0];
	    	gamerotation_vector_buff[counter_gamerotation_vector*4+2]=orientation[1];
	    	gamerotation_vector_buff[counter_gamerotation_vector*4+3]=orientation[2];
	    	counter_gamerotation_vector++;
	    	if (counter_gamerotation_vector==samples_per_pack){
				counter_gamerotation_vector=0;
				send_flag_gamerotation_vector=true;
			}
	    	
	    	try
	    	{
	    		filelogger.bfwriter_gamerotationvector.write(Float.toString(real_timestamp)+",");
	    		filelogger.bfwriter_gamerotationvector.write(Float.toString(orientation[0])+",");
	    		filelogger.bfwriter_gamerotationvector.write(Float.toString(orientation[1])+",");
	    		filelogger.bfwriter_gamerotationvector.write(Float.toString(orientation[2]));
	    		filelogger.bfwriter_gamerotationvector.newLine();
	    	}
	    	catch(IOException e){
	    		Log.d(TAG,e.toString());
	    	}
	        break;
	        
	    }
		}
	}
	
	
	// Handling wifi samples
		public class WiFiScanReceiver extends BroadcastReceiver {
			
			@Override
			  public void onReceive(Context c, Intent intent) {
				
				timestamp2 = System.nanoTime();
		        real_timestamp2=(float)(timestamp2-reference_time2)/1000000000.0f;
		        wifi_t=real_timestamp2;
		        
				ArrayList<Double> list = null;
				list=new ArrayList<Double>();
				
				List<ScanResult> results = wifiManager.getScanResults();
				
				if (results != null){
		     // Update time column
				if (rssSamples.containsKey("Time")){
					list = rssSamples.get("Time");
					list.add(Double.valueOf(real_timestamp2));
					rssSamples.put("Time", list);
				}else{
					list.add(Double.valueOf(real_timestamp2));
					rssSamples.put("Time", list);
				}
				
				
				for (counter_wifi=0;counter_wifi<MAX_WIFI_APs;counter_wifi++){
					wifi_rss_buff[counter_wifi]=-200;
					wifi_mac_buff[counter_wifi]="00:00:00:00:00:00";
				}
				counter_wifi=0;
				
			    for (ScanResult result : results) {
			    	
			    	String name = result.BSSID;
					int level = result.level;
					
					wifi_rss_buff[counter_wifi]=level;
					wifi_mac_buff[counter_wifi]=name;
					counter_wifi++;
					
					//name.getBytes();
					if (!visibleMAC.contains(name)) {
						visibleMAC.add(name);
					}

						
					// insert MAC name and RSS
					if (rssSamples.containsKey(name)) {
						// insert RSS readings to existing entries
						list = rssSamples.get(name);

						for (int j = list.size(); j < counter - 1; j++) {
							list.add(DEFAULT_RSS_MISSING_VALUE);
						}
						list.add(Double.valueOf(level));
					} else {
						// insert new entries in sample list
						list = new ArrayList<Double>();
						for (int j = 0; j < counter - 1; j++) {
							list.add(DEFAULT_RSS_MISSING_VALUE);
						}
						list.add(Double.valueOf(level));					
					}
			    	
					rssSamples.put(name, list);
					//Log.d(TAG,Integer.toString(list.size()));
			    } 		    
		    	
			    counter++;
			    
				}

				server.Write_Wifi(wifi_t,wifi_rss_buff,wifi_mac_buff);
			    wifiManager.startScan();
				
			  }
		}
		
		
		
		
		// BLE Scanner
		private void Send_BLE_results(){
			
			timestamp2 = System.nanoTime();
	        real_timestamp2=(float)(timestamp2-reference_time2)/1000000000.0f;
	        ble_t=real_timestamp2;
	        
			for (int l=0;l<MAX_BLE_APs;l++){
				ble_rss_buff[l]=-200;
				ble_mac_buff[l]="00:00:00:00:00:00";
			}

			for (int l=0;l<mDevices.size();l++){
				
				ble_rss_buff[l]=mrss.get(l);
				ble_mac_buff[l]=mDevices.get(l).getAddress();
				
			}
			
			Log.d("BLE",Integer.toString(mDevices.size())+","+Float.toString(ble_t));
			
			 if(mDevices.size()>0)
				server.Write_Ble(ble_t,ble_rss_buff,ble_mac_buff,madvdata);
			
			mDevices.clear();
			mrss.clear();
			madvdata.clear();
			
		}
		
		
		
		// GPS
		//Methods in this class are called when the location providers give an update
		private class MyLocationListener implements LocationListener
			{
			
				
				
				// the provider has updated the location
		     public void onLocationChanged(Location location) 
		     {
		         timestamp2=System.nanoTime();
		         real_timestamp2=(float)(timestamp2-reference_time2)/1000000000.0f;
		         
		    	 gps_data[0]=location.getLatitude();
		    	 gps_data[1]=location.getLongitude();
		    	 gps_data[2]=location.getAltitude();
		    	 gps_bearing=location.getBearing();
		    	 gps_speed=location.getSpeed();
		    	 gps_accuracy=location.getAccuracy();
			     gps_buff[0]=real_timestamp;
			     gps_buff[1]=(float) gps_data[0];
			     gps_buff[2]=(float) gps_data[1];
			     gps_buff[3]=(float) gps_data[2];
			     gps_buff[4]=gps_bearing;
			     gps_buff[+5]=gps_speed;
			     gps_buff[+6]=gps_accuracy;
			     server.Write_Gps(gps_buff);
		    	 if (start){
			    	try
			    	{
			    		filelogger.bfwriter_gps.write(Float.toString(real_timestamp)+",");
			    		filelogger.bfwriter_gps.write(Double.toString(gps_data[0])+",");
			    		filelogger.bfwriter_gps.write(Double.toString(gps_data[1])+",");
			    		filelogger.bfwriter_gps.write(Double.toString(gps_data[2])+",");
			    		filelogger.bfwriter_gps.write(Float.toString(gps_bearing)+",");
			    		filelogger.bfwriter_gps.write(Float.toString(gps_speed)+",");
			    		filelogger.bfwriter_gps.write(Float.toString(gps_accuracy));
			    		filelogger.bfwriter_gps.newLine();
			    	}
			    	catch(IOException e){
			    		Log.d(TAG,e.toString());
			    	}
		    	 }
		     		//Log.d(TAG,Float.toString(real_timestamp));
		     }

		     // do nothing when status changed
		     public void onStatusChanged(String provider, int status, Bundle extras) 
		     {
		     }

		     // handled in GPS status listener 
		     public void onProviderEnabled(String provider) 
		     {
		     }

		     // handled in GPS status listener
		     public void onProviderDisabled(String provider) 
		     {       	
		     }

			}
		
		
		
			// Network Location
			//Methods in this class are called when the location providers give an update
		private class MyLocationListener2 implements LocationListener
				{
				
					// the provider has updated the location
			     public void onLocationChanged(Location location) 
			     {
			         timestamp2=System.nanoTime();
			         real_timestamp2=(float)(timestamp2-reference_time2)/1000000000.0f;
			         
			    	 netlocation_data[0]=location.getLatitude();
			    	 netlocation_data[1]=location.getLongitude();
			    	 netlocation_accuracy=location.getAccuracy();
			    	 gps_buff[0]=real_timestamp;
				     gps_buff[1]=(float) netlocation_data[0];
				     gps_buff[2]=(float) netlocation_data[1];
				     gps_buff[3]= (float) -100.0;
				     gps_buff[4]=0;
				     gps_buff[5]=0;
				     gps_buff[6]=netlocation_accuracy;
			    	 if (start){
				    	try
				    	{
				    		filelogger.bfwriter_netlocation.write(Float.toString(real_timestamp)+",");
				    	filelogger.bfwriter_netlocation.write(Double.toString(netlocation_data[0])+",");
				    	filelogger.bfwriter_netlocation.write(Double.toString(netlocation_data[1])+",");
				    	filelogger.bfwriter_netlocation.write(Float.toString(netlocation_accuracy));
				    	filelogger.bfwriter_netlocation.newLine();
				    	}
				    	catch(IOException e){
				    		Log.d(TAG,e.toString());
				    	}
			    	 }
			     		//Log.d(TAG,Float.toString(real_timestamp));
			     }

			     // do nothing when status changed
			     public void onStatusChanged(String provider, int status, Bundle extras) 
			     {
			     }

			     // handled in GPS status listener 
			     public void onProviderEnabled(String provider) 
			     {
			     }

			     // handled in GPS status listener
			     public void onProviderDisabled(String provider) 
			     {       	
			     }

				}
		
		
	public void onToggleClicked(View view) {
		    // Is the toggle on?
		    boolean on = ((ToggleButton) view).isChecked();
		    
		    if (on) {
		        start_process();
		        switch_accel.setEnabled(false);
		        switch_magnet.setEnabled(false);
		        switch_gyro.setEnabled(false);
		        switch_baro.setEnabled(false);
		        switch_gps.setEnabled(false);
		        switch_wifi.setEnabled(false);
		        switch_ble.setEnabled(false);
		    } else {
		        stop_process();
				if (accel_available){  
					switch_accel.setEnabled(true);
					accel_enable=true;
				}else{
					switch_accel.setChecked(false);
					switch_accel.setEnabled(false);
					accel_enable=false;
				}
				
				if (magnet_available){  
					switch_magnet.setEnabled(true);
					magnet_enable=true;
				}else{
					switch_magnet.setChecked(false);
					switch_magnet.setEnabled(false);
					magnet_enable=false;
				}
				
				if (gyro_available){  
					switch_gyro.setEnabled(true);
					gyro_enable=true;
				}else{
					switch_gyro.setChecked(false);
					switch_gyro.setEnabled(false);
					gyro_enable=false;
				}

				if (baro_available){  
					switch_baro.setEnabled(true);
					baro_enable=true;
				}else{
					switch_baro.setChecked(false);
					switch_baro.setEnabled(false);
					baro_enable=false;
				}
				
				if (ble_available){  
					switch_ble.setEnabled(true);
					blescanner_enable=true;
				}else{
					switch_ble.setChecked(false);
					switch_ble.setEnabled(false);
					blescanner_enable=false;
				}

			    switch_gps.setEnabled(true);
			    switch_wifi.setEnabled(true);

		    }
		}
		
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (start){
	        menu.getItem(0).setEnabled(false);
	    }else{
	    	menu.getItem(0).setEnabled(true);
	    }
	    return true;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	 Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
                 startActivityForResult(i, 0); 
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  // TODO Auto-generated method stub
	  //super.onActivityResult(requestCode, resultCode, data);
	  
	  /*
	   * To make it simple, always re-load Preference setting.
	   */
	  
	  loadPref();
	 }
	    
	 private void loadPref(){
	  SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	  
	  filelogger.use_date_time = mySharedPreferences.getBoolean("use_date_time", false);

	  filelogger.foldername = mySharedPreferences.getString("folder_name", "SensorTransmitter");
	  
	  sensors_port_number=Integer.valueOf(mySharedPreferences.getString("sensors_port_num", "6000"));
	  gps_port_number=Integer.valueOf(mySharedPreferences.getString("gps_port_num", "6001"));
	  wifi_port_number=Integer.valueOf(mySharedPreferences.getString("wifi_port_num", "6002"));
	  ble_port_number=Integer.valueOf(mySharedPreferences.getString("ble_port_num", "6003"));
	 }
	 
	 
	 class MyTimerTask extends TimerTask {

		  @Override
		  public void run() {
			  if (blescanner_enable){
		    			Send_BLE_results();
			  }
		  }
		  
		 }
}
