package co.itn.lcdw4wifi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends Activity {

	public static final String PREFS_NAME = "MyConfig";
	Handler mHandler = new Handler();
	Thread ThdSearch;
	Thread ThdChkAvail;
	Button IP1_btn;
	Button IP2_btn;
	Button IP3_btn;
	Button IP4_btn;
	Button IP5_btn;
	Button Searchbtn;
    ProgressBar prgB1;
	TextView  SchNote;
	String SchBtnTxt = "Search";
	String SchNteTxt = "";
	boolean SocketOpen = false;
	Socket LanSocket = null;
	boolean SearchFinish = false;
	boolean DoSearch = false;
	boolean AvaiBuf = false;
	byte[] DataOut = new byte[4];
    byte[] DataIn  = new byte[18];
	int WaitAns;
	int IP;
	int FinCode;
    private int progressStatus = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		IP1_btn  = (Button) findViewById(R.id.IP1_button);
		IP2_btn  = (Button) findViewById(R.id.IP2_button);
		IP3_btn  = (Button) findViewById(R.id.IP3_button);
		IP4_btn  = (Button) findViewById(R.id.IP4_button);
		IP5_btn  = (Button) findViewById(R.id.IP5_button);
		Searchbtn = (Button) findViewById(R.id.Search_button);
		SchNote    = (TextView)  findViewById(R.id.SearchNote);
        prgB1 = (ProgressBar) findViewById(R.id.progressBar);
        String MyIP = GetLocalIpAddress();
        TextView IPtxt = (TextView)findViewById(R.id.IPtxt);
        IPtxt.setText(MyIP);

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
		mHandler.removeCallbacks(ChkAvailRun);
		
		try{ LanSocket.close(); }
		catch(Exception ex) {}
		SocketOpen = false;
		
		SaveConfig();
		
	}
	
    //----------------------------------------------------------------
    //  OnResume
    //----------------------------------------------------------------
	@Override
	protected void onResume() {
		super.onResume();

		DataOut[0] = (byte) 0x41;		// 'A'
		DataOut[1] = (byte) 0x00;		// Invert of DataOut[2]
		DataOut[2] = (byte) 0xFF;		// no key pressed
		DataOut[3] = (byte) 0x72;		// 'r'
		
		mHandler.postDelayed(UpdateUI, 100);
		
		mHandler.removeCallbacks(ChkAvailRun); 
		mHandler.postDelayed(ChkAvailRun, 2000);
	}

	public void press_ip1(View view){ 													// Botton IP1
		if(InternalData.DeviceMember.get(0).Available){InternalData.Device_id = 0; openCtrlPage(); }
		else{ Toast.makeText(getApplicationContext(), "Not Available", Toast.LENGTH_SHORT).show(); }
	}			
	public void press_ip2(View view){  													// Botton IP2
		if(InternalData.DeviceMember.get(1).Available){InternalData.Device_id = 1; openCtrlPage(); }
		else{ Toast.makeText(getApplicationContext(), "Not Available", Toast.LENGTH_SHORT).show(); }
	}
	public void press_ip3(View view){ 													// Botton IP3
		if(InternalData.DeviceMember.get(2).Available){InternalData.Device_id = 2; openCtrlPage(); }
		else{ Toast.makeText(getApplicationContext(), "Not Available", Toast.LENGTH_SHORT).show(); }
	}
	public void press_ip4(View view){ 													// Botton IP4
		if(InternalData.DeviceMember.get(3).Available){InternalData.Device_id = 3; openCtrlPage(); }
		else{ Toast.makeText(getApplicationContext(), "Not Available", Toast.LENGTH_SHORT).show(); }
	}
	public void press_ip5(View view){ 													// Botton IP5
		if(InternalData.DeviceMember.get(4).Available){InternalData.Device_id = 4; openCtrlPage(); }
		else{ Toast.makeText(getApplicationContext(), "Not Available", Toast.LENGTH_SHORT).show(); }
	}
	
	public void press_det1(View view){ InternalData.Device_id = 0; openDetlPage(); }	// Botton Detail 1
	public void press_det2(View view){ InternalData.Device_id = 1; openDetlPage(); }	// Botton Detail 2
	public void press_det3(View view){ InternalData.Device_id = 2; openDetlPage(); }	// Botton Detail 3
	public void press_det4(View view){ InternalData.Device_id = 3; openDetlPage(); }	// Botton Detail 4
	public void press_det5(View view){ InternalData.Device_id = 4; openDetlPage(); }	// Botton Detail 5
	
	public void press_search(View view)						// Botton Search
    {
		if(SchBtnTxt.equals("Search")){
			SchBtnTxt = "Stop";
			
			DoSearch = true;						// to block Check avail run in searching time
			mHandler.removeCallbacks(SearchRun); 
			mHandler.postDelayed(SearchRun, 1500);
		}
		else { SchBtnTxt = "Search"; DoSearch = false; FinCode = 2; }
		
    }	
	
	private void openCtrlPage() {
		// TODO Auto-generated method stub
 		Intent myIntent = new Intent(this, CtrlActivity.class);
        startActivity(myIntent);
	}
	private void openDetlPage() {
		// TODO Auto-generated method stub
 		Intent myIntent = new Intent(this, DetailActivity.class);
        startActivity(myIntent);
	}
	
	//----------------------------------------------------------------
    //  Runnable UpdateUI
    //----------------------------------------------------------------
	final Runnable UpdateUI = new Runnable() {
		public void run() {
			
			IP1_btn.setText(InternalData.DeviceMember.get(0).NAME);
			IP2_btn.setText(InternalData.DeviceMember.get(1).NAME);
			IP3_btn.setText(InternalData.DeviceMember.get(2).NAME);
			IP4_btn.setText(InternalData.DeviceMember.get(3).NAME);
			IP5_btn.setText(InternalData.DeviceMember.get(4).NAME);

			if(InternalData.DeviceMember.get(0).Available){ IP1_btn.setTextColor(0xFFFFFFFF); }
			else{ IP1_btn.setTextColor(0x44FFFFFF); }
			if(InternalData.DeviceMember.get(1).Available){ IP2_btn.setTextColor(0xFFFFFFFF); }
			else{ IP2_btn.setTextColor(0x44FFFFFF); }
			if(InternalData.DeviceMember.get(2).Available){ IP3_btn.setTextColor(0xFFFFFFFF); }
			else{ IP3_btn.setTextColor(0x44FFFFFF); }
			if(InternalData.DeviceMember.get(3).Available){ IP4_btn.setTextColor(0xFFFFFFFF); }
			else{ IP4_btn.setTextColor(0x44FFFFFF); }
			if(InternalData.DeviceMember.get(4).Available){ IP5_btn.setTextColor(0xFFFFFFFF); }
			else{ IP5_btn.setTextColor(0x44FFFFFF); }
			
			Searchbtn.setText(SchBtnTxt);
			
			SchNote.setText(SchNteTxt);
			
			if(SearchFinish){
				if(FinCode == 0){
					Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
				}
				else if(FinCode == 1){
					Toast.makeText(getApplicationContext(), "Finished", Toast.LENGTH_SHORT).show();
				}
				else if(FinCode == 2){
					Toast.makeText(getApplicationContext(), "Stoped", Toast.LENGTH_SHORT).show();
				}
				SearchFinish = false; SchBtnTxt = "Search";
				mHandler.removeCallbacks(ChkAvailRun); 
				mHandler.postDelayed(ChkAvailRun, 2000);
			}
				
			if(WaitAns>0){ WaitAns--; }
			
			mHandler.postDelayed(this, 20);	// 20 ms
		}
	};
	
	//----------------------------------------------------------------
    //  Runnable Check Available
    //----------------------------------------------------------------
	final Runnable ChkAvailRun = new Runnable() {
		public void run() {
			
			if(!DoSearch){
				
				ThdChkAvail = new Thread(new ChkAvailThd());
				ThdChkAvail.start();
				
				mHandler.removeCallbacks(this); 
				mHandler.postDelayed(this, 2000);
			}
			
		}
	};
	//----------------------------------------------------------------
    //  LAN Search Thread
    //----------------------------------------------------------------
    public class ChkAvailThd implements Runnable {
    	public void run(){
    		
    		int j = 0;
    		SocketAddress avaiaddr;
    		for(j=0; j<5; j++){
    			
    			// if No IP then false
    			if(InternalData.DeviceMember.get(j).IPAD.equals("")){
    				InternalData.DeviceMember.get(j).Available = false;
    			}
    			// else try to connect
    			else{
    				avaiaddr = new InetSocketAddress(InternalData.DeviceMember.get(j).IPAD, Integer.valueOf(InternalData.Lan_Port));
    				LanSocket = new Socket();
    			
    				AvaiBuf = true; // assume
    				
    				try {LanSocket.connect(avaiaddr, 200); }
    				catch (IllegalArgumentException e){ AvaiBuf = false; }
    				catch (IOException e){ AvaiBuf = false; }
    				if(AvaiBuf){

    					InternalData.DeviceMember.get(j).Available = true;
    					try { LanSocket.close(); }
						catch  (IOException e){  }
    				}
    				else{ InternalData.DeviceMember.get(j).Available = false; }
    					
    			}
    		}
    	}
    }
	//----------------------------------------------------------------
    //  Runnable SearchThead
    //----------------------------------------------------------------
    private Runnable SearchRun = new Runnable(){
    	public void run(){
    		
    		FinCode = 0; 				// init "Not found"
    		SearchFinish = false;		// to reset triger Toast
    		
    		ThdSearch = new Thread(new SearchThd());
    		ThdSearch.start();
    	}
    };
    //----------------------------------------------------------------
    //  LAN Search Thread
    //----------------------------------------------------------------
    public class SearchThd implements Runnable {
		public void run(){
    		if(SocketOpen == false){
    			
    			int DvCnt = 0;		// Device Counter
    			int IP_Ad04 = 1;
    			String MyIP = null;
    			String IP_Test = null;
    			String IP_Group = null;
    			boolean Found_IP = false;
    			boolean DoClose = false;
    			boolean StreamOpen = false;
    			DataInputStream disL = null;
    			DataOutputStream dosL = null;
    			int LastdotID;
    			int TryAgain;
    			SocketAddress sockaddr;
    			//----------------------------------------------//
    			MyIP = GetLocalIpAddress();						// Get this phone IP Address
    			LastdotID = MyIP.lastIndexOf('.');				// 
    			IP_Group = MyIP.substring(0, LastdotID+1);		//
    			//----------------------------------------------//
    			
    			//----------------------------------------------//
    			// 		While Search Loop						//
    			//----------------------------------------------//
				while((IP_Ad04<=254)&&(DoSearch)){
					
					//------------------------------------------// Prepare IP Address
					IP_Test = IP_Group + IP_Ad04;
					sockaddr = new InetSocketAddress(IP_Test, Integer.valueOf(InternalData.Lan_Port));
					SchNteTxt = "Searching : " + ((IP_Ad04*100)/254) + "%" + " [" + IP_Test + "]";
                    //SchNteTxt = "Searching : " + ((IP_Ad04*100)/255) + "%" + " [" + IP_Test + "]";
					//------------------------------------------// Try Open Socket
					Found_IP = true; // assume
					LanSocket = new Socket();
					try {LanSocket.connect(sockaddr, 100); }
					catch (IllegalArgumentException e){SchNteTxt = "Timeout ";}			// ***
					catch (IOException e){ SocketOpen = false; Found_IP = false; }
					
					//------------------------------------------// If IP is found


                    new Thread(new Runnable() {
                        public void run() {
                            while (progressStatus < 100) {
                                progressStatus += 1;
                                // Update the progress bar and display the current value in the text view
                                mHandler.post(new Runnable() {
                                    public void run() {
                                        prgB1.setProgress(progressStatus);
                                        //textView.setText(prgB1+"/"+prgB1.getMax());
                                    }
                                });
                                try {
                                    // Sleep for 200 milliseconds. Just to display the progress slowly
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

					if(Found_IP){
						SchNteTxt = "Verifying : " + IP_Test;
						//--------------------------------------// Try Open Stream
						StreamOpen = true;	// assume
						try { disL = new DataInputStream(new BufferedInputStream(LanSocket.getInputStream())); }
						catch(Exception e) { StreamOpen = false; }
						try { dosL = new DataOutputStream(new BufferedOutputStream(LanSocket.getOutputStream())); }	
						catch(Exception e) { StreamOpen = false; }
						
						if(StreamOpen){
							//----------------------------------// Out Data to Thermostat
							TryAgain = 4;
							while(TryAgain!=0){
								TryAgain--;
								
								try { dosL.write(DataOut); } 		
								catch (IOException e){  }  
								try { dosL.flush(); } 
								catch (IOException e){  }  
								
								//----------------------------------// Wait for Get Data from Thermostat
								WaitAns = 100;	// Set 200 ms counter
								
								while(WaitAns!=0){
								try { 
									if((disL.available()!=0)&&(disL.read(DataIn) == DataIn.length )){
										int i;
							    		if(DataIn[0]==0x44){									// Check Header 'D'
							    		for(i=0;i<=16;i++){ DataIn[17] += (byte) DataIn[i]; }	// Check CheckSum
							    		if(DataIn[17]==(byte) 0xFF){
							    			InternalData.DeviceMember.get(DvCnt).MCAD = GetIPAdr();
							    			InternalData.DeviceMember.get(DvCnt).IPAD = IP_Test;
							    			InternalData.DeviceMember.get(DvCnt).Available = true;
							    			if(DvCnt<4){ DvCnt++; }
							    			WaitAns = 0; TryAgain = 0; // get off from while loop
							    		}}	
									}
								}catch (IOException e1){  e1.printStackTrace();  }
								}
								
							}
							
						}

						
						FinCode = 1;
						
						//-- Confirm Close ----------------------
						DoClose = true;
						while(DoClose){
							try { LanSocket.close(); DoClose = false; }
							catch  (IOException e){ DoClose = true; }
						}
					}
					IP_Ad04++;
				}
				SearchFinish = true; DoSearch = false;
				SchNteTxt = "";
    		}
    	}
    }
    
    public String GetIPAdr(){
    	String	IPAdr;
    	int	GenBUF1 = (DataIn[13]&0x7F) + ((DataIn[13]&0x80)>>7)*128;
    	int	GenBUF2 = (DataIn[14]&0x7F) + ((DataIn[14]&0x80)>>7)*128;
    	int	GenBUF3 = (DataIn[15]&0x7F) + ((DataIn[15]&0x80)>>7)*128;
    	int	GenBUF4 = (DataIn[16]&0x7F) + ((DataIn[16]&0x80)>>7)*128;
    	
    	IPAdr = GenBUF1+"."+GenBUF2+"."+GenBUF3+"."+GenBUF4;

    	return	IPAdr;
    }
    
    public String GetMacAdr(){
    	
    	byte	GenBUF1,GenBUF2;
    	String 	MacAdr;
    	
    	GenBUF1 = (byte) ((DataIn[7]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[7]&0x0F);
    	MacAdr = Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2) + ":";
    	
    	GenBUF1 = (byte) ((DataIn[8]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[8]&0x0F);
    	MacAdr += Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2) + ":";
    	
    	GenBUF1 = (byte) ((DataIn[9]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[9]&0x0F);
    	MacAdr += Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2) + ":";
    	
    	GenBUF1 = (byte) ((DataIn[10]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[10]&0x0F);
    	MacAdr += Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2) + ":";
    	
    	GenBUF1 = (byte) ((DataIn[11]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[11]&0x0F);
    	MacAdr += Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2) + ":";
    	
    	GenBUF1 = (byte) ((DataIn[12]>>4)&0x0F);
    	GenBUF2 = (byte) (DataIn[12]&0x0F);
    	MacAdr += Integer.toHexString(GenBUF1) + Integer.toHexString(GenBUF2);
    	
    	return MacAdr;
    } 
    
    private String GetLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            return "";
        }
        return "";   
    }
 
    private void SaveConfig(){
    	SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
	    SharedPreferences.Editor editor = sharedPreferences.edit();
	   
	    editor.putString("NAME0", InternalData.DeviceMember.get(0).NAME);
	    editor.putString("NAME1", InternalData.DeviceMember.get(1).NAME);
	    editor.putString("NAME2", InternalData.DeviceMember.get(2).NAME);
	    editor.putString("NAME3", InternalData.DeviceMember.get(3).NAME);
	    editor.putString("NAME4", InternalData.DeviceMember.get(4).NAME);
	    editor.putString("IPAD0", InternalData.DeviceMember.get(0).IPAD);
	    editor.putString("IPAD1", InternalData.DeviceMember.get(1).IPAD);
	    editor.putString("IPAD2", InternalData.DeviceMember.get(2).IPAD);
	    editor.putString("IPAD3", InternalData.DeviceMember.get(3).IPAD);
	    editor.putString("IPAD4", InternalData.DeviceMember.get(4).IPAD);
	    editor.putString("MCAD0", InternalData.DeviceMember.get(0).MCAD);
	    editor.putString("MCAD1", InternalData.DeviceMember.get(1).MCAD);
	    editor.putString("MCAD2", InternalData.DeviceMember.get(2).MCAD);
	    editor.putString("MCAD3", InternalData.DeviceMember.get(3).MCAD);
	    editor.putString("MCAD4", InternalData.DeviceMember.get(4).MCAD);
	    
	    editor.commit();
    }
    
}
