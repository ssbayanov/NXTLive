package com.example.nxtlive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

public class httpService {
	// Name of the connected device
	private String mConnectedDeviceName = null;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private BluetoothAdapter mBluetoothAdapter = null;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_DISCONNECT_DEVICE = 2;
	static final int REQUEST_ENABLE_BT = 3;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	private static final String TAG = "httpService";

	private static final boolean D = true;

	private AcceptThread mHttpThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8080;

	private Context mainContext = null;

	public boolean isEnabled = false;

	InputStream in = null;
	OutputStream out = null;
	ServerSocket ss = null;
	Socket s = null;

	public httpService(Context context, Handler handler) {
		mainContext = context;
		mHandler = handler;

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
		getIp();

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

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";
	}

	public void getIp() {
		if (ss != null) {
			Message msg = mHandler.obtainMessage(MainActivity.LOCAL_IP);
			Bundle bundle = new Bundle();
			bundle.putString("0", getIPAddress(true) + ":" + ss.getLocalPort());
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		} else
			mHandler.obtainMessage(MainActivity.NOT_CONNECTED).sendToTarget();
	}

	private class AcceptThread extends Thread {

		public void run() {
			try {
				Log.d(TAG, "mHttpThread running");

				while (!ss.isClosed()) {
					if (s != null) {
						s.close();
					}
					
					getIp();
					s = ss.accept(); // wait new connection

					getIp();
					if (D)
						Log.d(TAG, "Try create mHttpThread");

					in = s.getInputStream();

					out = s.getOutputStream();
					// Keep listening to the InputStream while connected

					int size = in.available(); // get sze query
					byte[] buffer = new byte[size];

					in.read(buffer); // read all from buffer

					String query = new String(buffer); // query need for parsing

					String[] tok = query.split("[ \r\n][ \r\n]*");

					if (D)
						Log.d(TAG, "Get query: " + query);

					if (tok.length > 1 && D) {
						Log.d(TAG, "query page: \""+tok[1]+"\"");
						// Log.i("TcpServer", "received: " + query);

						out.write(("HTTP/1.0 200 Ok\r\n"
								+ "Pragma: no-cache\r\n"
								+ "Expires: Thu, 01 Jan 1970 00:00:01 GMT\r\n"
								+ "Content-Type: text/html; charset=\"utf-8\""
								+ "\r\n\r\n").getBytes());

						AssetManager am = mainContext.getAssets();

						InputStream input = null;
						if (tok[1].equals("/") || tok[1].equals("/index.html")) {
							input = am.open("index.html"); // open file
							Log.d(TAG, "sending page: index.html");
						} else if (tok[1].equals("/controll.html")) {
							input = am.open("controll.html"); // open file
						} else if (tok[1].equals("/video.html")) {
							input = am.open("video.html"); // open file
						} else {
							input = am.open("404.html"); // open file
						}

						size = input.available(); // get size file
						buffer = new byte[size];
						input.read(buffer);
						input.close();
						
						String page = new String(buffer);
						page = page.replace("%ip%", getIPAddress(true));
						page = page.replace("%port%", ""+ss.getLocalPort());
						page = page.replace("%vport%", ""+(ss.getLocalPort()+1));

						out.write(page.getBytes());

						out.flush();
					}
					else
						Log.d(TAG, "Empty query page");

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {
			if (ss != null) {
				if (D)
					Log.d(TAG, "Try stop mHttpThread");
				try {
					if (s != null)
						s.close();
					if (ss != null)
						ss.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Error close sockets");
				}
			}
		};
	};

}
