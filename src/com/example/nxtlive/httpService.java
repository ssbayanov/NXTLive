package com.example.nxtlive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class httpService {

	private static final String TAG = "httpService";

	private static final boolean D = true;

	private AcceptThread mHttpThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8080;

	public httpService(Context context, Handler handler) {

		mHandler = handler;
	}

	public synchronized void start() {

		// Start the thread to listen on a BluetoothServerSocket
		if (mHttpThread == null) {
			mHttpThread = new AcceptThread();
			mHttpThread.start();
		}
	}

	private class AcceptThread extends Thread {
		BufferedReader in = null;
		BufferedWriter out = null;
		ServerSocket ss = null;
		Socket s = null;

		public AcceptThread() {
			try {
				Log.d(TAG, "Try run mHttpService");
				ss = new ServerSocket(TCP_SERVER_PORT);
				
				if(ss == null){	
					Log.d(TAG, "ss is null. Exit");
					return;
				}
				
				s = ss.accept();
				Log.d(TAG, "Try create mHttpThread s2");
				in = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				
				out = new BufferedWriter(new OutputStreamWriter(
						s.getOutputStream()));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			Log.d(TAG, "mHttpThread running");
			// Keep listening to the InputStream while connected
			while (true) {

				try {
					// receive a message
					String incomingMsg = in.readLine()
							+ System.getProperty("line.separator");
					Log.i("TcpServer", "received: " + incomingMsg);
					// mHandler.obtainMessage("received: " +
					// incomingMsg).sendToTarget();
					// send a message
					String outgoingMsg = "goodbye from port " + TCP_SERVER_PORT
							+ System.getProperty("line.separator");
					out.write(outgoingMsg);
					out.flush();
					Log.i("TcpServer", "sent: " + outgoingMsg);
					// mHandler.obtainMessage("sent: " +
					// outgoingMsg).sendToTarget();

					// SystemClock.sleep(5000);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void cancel() {
			if (ss != null) {
				try {
					s.close();
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	};

}
