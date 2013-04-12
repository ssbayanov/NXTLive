package com.example.nxtlive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

	public httpService(Context context, Handler handler) {
		mainContext = context;
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

				while (true) {
					if (s != null) {
						s.close();
					}
					s = ss.accept();
					Log.d(TAG, "Try create mHttpThread s5");
					in = new BufferedReader(new InputStreamReader(
							s.getInputStream()));

					out = new BufferedWriter(new OutputStreamWriter(
							s.getOutputStream()));
					// Keep listening to the InputStream while connected
					List<String> query = new ArrayList<String>();

					String buf;
					do {
						buf = in.readLine();
						query.add(buf);
						Log.i("TcpServer", "received: " + buf);
					} while (buf.length() > 2);
					Log.i(TAG, "Readin query finish");
					String outgoingMsg = "HTTP/1.0 200 Ok\r\nPragma: no-cache\r\nExpires: Thu, 01 Jan 1970 00:00:01 GMT\r\nContent-Type: text/html; charset=\"utf-8\"\r\n\r\n";//<html><head><title>Панель управления</title></head><body><h1>Панель управления</h1></body></html>";
					out.write(outgoingMsg);
					AssetManager am = mainContext.getAssets();

					InputStream input = am.open("index.html");

					int size = input.available();
					byte[] buffer = new byte[size];
					input.read(buffer);
					input.close();

					// byte buffer into a string
					String text = new String(buffer);

					out.write(text);
					

					out.flush();
					// Log.i("TcpServer", "sent: " + outgoingMsg);
					// mHandler.obtainMessage("sent: " +
					// outgoingMsg).sendToTarget();

					// SystemClock.sleep(5000);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
