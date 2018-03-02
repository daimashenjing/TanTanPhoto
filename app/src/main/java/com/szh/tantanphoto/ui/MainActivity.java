package com.szh.tantanphoto.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;
import android.widget.RelativeLayout;

import com.szh.tantanphoto.R;
import com.szh.tantanphoto.dragalbum.AlbumView;
import com.szh.tantanphoto.dragalbum.AlbumView.OnItemClickListener;
import com.szh.tantanphoto.dragalbum.entity.PhotoItem;
import com.szh.tantanphoto.dragalbum.log.L;
import com.szh.tantanphoto.dragalbum.util.DemoUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
	private AlbumView mAlbumView;
	private RelativeLayout msetUserInfoLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAlbumView = getId(R.id.imageListView);
		mAlbumView.setRootView((GridLayout) getId(R.id.Rootlayout));
		mAlbumView.setImages(new DemoUtils().moarItems(6, getImageDate()));
		msetUserInfoLayout = getId(R.id.setUserInfoLayout);
		msetUserInfoLayout.setOnClickListener(this);
		mAlbumView.setOnItemClickListener(this);
	}

	private List<PhotoItem> getImageDate() {
		// TODO Auto-generated method stub
		List<PhotoItem> mDataList = new ArrayList<PhotoItem>();
		PhotoItem item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head1;
		item.id = 1;
		item.sort = 1;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head2;
		item.id = 2;
		item.sort = 2;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head3;
		item.id = 3;
		item.sort = 3;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head4;
		item.id = 4;
		item.sort = 4;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head5;
		item.id = 5;
		item.sort = 5;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head6;
		item.id = 6;
		item.sort = 6;
		mDataList.add(item);
		
//		item = new PhotoItem();
//		item.hyperlink = "drawable://" + R.drawable.head7;
//		item.id = 7;
//		item.sort = 7;
//		mDataList.add(item);
//
//		item = new PhotoItem();
//		item.hyperlink = "drawable://" + R.drawable.head8;
//		item.id = 8;
//		item.sort = 8;
//		mDataList.add(item);

		return mDataList;
	}

	/**
	 * 获取控件ID
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends View> T getId(int id) {
		return (T) findViewById(id);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onItemClick(View view, int position, boolean Photo) {
		// TODO Auto-generated method stub
		L.out("position", position + ",Photo : " + Photo);
	}
}
