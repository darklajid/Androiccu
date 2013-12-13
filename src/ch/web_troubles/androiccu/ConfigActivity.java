package ch.web_troubles.androiccu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);

		filesDir = this.getFilesDir();
		dataDir = Environment.getDataDirectory();

		TextView textView = (TextView) findViewById(R.id.config_description_text);
		textView.setText(Html.fromHtml(getResources().getString(
				R.string.config_description_text)));
		textView.setLinksClickable(true);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	// @Override
	public void onResume() {
		super.onResume();

		File file = new File(filesDir, "aiccu.conf");

		if (file.exists()) {
			new CheckTask().execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_config, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_config_back:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void search(View view) {
		username = ((EditText) findViewById(R.id.config_user)).getText()
				.toString();
		password = ((EditText) findViewById(R.id.config_pass)).getText()
				.toString();

		((Button) findViewById(R.id.config_search_button)).setEnabled(false);

		new SearchTask().execute();
	}

	public void save(View view) {
		username = ((EditText) findViewById(R.id.config_user)).getText()
				.toString();
		password = ((EditText) findViewById(R.id.config_pass)).getText()
				.toString();
		tunnel = String.valueOf(
				((Spinner) findViewById(R.id.config_search_spinner))
						.getSelectedItem()).split(" ")[0];

		((Button) findViewById(R.id.config_save_button)).setEnabled(false);

		new SaveTask().execute();
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
		EditText editText = (EditText) findViewById(R.id.config_user);
		editText.setText(username);
		editText = (EditText) findViewById(R.id.config_pass);
		editText.setText(password);

		Spinner spinner = (Spinner) findViewById(R.id.config_search_spinner);
		List<String> spinnerList = new ArrayList<String>();

		spinnerList.add("");
		if (tunnels.size() > 0) {
			for (int i = 0; i < tunnels.size(); i++) {
				String[] s = tunnels.elementAt(i).split(" ");

				if (s[2].equalsIgnoreCase("ayiya")) {
					spinnerList.add(s[0] + " (@" + s[3] + ")");
				}
			}
		} else {
			spinnerList.add(tunnel);
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spinnerList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);

		if (tunnels.size() > 0) {
			spinner.performClick();
		}
	}

	//
	// AsyncTasks
	//

	private class CheckTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				username = "";
				password = "";
				tunnel = "";
				MyShell shell = new MyShell();
				shell.exec("su", "cp " + dataDir.getPath()
						+ "/aiccu/aiccu.conf" + filesDir.getPath());
				FileInputStream inputStream;

				inputStream = openFileInput("aiccu.conf");
				BufferedReader r = new BufferedReader(new InputStreamReader(
						inputStream));
				String s;

				while (r.ready()) {
					s = r.readLine();

					if (s.substring(0, 8).equalsIgnoreCase("username")) {
						username = s.substring(9);
					} else if (s.substring(0, 8).equalsIgnoreCase("password")) {
						password = s.substring(9);
					} else if (s.substring(0, 9).equalsIgnoreCase("tunnel_id")) {
						tunnel = s.substring(10);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				// popup("Error", "Installation failed");
			}

			updateUI();
		}
	}

	private class SearchTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				String s = "username "
						+ username
						+ "\npassword "
						+ password
						+ "\nprotocol tic\nserver tic.sixxs.net\nipv6_interface sixxs\nverbose false\ndaemonize true\nautomatic true\nrequiretls false";
				FileOutputStream outputStream;
				outputStream = openFileOutput("test_aiccu.conf",
						Context.MODE_PRIVATE);
				outputStream.write(s.getBytes());
				outputStream.close();

				MyShell shell = new MyShell();
				tunnels.clear();
				errors.clear();
				shell.exec("su", "aiccu tunnels " + filesDir.getPath()
						+ "/test_aiccu.conf", tunnels, errors);

				if (tunnels.size() == 0) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			((Button) findViewById(R.id.config_search_button)).setEnabled(true);

			if (!result) {
				String msg = "Get tunnels failed:\n";

				if (errors.size() == 0) {
					msg += "no error message";
				} else {
					for (int i = 0; i < errors.size(); i++) {
						msg += errors.elementAt(i) + "\n";
					}
				}

				popup("Error", msg);
			}

			updateUI();
		}

		private Vector<String> errors = new Vector<String>();
	}

	private class SaveTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... dummy) {
			try {
				String s = "username " + username + "\npassword " + password
						+ "\n";

				if (tunnel.length() > 0) {
					s += "tunnel_id " + tunnel + "\n";
				}
				s += "protocol tic\nserver tic.sixxs.net\nipv6_interface sixxs\nverbose false\ndaemonize true\nautomatic true\nrequiretls false";
				FileOutputStream outputStream;
				outputStream = openFileOutput("aiccu.conf",
						Context.MODE_PRIVATE);
				outputStream.write(s.getBytes());
				outputStream.close();

				MyShell shell = new MyShell();
				results.clear();
				errors.clear();
				String[] cmds = {
						"mkdir /data/aiccu",
						"cp " + filesDir.getPath() + "/aiccu.conf "
								+ dataDir.getPath() + "/aiccu/",
						"chmod 600 " + dataDir.getPath() + "/aiccu/aiccu.conf" };
				shell.execMulti("su", cmds, results, errors);

				if (shell.exec(
						"sh",
						"ls " + Environment.getDataDirectory().getPath()
								+ "/aiccu/aiccu.conf").size() == 0) {
					return false;
				}

				File file = new File(filesDir, "test_aiccu.conf");

				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			((Button) findViewById(R.id.config_save_button)).setEnabled(false);

			if (!result) {
				String msg = "Save failed:\n";

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

				updateUI();
			} else {
				finish();
			}
		}

		private Vector<String> results = new Vector<String>();
		private Vector<String> errors = new Vector<String>();
	}

	private File filesDir;
	private File dataDir;
	private String username;
	private String password;
	private Vector<String> tunnels = new Vector<String>();
	private String tunnel;
}
