 package com.example.nxtlive;

 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;

 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Environment;
 import android.provider.SyncStateContract.Constants;
 import android.util.Log;
 import android.widget.Toast;

  public class PhotoHandler implements PictureCallback {

public byte[] lastPicture = null;

  public void onPictureTaken(byte[] data, Camera camera) {

	  lastPicture = data;
  }

 }