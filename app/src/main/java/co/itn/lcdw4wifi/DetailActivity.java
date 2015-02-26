package co.itn.lcdw4wifi;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {

	public static final String PREFS_NAME = "MyConfig";
	EditText NameEdt;
	TextView IPAddr;
	TextView MCAddr;
	String BufString;
	
	//----------------------------------------------------------------
    //  OnCreate
    //----------------------------------------------------------------	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		NameEdt  = (EditText)  findViewById(R.id.NameEdit);
		IPAddr	 = (TextView)  findViewById(R.id.IPView);
		MCAddr	 = (TextView)  findViewById(R.id.MCView);
		
        NameEdt.setText(InternalData.DeviceMember.get(InternalData.Device_id).NAME);	// Display Name
        IPAddr.setText(InternalData.DeviceMember.get(InternalData.Device_id).IPAD);		// Display IP
		MCAddr.setText(InternalData.DeviceMember.get(InternalData.Device_id).MCAD);		// Display MAC
	}
	
	private void SaveData() {
		
		if(!NameEdt.getText().toString().equals("")){
			InternalData.DeviceMember.get(InternalData.Device_id).NAME = NameEdt.getText().toString();
		}
		else{
			InternalData.DeviceMember.get(InternalData.Device_id).NAME = "Room " + (InternalData.Device_id+1);
		}
		
		SaveConfig();
	}
	
	public void press_save(View view){ SaveData(); 		// Botton Save
	Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show(); 
	NameEdt.setText(InternalData.DeviceMember.get(InternalData.Device_id).NAME);}	
	
	public void press_reset(View view){
		InternalData.DeviceMember.get(InternalData.Device_id).NAME = "Room " + (InternalData.Device_id+1);
		InternalData.DeviceMember.get(InternalData.Device_id).IPAD = "";
		InternalData.DeviceMember.get(InternalData.Device_id).MCAD = "";
		
		NameEdt.setText(InternalData.DeviceMember.get(InternalData.Device_id).NAME);	// Display Name
        IPAddr.setText(InternalData.DeviceMember.get(InternalData.Device_id).IPAD);		// Display IP
		MCAddr.setText(InternalData.DeviceMember.get(InternalData.Device_id).MCAD);		// Display MAC
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
