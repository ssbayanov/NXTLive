package com.example.nxtlive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

public class MJPGStreamer {
	private static final String TAG = "MJPGStreamer";

	private static final boolean D = true;

	private AcceptThread mMJPGThread;

	private final Handler mHandler;

	private static final int TCP_SERVER_PORT = 8081;

	public boolean isEnabled = false;

	public Camera camera;

	private byte[] lastPicture = null;

	private boolean inPreview = false;

	private boolean cameraConfigured = false;

	public SurfaceView surfaceView;

	public SurfaceHolder surfaceHolder;

	InputStream in = null;
	OutputStream out = null;
	ServerSocket ss = null;
	Socket s = null;

	public MJPGStreamer(Context context, Handler handler, SurfaceView sV) {
		mHandler = handler;

		surfaceView = sV;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		// surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.lockCanvas();

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

		startPreview();

		mHandler.obtainMessage(MainActivity.CAMERA_FOUND).sendToTarget();

	}

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

	/*
	 * private Camera.Size getSmallestPictureSize(Camera.Parameters parameters)
	 * { Camera.Size result = null;
	 * 
	 * for (Camera.Size size : parameters.getSupportedPictureSizes()) { if
	 * (result == null) { result = size; } else { int resultArea = result.width
	 * * result.height; int newArea = size.width * size.height;
	 * 
	 * if (newArea < resultArea) { result = size; } } }
	 * 
	 * return (result); }
	 */

	private void initPreview(int width, int height) {
		if (camera != null && surfaceHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				/*
				 * Toast.makeText(PictureDemo.this, t.getMessage(),
				 * Toast.LENGTH_LONG).show();
				 */
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);
				// Camera.Size pictureSize = getSmallestPictureSize(parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					parameters.setPreviewFpsRange(5, 15);

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
			initPreview(320, 240);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

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
		if (mMJPGThread != null) {
			mMJPGThread.cancel();
			isEnabled = false;

		}
	}

	public void onResume() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, info);

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camera = Camera.open(i);
				}
			}
		}

		if (camera == null) {
			camera = Camera.open();
		}

		startPreview();
	}

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

	Camera.PictureCallback JPGCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			lastPicture = new byte[data.length];
			lastPicture = data;
			startPreview();
			inPreview = true;
		}
	};

	private class AcceptThread extends Thread {

		public void run() {

			Log.d(TAG, "mMJPGThread running");

			while (this.isAlive()) {
				try {
					if (s != null) {
						s.close();
					}

					s = ss.accept(); // wait new connection

					Log.d(TAG, "LocalIp " + s.getLocalAddress().toString());

					in = s.getInputStream();

					out = s.getOutputStream();

					int size = in.available(); // get size query
					byte[] buffer = new byte[size];
					in.read(buffer); // read all from buffer
					String query = new String(buffer); // query need for parsing

					if (D)
						Log.d(TAG, "query get. Start sending");

					// PhotoHandler jpeger = new PhotoHandler();

					out.write(("HTTP/1.1 200 OK\r\n"
							+ "Content-Type: multipart/x-mixed-replace;boundary=b\r\n"
							+ "Cache-Control: no-store\r\n"
							+ "Pragma: no-cache\r\n" + "Connection: close\r\n"
							+ "\r\n").getBytes());

					camera.setPreviewCallback(JPGPrewCallback);

					while (!s.isOutputShutdown()) {

						try {
							if (lastPicture != null) {
								String outBuf = "--b\r\n"
										+ "Content-Type: image/jpeg\r\n"
										+ "Content-length: "
										+ lastPicture.length + "\r\n\r\n";
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
							break;

						}
					}

					if (D)
						Log.d(TAG, "Disconect");

				} catch (IOException e) {
					cancel();
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					cancel();
					mHandler.obtainMessage(MainActivity.CAMERA_NOT_FOUND);
				}

			}

		}

		public void cancel() {
			if (ss != null) {
				if (D)
					Log.d(TAG, "Try stop mMJPGThread");
				try {
					if (camera != null)
					s.close();
					ss.close();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error close sockets");
				}
			}
		};
	};

}
