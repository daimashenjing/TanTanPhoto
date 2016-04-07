package com.szh.tantanphoto;

import java.io.File;
import java.util.Calendar;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

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
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPoolSize(4);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.diskCacheFileCount(300);
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.memoryCacheExtraOptions(480, 800);// default = device screen
		config.tasksProcessingOrder(QueueProcessingType.FIFO);
		config.imageDownloader(new BaseImageDownloader(context));//  default
		config.memoryCacheSizePercentage(20);
		config.imageDecoder(new BaseImageDecoder(true));//  default
		config.writeDebugLogs();
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, "szh/app/Cache");
		config.diskCache(new UnlimitedDiskCache(cacheDir));
		ImageLoader.getInstance().init(config.build());
	}
}
