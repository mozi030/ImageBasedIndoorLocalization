package com.example.moziliang.indoorlocalizationrelease;

import java.io.File;
import java.io.FileOutputStream;

import com.example.moziliang.utils.CameraPreview;
import com.example.moziliang.utils.SensorUtils;
import com.example.moziliang.utils.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

public class TakePhotoActivity extends Activity {

	@SuppressLint("SdCardPath")
	private String photo_path = "/sdcard/data/manyImages/my_photo/";

	private FrameLayout mFrameLayout;
	private Button mCaptureButton;
	private PictureCallback mPicture;
	private Camera mCamera;
	private CameraPreview mCameraPreview;
	private Camera.AutoFocusCallback autoFocusCallBack;
	private Context mContext;
	private SensorUtils mSensorUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);

		mSensorUtils = SensorUtils.getInstance();
		// mSensorUtils.reset();

		setContentView(R.layout.activity_take_photo);

		mCaptureButton = (Button) findViewById(R.id.take_photo_button);
		mCaptureButton.getBackground().setAlpha(50);

		mContext = this;
		mCamera = Tools.getCameraInstance();
		// System.out.println("In TakePhotoActivity   " + (mCamera == null));
		mCameraPreview = new CameraPreview(mContext, mCamera);

		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mFrameLayout.addView(mCameraPreview);

		mCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.autoFocus(autoFocusCallBack);
			}
		});

		autoFocusCallBack = new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {

				if (!success) {
					mCaptureButton.setEnabled(true);
				} else {
					mCamera.takePicture(null, null, mPicture);
				}
			}
		};

		mPicture = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				try {
					String temp = "";
					Bitmap photo = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					Matrix matrix = new Matrix();
					matrix.postScale(1f, 1f);
					matrix.postRotate(90);
					photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), 
							photo.getHeight(), matrix, true);
					// ByteArrayOutputStream stream = new
					// ByteArrayOutputStream();
					// photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);//
					// (0-100)ѹ���ļ�
					// �˴����԰�Bitmap���浽sd���У������뿴��http://www.cnblogs.com/linjiqin/archive/2011/12/28/2304940.html
					int current_photo_num = getIntent().getIntExtra(
							"current_photo_num", 0);// �������ĵڼ�����Ƭ
					double gyroAngle = 0;
					double compassAngle = 0;
					temp = photo_path + current_photo_num + ".jpeg";
					gyroAngle = mSensorUtils.getAngle();
					mSensorUtils.reset();
					compassAngle = mSensorUtils.getCompassDirection();
					
					File fImage = new File(temp);
					fImage.createNewFile();
					FileOutputStream iStream = new FileOutputStream(fImage);
					photo.compress(CompressFormat.JPEG, 100, iStream);
					iStream.close();

					// mCamera.startPreview();
					// mCaptureButton.setEnabled(true);
					Intent intent = new Intent();
					intent.putExtra("gyroAngle", gyroAngle);
					intent.putExtra("compassAngle", compassAngle);
					setResult(RESULT_OK, intent);
					TakePhotoActivity.this.finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public void onPause() {
		//mSensorUtils.unregisterSensor();
		mFrameLayout.removeView(mCameraPreview);
		mCamera.release();
		mCamera = null;
		super.onPause();
	}

	@Override
	public void onResume() {
		if (mCamera == null) {
			mCamera = Tools.getCameraInstance();
			mCameraPreview = new CameraPreview(mContext, mCamera);
			mFrameLayout.addView(mCameraPreview);

			//mSensorUtils.registerSensor();
		}

		super.onResume();
	}
}
