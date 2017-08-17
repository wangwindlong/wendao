package org.geometerplus.android.fbreader.plugin.tts;

/**
 * Created by wangyl on 17-7-22.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

public class Lt {
    private static String myTag = "FBReaderTTS";
    private static boolean myResult = false;
    private Lt() {}
    static void setTag(String tag) { myTag = tag; }
    public static void d(String msg) {
        // Uncomment line below to turn on debug output
        Log.d(myTag, msg == null ? "(null)" : msg);
    }
    public static void df(String msg) {
        // Forced output, do not comment out - for exceptions etc.
        Log.d(myTag, msg == null ? "(null)" : msg);
    }
    public static void e(String msg) {
        Log.e(myTag, msg == null ? "(null)" : msg);
    }

    static void alert(Activity activity, String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(activity);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    static void alert(Activity activity, int resId) {
        alert(activity, activity.getResources().getString(resId));
    }
}
