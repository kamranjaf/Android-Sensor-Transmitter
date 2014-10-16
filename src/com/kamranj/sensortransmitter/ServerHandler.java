package com.kamranj.sensortransmitter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;


public class ServerHandler {
    
	public int MAIN_PORT=6000;
    public int GPS_PORT=6001;
    public int WIFI_PORT=6002;
    public int BLE_PORT=6003;
    
    public int MAIN_PORT_CONNECTED=0;
    public int GPS_PORT_CONNECTED=0;
    public int WIFI_PORT_CONNECTED=0;
    public int BLE_PORT_CONNECTED=0;
    
    public String MAIN_PORT_REMOTE_ADDRESS="";
    public String GPS_PORT_REMOTE_ADDRESS="";
    public String WIFI_PORT_REMOTE_ADDRESS="";
    public String BLE_PORT_REMOTE_ADDRESS="";
    
    public String MAIN_PORT_LOCAL_ADDRESS="";
    public String GPS_PORT_LOCAL_ADDRESS="";
    public String WIFI_PORT_LOCAL_ADDRESS="";
    public String BLE_PORT_LOCAL_ADDRESS="";
    
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
    private float[] orientation_buff = new float[samples_per_pack*4];
    private float[] rotationmatrix_buff = new float[samples_per_pack*10];
    private float[] gps_buff = new float[7];
    private int[] wifi_rss_buff= new int[MAX_WIFI_APs];
    private String[] wifi_mac_buff= new String[MAX_WIFI_APs];
    private float wifi_t;
    private int[] ble_rss_buff= new int[MAX_BLE_APs];
    private String[] ble_mac_buff= new String[MAX_BLE_APs];
    private ArrayList<byte[]> ble_advdata_buff = new ArrayList<byte[]>();
    private float ble_t;
    
    
    private boolean send_flag_sensors=false;
    private boolean send_flag_wifi=false;
    private boolean send_flag_ble=false;
    private boolean send_flag_gps=false;
    
    
    private ServerSocket serverSocket_MAIN;
    private ServerSocket serverSocket_gps;
    private ServerSocket serverSocket_wifi;
    private ServerSocket serverSocket_ble;
    
    
    private Thread serverThread_MAIN = null;
    private Thread serverThread_gps = null;
    private Thread serverThread_wifi = null;
    private Thread serverThread_ble = null;

	public ServerHandler() {
		// TODO Auto-generated constructor stub
	}

	
	public void Start(){
		
		serverThread_MAIN = new Thread(new serverThread_MAIN());
		serverThread_gps = new Thread(new ServerThread_gps());
		serverThread_wifi = new Thread(new ServerThread_wifi());
		serverThread_ble = new Thread(new ServerThread_ble());
		
		serverThread_MAIN.start();
		serverThread_gps.start();
		serverThread_wifi.start();
		serverThread_ble.start();
		
	}
	
