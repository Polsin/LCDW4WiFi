package co.itn.lcdw4wifi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

	Handler mHandler = new Handler();
	Thread ThdConfig;
	Thread ThdTCPCmu;
	TextView  StatTextView;
	Socket ConfigSocket = null;
	byte[] DataOut = new byte[3];
	boolean SocketOpen = false;
	boolean StreamOpen = false;
	boolean SendReq = false;
	String StatusText = "";
	String SSIDString = "";
	String PASSString = "";
	DataInputStream  disCnfg = null;
	DataOutputStream dosCnfg = null;
	EditText SSIDEdt;
	EditText PASSEdt;
	int ToastCode;
	int TryAgnCnt = 0;
	int DelayCnt = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		StatTextView    = (TextView)  findViewById(R.id.Stattext);
		SSIDEdt			= (EditText)  findViewById(R.id.editSSID);
		PASSEdt 		= (EditText)  findViewById(R.id.editPASS);
		
		
		
		
		mHandler.removeCallbacks(UpdateUI);
        mHandler.postDelayed(UpdateUI, 100);
	}

	//----------------------------------------------------------------
    //  OnPause
    //----------------------------------------------------------------
    @Override
	protected void onPause() {
    	super.onPause();
		mHandler.removeCallbacks(UpdateUI);
		
		try{ ConfigSocket.close(); }
		catch(Exception ex) {}
		SocketOpen = false;
		
		//SaveConfig();
		
	}
	
    //----------------------------------------------------------------
    //  OnResume
    //----------------------------------------------------------------
	@Override
	protected void onResume() {
		super.onResume();
		
		//LoadConfig();
		
		mHandler.postDelayed(UpdateUI, 100);

	}
	
	
	//----------------------------------------------------------------
    //  Runnable UpdateUI
    //----------------------------------------------------------------
	final Runnable UpdateUI = new Runnable() {
		public void run() {
			StatTextView.setText(StatusText);
			
			if(DelayCnt!=0){ DelayCnt--; }
			
			if(ToastCode==1){
				Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_SHORT).show();
			}
			if(ToastCode==2){
				Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
				StatusText = "Plese try put WiFi-Thermostat into AP mode by press mode button for 8 sec.";
			}
			if(ToastCode==3){
				Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
				StatusText = "Plese try put WiFi-Thermostat into AP mode by press mode button for 8 sec.";
			}
			if(ToastCode==4){
				Toast.makeText(getApplicationContext(), "Data empty", Toast.LENGTH_SHORT).show();
			}
			ToastCode = 0;	// Reset ToastCode after finish process
			mHandler.postDelayed(this, 20);	// 20 ms
		}
	};
	
	
	//----------------------------------------------------------------
    //  Runnable ConfigRun -> ConfigThd
    //----------------------------------------------------------------
	private Runnable ConfigRun = new Runnable(){
    	public void run(){
    		ThdConfig = new Thread(new ConfigThd()); 
    		ThdConfig.start();
    	}
    };

    //----------------------------------------------------------------
    //  Network Config Thread
    //----------------------------------------------------------------
    public class ConfigThd implements Runnable {
    	public void run(){
    		
    		if(!SocketOpen)
    		{
    			SocketAddress sockaddr = new InetSocketAddress("192.168.0.1", 2000);
    			ConfigSocket = new Socket();
    			SocketOpen = true; 		// assume
    			//StatusText = "Status : Opening Socket";
				try {ConfigSocket.connect(sockaddr, 500); }
				catch (IllegalArgumentException e){StatusText = "Try Opening Socket..."; SocketOpen = false; }
				catch (IOException e){StatusText = "Try Opening Socket..."; SocketOpen = false; }
				if((!SocketOpen)&&(TryAgnCnt<4)){ 
					TryAgnCnt++;
					mHandler.removeCallbacks(ConfigRun);		// Try again for 4 time
					mHandler.postDelayed(ConfigRun, 1000); 
				}	
				if(TryAgnCnt>=4){ ToastCode = 2; mHandler.removeCallbacks(ConfigRun);}
				if(SocketOpen){ TryAgnCnt = 0; }
    		}
    		
			if((SocketOpen)&&(!StreamOpen))
        	{
        		StreamOpen = true;	// assume
        		//StatusText = "Status : Opening I/O Stream";
				//-- Create Input Stream ------------------------
				try { disCnfg = new DataInputStream(new BufferedInputStream(ConfigSocket.getInputStream())); }
				catch(Exception e) { StatusText = "Try Opening I/O"; StreamOpen = false; }
				//-- Create Output Stream -----------------------
				try { dosCnfg = new DataOutputStream(new BufferedOutputStream(ConfigSocket.getOutputStream())); }	
				catch(Exception e) { StatusText = "Try Opening I/O"; StreamOpen = false; }
				
				if(StreamOpen){
  				    mHandler.removeCallbacks(TCPCmmuRun);		
  				    mHandler.postDelayed(TCPCmmuRun, 100);
				}
				else if(TryAgnCnt<4){ 
					TryAgnCnt++;
    				mHandler.removeCallbacks(ConfigRun);		
    				mHandler.postDelayed(ConfigRun, 500); 	// else try again in 500 ms.
    			}
				if(TryAgnCnt>=4){ ToastCode = 2; mHandler.removeCallbacks(ConfigRun);}
        	}
    	}
    }
    
    //----------------------------------------------------------------
    //  Runnable ConfigRun -> ConfigThd
    //----------------------------------------------------------------
	private Runnable TCPCmmuRun = new Runnable(){
    	public void run(){
    		ThdTCPCmu = new Thread(new TCPCmmuThd()); 
    		ThdTCPCmu.start();
    	}
    };
    
    //----------------------------------------------------------------
    //  Network Config Thread
    //----------------------------------------------------------------
    public class TCPCmmuThd implements Runnable {
    	public void run(){
    		
    		P_DataOut(); 
    		
    	}
    }
    
    //------------------------------
    //		Process Data Out
    //------------------------------
    public void P_DataOut(){

		//-- Send Data Out -----------------
    	boolean	Test = true;						// for check sending is not error
		try { dosCnfg.writeBytes(SSIDString); } 
		catch (IOException e){ StatusText = "Sending Data Fail"; Test = false; }  // Write error
		try { dosCnfg.flush(); } 
		catch (IOException e){ StatusText = "Sending Data Fail"; Test = false; }  // Flush error
		
		DelayCnt = 20;
		while(DelayCnt!=0){}	// Delay for 400 ms
		
		try { dosCnfg.writeBytes(PASSString); } 
		catch (IOException e){ StatusText = "Sending Data Fail"; Test = false; }  // Write error
		try { dosCnfg.flush(); } 
		catch (IOException e){ StatusText = "Sending Data Fail"; Test = false; }  // Flush error
		
		if(Test){ ToastCode = 1; }
		else{ ToastCode = 3; }
    }
    
    
    //------------------------------
    //      Process Button
    //------------------------------
    public void press_as(View view){ 
    	
    	
    	if(!SSIDEdt.getText().toString().equals("")){
    		TryAgnCnt = 0;
    		SSIDString = "S:" + SSIDEdt.getText().toString() + ":";
    		PASSString = "P:" + PASSEdt.getText().toString() + ":";
	
    		if((!SocketOpen)||(!StreamOpen))
    		{
    			mHandler.removeCallbacks(ConfigRun);		
    			mHandler.postDelayed(ConfigRun, 100); 
    		}
    		else
    		{
    			mHandler.removeCallbacks(TCPCmmuRun);		
    			mHandler.postDelayed(TCPCmmuRun, 100); 
    		}
    	}
    	else{ ToastCode = 4; }
    }
    
    //------------------------------
    //      Process Checkbox
    //------------------------------
    public void check_show(View view){ 
    	
    	boolean checked = ((CheckBox) view).isChecked();
    	if(checked){
    		PASSEdt.setInputType(1);
    	}
    	else{
    		PASSEdt.setInputType(0x81);
    	}
    		
    	
    }
    
    
}
