package com.szh.tantanphoto.dragalbum.util;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.szh.tantanphoto.R;

import android.graphics.Bitmap;

public class ImageUtils {

	public static DisplayImageOptions getFaceVideoOptions() {
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
