package com.example.moziliang.indoorlocalizationrelease;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;

import android.R.anim;
import android.R.color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.core.CvType;
//import org.opencv.core.Scalar;
//import org.opencv.core.MatOfKeyPoint;
//import org.opencv.highgui.Highgui;
//import org.opencv.features2d.*;

import com.example.moziliang.utils.Position;
import com.example.moziliang.utils.SensorUtils;
import com.example.moziliang.utils.TriangleCalc;
import com.example.moziliang.utils.ConstantValue;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
import org.w3c.dom.Text;

public class MainActivity extends Activity {
    private Button match_button;
    private Button start_calibration_button;
    private Button reset_button;
    private EditText choose_first_n_edittext;
    private Button record_button;
    private EditText test_folder_input;
    private Button clean_button;
    private ScrollView parentScrollView;
    private Button location_button;

    private boolean hasBuiltTree = false;

    private boolean taking_photo = false;
    private boolean selecting_photo = false;
    private boolean localizationing = false;
    private int current_photo_num = 0;
    private boolean localization_set = false;
    private Position currentPosition = null;
    private int currentRealStores[] = null;

    private ArrayList<String>AdapterArrayList = null;

    @SuppressLint("SdCardPath")
    private String photo_path = "/sdcard/data/manyImages/my_photo/";

    private double gyro[] = null;
    private double compass[] = null;

    private float match_result[];

    private SensorUtils mSensorUtils;

    private int ScreenWidth = 0;
    private int ScreenHeight = 0;
    private int horizontalLinearLayoutHeight = 0;
    private int pictureLength = 0;
    private int pluginHeight = 0;
    private int spinnerHeight = 0;

    private int photo_num = 7;
    private int ChooseFirstNNum = 0;

    private boolean[] photo_set = null;
    private int MatchResult[][] = null;