	public void Stop(){
    	serverThread_MAIN.interrupt();
		serverThread_gps.interrupt();
		serverThread_wifi.interrupt();
		serverThread_ble.interrupt();
		
    	try {
			serverSocket_MAIN.close();
			serverSocket_gps.close();
			serverSocket_wifi.close();
			serverSocket_ble.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void Write_Sensors(float[] gyro,float[] accel,float[] magnet,float[] gravity,float[] linear_accel,float[] rotation_vector
			,float[] gamerotation_vector,float[] orientation,float[] pressure,float[] rotationmatrix){
		int i;
		for (i=0;i<samples_per_pack*4;i++){
			gyro_buff[i]=gyro[i];
			magnet_buff[i]=magnet[i];
			accel_buff[i]=accel[i];
			gravity_buff[i]=gravity[i];
			linear_accel_buff[i]=linear_accel[i];
			rotation_vector_buff[i]=rotation_vector[i];
			gamerotation_vector_buff[i]=gamerotation_vector[i];
			orientation_buff[i]=orientation[i];
		}
		for (i=0;i<2;i++){
			pressure_buff[i]=pressure[i];
		}
		for (i=0;i<samples_per_pack*10;i++){
			rotationmatrix_buff[i]=rotationmatrix[i];
		}
		send_flag_sensors=true;
	}
	
	
	public void Write_Wifi(float wifi_time,int[] wifi_rss,String[] wifi_mac){
		int i;
		for (i=0;i<MAX_WIFI_APs;i++){
			wifi_rss_buff[i]=wifi_rss[i];
			wifi_mac_buff[i]=wifi_mac[i];
		}
		send_flag_wifi=true;
	}
	
	public void Write_Ble(float ble_time,int[] ble_rss,String[] ble_mac, ArrayList<byte[]> madvdata){
		int i;
		ble_advdata_buff.clear();
		for (i=0;i<MAX_BLE_APs;i++){
			ble_rss_buff[i]=ble_rss[i];
			ble_mac_buff[i]=ble_mac[i];
			if(i<madvdata.size()){
				ble_advdata_buff.add(madvdata.get(i));
			}else{
				ble_advdata_buff.add(new byte[31]);
			}
		}
		ble_t = ble_time;
		send_flag_ble=true;
	}
	
	public void Write_Gps(float[] gps){
		int i;
		for (i=0;i<7;i++){
			gps_buff[i]=gps[i];
		}
		send_flag_gps=true;
	}
/////////////////////// TCP/IP Handling
//////////////////////


class serverThread_MAIN implements Runnable {

	public void run() {
		Socket socket = null;
		try {
			serverSocket_MAIN = new ServerSocket(MAIN_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (!Thread.currentThread().isInterrupted()) {

			try {

				socket = serverSocket_MAIN.accept();
				//Log.d("serverrr",socket.getLocalSocketAddress().toString());
				MAIN_PORT_CONNECTED=1;
				MAIN_PORT_REMOTE_ADDRESS=socket.getRemoteSocketAddress().toString().substring(1);
				MAIN_PORT_LOCAL_ADDRESS=socket.getLocalSocketAddress().toString().substring(1);
				//serverSocket.getLocalSocketAddress().toString()
				//text.setText(serverSocket.getLocalSocketAddress().toString());
	CommunicationThread_MAIN commThread = new CommunicationThread_MAIN(socket);
Thread a=new Thread(commThread);
a.start();

} catch (IOException e) {
e.printStackTrace();
}
}
}
}


class CommunicationThread_MAIN implements Runnable {

private Socket clientSocket;
private byte[] buffer= new byte[samples_per_pack*176];
private DataOutputStream out;
public CommunicationThread_MAIN(Socket clientSocket) {

this.clientSocket = clientSocket;

try {
this.clientSocket.setTcpNoDelay(true);

this.out = new DataOutputStream(this.clientSocket.getOutputStream());

} catch (IOException e) {
Thread.currentThread().interrupt();
e.printStackTrace();
}
}

public void run() {


while (!Thread.currentThread().isInterrupted()) {
if (send_flag_sensors){
for (int j=0;j<samples_per_pack;j++){
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(gyro_buff[i+j*4]);
		buffer[j*176+i*4] = (byte)(bits & 0xff);
		buffer[j*176+i*4+1] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+2] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+3] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(accel_buff[i+j*4]);
		buffer[j*176+i*4+16] = (byte)(bits & 0xff);
		buffer[j*176+i*4+17] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+18] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+19] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(magnet_buff[i+j*4]);
		buffer[j*176+i*4+32] = (byte)(bits & 0xff);
		buffer[j*176+i*4+33] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+34] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+35] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(gravity_buff[i+j*4]);
		buffer[j*176+i*4+48] = (byte)(bits & 0xff);
		buffer[j*176+i*4+49] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+50] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+51] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(linear_accel_buff[i+j*4]);
		buffer[j*176+i*4+64] = (byte)(bits & 0xff);
		buffer[j*176+i*4+65] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+66] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+67] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(rotation_vector_buff[i+j*4]);
		buffer[j*176+i*4+80] = (byte)(bits & 0xff);
		buffer[j*176+i*4+81] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+82] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+83] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(gamerotation_vector_buff[i+j*4]);
		buffer[j*176+i*4+96] = (byte)(bits & 0xff);
		buffer[j*176+i*4+97] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+98] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+99] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<4;i++){
		int bits = Float.floatToIntBits(orientation_buff[i+j*4]);
		buffer[j*176+i*4+112] = (byte)(bits & 0xff);
		buffer[j*176+i*4+113] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+114] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+115] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<10;i++){
		int bits = Float.floatToIntBits(rotationmatrix_buff[i+j*10]);
		buffer[j*176+i*4+128] = (byte)(bits & 0xff);
		buffer[j*176+i*4+129] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+130] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+131] = (byte)((bits >> 24) & 0xff);
	}
	
	for (int i=0;i<2;i++){
		int bits = Float.floatToIntBits(pressure_buff[i]);
		buffer[j*176+i*4+168] = (byte)(bits & 0xff);
		buffer[j*176+i*4+169] = (byte)((bits >> 8) & 0xff);
		buffer[j*176+i*4+170] = (byte)((bits >> 16) & 0xff);
		buffer[j*176+i*4+171] = (byte)((bits >> 24) & 0xff);
	}
}
try {
out.write(buffer, 0, samples_per_pack*176);
out.flush();
//Log.d("TCP","Main Packet Sent");
} catch (IOException e) {
Log.d("TCP","ERROR");
e.printStackTrace();
Thread.currentThread().interrupt();

}
send_flag_sensors=false;

}
}
}

}


