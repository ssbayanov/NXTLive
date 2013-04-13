package com.example.nxtlive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MJPGStreamer {
	private static final String TAG = "MJPGStreamer";

	private static final boolean D = true;

	private AcceptThread mMJPGThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8081;

	private Context mainContext = null;

	public boolean isEnabled = false;

	public Camera camera;

	private int cameraId = 0;

	private byte[] lastPicture = null;

	public MJPGStreamer(Context context, Handler handler) {
		mainContext = context;
		mHandler = handler;

		try{
		if (camera != null) {
	        camera.release();
	    }
		camera = Camera.open();}
		catch(Exception e) {
			e.printStackTrace();
			mHandler.obtainMessage(MainActivity.CAMERA_NOT_FOUND);
		}

		/*
		 * if (!initCamera()) {
		 * mHandler.obtainMessage(MainActivity.CAMERA_NOT_FOUND); }
		 * mHandler.obtainMessage(MainActivity.CAMERA_FOUND);
		 */

	}

	public boolean initCamera() {

		if (!mainContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return false;
		} else {
			cameraId = findFrontFacingCamera();
			camera = Camera.open();
			if (cameraId < 0) {
				return false;
			} else {
				return true;
			}
		}
	}

	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();

		for (int i = 0; i < numberOfCameras; i++)

		{

			CameraInfo info = new CameraInfo();

			Camera.getCameraInfo(i, info);

			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				Log.d(TAG, "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	public synchronized void start() {

		// Start the thread to listen on a BluetoothServerSocket
		if (mMJPGThread == null) {
			mMJPGThread = new AcceptThread();
			mMJPGThread.start();
			isEnabled = true;
		}
	}

	public synchronized void stop() {

		// Start the thread to listen on a BluetoothServerSocket
		if (mMJPGThread == null) {
			mMJPGThread.cancel();
			isEnabled = false;
			camera.release();
		}
	}

	private class AcceptThread extends Thread {
		InputStream in = null;
		OutputStream out = null;
		ServerSocket ss = null;
		Socket s = null;

		public AcceptThread() {
			try {
				Log.d(TAG, "Try run mMJPGThread");
				ss = new ServerSocket(TCP_SERVER_PORT);

				if (ss == null) {
					Log.d(TAG, "ss is null. Exit");
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PictureCallback myPictureCallback_JPG = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera cam) {
				lastPicture = data;
			}
		};

		public void run() {
			try {
				Log.d(TAG, "mMJPGThread running");

				while (!ss.isClosed()) {
					if (s != null) {
						s.close();
					}
					s = ss.accept(); // wait new connection

					if (D)
						Log.d(TAG, "Try create mMJPGThread");

					in = s.getInputStream();

					out = s.getOutputStream();
					// Keep listening to the InputStream while connected

					if (D)
						Log.d(TAG, "mMJPGThread streams created");

					int size = in.available(); // get size query

					byte[] buffer = new byte[size];

					in.read(buffer); // read all from buffer

					String query = new String(buffer); // query need for parsing

					// Log.i("TcpServer", "received: " + query);

					if (D)
						Log.d(TAG, "query get. Start sending");

					out.write(("HTTP/1.1 200 OK\r\n"
					       + "Content-Type: multipart/x-mixed-replace;boundary=b\r\n"
					       + "Cache-Control: no-store\r\n"
					       + "Pragma: no-cache\r\n"
					       + "Connection: close\r\n"
					       + "\r\n").getBytes());

					// PhotoHandler jpeger = new PhotoHandler();
					camera.setDisplayOrientation(90);

					camera.setPreviewDisplay(MainActivity.surfaceHolder);

					List<Size> supportedPreviewSizes = camera.getParameters()
							.getSupportedPreviewSizes();
					Parameters parameters = camera.getParameters();
					parameters.setPreviewSize(
							supportedPreviewSizes.get(1).width,
							supportedPreviewSizes.get(1).height);
					camera.setParameters(parameters);

					camera.startPreview();

					while (!s.isClosed()) {
						camera.takePicture(null, null, myPictureCallback_JPG);
						SystemClock.sleep(70);
						if (lastPicture != null) {
								
							out.write(("--b\r\n"
						            + "Content-Type: image/jpeg\r\n"
						            + "Content-length: " + lastPicture.length + "\r\n\r\n")
									.getBytes());
							out.write(lastPicture);
							out.write("\r\n\r\n".getBytes());
							out.flush();
						} else {
							
							if (D)
								Log.d(TAG, "Nothing to send");
						}
					}

					if (D)
						Log.d(TAG, "Disconect");

				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				mHandler.obtainMessage(MainActivity.CAMERA_NOT_FOUND);
			}
		}

		public void cancel() {
			if (ss != null) {
				if (D)
					Log.d(TAG, "Try stop mHttpThread");
				try {
					s.close();
					ss.close();
					camera.release();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Error close sockets");
				} 
			}
		};
	};

}
