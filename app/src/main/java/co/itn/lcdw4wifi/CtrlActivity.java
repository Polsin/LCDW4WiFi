package co.itn.lcdw4wifi;

import java.io.*;
import java.net.*;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.view.View;


public class CtrlActivity extends Activity {
    //--------------------------------------
    // [Release Note V 0.2.2]
    // [16-07-2013]
    // - Fix Bug : reinitiate didn't work if never get connection before.
    //       by  : compare Running and init CnTimeOut
    // [17-07-2013]
    // - Fix Bug : Not show Fan auto icon
    //       by  : add Fanauto (pure auto) picture
    //--------------------------------------
    String StatusText,WriteData,MacAdr;
    Handler mHandler = new Handler();
    TextView  name_ms;
    TextView  stat_ms;
    ImageView mode_img;
    ImageView char_1;
    ImageView char_2;
    ImageView fanspd;
    ImageView ecotrb;
    ImageView compc;
    int CnTimeOut = 0;
    double InitCnt = 0;
    byte cnt_1ms,cnt_100ms,cnt_1sec,BufKey, Cnnt_Cnt,GenBUF1,GenBUF2;
    byte[] DataOut = new byte[4];
    byte[] DataIn  = new byte[18];
    byte[] DataBuf  = new byte[4];
    boolean SocketOpen = false;
    boolean StreamOpen = false;
    boolean UseLan = false;
    boolean UseNet = false;
    boolean Running = false;
    boolean ReqAns = false;
    boolean ReqSend = false;
    boolean Connected = true;
    boolean ReSend = false;
    boolean SendReq = false;
    boolean InitFlg = false;
    Socket LanSocket = null;
    Socket NetSocket = null;
    Socket NNSocket = null;
    DataInputStream disL = null;
    DataInputStream disN = null;
    DataOutputStream dosL = null;
    DataOutputStream dosN = null;
    int j = 0;
    int k = 0;
    int id = 0;
    Thread cThread;
    Thread dspThread;
    Thread OpenWiFiThd;
    //------------------
    //  Initial UI
    //------------------
    int Res_Char1  = R.drawable.char_off;
    int Res_Char2  = R.drawable.char_off;
    int Res_FanSpd = R.drawable.dnoff;
    int Res_Mode   = R.drawable.offmd;
    int Res_EcTb   = R.drawable.nomd;
    int Res_ComSTS = R.drawable.coof;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_ctrl, menu);
        return true;
    }

    //----------------------------------------------------------------
    //  OnCreate
    //----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctrl);

        name_ms  = (TextView)  findViewById(R.id.Name_msg);
        stat_ms  = (TextView)  findViewById(R.id.status_msg);
        mode_img = (ImageView) findViewById(R.id.imageMode);
        char_1   = (ImageView) findViewById(R.id.imageChar1);
        char_2   = (ImageView) findViewById(R.id.imageChar2);
        fanspd   = (ImageView) findViewById(R.id.imageFan);
        ecotrb   = (ImageView) findViewById(R.id.imageEcoTrb);
        compc    = (ImageView) findViewById(R.id.imageCompC);

    }

    //----------------------------------------------------------------
    //  OnPause
    //----------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(UpdateUI);
        mHandler.removeCallbacks(TimerRun);
        mHandler.removeCallbacks(WiFiOpRun);
        mHandler.removeCallbacks(WiFiCmRun);
        try{ LanSocket.close(); }
        catch(Exception ex) {}
        SocketOpen = false;
        StreamOpen = false;
        Running = false;
        InitFlg = false;
    }

    //----------------------------------------------------------------
    //  OnResume
    //----------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        StatusText = "Status : Starting..";
        InitFlg = false;
        id = InternalData.Device_id;

        mHandler.removeCallbacks(UpdateUI);
        mHandler.postDelayed(UpdateUI, 100);

        mHandler.removeCallbacks(TimerRun);
        mHandler.postDelayed(TimerRun, 100);

        mHandler.removeCallbacks(WiFiOpRun);
        mHandler.postDelayed(WiFiOpRun, 1000);


    }


    //----------------------------------------------------------------
    //  Runnable UpdateUI
    //----------------------------------------------------------------
    final Runnable UpdateUI = new Runnable() {
        public void run() {

            name_ms.setText(InternalData.DeviceMember.get(id).NAME);
            if(InternalData.DeviceMember.get(id).Available){ name_ms.setTextColor(0xFFFFFFFF); }
            else{ name_ms.setTextColor(0x44FFFFFF); }


            //-- Get Display Data ------------------
            switch (DataBuf[0]) {
                case 0x3F:  Res_Char1 = R.drawable.char_0;break; // Char 0
                case 0x06:  Res_Char1 = R.drawable.char_1;break; // Char 1
                case 0x5B:  Res_Char1 = R.drawable.char_2;break; // Char 2
                case 0x4F:  Res_Char1 = R.drawable.char_3;break; // Char 3
                case 0x66:  Res_Char1 = R.drawable.char_4;break; // Char 4
                case 0x6D:  Res_Char1 = R.drawable.char_5;break; // Char 5
                case 0x7D:  Res_Char1 = R.drawable.char_6;break; // Char 6
                case 0x07:  Res_Char1 = R.drawable.char_7;break; // Char 7
                case 0x7F:  Res_Char1 = R.drawable.char_8;break; // Char 8
                case 0x6F:  Res_Char1 = R.drawable.char_9;break; // Char 9
                case 0x77:  Res_Char1 = R.drawable.char_a;break; // Char a
                case 0x7C:  Res_Char1 = R.drawable.char_b;break; // Char b
                case 0x39:  Res_Char1 = R.drawable.char_c;break; // Char c
                case 0x5E:  Res_Char1 = R.drawable.char_d;break; // Char d
                case 0x79:  Res_Char1 = R.drawable.char_e;break; // Char e
                case 0x71:  Res_Char1 = R.drawable.char_f;break; // Char f
                default: Res_Char1 = R.drawable.char_off;break;
            }

            //-- Get Display Data ------------------
            DataBuf[1] &= 0x7F;
            switch (DataBuf[1]) {
                case 0x3F:  Res_Char2 = R.drawable.char_0;break; // Char 0
                case 0x06:  Res_Char2 = R.drawable.char_1;break; // Char 1
                case 0x5B:  Res_Char2 = R.drawable.char_2;break; // Char 2
                case 0x4F:  Res_Char2 = R.drawable.char_3;break; // Char 3
                case 0x66:  Res_Char2 = R.drawable.char_4;break; // Char 4
                case 0x6D:  Res_Char2 = R.drawable.char_5;break; // Char 5
                case 0x7D:  Res_Char2 = R.drawable.char_6;break; // Char 6
                case 0x07:  Res_Char2 = R.drawable.char_7;break; // Char 7
                case 0x7F:  Res_Char2 = R.drawable.char_8;break; // Char 8
                case 0x6F:  Res_Char2 = R.drawable.char_9;break; // Char 9
                case 0x77:  Res_Char2 = R.drawable.char_a;break; // Char a
                case 0x7C:  Res_Char2 = R.drawable.char_b;break; // Char b
                case 0x39:  Res_Char2 = R.drawable.char_c;break; // Char c
                case 0x5E:  Res_Char2 = R.drawable.char_d;break; // Char d
                case 0x79:  Res_Char2 = R.drawable.char_e;break; // Char e
                case 0x71:  Res_Char2 = R.drawable.char_f;break; // Char f
                default: Res_Char2 = R.drawable.char_off;break;
            }

            DataBuf[2] &= 0x7F;
            switch (DataBuf[2]) {
                case 0x03:  Res_FanSpd = R.drawable.fanlow;break; // Fan low
                case 0x0F:  Res_FanSpd = R.drawable.fanmed;break; // Fan mid
                case 0x3F:  Res_FanSpd = R.drawable.fanhigh;break; // Fan high
                case 0x40:  Res_FanSpd = R.drawable.fanauto;break; // Fan auto
                case 0x43:  Res_FanSpd = R.drawable.fanautolow;break; // Fan auto low
                case 0x4F:  Res_FanSpd = R.drawable.fanautomed;break; // Fan auto mid
                case 0x7F:  Res_FanSpd = R.drawable.fanautohigh;break; // Fan auto high
                default: Res_FanSpd = R.drawable.dnoff;break;
            }


/*
            Res_EcTb = R.drawable.nomd;		// init to off mode
            Res_Mode = R.drawable.offmd;	//
            switch (DataBuf[3]) {
                case 0x01:  Res_Mode = R.drawable.fanmd;break;  // Fan  Mode
                case 0x02:  Res_Mode = R.drawable.coolmd;break; 	// Cool Mode
                case 0x04:  Res_Mode = R.drawable.drymd;break; 	 // Dry Mode
                case 0x08:  Res_Mode = R.drawable.heatmd;break; 	// Heat Mode
                case 0x10:  Res_Mode = R.drawable.automd;break;	 // Auto Mode
                case 0x20:  Res_EcTb = R.drawable.economd;break;	// Econo Mode
                case 0x40:  Res_EcTb = R.drawable.turbomd;break; 	// Turbo Mode
                case (byte)0x80:  Res_ComSTS = R.drawable.compc;break;  	// Turbo Mode
                default: Res_ComSTS = R.drawable.coof;break;  // off
            }
*/

            Res_EcTb = R.drawable.nomd;		// init to off mode
            Res_Mode = R.drawable.offmd;	//
            if((DataBuf[3]&0x01)==0x01){ Res_Mode = R.drawable.fanmd; }	// Fan  Mode
            else if((DataBuf[3]&0x02)==0x02){ Res_Mode = R.drawable.coolmd; }	// Cool Mode
            else if((DataBuf[3]&0x04)==0x04){ Res_Mode = R.drawable.drymd; }	// Dry Mode
            else if((DataBuf[3]&0x08)==0x08){ Res_Mode = R.drawable.heatmd; }	// Heat Mode
            else if((DataBuf[3]&0x10)==0x10){ Res_Mode = R.drawable.automd; }	// Auto Mode
            else if((DataBuf[3]&0x20)==0x20){ Res_EcTb = R.drawable.economd; }	// Econo Mode
            else if((DataBuf[3]&0x40)==0x40){ Res_EcTb = R.drawable.turbomd; }	// Turbo Mode

            if((DataBuf[3]&0x80)==0x80){ Res_ComSTS = R.drawable.compc; }	// comp on
            else{ Res_ComSTS = R.drawable.tp_up_rgt; }

            if(DataBuf[3]==0x00){ Res_ComSTS = R.drawable.coof; }	// off

            char_1.setImageResource(Res_Char1);
            char_2.setImageResource(Res_Char2);
            fanspd.setImageResource(Res_FanSpd);
            mode_img.setImageResource(Res_Mode);
            ecotrb.setImageResource(Res_EcTb);
            compc.setImageResource(Res_ComSTS);


            if(ReqAns){
                switch(k)
                {
                    case 0:  StatusText = "Status : Requesting.    "; 	break;
                    case 1:	 StatusText = "Status : Requesting..   "; 	break;
                    case 2:	 StatusText = "Status : Requesting...  "; 	break;
                    case 3:	 StatusText = "Status : Requesting.... "; 	break;
                    case 4:	 StatusText = "Status : Requesting....."; 	break;
                }
                if((CnTimeOut<=50)&&(Running)){
                    StatusText += "[Reinitiate in " + CnTimeOut/10 + "]";
                }
            }



            stat_ms.setText(StatusText);





            mHandler.postDelayed(this, 80);
        }
    };

    //----------------------------------------------------------------
    //  Runnable TimerRun
    //----------------------------------------------------------------
    final Runnable TimerRun = new Runnable() {
        public void run() {

            //--------------------------------------------
            //  1 ms routine
            //--------------------------------------------
            cnt_1ms++;
            InitCnt++;
            //--------------------------------------------
            //  100 ms routine
            //--------------------------------------------
            if(cnt_1ms>=100){ cnt_1ms = 0; cnt_100ms++;
                //--------------------------------------------
                if(Running){
                    if(CnTimeOut>0){ CnTimeOut--; InternalData.DeviceMember.get(id).Available = true;}
                    else{ InternalData.DeviceMember.get(id).Available = false;

                        mHandler.removeCallbacks(WiFiOpRun);
                        mHandler.removeCallbacks(WiFiCmRun);
                        try{ LanSocket.close(); }
                        catch(Exception ex) {}
                        SocketOpen = false;
                        StreamOpen = false;
                        InitFlg = false;
                        ReqAns = false;
                        Running = false;

                        StatusText = "Status : Starting..";
                        mHandler.removeCallbacks(WiFiOpRun);
                        mHandler.postDelayed(WiFiOpRun, 1000);
                    }
                }
                //--------------------------------------------
                //  1 sec routine
                //--------------------------------------------
                if(cnt_100ms>=10){ cnt_100ms = 0; cnt_1sec++;
                    //--------------------------------------------


                    //--------------------------------------------
                    if(cnt_1sec>=10){ cnt_1sec = 0; }
                }
            }
            //--------------------------------------------

            mHandler.postDelayed(this, 1);	// 1 ms
        }
    };

    //----------------------------------------------------------------
    //  Runnable WiFiOpRun -> OpenWiFi
    //----------------------------------------------------------------
    private Runnable WiFiOpRun = new Runnable(){
        public void run(){
            OpenWiFiThd = new Thread(new OpenWiFi());
            OpenWiFiThd.start();
        }
    };


    //----------------------------------------------------------------
    //  Runnable WiFiCmRun -> start & manage WiFiThread
    //----------------------------------------------------------------
    private Runnable WiFiCmRun = new Runnable(){
        public void run(){
            Running = true; CnTimeOut = 100; 	// 10 sec
            cThread = new Thread(new WiFiCmThd());
            cThread.start();
        }
    };

    //----------------------------------------------------------------
    //  WiFi Start Thread
    //----------------------------------------------------------------
    public class OpenWiFi implements Runnable {
        public void run()
        {
            if(!SocketOpen)
            {
                int	i = InternalData.Device_id;
                //------------------------
                //         LAN
                //------------------------
                //-- Create Lan socket --------------------------
                SocketAddress sockaddr = new InetSocketAddress(InternalData.DeviceMember.get(i).IPAD, Integer.valueOf(InternalData.Lan_Port));
                LanSocket = new Socket();
                SocketOpen = true; 		// assume
                try {LanSocket.connect(sockaddr, 500); }
                catch (IllegalArgumentException e){StatusText = "Status : connecting..."; SocketOpen = false; }
                catch (IOException e){StatusText = "Status : connecting..."; SocketOpen = false; }
                if(!SocketOpen){
                    mHandler.removeCallbacks(WiFiOpRun);		// else try again in 1 sec.
                    mHandler.postDelayed(WiFiOpRun, 1000);
                }
            }

            if((SocketOpen)&&(!StreamOpen))
            {
                StreamOpen = true;	// assume
                //-- Create Input Stream ------------------------
                try { disL = new DataInputStream(new BufferedInputStream(LanSocket.getInputStream())); }
                catch(Exception e) { StatusText = "Status : connecting..."; StreamOpen = false; }
                //-- Create Output Stream -----------------------
                try { dosL = new DataOutputStream(new BufferedOutputStream(LanSocket.getOutputStream())); }
                catch(Exception e) { StatusText = "Status : connecting..."; StreamOpen = false; }

                if(StreamOpen){

                    mHandler.removeCallbacks(WiFiCmRun);	// Open Communication
                    mHandler.postDelayed(WiFiCmRun, 500);
                    StatusText = "Status : Initialize communication...";
                }
                else{
                    mHandler.removeCallbacks(WiFiOpRun);		// else try again in 1 sec.
                    mHandler.postDelayed(WiFiOpRun, 500);
                }
            }
        }
    }
    //----------------------------------------------------------------
    //  WiFi Commu and Get Data Thread
    //----------------------------------------------------------------
    public class WiFiCmThd implements Runnable {
        public void run()
        {
            while(Running){

                if(!InitFlg){
                    if(InitCnt>=580){
                        P_DataOut(); InitCnt = 0;
                        if(k<4){ k++; }
                        else{ k = 0;}
                    }
                }
                else{
                    if(InitCnt>=1000){
                        P_DataOut(); InitCnt = 0;
                        if(k<4){ k++; }
                        else{ k = 0;}
                    }
                }


                try { if((disL.available()!=0)&&(disL.read(DataIn) == DataIn.length )){

                    P_DataIn();

                    //if(ReSend){ DataOut[2] = BufKey; P_DataOut(); ReSend = false; }
                    if(SendReq) { P_DataOut(); SendReq = false; }




                }} catch (IOException e1){  e1.printStackTrace();  }
            }
        }
    }

    //------------------------------
    //      Process Button
    //------------------------------
    public void press_md(View view){ DataOut[2] &= (byte) 0xFE; SendReq = true; }	// Botton mode
    public void press_fn(View view){ DataOut[2] &= (byte) 0xFD; SendReq = true; }	// Botton fan
    public void press_dn(View view){ DataOut[2] &= (byte) 0xFB; SendReq = true; }	// Botton down
    public void press_up(View view){ DataOut[2] &= (byte) 0xF7; SendReq = true; }	// Botton up
    public void press_pw(View view){ DataOut[2] &= (byte) 0xEF; SendReq = true; }	// Botton power
    public void press_detail(View view){
        Intent myIntent = new Intent(this, DetailActivity.class);	// Botton set
        startActivity(myIntent);
    }

    //------------------------------
    //		Process Data Out
    //------------------------------
    public void P_DataOut(){

        DataOut[0] = (byte) 0x41;						// 'A'
        DataOut[1] = (byte) (DataOut[2]^(byte) 0xFF);	// Invert of DataOut[2]
        DataOut[3] = (byte) 0x72;

        //-- Send Data Out -----------------
        try { dosL.write(DataOut); }
        catch (IOException e){ StatusText = "Status : Sending Data Fail"; }  // Write error
        try { dosL.flush(); }
        catch (IOException e){ StatusText = "Status : Sending Data Fail"; }  // Flush error

        ReqAns = true;
        BufKey = DataOut[2];
        DataOut[2] = (byte) 0xFF;
    }

    //------------------------------
    //		Process Data In
    //------------------------------
    public void P_DataIn(){

        int i;

        //-- Check Header ------------------------
        if(DataIn[0]==0x44)		// 'D'
        {
            //-- Check CheckSum ------------------------
            for(i=0;i<=16;i++){ DataIn[17] += (byte) DataIn[i]; }
            if(DataIn[17]==(byte) 0xFF)
            {

                InitCnt = 0;
                ReqAns = false;
                InitFlg = true;
                CnTimeOut = 100; 	// 10 sec


                switch(j)
                {
                    case 0:  StatusText = "Status : Receiving Data."; 	j = 1; break;
                    case 1:	 StatusText = "Status : Receiving Data.."; 	j = 2; break;
                    case 2:	 StatusText = "Status : Receiving Data..."; 	j = 3; break;
                    case 3:	 StatusText = "Status : Receiving Data...."; 	j = 4; break;
                    case 4:	 StatusText = "Status : Receiving Data....."; j = 0; break;
                }


                DataBuf[0] = DataIn[1];
                DataBuf[1] = DataIn[2];
                DataBuf[2] = DataIn[3];
                DataBuf[3] = DataIn[4];


            }
            else { StatusText = "Status : Receiving Data fail"; }  // Checksum error
        }
        else { StatusText = "Status : Receiving Data fail"; }  // Header error
    }
}
