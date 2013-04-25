package com.communication.nxtlive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class HttpService {
	// Name of the connected device

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final String TAG = "httpService";

	private static final boolean D = true;

	private AcceptThread mHttpThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8080;

	private Context mainContext = null;

	public boolean isEnabled = false;

	InputStream in = null;

	ServerSocket ss = null;

	public Camera camera;

	private byte[] lastPicture = null;

	private boolean inPreview = false;

	private boolean cameraConfigured = false;

	public SurfaceView surfaceView;

	public SurfaceHolder surfaceHolder;

	BluetoothService mBTService;

	public HttpService(Context context, Handler handler, SurfaceView sV) {
		mainContext = context;
		mHandler = handler;

		mBTService = new BluetoothService(context, mHandler);

		surfaceView = sV;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		// surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.lockCanvas();

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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, info);

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camera = Camera.open(i);
				}
			}
		}

		if (camera == null) {
			camera = Camera.open();
		}

		startPreview();

		getIp();

	}

	public void onResume() {

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

		mBTService.stop();

		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.release();
		}
	}

	// -------------------------------------------------------------------------
	/*
	 * 
	 * Camera functions
	 */

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {

		if (camera != null && surfaceHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);
				// Camera.Size pictureSize = getSmallestPictureSize(parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					// parameters.setPreviewFpsRange(5, 15);

					parameters.setJpegQuality(30);

					camera.setParameters(parameters);
					// camera.setDisplayOrientation(90);
					cameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	Camera.PreviewCallback JPGPrewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Size previewSize = camera.getParameters().getPreviewSize();
			YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
					previewSize.width, previewSize.height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width,
					previewSize.height), 30, baos);
			baos.toByteArray();

			lastPicture = baos.toByteArray();
		}
	};

	// -------------------------------------------------------------------------
	/*
	 * 
	 * Network functions
	 */

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

	private class SendThread extends Thread {
		private OutputStream out;
		private Socket socket = null;

		public SendThread(Socket s) {
			socket = s;
		}

		public void run() {
			try {
				in = socket.getInputStream();

				int size = in.available(); // get size query

				if (size == 0) {
					SystemClock.sleep(100);
					size = in.available();
				}

				byte[] buffer = new byte[size];

				in.read(buffer); // read all from buffer

				String query = new String(buffer);

				String[] tok = query.split("[ \r\n][ \r\n]*");

				if (D)
					Log.d(TAG, "Get query: " + query);

				if (tok.length > 1) {

					if (D)
						Log.d(TAG, "query page: \"" + tok[1] + "\"");
					// Log.i("TcpServer", "received: " + query);

					if (tok[1].indexOf("/") == 0 && tok[1].indexOf("../") == -1) {
						if (tok[1].equals("/")) {
							writePage("index.html");
						} else {
							String[] page = tok[1].split("/");
							if (page[1].equals("commands")) {
								if (page[2].equals("sound")) {

									mBTService.write(NXTCommander.playTone(
											(short) 5000, (short) 1000));
									Log.d(TAG, "send command: \"" + page[2]
											+ "\"");
								} else if (page[2].equals("motors")) {
									mBTService.write(NXTCommander.run(
											Integer.parseInt(page[3]),
											Integer.parseInt(page[4])));
								}
								writePage("nullPage");
							} else if (page[1].equals("message")) {
								mBTService.write(NXTCommander.sendMesage(
										page[3], Integer.parseInt(page[2])));
								writePage("nullPage");
							} else if (page[1].equals("videoStream")) {
								writeVideoStream();
							} else {
								Log.d(TAG, "send page: \"" + page[1] + "\"");
								writePage(page[1]);
							}
						}
					} else {
						writePage("404.html"); // open file
					}

				} else {
					Log.d(TAG, "Empty query page");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void writePage(String page) {
			InputStream input = null;
			AssetManager am = mainContext.getAssets();
			int size = 0;
			String mimeType = "";
			try {
				out = socket.getOutputStream();

				try {
					input = am.open(page);
					size = input.available(); // get size file
				} catch (IOException e) {
					input = am.open("404.html");
					size = input.available();
				}

				if (page.contains(".js"))
					mimeType = "text/javascript";
				else if (page.contains(".css"))
					mimeType = "text/css";
				else
					mimeType = "text/html";

				out.write(("HTTP/1.0 200 Ok\r\n" + "Pragma: no-cache\r\n"
						+ "Expires: Thu, 01 Jan 1970 00:00:01 GMT\r\n"
						+ "Content-Type: " + mimeType + "; charset=\"utf-8\"" + "\r\n\r\n")
						.getBytes());

				byte[] buffer = new byte[size];
				input.read(buffer);
				input.close();

				String p = new String(buffer);
				p = p.replace("%ip%", getIPAddress(true));
				p = p.replace("%port%", "" + ss.getLocalPort());

				out.write(p.getBytes());
				out.write(("\r\n\r\n").getBytes());

				out.flush();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void writeVideoStream() {
			try {
				out = socket.getOutputStream();

				out.write(("HTTP/1.1 200 OK\r\n"
						+ "Content-Type: multipart/x-mixed-replace;boundary=b\r\n"
						+ "Cache-Control: no-store\r\n"
						+ "Pragma: no-cache\r\n" + "Connection: close\r\n"
						+ "\r\n").getBytes());

				camera.setPreviewCallback(JPGPrewCallback);

				while (!socket.isOutputShutdown()) {

					try {
						if (lastPicture != null) {
							String outBuf = "--b\r\n"
									+ "Content-Type: image/jpeg\r\n"
									+ "Content-length: " + lastPicture.length
									+ "\r\n\r\n";
							// Log.d(TAG, "send: " + outBuf);
							out.write(outBuf.getBytes());

							out.write(lastPicture);
							out.write("\r\n\r\n".getBytes());
							out.flush();
						} else {
							if (D)
								Log.d(TAG, "Nothing to send");
						}

						SystemClock.sleep(40);
					} catch (IOException e) {
						e.printStackTrace();
						camera.setPreviewCallback(null);
						socket.close();
						break;

					}
				}

				if (D)
					Log.d(TAG, "Disconect");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class AcceptThread extends Thread {

		public void run() {
			try {
				Log.d(TAG, "mHttpThread running");
				getIp();
				while (!ss.isClosed()) {

					SendThread send = new SendThread(ss.accept()); // wait new
																	// connection
					send.start();

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
					if (ss != null)
						ss.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Error close sockets");
				}
			}
		}
	}

}
