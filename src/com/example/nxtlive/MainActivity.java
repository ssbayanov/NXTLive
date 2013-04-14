package com.example.nxtlive;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	//
	private TextView textDisplay;

	private httpService mHttpService = null;

	private MJPGStreamer mJPEGStreamer = null;

	public static final int CAMERA_FOUND = 0;
	public static final int CAMERA_NOT_FOUND = 1;
	public static final int LOCAL_IP = 2;
	public static final int NOT_CONNECTED = 3;

	private static final int REQUEST_CONNECT_DEVICE_SECURE = 0;

	public SurfaceView surfaceView;

	public static SurfaceHolder surfaceHolder;

	private BluetoothAdapter mBluetoothAdapter = null;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Init layout

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		textDisplay = (TextView) this.findViewById(R.id.textView1);

		// Init http server

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);

		surfaceView.setDrawingCacheEnabled(true);

		mHttpService = new httpService(this, mHandler);

		mJPEGStreamer = new MJPGStreamer(this, mStreamerHandler, surfaceView);

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

	public void showIp(String text) {

		textDisplay.setText(text);

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("Handler", "MESSAGE: " + msg.what);

			switch (msg.what) {
			case LOCAL_IP:
				textDisplay.setText(msg.getData().getString("0"));
				break;

			case NOT_CONNECTED:
				textDisplay.setText("Сервер не запущен");
				break;
			// textDisplay.append(msg.arg1);
			}
		}
	};

	private final Handler mStreamerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAMERA_FOUND:
				// textDisplay.setText("Камера найдена");
				Toast.makeText(getApplicationContext(), "Камера найдена",
						Toast.LENGTH_LONG).show();
				break;
			case CAMERA_NOT_FOUND:
				// textDisplay.setText("Камера не найдена");
				Toast.makeText(getApplicationContext(), "Камера не найдена",
						Toast.LENGTH_LONG).show();
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
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mJPEGStreamer != null) {
			mJPEGStreamer.stop();
			if (mJPEGStreamer.camera != null)
				mJPEGStreamer.camera.release();
		}
		if (mHttpService != null)
			mHttpService.stop();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mJPEGStreamer != null)
			mJPEGStreamer.onResume();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_startServer:
			if (!mHttpService.isEnabled) {
				mHttpService.start();
				mJPEGStreamer.start();
				item.setTitle(R.string.stop_server);
			} else {
				mHttpService.stop();
				mJPEGStreamer.stop();
				item.setTitle(R.string.start_server);
			}
			return true;
		case R.id.action_settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.action_connect:

			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			// If the adapter is null, then Bluetooth is not supported
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, R.string.bt_not_supported,
						Toast.LENGTH_LONG).show();
				finish();
				return false;
			}

			// Check enabled Bluetooth adapter
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent,
						httpService.REQUEST_ENABLE_BT);
			}

			Intent serverIntent = null;
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		}
		return false;
	}

}
