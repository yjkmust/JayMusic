package com.yjkmust.jaymusic.Utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by 11432 on 2017/8/21.
 */

public class MyApplication extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
    public static Context getContext(){
        return mContext;
    }
}