    static {
        System.loadLibrary("nonfree");
        System.loadLibrary("opencv_java");
        System.loadLibrary("nonfree_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorUtils = SensorUtils.getInstance();
        mSensorUtils.registerSensor();
        mSensorUtils.reset();

        photo_set = new boolean[photo_num];
        MatchResult = new int[photo_num][];
        gyro = new double[photo_num];
        compass = new double[photo_num];
        currentRealStores = new int[photo_num];
        for (int i = 0; i < photo_num; i++) {
            photo_set[i] = false;
            MatchResult[i] = new int[3];
            gyro[i] = 0;
            compass[i] = 0;
            currentRealStores[i] = 0;
        }
        AdapterArrayList = TriangleCalc.getAllNumAndName();

        if (!hasBuiltTree) {
            NonfreeJNILib.buildtree();
            hasBuiltTree = true;
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenWidth = dm.widthPixels;//宽度
        ScreenHeight = dm.heightPixels;//高度
        horizontalLinearLayoutHeight = ScreenHeight * 4 / 5;
        pictureLength = horizontalLinearLayoutHeight / 4;
        spinnerHeight = horizontalLinearLayoutHeight / 4;
        pluginHeight = (horizontalLinearLayoutHeight - pictureLength - spinnerHeight) / 8;

        parentScrollView = (ScrollView) findViewById(R.id.parent_ScrollView);
//        parentScrollView.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                // Disallow the touch request for parent scroll on touch of child view
//                for (int i = 0; i < photo_num; i++){
//                    ScrollView currentSpinner = (ScrollView) findViewById(100 + i);
//                    currentSpinner.getParent().requestDisallowInterceptTouchEvent(false);
//                }
//                //parentScrollView.requestDisallowInterceptTouchEvent(false);
//                Log.e("javaCheck", "touch parentView");
//                return false;
//            }
//        });

        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.ScrollView);
        scrollView.setFillViewport(true);

        LinearLayout horizontalLinearLayout = new LinearLayout(this);
        horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        //horizontalLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        scrollView.addView(horizontalLinearLayout);

        for (int i = 0; i < photo_num; i++) {
            LinearLayout currentVerticalLinearLayout = new LinearLayout(this);
            currentVerticalLinearLayout.setOrientation(LinearLayout.VERTICAL);
            //currentVerticalLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, horizontalLinearLayoutHeight));
            horizontalLinearLayout.addView(currentVerticalLinearLayout);

            ImageView currentImageView = new ImageView(this);
            currentImageView.setImageResource(R.drawable.blank);
            currentImageView.setId(10 + i);
            LinearLayout currentImageViewLinearLayout = new LinearLayout(this);
            currentImageViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pictureLength));
            currentImageViewLinearLayout.setGravity(Gravity.CENTER);
            currentImageViewLinearLayout.addView(currentImageView);
            currentVerticalLinearLayout.addView(currentImageViewLinearLayout);

            Button currentTakePictureButton = new Button(this);
            currentTakePictureButton.setText("拍照");
            currentTakePictureButton.setTextSize(10);
            currentTakePictureButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    taking_photo = true;
                    selecting_photo = false;
                    localizationing = false;
                    localization_set = false;
                    Button currentButton = (Button) v;
                    current_photo_num = currentButton.getId() - 20 + 1;
                    takePicture();
                }
            });
            currentTakePictureButton.setId(20 + i);
            LinearLayout currentTakePictureButtonLinearLayout = new LinearLayout(this);
            currentTakePictureButtonLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentTakePictureButtonLinearLayout.setGravity(Gravity.CENTER);
            currentTakePictureButtonLinearLayout.addView(currentTakePictureButton);
            currentVerticalLinearLayout.addView(currentTakePictureButtonLinearLayout);

            Button currentSelectPhotoButton = new Button(this);
            currentSelectPhotoButton.setText("选择照片");
            currentSelectPhotoButton.setTextSize(10);
            currentSelectPhotoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    taking_photo = false;
                    selecting_photo = true;
                    localizationing = false;
                    localization_set = false;
                    Button currentButton = (Button) v;
                    current_photo_num = currentButton.getId() - 30 + 1;
                    seletctPicture();
                }
            });
            currentSelectPhotoButton.setId(30 + i);
            LinearLayout currentSelectPhotoButtonLinearLayout = new LinearLayout(this);
            currentSelectPhotoButtonLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentSelectPhotoButtonLinearLayout.setGravity(Gravity.CENTER);
            currentSelectPhotoButtonLinearLayout.addView(currentSelectPhotoButton);
            currentVerticalLinearLayout.addView(currentSelectPhotoButtonLinearLayout);

            TextView currentGyroTextView = new TextView(this);
            currentGyroTextView.setText("陀螺仪");
            currentGyroTextView.setId(40 + i);
            LinearLayout currentGyroTextViewLinearLayout = new LinearLayout(this);
            currentGyroTextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentGyroTextViewLinearLayout.setGravity(Gravity.CENTER);
            currentGyroTextViewLinearLayout.addView(currentGyroTextView);
            currentVerticalLinearLayout.addView(currentGyroTextViewLinearLayout);

            TextView currentCompassTextView = new TextView(this);
            currentCompassTextView.setText("罗盘");
            currentCompassTextView.setId(50 + i);
            LinearLayout currentCompassTextViewLinearLayout = new LinearLayout(this);
            currentCompassTextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentCompassTextViewLinearLayout.setGravity(Gravity.CENTER);
            currentCompassTextViewLinearLayout.addView(currentCompassTextView);
            currentVerticalLinearLayout.addView(currentCompassTextViewLinearLayout);

            TextView currentAnswer1TextView = new TextView(this);
            currentAnswer1TextView.setText("匹配1");
            currentAnswer1TextView.setId(60 + i);
            LinearLayout currentAnswer1TextViewLinearLayout = new LinearLayout(this);
            currentAnswer1TextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentAnswer1TextViewLinearLayout.setGravity(Gravity.CENTER);
            currentAnswer1TextViewLinearLayout.addView(currentAnswer1TextView);
            currentVerticalLinearLayout.addView(currentAnswer1TextViewLinearLayout);

            TextView currentAnswer2TextView = new TextView(this);
            currentAnswer2TextView.setText("匹配2");
            currentAnswer2TextView.setId(70 + i);
            LinearLayout currentAnswer2TextViewLinearLayout = new LinearLayout(this);
            currentAnswer2TextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentAnswer2TextViewLinearLayout.setGravity(Gravity.CENTER);
            currentAnswer2TextViewLinearLayout.addView(currentAnswer2TextView);
            currentVerticalLinearLayout.addView(currentAnswer2TextViewLinearLayout);

            TextView currentAnswer3TextView = new TextView(this);
            currentAnswer3TextView.setText("匹配3");
            currentAnswer3TextView.setId(80 + i);
            LinearLayout currentAnswer3TextViewLinearLayout = new LinearLayout(this);
            currentAnswer3TextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentAnswer3TextViewLinearLayout.setGravity(Gravity.CENTER);
            currentAnswer3TextViewLinearLayout.addView(currentAnswer3TextView);
            currentVerticalLinearLayout.addView(currentAnswer3TextViewLinearLayout);

            AutoCompleteTextView currentAutoCompleteTextView = new AutoCompleteTextView(this);
            currentAutoCompleteTextView.setThreshold(1);
            currentAutoCompleteTextView.setDropDownHeight(4 * pluginHeight);
            currentAutoCompleteTextView.setDropDownWidth(pictureLength);
            currentAutoCompleteTextView.setAdapter(new MyAdapter(this, R.layout.activity_main, R.id.lbl_name, AdapterArrayList));
            //currentAutoCompleteTextView.setOnItemSelectedListener(new MyOnItemSelectedListener(i));
            //currentAutoCompleteTextView.setOnItemClickListener(new );
            currentAutoCompleteTextView.addTextChangedListener(new MyTextWatcher(i) );
            currentAutoCompleteTextView.setHint("实际店铺号");
            currentAutoCompleteTextView.setTextSize(10);
            currentAutoCompleteTextView.clearFocus();
            currentAutoCompleteTextView.setTextColor(Color.BLACK);
            currentAutoCompleteTextView.setId(90 + i);
            LinearLayout currentAutoCompleteTextViewLinearLayout = new LinearLayout(this);
            //currentAutoCompleteTextViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(pictureLength, pluginHeight));
            currentAutoCompleteTextViewLinearLayout.setGravity(Gravity.CENTER);
            currentAutoCompleteTextViewLinearLayout.addView(currentAutoCompleteTextView);
            currentVerticalLinearLayout.addView(currentAutoCompleteTextViewLinearLayout);

        }

        choose_first_n_edittext = (EditText) findViewById(R.id.choose_first_n);
        choose_first_n_edittext.setTextSize(15);

        // ¿ªÊ¼Æ¥Åä°´Å¥
        match_button = (Button) findViewById(R.id.match_button);
        match_button.setOnClickListener(new MyMatchOnClickListener());

        test_folder_input = (EditText) findViewById(R.id.test_folder);
        test_folder_input.clearFocus();
        test_folder_input.setTextSize(15);

        record_button = (Button) findViewById(R.id.record_button);
        record_button.setOnClickListener(new recordOnClickListener());

        clean_button = (Button) findViewById(R.id.clean_button);
        clean_button.setOnClickListener(new cleanOnClickListener());

        location_button = (Button) findViewById(R.id.location_button);
        location_button.setOnClickListener(new locationOnClickListener());

        // Ð£×¼°´Å¥
        start_calibration_button = (Button) findViewById(R.id.start_calibration_button);
        start_calibration_button
                .setOnClickListener(new startCalibrationOnClickListener());

        // reset°´Å¥
        reset_button = (Button) findViewById(R.id.reset_button);
        reset_button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                mSensorUtils.reset();
            }
        });

        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                //start_calibration_button.setText("开始校准" + mSensorUtils.getAngle());
            }
        };
        timer.schedule(tt, 0, 500);

    }

    private Handler mHandler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case ConstantValue.MESSAGE_MATCH_BEGIN:
                    Toast.makeText(getApplicationContext(), "匹配开始",
                            Toast.LENGTH_SHORT).show();
                    break;
                case ConstantValue.MESSAGE_MATCH_LENGTH_ERROR:
                    Toast.makeText(getApplicationContext(), "匹配结果长度出错",
                            Toast.LENGTH_SHORT).show();
                    match_button.setEnabled(true);
                    break;
                case ConstantValue.MESSAGE_MATCH_ALL_FINISH:
                    for (int i = 0; i < ChooseFirstNNum; i++) {
                        MatchResult[i][0] = (int) match_result[i * 3 + 0];
                        MatchResult[i][1] = (int) match_result[i * 3 + 1];
                        MatchResult[i][2] = (int) match_result[i * 3 + 2];

                        TextView currentAnswer1TextView = (TextView) findViewById(60 + i);
                        currentAnswer1TextView.setText(
                                MatchResult[i][0] + TriangleCalc.getStoreNameFromNum(MatchResult[i][0]));
                        TextView currentAnswer2TextView = (TextView) findViewById(70 + i);
                        currentAnswer2TextView.setText(
                                MatchResult[i][1] + TriangleCalc.getStoreNameFromNum(MatchResult[i][1]));
                        TextView currentAnswer3TextView = (TextView) findViewById(80 + i);
                        currentAnswer3TextView.setText(
                                MatchResult[i][2] + TriangleCalc.getStoreNameFromNum(MatchResult[i][2]));
                    }
                    match_button.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "匹配结束",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private class MyMatchOnClickListener implements OnClickListener {
        public void onClick(View arg0) {
            match_button.setEnabled(false);

            taking_photo = false;
            selecting_photo = false;
            localizationing = false;
            localization_set = false;

            String ChooseFirstNString = choose_first_n_edittext.getText().toString();
            if (!ChooseFirstNString.equals("")) {
                try {
                    ChooseFirstNNum = Integer.parseInt(ChooseFirstNString);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "没有输入数字",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ChooseFirstNNum < 1 || ChooseFirstNNum > photo_num) {
                    Toast.makeText(getApplicationContext(), "请输入1-" + photo_num + "的数字",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < ChooseFirstNNum; i++) {
                    if (photo_set[i] == false) {
                        Toast.makeText(getApplicationContext(), "第" + (i + 1) + "张照片没拍好",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "请输入要取前几张照片进行匹配",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!hasBuiltTree) {
                NonfreeJNILib.buildtree();
                hasBuiltTree = true;
            }
            
            Thread matchThread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    sendMyMessage(ConstantValue.MESSAGE_MATCH_BEGIN);
                    match_result = NonfreeJNILib.runDemo(ChooseFirstNNum);

                    if (ChooseFirstNNum != match_result.length / 3) {
                        sendMyMessage(ConstantValue.MESSAGE_MATCH_LENGTH_ERROR);
                    } else {
                        sendMyMessage(ConstantValue.MESSAGE_MATCH_ALL_FINISH);
                    }
                }
            });
            matchThread.start();
        }
    };

    private class startCalibrationOnClickListener implements OnClickListener {
        public void onClick(View arg0) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CalibrationActivity.class);
            startActivity(intent);
            mSensorUtils.reset();
        }
    };

    private class recordOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String folderNameString = test_folder_input.getText().toString();
            int folderName = 0;
            if (!folderNameString.equals("")) {
                try {
                    folderName = Integer.parseInt(folderNameString);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "记录文件夹号没有输入数字",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(getApplicationContext(), "记录文件夹号没有填 ",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String outputMessage = "";
            for (int i = 0; i < ChooseFirstNNum; i++) {
                if (currentRealStores[i] == 0) {
                    Toast.makeText(getApplicationContext(), "第" + (i + 1) + "个真实店铺没输入完毕",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                outputMessage += MatchResult[i][0] + " " + MatchResult[i][1] + " " + MatchResult[i][2] + " ";
                outputMessage += currentRealStores[i] + " ";
                outputMessage += gyro[i] + " ";
                outputMessage += compass[i] + "\n";
            }

            if (localization_set == false) {
                Toast.makeText(getApplicationContext(), "还没有标注拍摄位置",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            outputMessage += currentPosition.getX() + ", " + currentPosition.getY() + "\n";

            String fileName = "/sdcard/data/manyImages/data_v2/test_cast_output/";
            FileOutputStream fout = null;
            File destDir = new File(fileName + folderName);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            try {
                fout = new FileOutputStream(fileName + folderName + "/info.txt");
                byte[] bytes = outputMessage.getBytes();
                fout.write(bytes);
                fout.close();

                Bitmap photo = null;
                File fImage = null;
                FileOutputStream iStream = null;
                for (int i = 0; i < ChooseFirstNNum; i++) {
                    String tempPath = photo_path + (i + 1) + ".jpeg";
                    photo = BitmapFactory.decodeFile(tempPath);
                    fImage = new File(fileName + folderName + "/" + (i + 1) + ".jpeg");
                    fImage.createNewFile();
                    iStream = new FileOutputStream(fImage);
                    photo.compress(CompressFormat.JPEG, 100, iStream);
                    iStream.close();
                }
                Toast.makeText(getApplicationContext(), "记录成功",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private class cleanOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < photo_num; i++) {
                ImageView currentImageView = (ImageView)findViewById(10 + i);
                currentImageView.setImageResource(R.drawable.blank);
                TextView currentGyroTextView = (TextView) findViewById(40 + i);
                currentGyroTextView.setText("陀螺仪");
                TextView currentCompassTextView = (TextView) findViewById(50 + i);
                currentCompassTextView.setText("罗盘");
                TextView currentAnswer1TextView = (TextView) findViewById(60 + i);
                currentAnswer1TextView.setText("匹配1");
                TextView currentAnswer2TextView = (TextView) findViewById(70 + i);
                currentAnswer2TextView.setText("匹配2");
                TextView currentAnswer3TextView = (TextView) findViewById(80 + i);
                currentAnswer3TextView.setText("匹配3");
                AutoCompleteTextView currentAutoCompleteTextView = (AutoCompleteTextView) findViewById(90 + i);
                currentAutoCompleteTextView.setText("");
                photo_set[i] = false;
                MatchResult[i] = new int[3];
                gyro[i] = 0;
                compass[i] = 0;
                currentRealStores[i] = 0;
            }
            choose_first_n_edittext.setText("");
            test_folder_input.setText("");
            taking_photo = false;
            selecting_photo = false;
            localization_set = false;
            localizationing = false;
            mSensorUtils.reset();
        }
    };

    private class locationOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String ChooseFirstNString = choose_first_n_edittext.getText().toString();
            if (!ChooseFirstNString.equals("")) {
                try {
                    ChooseFirstNNum = Integer.parseInt(ChooseFirstNString);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "没有输入数字",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ChooseFirstNNum < 1 || ChooseFirstNNum > photo_num) {
                    Toast.makeText(getApplicationContext(), "请输入1-" + photo_num + "的数字",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < ChooseFirstNNum; i++) {
                    if (photo_set[i] == false) {
                        Toast.makeText(getApplicationContext(), "第" + (i + 1) + "张照片没拍好",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "请输入要取前几张照片进行实验",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < ChooseFirstNNum; i++) {
                if (currentRealStores[i] == 0) {
                    Toast.makeText(getApplicationContext(), "第" + (i + 1) + "个真实店铺没输入完毕",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            taking_photo = false;
            selecting_photo = false;
            localizationing = true;

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MapActivity.class);
            intent.putExtra("ChooseFirstNNum", ChooseFirstNNum);
            intent.putExtra("gyro", gyro);
            intent.putExtra("compass", compass);
            intent.putExtra("currentRealStores", currentRealStores);
            if (currentPosition != null) {
                intent.putExtra("currentPositionX", currentPosition.getX());
                intent.putExtra("currentPositionY", currentPosition.getY());
            }
            startActivityForResult(intent, ConstantValue.CHANGE_TO_MAP_ACTIVITY_REQUEST_CODE);
        }
    }

    private class MyTextWatcher implements TextWatcher {
        private int index;
        public MyTextWatcher(int index) {
            this.index = index;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count){

        }

        @Override
        public void afterTextChanged(Editable s){
            if (s.equals("")) {
                return;
            }
            String currentStoreString = s.toString();
            if (AdapterArrayList.contains(currentStoreString)) {
                char char0 = currentStoreString.charAt(0);
                char char1 = currentStoreString.charAt(1);
                if (char1 >= '0' && char1 <= '9') {
                    currentRealStores[index] = Integer.parseInt(currentStoreString.substring(0, 2));
                } else if (char0 >= '0' && char0 <= '9'){
                    currentRealStores[index] = Integer.parseInt(currentStoreString.substring(0, 1));
                }
                Toast.makeText(getApplicationContext(), "成功标注第" + (index + 1) + "个店铺号" + currentRealStores[index],
                        Toast.LENGTH_SHORT).show();
            } else {
                currentRealStores[index] = 0;
            }
            //AutoCompleteTextView currentAutoCompleteTextView = (AutoCompleteTextView)findViewById(90 + index);

        }
    }

    private void takePicture() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            boolean first_picture = true;
            for (int i = 0; i < photo_num; i++) {
                if (photo_set[i] == true) {
                    first_picture = false;
                    break;
                }
            }
            if (first_picture) {
                mSensorUtils.reset();
            }
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, TakePhotoActivity.class);
            intent.putExtra("current_photo_num", current_photo_num);
            startActivityForResult(intent,
                    ConstantValue.TAKE_PHOTO_REQUEST_CODE);
        }
    }

    private void seletctPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (taking_photo) {
            if (requestCode == ConstantValue.TAKE_PHOTO_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    try {
                        double gyroAngle = data.getDoubleExtra("gyroAngle", 0);
                        double compassAngle = data.getDoubleExtra(
                                "compassAngle", 0);
                        String tempPath = photo_path + current_photo_num + ".jpeg";
                        ImageView currentImageView = (ImageView) findViewById(10 + current_photo_num - 1);
                        currentImageView.setImageBitmap(BitmapFactory
                                .decodeFile(tempPath));
                        photo_set[current_photo_num - 1] = true;

                        gyro[current_photo_num - 1] = gyroAngle;
                        TextView currentGyroTextView = (TextView) findViewById(40 + current_photo_num - 1);
                        currentGyroTextView.setText(String.format("%.6f", gyroAngle));

                        compass[current_photo_num - 1] = compassAngle;
                        TextView currentCompassTextView = (TextView) findViewById(50 + current_photo_num - 1);
                        currentCompassTextView.setText(String.format("%.6f", compassAngle));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    taking_photo = false;
                    selecting_photo = false;
                    localizationing = false;
                    current_photo_num = 0;
                }
            }

            super.onActivityResult(requestCode, resultCode, data);
        } else if (selecting_photo){
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                try {
                    Bitmap photo = BitmapFactory.decodeStream(cr
                            .openInputStream(uri));
                    ImageView currentImageView = (ImageView) findViewById(10 + current_photo_num - 1);
                    currentImageView.setImageBitmap(photo);
                    photo_set[current_photo_num - 1] = true;

                    gyro[current_photo_num - 1] = mSensorUtils.getAngle();;
                    TextView currentGyroTextView = (TextView) findViewById(40 + current_photo_num - 1);
                    currentGyroTextView.setText(String.format("%.6f", gyro[current_photo_num - 1]));

                    compass[current_photo_num - 1] = mSensorUtils.getCompassDirection();
                    TextView currentCompassTextView = (TextView) findViewById(50 + current_photo_num - 1);
                    currentCompassTextView.setText(String.format("%.6f", compass[current_photo_num - 1]));

                    String tempPath = photo_path + current_photo_num + ".jpeg";
                    File fImage = new File(tempPath);
                    fImage.createNewFile();
                    FileOutputStream iStream = new FileOutputStream(fImage);
                    photo.compress(CompressFormat.JPEG, 100, iStream);
                    iStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } else if (localizationing){
            if (requestCode == ConstantValue.CHANGE_TO_MAP_ACTIVITY_REQUEST_CODE) {
                double currentPositionX = data.getDoubleExtra("currentPositionX", -1);
                double currentPositionY = data.getDoubleExtra("currentPositionY", -1);
                if(currentPositionX == -1 || currentPositionY == -1) {
                    Toast.makeText(getApplicationContext(), "标注定位失败",
                            Toast.LENGTH_SHORT).show();
                    localization_set = false;
                } else {
                    Toast.makeText(getApplicationContext(), "标注定位成功",
                            Toast.LENGTH_SHORT).show();
                    currentPosition = new Position(currentPositionX, currentPositionY);
                    localization_set = true;
                }

                taking_photo = false;
                selecting_photo = false;
                localizationing = false;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class MyAdapter extends ArrayAdapter<String> {
        Context context;
        int resource, textViewResourceId;
        List<String> items, tempItems, suggestions;

        public MyAdapter(Context context, int resource, int textViewResourceId, List<String> items) {
            super(context, resource, textViewResourceId, items);
            this.context = context;
            this.resource = resource;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
            tempItems = new ArrayList<String>(items); // this makes the difference.
            suggestions = new ArrayList<String>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.autocompletetextview_row, parent, false);
            }
            String aString = items.get(position);
            if (aString != null) {
                TextView lblName = (TextView) view.findViewById(R.id.lbl_name);
                if (lblName != null)
                    lblName.setText(aString);
            }
            return view;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        /**
         * Custom Filter implementation for custom suggestions we provide.
         */
        Filter nameFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return (String) resultValue;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (String aString : tempItems) {
                        if (aString.contains(constraint)) {
                            suggestions.add(aString);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<String> filterList = (ArrayList<String>) results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (String aString : filterList) {
                        add(aString);
                        notifyDataSetChanged();
                    }
                }
            }
        };
    }

    private void sendMyMessage(int myMessageWhat) {
        Message message = new Message();
        message.what = myMessageWhat;
        mHandler.sendMessage(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSensorUtils.unregisterSensor();
        NonfreeJNILib.deletePointer();
    }
}