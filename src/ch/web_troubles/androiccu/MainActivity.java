package ch.web_troubles.androiccu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String ACTION_UPDATE_UI = "ch.web_troubles.androiccu.ACTION_UPDATE_UI";
	public static final String ACTION_SET_STATUS = "ch.web_troubles.androiccu.ACTION_SET_STATUS";
	public static final String EXTRA_STATUS = "ch.web_troubles.androiccu.EXTRA_STATUS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    	filesDir = this.getFilesDir();
    	dataDir = Environment.getDataDirectory();

		service = new Intent(this, NetworkService.class);

    	Button button = (Button) findViewById(R.id.main_button_start);
    	button.setEnabled(false);
    	button = (Button) findViewById(R.id.main_button_stop);
    	button.setEnabled(false);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
    	try {
    		new CheckTask().execute();
    		
			int previousVersion = 0;
			File file = new File(filesDir, "version");
			
			if (file.exists()) {
				FileInputStream inputStream;
				
				inputStream = openFileInput("version");
				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
				
				if (r.ready()) {
					previousVersion = Integer.valueOf(r.readLine());
				}
				
				inputStream.close();
			}
			
			if (previousVersion < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
		    	Intent intent = new Intent(this, ChangeLogActivity.class);
		        startActivity(intent);
			}
	
			registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_UI));
			registerReceiver(receiver, new IntentFilter(ACTION_SET_STATUS));
	
			Intent intent = new Intent();
			intent.setAction(NetworkService.ACTION_GET_STATUS);
			sendBroadcast(intent);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    case R.id.menu_install:
	    	if (running) {
	    		popup("Warning", getResources().getString(R.string.menu_install_warning));
	    		return true;
	    	}
	    	Intent intent_install = new Intent(this, InstallActivity.class);
	        startActivity(intent_install);
	        return true;
	    case R.id.menu_config:
	    	if (running) {
	    		popup("Warning", getResources().getString(R.string.menu_config_warning));
	    		return true;
	    	}
	    	Intent intent_config = new Intent(this, ConfigActivity.class);
	        startActivity(intent_config);
	        return true;
    	}
        return super.onOptionsItemSelected(item);
    }

    public void start(View view) {
    	Button button = (Button) findViewById(R.id.main_button_start);
    	button.setEnabled(false);

		running = true;
		startService(service);
		
    	button = (Button) findViewById(R.id.main_button_stop);
    	button.setEnabled(true);  	
    }
    
    public void stop(View view) {
    	Button button = (Button) findViewById(R.id.main_button_stop);
    	button.setEnabled(false);
    	
    	stopService(service);
    	running = false;
    	
    	button = (Button) findViewById(R.id.main_button_start);
    	button.setEnabled(true);
    }
    
    /*
    private String millisToTime(long millis) {
    	long seconds =  millis / 1000;
    	
    	if (seconds < 60) {
    		return seconds + " " + getResources().getString(R.string.main_second) + (seconds > 1 ? getResources().getString(R.string.main_plural) : "");
    	}
    	
    	long minutes = seconds / 60;
    	
    	if (minutes < 60) {
    		return minutes + " " + getResources().getString(R.string.main_minute) + (minutes > 1 ? getResources().getString(R.string.main_plural) : "");
    	}
    	
    	long hours = minutes / 60;
    	
    	if (hours < 24) {
    		return hours + " " + getResources().getString(R.string.main_hour) + (hours > 1 ? getResources().getString(R.string.main_plural) : "");
    	}
    	
    	long days = hours / 24;
    	
    	return days + " " + getResources().getString(R.string.main_day) + (days > 1 ? getResources().getString(R.string.main_plural) : "");
    }
    */
    
    private void popup(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		//alertDialog.setIcon(icon))
		alertDialog.show();
    }
    
    private void updateUI() {
    	TextView textView = (TextView) findViewById(R.id.main_status);
    	log = "";
    	
    	try { 
			File file = new File(filesDir, "log");
			if (file.exists()) {
		        FileInputStream inputStream;
				inputStream = openFileInput("log");
				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
				
				while (r.ready()) {
					log += r.readLine() + "\n";
				}
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
    	
    	if (!installed) {
    		textView.setText(R.string.main_not_installed);
    	} else if (!configured) {
    		textView.setText(R.string.main_not_configured);
    	} else {
        	if (running) {
        		textView.setText(getResources().getString(R.string.main_running) + "\n" + log);
        	} else {
        		textView.setText(getResources().getString(R.string.main_not_running) + "\n" + log);
        	}
    	}

    	ImageView imageView = (ImageView) findViewById(R.id.main_status_image);
		if (tunnel_status) {
			imageView.setImageDrawable(getResources().getDrawable(R.drawable.ok));
    	} else {
			imageView.setImageDrawable(getResources().getDrawable(R.drawable.nok));
    	}
    }
    
    
    //
    // AsyncTasks
    //


	private class CheckTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				MyShell shell = new MyShell();
		    	Vector<String> cmd_ret = shell.exec("sh", "ls /system/xbin/aiccu");

		    	if (cmd_ret.size() < 1) {
		    		installed = false;
		    	} else {
		    		installed = true;
		    	}

		    	cmd_ret = shell.exec("sh", "ls " + dataDir.getPath() + "/aiccu/aiccu.conf");
			
		    	if (cmd_ret.size() < 1) {
		    		configured = false;
		    	} else {
		    		configured = true;
		    	}
		    	
		    	running = false;
		    	ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		    		if (NetworkService.class.getName().equals(service.service.getClassName())) { 
		    			running = true;
		    		}
		    	}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		return false;
	    	}
			
			return true;
		}
		
		//protected void onProgressUpdate() {
		//}
		
		protected void onPostExecute(Boolean result) {
			if (!result) {
				popup("Error", "Check failed");
			}

	    	if (installed && configured) {
		    	Button startButton = (Button) findViewById(R.id.main_button_start);
		    	Button stopButton = (Button) findViewById(R.id.main_button_stop);
		    	
	    		if (running) {
			    	startButton.setEnabled(false);
			    	stopButton.setEnabled(true);
	    		} else {
			    	startButton.setEnabled(true);
			    	stopButton.setEnabled(false);
	    		}
	    	}
	    	
	    	updateUI();
		}
	}


	private File filesDir;
	private File dataDir;
	private boolean installed;
	private boolean configured;
    private boolean running;
    private boolean tunnel_status;
    private String log;
    private Intent service;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (intent.getAction().equals(ACTION_UPDATE_UI)) {
    			updateUI();
    		} else if (intent.getAction().equals(ACTION_SET_STATUS)) {
    			tunnel_status = intent.getExtras().getBoolean(EXTRA_STATUS);
    			updateUI();
    		}
    	}
    };
}