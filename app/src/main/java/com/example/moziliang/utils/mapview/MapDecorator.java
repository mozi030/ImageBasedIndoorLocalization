package com.example.moziliang.utils.mapview;

import com.example.moziliang.utils.Position;

import java.io.FileInputStream;
import java.util.List;

public class MapDecorator {

    private MapView mMapView;

    public MapDecorator() {
    }

    public void setMapView(MapView mapView) {
        mMapView = mapView;
    }

    public float[] transformToViewCoordinate(float[] mapCoordinate) {
        return mMapView.transformToViewCoordinate(mapCoordinate);
    }

    public float[] transformToMapCoordinate(float[] viewCoordinate) {
        return mMapView.transformToMapCoordinate(viewCoordinate);
    }

    public void centerSpecificLocation(Position location) {
        mMapView.centerSpecificLocation(location);
    }

    public void centerSpecificSymbol(BaseMapSymbol mapSymbol) {
        mMapView.centerSpecificLocation(mapSymbol.getLocation());
    }

    public long getCurrentMapFloorId() {
        return mMapView.getFloorId();
    }

    public void updateMyLocation(Position location) {
        mMapView.updateMyLocation(location);
    }

    public boolean removeMapSymbol(BaseMapSymbol mapSymbol) {
        return mMapView.getMapSymbols().remove(mapSymbol);
    }

    public void clearMapSymbols() {
        mMapView.getMapSymbols().clear();
    }

    public void reDrawMap() {
        mMapView.invalidate();
    }

    public List<BaseMapSymbol> getMapSymbols() {
        return mMapView.getMapSymbols();
    }

    public boolean trackPosition() {
        if (mMapView.getmMyLocationSymbol().getLocation() != null) {
            mMapView.centerMyLocation();
        }
        mMapView.setTrackPosition();
        return mMapView.getmMyLocationSymbol().getLocation() != null;
    }

    public void initNewMap(FileInputStream inputStream, Position currentPosition) {
        mMapView.initNewMap(inputStream, 1, 0, currentPosition);
    }

    public Position getRealLocation() {
        return mMapView.getRealLocation();
    }

    public void setOnRealLocationMoveListener(OnRealLocationMoveListener mOnRealLocationMoveListener) {
        mMapView.setOnRealLocationMoveListener(mOnRealLocationMoveListener);
    }
}
