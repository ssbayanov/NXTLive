package com.example.nxtlive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;


public class httpService {

	private static final String TAG = "httpService";

	private static final boolean D = true;

	private AcceptThread mHttpThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8080;

	private Context mainContext = null;
	
	public boolean isEnabled = false;
	


	public httpService(Context context, Handler handler) {
		mainContext = context;
		mHandler = handler;
	}

	public synchronized void start() {

		// Start the thread to listen on a BluetoothServerSocket
		if (mHttpThread == null) {
			mHttpThread = new AcceptThread();
			mHttpThread.start();
			isEnabled = true;
		}
	}
	
	public synchronized void stop() {

		// Start the thread to listen on a BluetoothServerSocket
		if (mHttpThread != null) {
			mHttpThread.cancel();
			isEnabled = false;
		}
	}

	private class AcceptThread extends Thread {
		InputStream in = null;
		OutputStream out = null;
		ServerSocket ss = null;
		Socket s = null;

		public AcceptThread() {
			try {
				Log.d(TAG, "Try run mHttpService");
				ss = new ServerSocket(TCP_SERVER_PORT);

				if (ss == null) {
					Log.d(TAG, "ss is null. Exit");
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				Log.d(TAG, "mHttpThread running");

				while (!ss.isClosed()) {
					if (s != null) {
						s.close();
					}
					s = ss.accept(); //wait new connection
					
					if (D) Log.d(TAG, "Try create mHttpThread");
					
					in = s.getInputStream();

					out = s.getOutputStream();
					// Keep listening to the InputStream while connected
					

					int size = in.available(); //get sze query
					byte[] buffer = new byte[size];
					
					in.read(buffer); //read all from buffer

					String query = new String(buffer); //query need for parsing
					
					//Log.i("TcpServer", "received: " + query);

					out.write(("HTTP/1.0 200 Ok\r\n"
							+ "Pragma: no-cache\r\n"
							+ "Expires: Thu, 01 Jan 1970 00:00:01 GMT\r\n"
							+ "Content-Type: text/html; charset=\"utf-8\""
							+ "\r\n\r\n").getBytes());
					
					AssetManager am = mainContext.getAssets();

					InputStream input = am.open("index.html"); //open file

					size = input.available(); //get size file
					buffer = new byte[size]; 
					input.read(buffer);
					input.close();

					out.write(buffer);

					out.flush();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {
			if (ss != null) {
				if (D) Log.d(TAG, "Try stop mHttpThread");
				try {
					s.close();
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Error close sockets");
				}
			}
		};
	};

}
