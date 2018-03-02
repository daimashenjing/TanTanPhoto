package com.szh.tantanphoto.dragalbum.util;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.szh.tantanphoto.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by HuaSao1024 on 2017/3/15.
 * 把图片加载封装到一个类
 * 方便日后替换框架
 *
 * 框架这种东西 以防万一有要替换的时候
 *
 */
public class ImageLoad {
    private static ImageLoad imageLoad;
    private ImageLoader mImageLoader;

    private ImageLoad() {
        mImageLoader= ImageLoader.getInstance();
    }

    public synchronized static ImageLoad getInstance() {
        if (imageLoad == null) {
            imageLoad = new ImageLoad();
        }
        return imageLoad;
    }

    /**
     * 加载图片
     * 最好只重构此方法，方便日后框架更换。
     * @param url
     * @param view
     */
    public void loadImage(String url, ImageView view) {
        loadImage(url, view, getDefaultOptions(), null);
    }

    public void loadPhotoImage(String url, ImageView view) {
        loadImage(url, view, getPhotoOptions(), null);
    }

    public void loadImage(String url, ImageView view, ImageLoadingListener listener) {
        loadImage(url, view, getDefaultOptions(), listener);
    }

    public void loadImage(String url, ImageView view, DisplayImageOptions mImageOptions, ImageLoadingListener listener) {
        mImageLoader.displayImage(url, view, mImageOptions, listener);
    }

    /**
     * 初始化ImageLoader配置
     * @param context
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
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, "FaceMaster/Image/Cache");
        config.diskCache(new UnlimitedDiskCache(cacheDir));
        ImageLoader.getInstance().init(config.build());
    }

    public void clearMemoryCache(){
        mImageLoader.clearMemoryCache();
    }

    private DisplayImageOptions getDefaultOptions() {
        return new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.icon_addpic_unfocused2) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.icon_addpic_unfocused2) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.icon_addpic_unfocused2) // 设置图片加载或解码过程中发生错误显示的图片
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                .resetViewBeforeLoading(false)// 设置图片在下载前是否重置，复位
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // 设置图片的缩放类型，该方法可以有效减少内存的占用
                .build();
    }

    private DisplayImageOptions getPhotoOptions() {
        return new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.icon_addpic_unfocused2) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.icon_addpic_unfocused) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.icon_addpic_unfocused2) // 设置图片加载或解码过程中发生错误显示的图片
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                .resetViewBeforeLoading(false)// 设置图片在下载前是否重置，复位
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // 设置图片的缩放类型，该方法可以有效减少内存的占用
                .build();
    }
}
