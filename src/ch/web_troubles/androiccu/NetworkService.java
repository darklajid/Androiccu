package ch.web_troubles.androiccu;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;

public class NetworkService extends Service {
	public static final String ACTION_GET_STATUS = "ch.web_troubles.androiccu.ACTION_GET_STATUS";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		updateUIIntent.setAction(MainActivity.ACTION_UPDATE_UI);
		setStatusIntent.setAction(MainActivity.ACTION_SET_STATUS);

		registerReceiver(receiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
		registerReceiver(receiver, new IntentFilter(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION));
		registerReceiver(receiver, new IntentFilter(ACTION_GET_STATUS));
		
		File file = new File(this.getFilesDir(), "log");
		
		if (file.exists()) {
		//	file.delete(); // TODO: put back again, to see how long the services stays stopped when that happens
		}
		aiccuStarted = false;
		
		try {
			log("started");
			
    		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		networkInfo = connectivityManager.getActiveNetworkInfo();
    		
    		if (networkInfo != null) {
    			networkConnected = networkInfo.isConnected();
	    		networkType = networkInfo.getType();
	    		networkTypeName = networkInfo.getTypeName();
    			
    			if (networkConnected) {
    				startAiccu(false);
    			} else {
					log("waiting to be connected to " + networkTypeName + "network");
    			}
    		} else {
    			networkConnected = false;
	    		networkType = -1;
	    		
				log("waiting to have a network");
    		}
		} catch (Exception e) {
    		e.printStackTrace();
    	}
		
		//handler.postDelayed(runnable, 100);
	}
	
	@Override
	public void onDestroy() {
    	handler.removeCallbacks(runnable);
		unregisterReceiver(receiver);
		stopAiccu(false);
		log("stopped");
	}
	
	private void log(String message) 
	{
		//long t = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String l = sdf.format(new Date()) + ": " + message + "\n";

		try {
			FileOutputStream outputStream;
			outputStream = openFileOutput("log", Context.MODE_PRIVATE | Context.MODE_APPEND);
			outputStream.write(l.getBytes());
			outputStream.close();
		
			sendBroadcast(updateUIIntent);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	private void startAiccu(boolean force) {
		if (!aiccuStarted || force) {
			MyShell shell = new MyShell();
			shell.exec("su", "aiccu start");
			aiccuStarted = true;
			setStatusIntent.putExtra(MainActivity.EXTRA_STATUS, aiccuStarted);
			sendBroadcast(setStatusIntent);

			log("aiccu started on " + networkTypeName + " network");
		}
	}
	
	private void stopAiccu(boolean force) {
		if (aiccuStarted || force) {
			MyShell shell = new MyShell();
	    	String []cmds = {"aiccu stop", "killall -9 aiccu"};
	    	shell.execMulti("su", cmds);
	    	aiccuStarted = false;
			setStatusIntent.putExtra(MainActivity.EXTRA_STATUS, aiccuStarted);
			sendBroadcast(setStatusIntent);
			log("aiccu stopped");
		}
	}

	private boolean aiccuStarted;
	private boolean networkConnected;
	private int networkType;
	private String networkTypeName;
	private String wifiSSID;
	ConnectivityManager connectivityManager;
	NetworkInfo networkInfo;
	private Intent updateUIIntent = new Intent();
	private Intent setStatusIntent = new Intent();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
    	//@Override
    	public void run() {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		handler.postDelayed(this, 1000 * 60 * 5);    	
    	}
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
    			try {
		    		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    		
		    		if (networkInfo == null) { // Completely lost any network
		    			if (networkType != -1) { // We had network before
		    				log("network lost, waiting for something to come up");
		    				stopAiccu(false);
		        			networkConnected = false;
		    	    		networkType = -1;
		    			}
		    		} else { // At least a network interface if found
		    			boolean oldNetworkConnected = networkConnected;
			    		int oldNetworkType = networkType;
			    		networkConnected = networkInfo.isConnected();
			    		networkType = networkInfo.getType();
			    		networkTypeName = networkInfo.getTypeName();
			    		
			    		if (oldNetworkType != networkType) { // Change of network, might need to stop and/or start aiccu			    		
			    			log("network change detected");
			    			
			    			if (oldNetworkConnected) {
			    				stopAiccu(false);
			    			}
			    			
			    			if (networkConnected) {
			    				startAiccu(false);
			    			}
			    		} else if (oldNetworkConnected != networkConnected) { // Network connection change
			    			if (networkConnected) {			    		
				    			log("network connection detected");
				    			
					    		startAiccu(false);
			    			} else {			    		
				    			log("network disconnection detected");
				    			
					    		stopAiccu(false);
			    			}
			    		} else {
			    			//log("some network changes occured");
			    		}
		    		}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		} else if (intent.getAction().equals(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
    			try {
    				if (networkType == ConnectivityManager.TYPE_WIFI)
    				{
    					NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
    					if (info.getState() == NetworkInfo.State.CONNECTED) {
    						if (!networkConnected) {
    							WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    							WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    							wifiSSID = wifiInfo.getSSID();
    							networkConnected = true;
    							
    							log("new wifi connection" + ((wifiSSID.length() > 0) ? (" to " + wifiSSID) : ""));
					    		startAiccu(false);
    						}
    					} else {
        					if (networkConnected) {
        						networkConnected = false;
        						log("wifi disconnection");
        						stopAiccu(false);
        					}
    					}
    				}
				} catch (Exception e) {
					e.printStackTrace();
				}
    		} else if (intent.getAction().equals(ACTION_GET_STATUS)) {
    			setStatusIntent.putExtra(MainActivity.EXTRA_STATUS, aiccuStarted);
    			sendBroadcast(setStatusIntent);
    		}
    	}
    };
}
