package com.shohei.put_on.controller.utils;

import android.os.Debug;
import android.util.Log;

import com.shohei.put_on.BuildConfig;

/**
 * Created by nakayamashohei on 15/09/19.
 */
public class Logger {

    private static final String TAG = "PutOn";

    public static final void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static final void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static final void d(Class aClass, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(aClass.getSimpleName(), msg);
        }
    }

    public static final void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static final void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static final void e(Class aClass, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(aClass.getSimpleName(), msg);
        }
    }

    public static final void i(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static final void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static final void i(Class aClass, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(aClass.getSimpleName(), msg);
        }
    }

    public static final void v(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static final void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static final void v(Class aClass, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(aClass.getSimpleName(), msg);
        }
    }

    public static final void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static final void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static final void w(Class aClass, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(aClass.getSimpleName(), msg);
        }
    }

    public static final void heap() {
        heap(TAG);
    }

    public static final void heap(String tag) {
        if (BuildConfig.DEBUG) {
            String msg = "heap : Free=" + Long.toString(Debug.getNativeHeapFreeSize() / 1024) + "kb" +
                    ", Allocated=" + Long.toString(Debug.getNativeHeapAllocatedSize() / 1024) + "kb" +
                    ", Size=" + Long.toString(Debug.getNativeHeapSize() / 1024) + "kb";

            Log.v(tag, msg);
        }
    }
}
