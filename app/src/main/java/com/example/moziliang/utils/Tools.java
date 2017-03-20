package com.example.moziliang.utils;

import android.R.integer;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.moziliang.BaseApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Tools {

	public static Context getContext() {
		return BaseApplication.getContext();
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public static void showKeyBoard(View view) {
		InputMethodManager imm = (InputMethodManager) BaseApplication
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
				InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	public static void hideKeyBoard(View view) {
		InputMethodManager imm = (InputMethodManager) BaseApplication
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static String getString(int resId) {
		return BaseApplication.getContext().getString(resId);
	}

	public static int dip2px(float dip) {
		final float scale = BaseApplication.getContext().getResources()
				.getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	public static float px2dip(float px) {
		final float scale = BaseApplication.getContext().getResources()
				.getDisplayMetrics().density;
		return px / scale + 0.5f;
	}

	private static int screenW = -1, screenH = -1;

	public static int getScreenW() {
		if (screenW < 0) {
			initScreenDisplayParams();
		}
		return screenW;
	}

	public static int getScreenH() {
		if (screenH < 0) {
			initScreenDisplayParams();
		}
		return screenH;
	}

	private static void initScreenDisplayParams() {
		DisplayMetrics dm = BaseApplication.getContext().getResources()
				.getDisplayMetrics();
		screenW = dm.widthPixels;
		screenH = dm.heightPixels;
	}

	public static Resources getResources() {
		return BaseApplication.getContext().getResources();
	}

	public static String getMacAddress() {
		WifiManager manager = (WifiManager) BaseApplication.getContext()
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * Check if this device has a camera
	 */
	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	public static Camera.Size determineBestSize(List<Camera.Size> sizes,
			int widthThreshold) {
		int screenWidth = 720;
		int screenHeight = 960;
		Camera.Size bestSize = null;
		for (Camera.Size currentSize : sizes) {
			// boolean isDesiredRatio = Math.abs((double) currentSize.width
			// / currentSize.height)
			// - ((double) getScreenH() / getScreenW()) < 0.1;
			boolean isDesiredRatio = Math.abs(currentSize.width * screenWidth
					- currentSize.height * screenHeight) < 1;
			boolean isBetterSize = (bestSize == null || currentSize.width >= bestSize.width);
			boolean isInBounds = currentSize.width <= widthThreshold;

			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}
		if (bestSize == null) {
			return sizes.get(0);
		}

		return bestSize;
	}

	public static String getAppPath() {
		String path = Environment.getExternalStorageDirectory() + "/Sextant/";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	public static String getStringFromTimeStamp(long ts) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
				Locale.CHINA);
		return format.format(new Date(ts));
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}
}
