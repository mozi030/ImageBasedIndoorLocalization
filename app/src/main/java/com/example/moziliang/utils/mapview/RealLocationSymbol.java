package com.example.moziliang.utils.mapview;

import com.example.moziliang.indoorlocalizationrelease.R;
import com.example.moziliang.utils.Tools;
import com.example.moziliang.utils.Position;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class RealLocationSymbol extends BaseMapSymbol {

	private Bitmap mBitmap;
	private boolean isMoving;

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	private Rect mClickRect = new Rect(0, 0, 0, 0);
	private OnMapSymbolListener mRealClickListener = new OnMapSymbolListener() {

		@Override
		public boolean onMapSymbolClick(BaseMapSymbol mapSymbol) {
			return true;
		}
	};

	public RealLocationSymbol() {
		mBitmap = BitmapFactory.decodeResource(Tools.getResources(), R.drawable.marker);
		mLocation = new Position();
		mRotation = 0f;
		mVisibility = true;
		mThreshold = 0f;
		setmOnMapSymbolListener(mRealClickListener);
	}

	private void calDisplayRect() {
		if (mBitmap != null) {
			int left = (int) (mLocation.getX() - mBitmap.getWidth() / 2);
			int right = left + mBitmap.getWidth();
			int top = (int) (mLocation.getY() - mBitmap.getHeight());
			int bottom = top + mBitmap.getHeight();
			mClickRect.set(left, top, right, bottom);
		}
	}

	@Override
	public void draw(Canvas canvas, Matrix matrix, float scale) {
		if (!mVisibility || scale < mThreshold)
			return;
		float[] xy = { (float) mLocation.getX(), (float) mLocation.getY() };
		matrix.mapPoints(xy);

		Paint paint = new Paint();
		paint.setAlpha(isMoving ? 128 : 255);
		canvas.drawBitmap(mBitmap, xy[0] - mBitmap.getWidth() / 2, xy[1] - mBitmap.getHeight(), paint);

		calDisplayRect();
	}

	@Override
	public boolean isPointInClickRect(float x, float y) {
		// System.out.println("isClick?" + isPointInRect(x, y, mClickRect));
		return isPointInRect(x, y, mClickRect);
	}

	private boolean isPointInRect(float x, float y, Rect rect) {
		if (rect == null)
			return false;
		if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
			return true;
		}
		return false;
	}
}
