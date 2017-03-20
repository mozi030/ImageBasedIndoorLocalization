package com.example.moziliang.indoorlocalizationrelease;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.R;
import com.example.moziliang.utils.Position;
import com.example.moziliang.utils.ConstantValue;
import com.example.moziliang.utils.PreferenceUtils;
import com.example.moziliang.utils.Tools;
//import com.example.views.DialogFactory;
import com.example.moziliang.utils.TriangleCalc;
import com.example.moziliang.utils.mapview.MapDecorator;
import com.example.moziliang.utils.mapview.MapView;
import com.example.moziliang.utils.mapview.OnRealLocationMoveListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by limkuan on 15/6/13.
 */
public class MapActivity extends Activity {

	@SuppressLint("SdCardPath") private String map_path = "/sdcard/data/manyImages/data_v2/new_map.jpg";

    private Position mPosition;
    private MapDecorator mDecorator;

    private LinearLayout mapActivityLayout = null;
    private TextView currentLocationTextView = null;
    private HorizontalScrollView differenceScrollView = null;

    private int differenceCellHeight = 80;
    private int differenceCellWidth = 250;
    private int differenceCellTextHeight = 80;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private String differentCellTitles[] = {"陀螺仪测量值", "陀螺仪真实值", "陀螺仪误差", "罗盘测量值", "罗盘真实值", "罗盘误差"};

    private int ChooseFirstNNum = 0;
    private double gyro[] = null;
    private double orientationFromNorths[] = null;
    private double compass[] = null;
    private int currentRealStores[] = null;
    private Position currentPosition = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        ChooseFirstNNum = intent.getIntExtra("ChooseFirstNNum", 0);
        gyro = intent.getDoubleArrayExtra("gyro");
        compass = intent.getDoubleArrayExtra("compass");
        currentRealStores = intent.getIntArrayExtra("currentRealStores");
        orientationFromNorths = new double[ChooseFirstNNum];
        double currentPositionX = intent.getDoubleExtra("currentPositionX", -1);
        double currentPositionY = intent.getDoubleExtra("currentPositionY", -1);
        if (currentPositionX != -1 && currentPositionY != -1) {
            currentPosition = new Position(currentPositionX, currentPositionY);
        } else {
            currentPosition = null;
        }

        mDecorator = new MapDecorator();
        //MapView mapView = (MapView) findViewById(R.id.map_view);
        MapView mapView = new MapView(this);
        mDecorator.setMapView(mapView);
    	
//        mPosition = new Position();
//        mPosition.setX(intent.getDoubleExtra("LocationX", 100));
//        mPosition.setY(intent.getDoubleExtra("LocationY", 100));
//        Log.e("1111", "position:" + mPosition.getX() + ";" + mPosition.getY());
        try {
            String filename = map_path;
            File file = new File(filename);
            FileInputStream inStream = new FileInputStream(file);
            mDecorator.initNewMap(inStream, currentPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;//宽度
        screenHeight = dm.heightPixels;

        mapActivityLayout = (LinearLayout) findViewById(R.id.map_activity_layout);
        LinearLayout mapViewLinearLayout = new LinearLayout(this);
        mapViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight - differenceCellHeight * 3 * 2 - differenceCellTextHeight));
        mapViewLinearLayout.addView(mapView);
        mapActivityLayout.addView(mapViewLinearLayout, 0);

        currentLocationTextView = (TextView) findViewById(R.id.current_location);
        currentLocationTextView.setHeight(differenceCellTextHeight);
        differenceScrollView = (HorizontalScrollView) findViewById(R.id.difference_scrollview);


        LinearLayout differenceHorizontalLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams differenceHorizontalLinearLayoutParams = new LinearLayout.LayoutParams(differenceCellWidth * 8, differenceCellHeight * 3 * 2);
        //differenceHorizontalLinearLayoutParams.setMargins(0, 0, 0, 3 * differenceCellHeight * (1 - scrollViewNum));
        differenceHorizontalLinearLayout.setLayoutParams(differenceHorizontalLinearLayoutParams);
        differenceHorizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        differenceScrollView.addView(differenceHorizontalLinearLayout);

