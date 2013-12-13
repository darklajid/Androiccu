package ch.web_troubles.androiccu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class MyShell {
	public Vector<String> exec(String sh, String command) {
		Vector<String> res = new Vector<String>();

		try {
			Process process = Runtime.getRuntime().exec(sh);

			// Input/output naming convention in the launched process' point of
			// view
			DataOutputStream inputStream = new DataOutputStream(
					process.getOutputStream());
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			inputStream.writeBytes(command + "\n");
			inputStream.flush();

			Thread.sleep(100);

			while (outputStream.ready()) {
				res.add(outputStream.readLine());
			}

			inputStream.writeBytes("exit\n");
			inputStream.flush();
			process.waitFor();

			while (outputStream.ready()) {
				res.add(outputStream.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public void exec(String sh, String command, Vector<String> results,
			Vector<String> errors) {
		try {
			Process process = Runtime.getRuntime().exec(sh);

			// Input/output naming convention in the launched process' point of
			// view
			DataOutputStream inputStream = new DataOutputStream(
					process.getOutputStream());
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			BufferedReader errorStream = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			results.clear();
			errors.clear();

			inputStream.writeBytes(command + "\n");
			inputStream.flush();

			Thread.sleep(100);

			while (outputStream.ready()) {
				results.add(outputStream.readLine());
			}
			while (errorStream.ready()) {
				errors.add(errorStream.readLine());
			}

			inputStream.writeBytes("exit\n");
			inputStream.flush();
			process.waitFor();

			while (outputStream.ready()) {
				results.add(outputStream.readLine());
			}
			while (errorStream.ready()) {
				errors.add(errorStream.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector<String> execMulti(String sh, String commands[]) {
		Vector<String> res = new Vector<String>();

		try {
			Process process = Runtime.getRuntime().exec(sh);

			// Input/output naming convention in the launched process' point of
			// view
			DataOutputStream inputStream = new DataOutputStream(
					process.getOutputStream());
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			for (String single : commands) {
				inputStream.writeBytes(single + "\n");
				inputStream.flush();

				Thread.sleep(100);

				while (outputStream.ready()) {
					res.add(outputStream.readLine());
				}
			}
			inputStream.writeBytes("exit\n");
			inputStream.flush();
			process.waitFor();

			while (outputStream.ready()) {
				res.add(outputStream.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public void execMulti(String sh, String commands[], Vector<String> results,
			Vector<String> errors) {
		try {
			Process process = Runtime.getRuntime().exec(sh);

			// Input/output naming convention in the launched process' point of
			// view
			DataOutputStream inputStream = new DataOutputStream(
					process.getOutputStream());
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			BufferedReader errorStream = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			results.clear();
			errors.clear();

			for (String single : commands) {
				inputStream.writeBytes(single + "\n");
				inputStream.flush();

				Thread.sleep(100);

				while (outputStream.ready()) {
					results.add(outputStream.readLine());
				}
				while (errorStream.ready()) {
					errors.add(errorStream.readLine());
				}
			}
			inputStream.writeBytes("exit\n");
			inputStream.flush();
			process.waitFor();

			while (outputStream.ready()) {
				results.add(outputStream.readLine());
			}
			while (errorStream.ready()) {
				errors.add(errorStream.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
