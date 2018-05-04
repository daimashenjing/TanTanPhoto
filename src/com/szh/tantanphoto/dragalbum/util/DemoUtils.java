package com.szh.tantanphoto.dragalbum.util;

import java.util.ArrayList;
import java.util.List;

import com.szh.tantanphoto.dragalbum.entity.PhotoItem;

public class DemoUtils {

	public DemoUtils() {
	}

	public List<PhotoItem> moarItems(int maxNum, List<PhotoItem> Datas) {
		List<PhotoItem> items = new ArrayList<PhotoItem>();
		int position = 0;
		int len = Datas != null ? Datas.size() : 0;
		for (int i = 0; (i < len && i < maxNum); i++) {
			position++;
			items.add(Datas.get(i));
		}
		
		// 添加null
		while (position < maxNum) {
			position++;
			items.add(new PhotoItem());
		}
		return items;
	}
}