class ServerThread_gps implements Runnable {

public void run() {
Socket socket = null;
try {
serverSocket_gps = new ServerSocket(GPS_PORT);
} catch (IOException e) {
e.printStackTrace();
}
while (!Thread.currentThread().isInterrupted()) {

try {

socket = serverSocket_gps.accept();
//serverSocket.getLocalSocketAddress().toString()
//text.setText(serverSocket.getLocalSocketAddress().toString());
GPS_PORT_CONNECTED=1;
GPS_PORT_REMOTE_ADDRESS=socket.getRemoteSocketAddress().toString().substring(1);
GPS_PORT_LOCAL_ADDRESS=socket.getLocalSocketAddress().toString().substring(1);
CommunicationThread_gps commThread = new CommunicationThread_gps(socket);
Thread a=new Thread(commThread);
a.start();

} catch (IOException e) {
e.printStackTrace();
}
}
}
}


class CommunicationThread_gps implements Runnable {

private Socket clientSocket;
private byte[] buffer= new byte[28];
private DataOutputStream out;
public CommunicationThread_gps(Socket clientSocket) {

this.clientSocket = clientSocket;

try {
this.clientSocket.setTcpNoDelay(true);
//	this.clientSocket.setSendBufferSize(gps_per_pack*16);
this.out = new DataOutputStream(this.clientSocket.getOutputStream());

} catch (IOException e) {
Thread.currentThread().interrupt();
e.printStackTrace();
}
}

public void run() {


while (!Thread.currentThread().isInterrupted()) {
if (send_flag_gps){
for (int i=0;i<7;i++){
	int bits = Float.floatToIntBits(gps_buff[i]);
	buffer[i*4] = (byte)(bits & 0xff);
	buffer[i*4+1] = (byte)((bits >> 8) & 0xff);
	buffer[i*4+2] = (byte)((bits >> 16) & 0xff);
	buffer[i*4+3] = (byte)((bits >> 24) & 0xff);
}
try {
out.write(buffer, 0, 28);
out.flush();
//Log.d("TCP","GPS Sent");
} catch (IOException e) {
Thread.currentThread().interrupt();
e.printStackTrace();
}
send_flag_gps=false;
}
}
}

}




class ServerThread_wifi implements Runnable {

public void run() {
Socket socket = null;
try {
serverSocket_wifi = new ServerSocket(WIFI_PORT);
} catch (IOException e) {
e.printStackTrace();
}
while (!Thread.currentThread().isInterrupted()) {

try {

socket = serverSocket_wifi.accept();
//serverSocket.getLocalSocketAddress().toString()
//text.setText(serverSocket.getLocalSocketAddress().toString());
WIFI_PORT_CONNECTED=1;
WIFI_PORT_REMOTE_ADDRESS=socket.getRemoteSocketAddress().toString().substring(1);
WIFI_PORT_LOCAL_ADDRESS=socket.getLocalSocketAddress().toString().substring(1);
CommunicationThread_wifi commThread = new CommunicationThread_wifi(socket);
Thread a=new Thread(commThread);
a.start();

} catch (IOException e) {
	
	e.printStackTrace();
}
}
}
}


