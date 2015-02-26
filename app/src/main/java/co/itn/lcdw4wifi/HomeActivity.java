package co.itn.lcdw4wifi;

import co.itn.lcdw4wifi.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class HomeActivity extends Activity {
	
	public static final String PREFS_NAME = "MyConfig";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		InternalData.InitData();
		LoadConfig();
	}	
	
	public void press_enter(View view)							// Botton Enter
    { 
		Intent intent = new Intent(this, ListActivity.class);
		startActivity(intent);
    }	
    public void press_setting(View view)						// Botton Setting
    { 
    	Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);	
    }	
	
    private void LoadConfig(){
    	
    	SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
    	InternalData.DeviceMember.get(0).NAME = sharedPreferences.getString("NAME0", "Room 1");
    	InternalData.DeviceMember.get(1).NAME = sharedPreferences.getString("NAME1", "Room 2");
    	InternalData.DeviceMember.get(2).NAME = sharedPreferences.getString("NAME2", "Room 3");
    	InternalData.DeviceMember.get(3).NAME = sharedPreferences.getString("NAME3", "Room 4");
    	InternalData.DeviceMember.get(4).NAME = sharedPreferences.getString("NAME4", "Room 5");
    	InternalData.DeviceMember.get(0).IPAD = sharedPreferences.getString("IPAD0", "");
    	InternalData.DeviceMember.get(1).IPAD = sharedPreferences.getString("IPAD1", "");
    	InternalData.DeviceMember.get(2).IPAD = sharedPreferences.getString("IPAD2", "");
    	InternalData.DeviceMember.get(3).IPAD = sharedPreferences.getString("IPAD3", "");
    	InternalData.DeviceMember.get(4).IPAD = sharedPreferences.getString("IPAD4", "");
    	InternalData.DeviceMember.get(0).MCAD = sharedPreferences.getString("MCAD0", "");
    	InternalData.DeviceMember.get(1).MCAD = sharedPreferences.getString("MCAD1", "");
    	InternalData.DeviceMember.get(2).MCAD = sharedPreferences.getString("MCAD2", "");
    	InternalData.DeviceMember.get(3).MCAD = sharedPreferences.getString("MCAD3", "");
    	InternalData.DeviceMember.get(4).MCAD = sharedPreferences.getString("MCAD4", "");
	
    }
  
}
