package com.example.nxtlive;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	  private final static String DEBUG_TAG = "MakePhotoActivity";

	  private Camera camera;

	  private int cameraId = 0;
	
    @TargetApi(Build.VERSION_CODES.GINGERBREAD) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
              Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
            }
    else {
              cameraId = findFrontFacingCamera();
              camera = Camera.open(cameraId);
              if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                    Toast.LENGTH_LONG).show();
              }
              else{
            	  Toast.makeText(this, "Yes. You have camera.",
                          Toast.LENGTH_LONG).show();
            	  }
            }
     
     Button but = (Button) findViewById(R.id.button1);
     but.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {

        	 camera.takePicture(null, null,new PhotoHandler(getApplicationContext()));

         }
     });
		//Setup listener on touch to button

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void onClick(View view) {

        camera.takePicture(null, null,new PhotoHandler(getApplicationContext()));

      }

    private int findFrontFacingCamera()
     {
    int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++)

    {

    CameraInfo info = new CameraInfo();

     Camera.getCameraInfo(i, info);

     if (info.facing == CameraInfo.CAMERA_FACING_BACK)
    {
            Log.d(DEBUG_TAG, "Camera found");
           cameraId = i;
         break;
         }
       }
      return cameraId;
      }

    @Override

     protected void onPause() 
    {

     if (camera != null) 
    {
        camera.release();
        camera = null;
     }
    super.onPause();
    }
    
}
