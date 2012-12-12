package ch.web_troubles.androiccu;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class ChangeLogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_log);

		File file = new File(this.getFilesDir(), "started");
		
		if (file.exists()) {
			file.delete();
		}

		file = new File(this.getFilesDir(), "androiccu_0.1.tar.bz2");
		
		if (file.exists()) {
			file.delete();
		}

		file = new File(this.getFilesDir(), "androiccu_0.1.tgz");
		
		if (file.exists()) {
			file.delete();
		}
	}
    
    //@Override
    public void onResume() {
    	super.onResume();
    }
    
    public void onOk(View view) {
    	try {
			FileOutputStream outputStream;
			outputStream = openFileOutput("version", Context.MODE_PRIVATE);
			outputStream.write(Integer.toString(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).getBytes());
			outputStream.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	finish();
    }
}
