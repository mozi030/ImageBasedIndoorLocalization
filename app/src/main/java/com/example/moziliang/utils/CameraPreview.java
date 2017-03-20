package com.example.moziliang.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A basic Camera preview class
 */
@SuppressLint("NewApi") public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {
    private String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCamera = camera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        //System.out.println("create surface");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setDisplayOrientation(90);
            Camera.Parameters param = mCamera.getParameters();

            Camera.Size previewSize = Tools.determineBestSize( 
                    param.getSupportedPreviewSizes(), 720);
            param.setPreviewSize(previewSize.width, previewSize.height);
            Camera.Size pictureSize = Tools.determineBestSize(
                    param.getSupportedPictureSizes(), 800);
            param.setPictureSize(pictureSize.width, pictureSize.height);
//            param.setPreviewSize(800, 480);
//            param.setPictureSize(480, 800);
            mCamera.setParameters(param);

            mCamera.setPreviewDisplay(mHolder);

            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// focusOnTouch(event);
        }
        return true;
    }

    protected void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {
            // cancel previous actions
            mCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(),
                    1.5f);

            Camera.Parameters parameters = null;
            try {
                parameters = mCamera.getParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check if parameters are set (handle RuntimeException:
            // getParameters failed (empty parameters))
            if (parameters != null) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                ArrayList<Camera.Area> focusList = new ArrayList<Camera.Area>();
                ArrayList<Camera.Area> meterList = new ArrayList<Camera.Area>();

                focusList.add(new Area(focusRect, 1000));
                meterList.add(new Area(meteringRect, 1000));
                parameters.setFocusAreas(focusList);
                parameters.setMeteringAreas(meterList);

                try {
                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(autoFocusCallBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(200 * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top),
                Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    AutoFocusCallback autoFocusCallBack = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d("AutoFocus", Boolean.toString(success));
            if (!success) {
                Toast.makeText(getContext(), "对焦失败", Toast.LENGTH_SHORT).show();
            }
        }
    };
}