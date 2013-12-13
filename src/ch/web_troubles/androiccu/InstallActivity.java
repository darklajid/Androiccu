package ch.web_troubles.androiccu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class InstallActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install);

		downloadFilename = getResources().getString(R.string.install_filename);
		filesDir = this.getFilesDir();

		ImageView imageView = (ImageView) findViewById(R.id.install_download_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		Button button = (Button) findViewById(R.id.install_download_button);
		button.setEnabled(false);
		imageView = (ImageView) findViewById(R.id.install_install_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		button = (Button) findViewById(R.id.install_install_button);
		button.setEnabled(false);
		imageView = (ImageView) findViewById(R.id.install_config_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		button = (Button) findViewById(R.id.install_config_button);
		button.setEnabled(false);
	}

	// @Override
	public void onResume() {
		super.onResume();

		new CheckTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_install, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_install_back:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public void download(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				ImageView imageView = (ImageView) findViewById(R.id.install_download_image);
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.prog));
				Button button = (Button) findViewById(R.id.install_download_button);
				button.setEnabled(false);

				URL requestURL = new URL(getResources().getString(
						R.string.install_download_url)
						+ "/" + downloadFilename);
				new DownloadTask().execute(requestURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			popup("Error", "No network connection available");
		}
	}

	public void install(View view) {
		ImageView imageView = (ImageView) findViewById(R.id.install_install_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		Button button = (Button) findViewById(R.id.install_install_button);
		button.setEnabled(false);

		new InstallTask().execute();
	}

	public void config(View view) {
		ImageView imageView = (ImageView) findViewById(R.id.install_config_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		Button button = (Button) findViewById(R.id.install_config_button);
		button.setEnabled(false);

		Intent intent_config = new Intent(this, ConfigActivity.class);
		startActivity(intent_config);
	}

	public void uninstall(View view) {
		ImageView imageView = (ImageView) findViewById(R.id.install_download_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		imageView = (ImageView) findViewById(R.id.install_install_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));
		imageView = (ImageView) findViewById(R.id.install_config_image);
		imageView.setImageDrawable(getResources().getDrawable(R.drawable.prog));

		new UninstallTask().execute();
	}

	private void popup(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		// alertDialog.setIcon(icon))
		alertDialog.show();
	}

	private void updateUI() {
		ImageView imageView = (ImageView) findViewById(R.id.install_download_image);
		Button button = (Button) findViewById(R.id.install_download_button);

		if (downloaded) {
			imageView.setImageDrawable(getResources()
					.getDrawable(R.drawable.ok));
			button.setEnabled(false);
		} else {
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.nok));
			button.setEnabled(true);
		}

		imageView = (ImageView) findViewById(R.id.install_install_image);
		button = (Button) findViewById(R.id.install_install_button);

		if (installed) {
			imageView.setImageDrawable(getResources()
					.getDrawable(R.drawable.ok));
			button.setEnabled(false);
		} else {
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.nok));

			if (downloaded) {
				button.setEnabled(true);
			} else {
				button.setEnabled(false);
			}
		}

		imageView = (ImageView) findViewById(R.id.install_config_image);
		button = (Button) findViewById(R.id.install_config_button);

		if (configured) {
			imageView.setImageDrawable(getResources()
					.getDrawable(R.drawable.ok));
			button.setEnabled(false);
		} else {
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.nok));

			if (installed) {
				button.setEnabled(true);
			} else {
				button.setEnabled(false);
			}
		}
	}

	private String[] getSystemMountInfo() {
		MyShell shell = new MyShell();
		Vector<String> cmd_ret = shell.exec("sh", "mount");
		String res[] = { "", "", "", "" };

		// HTC Desire HD running on LeeDroid reports:
		// /dev/block/mmcblk0p25 on /system type ext4
		// (ro,relatime,barrier=1,data=ordered,noauto_da_alloc)
		// Samsung S5570 mini on rooted stock ROM reports:
		// /dev/stl12 /system rfs
		// ro,relatime,vfat,log_off,check=no,gid/uid/rwx,iocharset=utf8 0 0

		// So there seems to be 2 busybox? variants of reporting mounted
		// partions, handling both

		for (int i = 0; i < cmd_ret.size(); i++) {
			String[] s = cmd_ret.elementAt(i).split(" ");

			if (s[1].equalsIgnoreCase("on") && s[3].equalsIgnoreCase("type")) { // HTC
																				// mode
				if (s[2].equalsIgnoreCase("/system")) {
					res[0] = s[0];
					res[1] = s[2];
					res[2] = s[4];
					res[3] = s[5].substring(1, 3);
					break;
				}
			} else {
				if (s[1].equalsIgnoreCase("/system")) { // Samsung mode
					res[0] = s[0];
					res[1] = s[1];
					res[2] = s[2];
					res[3] = s[3].substring(0, 2);
					break;
				}
			}
		}

		return res;
	}

	//
	// AsyncTasks
	//

	private class CheckTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				MyShell shell = new MyShell();
				File file = new File(filesDir, downloadFilename);
				if (file.exists()) {
					downloaded = true;
				} else {
					downloaded = false;
				}

				if (shell.exec("sh", "ls /system/xbin/aiccu").size() > 0) {
					installed = true;
				} else {
					installed = false;
				}

				if (shell.exec(
						"sh",
						"ls " + Environment.getDataDirectory().getPath()
								+ "/aiccu/aiccu.conf").size() > 0) {
					configured = true;
				} else {
					configured = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				popup("Error", "Installation failed");
			}

			updateUI();
		}
	}

	private class DownloadTask extends AsyncTask<URL, Void, Boolean> {
		protected Boolean doInBackground(URL... urls) {
			try {
				URLConnection connection = urls[0].openConnection();
				connection.connect();
				InputStream response = connection.getInputStream();
				FileOutputStream outputStream;
				byte[] buffer = new byte[300 * 1024]; // TODO must be dynamic !
				int read = 0;
				int tread = 0;

				while ((read = response.read(buffer, tread, 300 * 1024 - tread)) > 0) {
					tread += read;
				}

				response.close();

				outputStream = openFileOutput(downloadFilename,
						Context.MODE_PRIVATE);
				outputStream.write(buffer, 0, tread);
				outputStream.close();

				File file = new File(filesDir, downloadFilename);

				if (file.exists()) {
					downloaded = true;
				} else {
					downloaded = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return downloaded;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				popup("Error", "Download failed");
			}

			updateUI();
		}
	}

	private class InstallTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				MyShell shell = new MyShell();
				String m[] = getSystemMountInfo();
				String cmds[] = {
						"mount -o rw,remount -t " + m[2] + " " + m[0] + " "
								+ m[1],
						"cp -d /system/xbin/ip /system/xbin/ip.androiccu.backup",
						"tar xf " + filesDir.getPath() + "/" + downloadFilename,
						"mount -o ro,remount -t " + m[2] + " " + m[0] + " "
								+ m[1], "mkdir /data/aiccu",
						"chmod 775 /data/aiccu" };
				results.clear();
				errors.clear();
				shell.execMulti("su", cmds, results, errors);

				if (shell.exec("sh", "ls /system/xbin/aiccu").size() > 0) {
					installed = true;
				} else {
					installed = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return installed;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				String msg = "Installation failed:\n";

				if (errors.size() == 0) {
					if (results.size() == 0) {
						msg += "no error message";
					} else {
						for (int i = 0; i < results.size(); i++) {
							msg += results.elementAt(i) + "\n";
						}
					}
				} else {
					for (int i = 0; i < errors.size(); i++) {
						msg += errors.elementAt(i) + "\n";
					}
				}

				popup("Error", msg);
			}

			updateUI();
		}

		private Vector<String> results = new Vector<String>();
		private Vector<String> errors = new Vector<String>();
	}

	private class UninstallTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				MyShell shell = new MyShell();
				File file = new File(filesDir, downloadFilename);
				file.delete();
				String m[] = getSystemMountInfo();
				String cmds[] = {
						"mount -o rw,remount -t " + m[2] + " " + m[0] + " "
								+ m[1],
						"mv /system/xbin/ip.androiccu.backup /system/xbin/ip",
						"rm /system/xbin/aiccu",
						"mount -o ro,remount -t " + m[2] + " " + m[0] + " "
								+ m[1], "rm -r /data/aiccu" };
				results.clear();
				errors.clear();
				shell.execMulti("su", cmds, results, errors);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				String msg = "Uninstallation failed:\n";

				if (errors.size() == 0) {
					if (results.size() == 0) {
						msg += "no error message";
					} else {
						for (int i = 0; i < results.size(); i++) {
							msg += results.elementAt(i) + "\n";
						}
					}
				} else {
					for (int i = 0; i < errors.size(); i++) {
						msg += errors.elementAt(i) + "\n";
					}
				}

				popup("Error", msg);
			}

			new CheckTask().execute();
		}

		private Vector<String> results = new Vector<String>();
		private Vector<String> errors = new Vector<String>();
	}

	private File filesDir;
	private String downloadFilename;
	private boolean downloaded;
	private boolean installed;
	private boolean configured;
}