        for (int scrollViewHorizontalNum = 0; scrollViewHorizontalNum < ChooseFirstNNum + 1; scrollViewHorizontalNum++) {
            LinearLayout differenceVerticalLinearLayout = new LinearLayout(this);
            differenceVerticalLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(differenceCellWidth, differenceCellHeight * 3 * 2));
            differenceVerticalLinearLayout.setOrientation(LinearLayout.VERTICAL);
            differenceHorizontalLinearLayout.addView(differenceVerticalLinearLayout);

            for (int scrollViewVerticalNum = 0; scrollViewVerticalNum < 3 * 2; scrollViewVerticalNum++) {
                TextView differenceCellTextView = new TextView(this);
                differenceCellTextView.setHeight(differenceCellHeight);
                differenceVerticalLinearLayout.addView(differenceCellTextView);
                if (scrollViewHorizontalNum == 0) {
                    differenceCellTextView.setText(differentCellTitles[scrollViewVerticalNum]);
                } else if (scrollViewVerticalNum == 0) {
                    differenceCellTextView.setText(String.format("%.2f", gyro[scrollViewHorizontalNum - 1]));
                }  else if (scrollViewVerticalNum == 3) {
                    differenceCellTextView.setText(String.format("%.2f", compass[scrollViewHorizontalNum - 1]));
                }
                differenceCellTextView.setId(scrollViewHorizontalNum * 10 + scrollViewVerticalNum * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
            }
        }


        mDecorator.setOnRealLocationMoveListener(new OnRealLocationMoveListener() {
            @Override
            public void onMove(Position position) {
                currentPosition = position;
                currentLocationTextView.setText("current location: (" + String.format("%.2f",position.getX()) + ", " + String.format("%.2f", position.getY()) + ")");

                for (int scrollViewHorizontalNum = 0; scrollViewHorizontalNum < ChooseFirstNNum + 1; scrollViewHorizontalNum++) {
                    if (scrollViewHorizontalNum == 0) {
                        continue;
                    }

                    double orientationFromNorth = TriangleCalc.calOrientFromNorth(position, currentRealStores[scrollViewHorizontalNum - 1]);
                    //Log.e("OnRealLocationMoveCheck", "location: " + position.getX() + ", " + position.getY() + "\norientationFromNorth: " + orientationFromNorth);

                    //TextView gyroTestTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 0 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
                    TextView gyroActualTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 1 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
                    TextView gyroDifferenceTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 2 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
                    //TextView compassTestTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 3 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
                    TextView compassActualTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 4 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);
                    TextView compassDifferenceTextView = (TextView) findViewById(scrollViewHorizontalNum * 10 + 5 * 1 + ConstantValue.SCROLLVIEW_DIFFERENCE_ID_OFFSET);

                    orientationFromNorths[scrollViewHorizontalNum - 1] = orientationFromNorth;
                    if (scrollViewHorizontalNum == 1) {
                        gyroActualTextView.setText(String.format("%.2f", gyro[0]));
                        gyroDifferenceTextView.setText("0");
                    } else {
                        double difference = orientationFromNorth - orientationFromNorths[scrollViewHorizontalNum - 2];
                        if (difference > 180) {
                            difference -= 360;
                        } else if (difference < -180) {
                            difference += 360;
                        }
                        gyroActualTextView.setText(String.format("%.2f", difference));

                        gyroDifferenceTextView.setText(String.format("%.2f", Math.abs(difference - gyro[scrollViewHorizontalNum - 1])));
                    }
                    compassActualTextView.setText(String.format("%.2f", orientationFromNorth));
                    orientationFromNorth += 5.87;
                    if (orientationFromNorth > 360) {
                        orientationFromNorth -= 360;
                    }
                    compassDifferenceTextView.setText(String.format("%.2f",
                            Math.abs(orientationFromNorth - compass[scrollViewHorizontalNum - 1])));
                }
            }
        });

        //mDecorator.updateMyLocation(mPosition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent intent = new Intent();
            intent.putExtra("currentPositionX", currentPosition.getX());
            intent.putExtra("currentPositionY", currentPosition.getY());
            setResult(RESULT_OK, intent);
            MapActivity.this.finish();
        }

        return false;

    }

}
