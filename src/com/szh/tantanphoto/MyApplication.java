package com.szh.tantanphoto;

import com.szh.tantanphoto.dragalbum.util.ImageLoad;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	private static MyApplication mInstance;

	public static MyApplication getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		initImageLoader(mInstance);
	}

	/**
	 * 设置Image配置
	 */
	public void initImageLoader(Context context) {
		//初始化ImageLoader配置
        ImageLoad.getInstance().initImageLoader(this);
	}
}
