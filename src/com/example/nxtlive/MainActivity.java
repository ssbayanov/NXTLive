package com.example.nxtlive;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView textDisplay;

	private httpService mHttpService = null;

	private MJPGStreamer mJPEGStreamer = null;

	public static final int CAMERA_FOUND = 0;

	public static final int CAMERA_NOT_FOUND = 1;

	public SurfaceView surfaceView;

	public static SurfaceHolder surfaceHolder;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Init layout
		setContentView(R.layout.activity_main);

		/*textDisplay = (TextView) this.findViewById(R.id.editText1);
		textDisplay.setText("");*/

		// Init http server

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		

		surfaceView.setDrawingCacheEnabled(true);

		mHttpService = new httpService(this, mHandler);

		mJPEGStreamer = new MJPGStreamer(this, mStreamerHandler, surfaceView,
				getWindow().getDecorView().getRootView());

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
			Log.i("Handler", "MESSAGE: " + msg.arg1);
			// textDisplay.append(msg.arg1);
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
		if (mJPEGStreamer != null)
			mJPEGStreamer.stop();
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
				item.setTitle(R.string.stop_server);
			} else {
				mHttpService.stop();
				item.setTitle(R.string.start_server);
			}
			return true;
		case R.id.action_startStream:

			if (!mJPEGStreamer.isEnabled) {
				mJPEGStreamer.start();
				item.setTitle(R.string.stop_stream);
			} else {
				mJPEGStreamer.stop();
				item.setTitle(R.string.start_stream);
			}
			return true;
		}
		return false;
	}

}
