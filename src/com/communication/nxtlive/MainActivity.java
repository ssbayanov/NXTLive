package com.communication.nxtlive;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// constant for debugging
	private static final String TAG = "MainActivity";
	private static final boolean D = true;

	// variables for bluetooth

	private String mConnectedDeviceName = null;

	// Codes of MJPGStreamer handle
	public static final int CAMERA_FOUND = 0;
	public static final int CAMERA_NOT_FOUND = 1;

	// Codes of HttpService handle

	public static final int LOCAL_IP = 1;
	public static final int NOT_CONNECTED = 2;
	public static final int BT_SERVICE_MESSAGE = 3;

	// Codes of BluetoothService handle
	public static final int SET_NAME = 1;

	public static final int BT_NOT_ENABLED = 2;

	// interface variables
	private TextView httpStatus;

	private TextView deviceName;

	private HttpService mHttpService = null;

	private MJPGStreamer mJPEGStreamer = null;

	public SurfaceView surfaceView;

	public static SurfaceHolder surfaceHolder;

	private MenuItem btConnect;

	private PowerManager.WakeLock wl;

	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_DISCONNECT_DEVICE = 2;
	static final int REQUEST_ENABLE_BT = 3;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize interface
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		httpStatus = (TextView) this.findViewById(R.id.http_status);

		deviceName = (TextView) this.findViewById(R.id.device_name);

		btConnect = (MenuItem) this.findViewById(R.id.action_connect);

		// Initialize http server

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);

		surfaceView.setDrawingCacheEnabled(true);

		mHttpService = new HttpService(this, mHttpServiceHandler);

		mJPEGStreamer = new MJPGStreamer(this, mStreamerHandler, surfaceView);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showToast(String text) {

		Toast.makeText(this, text, Toast.LENGTH_LONG).show();

	}

	public void setHttpStatus(String text) {

		httpStatus.setText(text);

	}

	private final Handler mHttpServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("Handler", "MESSAGE: " + msg.what);

			switch (msg.what) {
			case BT_SERVICE_MESSAGE:

				switch (msg.arg1) {
				case SET_NAME:
					deviceName.setText(msg.getData().getString(
							BluetoothService.DEVICE_NAME));
					break;
				case BT_NOT_ENABLED:
					btConnect.setEnabled(false);
					break;
				}
				break;
			case LOCAL_IP:
				setHttpStatus(msg.getData().getString("0"));
				break;

			case NOT_CONNECTED:
				setHttpStatus("Сервер не запущен");
				break;

			}
		}
	};

	private final Handler mStreamerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAMERA_FOUND:
				Toast.makeText(getApplicationContext(), "Камера найдена",
						Toast.LENGTH_LONG).show();
				break;
			case CAMERA_NOT_FOUND:
				Toast.makeText(getApplicationContext(), "Камера не найдена",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private final Handler mBleutoothServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i("Handler", "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					// setStatus(getString(R.string.title_connected_to,
					// mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothService.STATE_CONNECTING:
					// setStatus(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					// setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case BluetoothService.MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case BluetoothService.MESSAGE_READ:
				/*
				 * byte[] readBuf = (byte[]) msg.obj; short i = (short) (0x0000
				 * + (readBuf[1] << 8) + readBuf[0]+32767);
				 */

				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						BluetoothService.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(BluetoothService.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	protected void onPause() {
		if (mJPEGStreamer != null)
			if (mJPEGStreamer.camera != null) {
				mJPEGStreamer.camera.release();
				mJPEGStreamer.camera = null;
			}
		if (wl != null)
			wl.release();
		super.onPause();

	}

	@Override
	protected void onDestroy() {

		/*
		 * if (mJPEGStreamer != null) { mJPEGStreamer.stop(); if
		 * (mJPEGStreamer.camera. != null) mJPEGStreamer.camera.release(); }
		 */
		if (mHttpService != null)
			mHttpService.stop();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mJPEGStreamer != null)
			mJPEGStreamer.onResume();
		if (wl != null)
			wl.acquire();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d("onActivityResult", "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				mHttpService.mBTService.connectDevice(data);
			}
			break;
		case REQUEST_DISCONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a BT session
				// setupProgram();
				startScan();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d("onActivityResult", "BT not enabled");
				this.findViewById(R.id.action_connect).setEnabled(false);
			}
		}
	}

	private void startScan() {

		Intent serverIntent = null;
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_startServer:
			if (!mHttpService.isEnabled) {
				mHttpService.start();
				mJPEGStreamer.start();
				item.setTitle(R.string.stop_server);
				PowerManager pm = (PowerManager) getBaseContext()
						.getSystemService(Context.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "wakeup");
				wl.acquire();
			} else {
				mHttpService.stop();
				mJPEGStreamer.stop();
				item.setTitle(R.string.start_server);
				wl.release();
			}
			return true;
		case R.id.action_settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.action_connect:

			// Check enabled Bluetooth adapter
			if (!mHttpService.mBTService.mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else
				startScan();

			return true;
		}
		return false;
	}

}
