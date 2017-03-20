package com.example.moziliang.utils.mapview;

import android.graphics.Canvas;
import android.graphics.Matrix;

import com.example.moziliang.utils.Position;

public abstract class BaseMapSymbol {
    protected float mThreshold;
    protected Position mLocation;
    protected Object mTag;
    protected float mRotation;
    protected boolean mVisibility;
    public OnMapSymbolListener mOnMapSymbolListener;

    public abstract void draw(Canvas canvas, Matrix matrix, float scale);

    public abstract boolean isPointInClickRect(float x, float y);

    public void setmOnMapSymbolListener(OnMapSymbolListener mOnMapSymbolListener) {
	this.mOnMapSymbolListener = mOnMapSymbolListener;
    }

    public Position getLocation() {
	return mLocation;
    }

    public void setLocation(Position position) {
	mLocation = position;
    }

    public boolean isVisible() {
	return mVisibility;
    }

    public void setVisiable(boolean visiable) {
	mVisibility = visiable;
    }

    public float getThreshold() {
	return mThreshold;
    }
}
