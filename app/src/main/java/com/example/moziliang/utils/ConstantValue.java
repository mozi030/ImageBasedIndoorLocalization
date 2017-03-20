package com.example.moziliang.utils;

public class ConstantValue {

//    public static final String HOST = "http://143.89.145.138/app/Sextant/Index/";
//    public static final String URL_PREPARE = HOST + "prepare";
//    public static final String URL_UPLOAD_IMAGE = HOST + "upload_image";
//    public static final String URL_GET_BENCHMARK = HOST + "get_benchmark";
//    public static final String UTL_GET_LOCATION_RESULT = HOST + "get_location";

    public static  String HOST;
    public static  String URL_PREPARE;
    public static  String URL_UPLOAD_IMAGE;
    public static  String URL_GET_BENCHMARK;
    public static  String UTL_GET_LOCATION_RESULT;

    public static double pi = 3.141592654;

    public static final int MYLOCATION_CIRCLE_COLOR = 0xFFFF0000;
    public static final int NORMAL_CIRCLE_EDGE_COLOR = 0xFFFFFFFF;
    public static final int MYLOCATION_RANGE_COLOR = 0x1EFF0000;
    public static final float MYLOCATION_RADIUS_TO_SCREEN = 64;
    public static final float MYLOCATION_RANGE_RADIUS_TO_SCREEN = 64;

    public final static float MAP_MIN_SCALE = 0.1f;
    public final static float MAP_MAX_SCALE = 4f;

    public static final int MESSAGE_SEND_PREPARE = 0x7fffffff;
    public static final int MESSAGE_BEGIN_TAKING_PIC = 0x7ffffffe;
    public static final int MESSAGE_BACK_TO_PREPARE = 0x7ffffffd;

    public static final int MESSAGE_UPLOAD_IMAGE_RESULT = 0x7ffffffc;
    public static final int MESSAGE_BEGIN_SELECT_BENCHMARK = 0x7ffffffb;
    public static final int MESSAGE_RESEND_IMAGE = 0x7ffffffa;
    public static final int MESSAGE_EXIT_APPLICATION = 0x7ffffff9;
    public static final int MESSAGE_GET_BENCHMARK = 0x7ffffff8;
    public static final int MESSAGE_GET_LOCATION_RESULT = 0x7ffffff7;

    public static final int MESSAGE_UPDATE_SERVER_IP = 0x7fffff6;

    public static final int MESSAGE_CHANGE_MAP = 0x7fffff5;

    public static final int MESSAGE_CALIBRATE = 0x7ffffff4;

    public static final int TAKE_PHOTO_REQUEST_CODE = 0x7ffffff3;

    public static final int MESSAGE_MATCH_BEGIN = 0x6fffd00;
    public static final int MESSAGE_MATCH_ALL_FINISH = 0x6fffd01;
    public static final int MESSAGE_MATCH_LENGTH_ERROR = 0x6fffd02;

    //for MapActivity
    public static final int SCROLLVIEW_DIFFERENCE_ID_OFFSET = 0x6fffff00;
    public static final int CHANGE_TO_MAP_ACTIVITY_REQUEST_CODE = 0x6fffe00;
}
