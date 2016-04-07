package com.szh.tantanphoto.dragalbum.util;

import java.util.ArrayList;
import java.util.List;

import com.szh.tantanphoto.dragalbum.entity.PhotoItem;

public class DemoUtils {

	public DemoUtils() {
	}

	public List<PhotoItem> moarItems(int qty, List<PhotoItem> Datas) {
		List<PhotoItem> items = new ArrayList<PhotoItem>();
		if(Datas!=null){
			items.addAll(Datas);
		}
		//添加null
		for (int i = Datas == null ? 0 : Datas.size(); i < qty; i++) {
			items.add(new PhotoItem());
		}
		return items;
	}
}
