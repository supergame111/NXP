package com.nxp.nxpwalletconndev.httpcomm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class HttpUploadFile {
	static int serverResponseCode = 0;
	static String upLoadServerUri = null;

	public static int uploadFile(String address, int scriptId, int devId,String IMEI, String sourceFilePath, String sourceFileName) {
		/************* Php script path ****************/
        upLoadServerUri = address + "?IMEI=" + IMEI + "&scriptId=" + scriptId + "&devId=" + devId;

		String filePath = sourceFilePath;
		String fileName = sourceFileName;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(filePath + fileName);

		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File not exist :" + sourceFile);
			return 0;
		} else {
			try {

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);
				
				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);

				// filename?
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {
				ex.printStackTrace();

				// runOnUiThread(new Runnable() {
				// public void run() {
				// messageText.setText("MalformedURLException Exception : check script url.");
				// Toast.makeText(UploadToServer.this, "MalformedURLException",
				// Toast.LENGTH_SHORT).show();
				// }
				// });

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {
				e.printStackTrace();

				// runOnUiThread(new Runnable() {
				// public void run() {
				// messageText.setText("Got Exception : see logcat ");
				// Toast.makeText(UploadToServer.this,
				// "Got Exception : see logcat ",
				// Toast.LENGTH_SHORT).show();
				// }
				// });
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}

			return serverResponseCode;

		} // End else block
	}
}
