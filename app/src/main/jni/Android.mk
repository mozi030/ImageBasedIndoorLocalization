LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
OpenCV_INSTALL_MODULES := on
OpenCV_CAMERA_MODULES := on

OPENCV_LIB_TYPE := STATIC

ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
include ../../../../native/jni/OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif

include $(CLEAR_VARS)
LOCAL_MODULE    := nonfree_prebuilt
LOCAL_SRC_FILES := libnonfree.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := opencv_java_prebuilt
LOCAL_SRC_FILES := libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
FILE_LIST := $(wildcard $(LOCAL_PATH)/vlfeat/*.c)
FILE_LIST += $(wildcard $(LOCAL_PATH)/vlfeat/*.h)

LOCAL_C_INCLUDES:= /Users/moziliang/code/AndroidStudioProjects/IndoorLocalizationRelease/native/jni/include
LOCAL_MODULE    := nonfree_jni
#LOCAL_CFLAGS    := -Werror -O3 -ffast-math
LOCAL_CFLAGS	:= -DVL_DISABLE_SSE2 -DVL_DISABLE_AVX
LOCAL_SHARED_LIBRARIES := nonfree_prebuilt opencv_java_prebuilt
LOCAL_SRC_FILES := nonfree_jni.cpp
LOCAL_SRC_FILES += $(FILE_LIST)

LOCAL_LDLIBS    += -llog  -lm
include $(BUILD_SHARED_LIBRARY)