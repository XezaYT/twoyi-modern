/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import io.twoyi.utils.RomManager;

/**
 * @author weishu
 * @date 2020/12/24.
 */

public class TwoyiApplication extends Application {

    private static final String TAG = "TwoyiApp";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Install Global Crash Logger
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "FATAL CRASH on thread " + thread.getName(), throwable);
            try {
                File crashFile = new File("/sdcard/Download/twoyi_crash.txt");
                try (PrintWriter pw = new PrintWriter(new FileWriter(crashFile, false))) {
                    throwable.printStackTrace(pw);
                }
            } catch (Throwable ignored) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        });

        try {
            RomManager.ensureBootFiles(base);
        } catch (Throwable t) {
            Log.e(TAG, "ensureBootFiles failed", t);
        }

        try {
            TwoyiSocketServer.getInstance(base).start();
        } catch (Throwable t) {
            Log.e(TAG, "TwoyiSocketServer start failed", t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Enable AMOLED Dark Mode by default
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } catch (Throwable t) {
            Log.e(TAG, "setDefaultNightMode failed", t);
        }
    }

    static int statusBarHeight = -1;

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != -1) {
            return statusBarHeight;
        }

        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resId);
        }

        if (statusBarHeight < 0) {
            int result = 0;
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object obj = clazz.newInstance();
                Field field = clazz.getField("status_bar_height");
                int resourceId = Integer.parseInt(field.get(obj).toString());
                result = context.getResources().getDimensionPixelSize(resourceId);
            } catch (Exception e) {
            } finally {
                statusBarHeight = result;
            }
        }

        //Use 25dp if no status bar height found
        if (statusBarHeight < 0) {
            statusBarHeight = dip2px(context, 25);
        }
        return statusBarHeight;
    }

    private static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (dpValue * scale + 0.5f);
        return px;
    }

    public static float px2dp(float pxValue) {
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }
}
