package com.hfut235.emberwind;

import android.app.Application;

import com.duckduckgo.app.Injector;
import com.hfut235.emberwind.utils.Utils;

import timber.log.Timber;

/**
 * Created by wangyl on 17-8-15.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(this);
        Utils.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