class CommunicationThread_wifi implements Runnable {

private Socket clientSocket;
private byte[] buffer= new byte[MAX_WIFI_APs*21+4];
private DataOutputStream out;
private byte[] mac_byte= new byte[17];

public CommunicationThread_wifi(Socket clientSocket) {

this.clientSocket = clientSocket;

try {
this.clientSocket.setTcpNoDelay(true);
this.out = new DataOutputStream(this.clientSocket.getOutputStream());

} catch (IOException e) {
Thread.currentThread().interrupt();
e.printStackTrace();
}
}

public void run() {

	while (!Thread.currentThread().isInterrupted()) {
		if (send_flag_wifi){

			int bits = Float.floatToIntBits(wifi_t);
			buffer[0] = (byte)(bits & 0xff);
			buffer[1] = (byte)((bits >> 8) & 0xff);
			buffer[2] = (byte)((bits >> 16) & 0xff);
			buffer[3] = (byte)((bits >> 24) & 0xff);

			for (int i=1;i<=(MAX_WIFI_APs);i++){
				bits = wifi_rss_buff[i-1];
				buffer[i*4] = (byte)(bits & 0xff);
				buffer[i*4+1] = (byte)((bits >> 8) & 0xff);
				buffer[i*4+2] = (byte)((bits >> 16) & 0xff);
				buffer[i*4+3] = (byte)((bits >> 24) & 0xff);
			}

			for (int i=0;i<(MAX_WIFI_APs);i++){
				mac_byte = wifi_mac_buff[i].getBytes();
				for (int j=0;j<17;j++){
					buffer[i*17+j+(MAX_WIFI_APs+1)*4]=mac_byte[j];
				}
		
			}

			try {
				out.write(buffer, 0, MAX_WIFI_APs*21+4);
				out.flush();
				//Log.d("TCP","Wifi Sent");

			} catch (IOException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
			send_flag_wifi=false;
		}
	}
	}

	}



class ServerThread_ble implements Runnable {

public void run() {
Socket socket = null;
try {
serverSocket_ble = new ServerSocket(BLE_PORT);
} catch (IOException e) {
e.printStackTrace();
}
while (!Thread.currentThread().isInterrupted()) {

try {

socket = serverSocket_ble.accept();
//serverSocket.getLocalSocketAddress().toString()
//text.setText(serverSocket.getLocalSocketAddress().toString());
BLE_PORT_CONNECTED=1;
BLE_PORT_REMOTE_ADDRESS=socket.getRemoteSocketAddress().toString().substring(1);
BLE_PORT_LOCAL_ADDRESS=socket.getLocalSocketAddress().toString().substring(1);
CommunicationThread_ble commThread = new CommunicationThread_ble(socket);
Thread a=new Thread(commThread);
a.start();

} catch (IOException e) {
	e.printStackTrace();
	
}
}
}
}


class CommunicationThread_ble implements Runnable {

private Socket clientSocket;
private byte[] buffer= new byte[MAX_BLE_APs*52+4];
private DataOutputStream out;
private byte[] mac_byte= new byte[17];
private byte[] adv_byte= new byte[31];

public CommunicationThread_ble(Socket clientSocket) {

this.clientSocket = clientSocket;

try {
this.clientSocket.setTcpNoDelay(true);
this.out = new DataOutputStream(this.clientSocket.getOutputStream());

} catch (IOException e) {
Thread.currentThread().interrupt();
e.printStackTrace();
}
}

public void run() {

	while (!Thread.currentThread().isInterrupted()) {
		if (send_flag_ble){

			int bits = Float.floatToIntBits(ble_t);
			buffer[0] = (byte)(bits & 0xff);
			buffer[1] = (byte)((bits >> 8) & 0xff);
			buffer[2] = (byte)((bits >> 16) & 0xff);
			buffer[3] = (byte)((bits >> 24) & 0xff);

			for (int i=1;i<=(MAX_BLE_APs);i++){
				bits = ble_rss_buff[i-1];
				buffer[i*4] = (byte)(bits & 0xff);
				buffer[i*4+1] = (byte)((bits >> 8) & 0xff);
				buffer[i*4+2] = (byte)((bits >> 16) & 0xff);
				buffer[i*4+3] = (byte)((bits >> 24) & 0xff);
			}

			for (int i=0;i<(MAX_BLE_APs);i++){
				mac_byte = ble_mac_buff[i].getBytes();
				for (int j=0;j<17;j++){
					buffer[i*17+j+(MAX_BLE_APs+1)*4]=mac_byte[j];
				}
		
			}
			
			
			for (int i=0;i<(MAX_BLE_APs);i++){
				adv_byte = ble_advdata_buff.get(i);
				for (int j=0;j<31;j++){
					buffer[i*31+j+(MAX_BLE_APs+1)*4+MAX_BLE_APs*17]=adv_byte[j];
				}
		
			}
			
			try {
				out.write(buffer, 0, MAX_BLE_APs*52+4);
				out.flush();

			} catch (IOException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
			send_flag_ble=false;
		}
	}
	}

	}
	}

	

