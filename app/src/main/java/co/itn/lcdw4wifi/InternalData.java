package co.itn.lcdw4wifi;

import java.util.ArrayList;

public class InternalData {

	public static String NetWork;
	public static String Lan_IP;
	public static String Net_IP;
	public static String Net_Port;
	
	public static int Device_id;	// For select which Device to be controlled or to be shown detail data.
	public static String Lan_Port = "2000";
	public static DeviceInfo Device01 = new DeviceInfo();	//
	public static DeviceInfo Device02 = new DeviceInfo();	// Create 5 New Object of Device
	public static DeviceInfo Device03 = new DeviceInfo();	//
	public static DeviceInfo Device04 = new DeviceInfo();	//
	public static DeviceInfo Device05 = new DeviceInfo();	//
	
	public static ArrayList<DeviceInfo> DeviceMember = new ArrayList<DeviceInfo>(5);	// Create New Object Device list

	public static void InitData() {		// Init Device List & all Object , Called at Home Activity
		
		DeviceMember.add(Device01);		//
		DeviceMember.add(Device02);		// Add Device object into Member-list
		DeviceMember.add(Device03);		//
		DeviceMember.add(Device04);		//
		DeviceMember.add(Device05);		//
		
		DeviceMember.get(0).Available = false;
		DeviceMember.get(1).Available = false;
		DeviceMember.get(2).Available = false;
		DeviceMember.get(3).Available = false;
		DeviceMember.get(4).Available = false;
		
	}
	
}
