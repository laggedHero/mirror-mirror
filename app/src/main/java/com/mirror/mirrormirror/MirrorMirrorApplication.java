package com.mirror.mirrormirror;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by laggedhero on 10/30/16.
 */

public class MirrorMirrorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
