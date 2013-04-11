package com.example.nxtlive;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static String DEBUG_TAG = "MakePhotoActivity";

	private Camera camera;

	private int cameraId = 0;

	private TextView textDisplay;

	private httpService mHttpService = null;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Init layout
		setContentView(R.layout.activity_main);

		textDisplay = (TextView) this.findViewById(R.id.editText1);
		textDisplay.setText("");

		// Init cam

		if (!initCamera()) {
			showToast("No camera found.");
			finish();
		}

		// Init http server

		mHttpService = new httpService(this, mHandler);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean initCamera() {

		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return false;
		} else {
			cameraId = findFrontFacingCamera();
			camera = Camera.open(cameraId);
			if (cameraId < 0) {
				return false;
			} else {
				return true;
			}
		}
	}

	public void showToast(String text) {

		Toast.makeText(this, text, Toast.LENGTH_LONG).show();

	}

	public void onClick(View view) {

		camera.takePicture(null, null,
				new PhotoHandler(getApplicationContext()));

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
				Log.d(DEBUG_TAG, "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("Handler", "MESSAGE: " + msg.arg1);
			// textDisplay.append(msg.arg1);
		}
	};

	@Override
	protected void onPause() {

		if (camera != null) {
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_startServer:
			mHttpService.start();
			return true;
		}
		return false;
	}

}
