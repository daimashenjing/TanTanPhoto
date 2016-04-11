package com.szh.tantanphoto.dragalbum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.szh.tantanphoto.dragalbum.entity.PhotoItem;
import com.szh.tantanphoto.dragalbum.util.ImageUtils;
import com.szh.tantanphoto.dragalbum.util.StringUtils;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 模仿探探 相册View
 * 
 * @author szh QQ1095897632
 * 
 */
public class AlbumView extends ViewGroup implements OnTouchListener {

	private List<View> views = new ArrayList<View>();
	private List<PhotoItem> images = new ArrayList<PhotoItem>();

	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions mImageOptions;

	private boolean mAnimationEnd = true;

	// 第一个最大的view的宽高
	private int mItmeOne;
	// item 其余宽高
	private int ItemWidth;

	private int hidePosition = -1;
	// 根据数据 获取的 最大可拖拽移动换位的范围
	private int maxSize;
	// 当前控件 距离屏幕 顶点 的高度
	private int mTopHeight = -1;
	// 每个item之间的间隙
	public int padding = -1;

	// 正在拖拽的position
	private int mDragPosition;
	private int mDownX;
	private int mDownY;
	private boolean isOnItemClick = false;

	/**
	 * 刚开始拖拽的item对应的View
	 */
	private View mStartDragItemView = null;

	/**
	 * 用于拖拽的镜像，这里直接用一个ImageView
	 */
	private ImageView mDragImageView;

	/**
	 * 我们拖拽的item对应的Bitmap
	 */
	private Bitmap mDragBitmap;

	// x,y坐标的计算
	private int dragPointX;
	private int dragPointY;
	private int dragOffsetX;
	private int dragOffsetY;

	private GridLayout RootView;

	/**
	 * 为了兼容小米那个日了狗的系统 就不用 WindowManager了
	 * 
	 * @param rootView
	 */
	public void setRootView(GridLayout rootView) {
		RootView = rootView;
	}

	public AlbumView(Context context) {
		this(context, null);
	}

	public AlbumView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AlbumView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mImageOptions = ImageUtils.getFaceVideoOptions();
		padding = dp2px(4, context);
		initUI();
	}

	int moveX;
	int moveY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHandler.removeCallbacks(mDragRunnable);
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			mDragPosition = pointToPosition(mDownX, mDownY);
			if (mDragPosition > maxSize) {
				return super.dispatchTouchEvent(ev);
			}
			if (mDragPosition == -1) {
				return super.dispatchTouchEvent(ev);
			}
			// 根据position获取该item所对应的View
			mStartDragItemView = getChildAt(mDragPosition);
			// 获取BitMap
			mStartDragItemView.setDrawingCacheEnabled(true);
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
			mStartDragItemView.destroyDrawingCache();

			dragPointX = (mStartDragItemView.getLeft() + mStartDragItemView.getWidth() / 2)
					- mStartDragItemView.getLeft();
			dragPointY = (mStartDragItemView.getTop() + mStartDragItemView.getHeight() / 2)
					- mStartDragItemView.getTop();
			dragOffsetX = (int) (ev.getRawX() - mDownX);
			dragOffsetY = (int) (ev.getRawY() - mDownY);
			mHandler.postDelayed(mDragRunnable, 50);
			break;
		case MotionEvent.ACTION_MOVE:
			moveX = (int) ev.getX();
			moveY = (int) ev.getY();
			if (mDragImageView != null) {
				onDragItem(moveX - dragPointX + dragOffsetX, moveY - dragPointY + dragOffsetY - mTopHeight);
				onSwapItem(moveX, moveY);
			}
			break;
		case MotionEvent.ACTION_UP:
			onStopDrag();
			mHandler.removeCallbacks(mDragRunnable);
			break;
		case MotionEvent.ACTION_CANCEL:
			onStopDrag();
			mHandler.removeCallbacks(mDragRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private Handler mHandler = new Handler();

	// 用来处理是否为长按的Runnable
	private Runnable mDragRunnable = new Runnable() {
		@Override
		public void run() {
			// 根据我们按下的点显示item镜像
			if (isOnItemClick)
				return;
			createDragImage();
			mStartDragItemView.setVisibility(View.GONE);
		}

	};

	private Rect mTouchFrame;

	/**
	 * 判断按下的位置是否在Item上 并返回Item的位置 {@link AbsListView #pointToPosition(int, int)}
	 */
	public int pointToPosition(int x, int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}
		int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 创建拖动的镜像
	 * 
	 * @param bitmap
	 * @param downX
	 *            按下的点相对父控件的X坐标
	 * @param downY
	 *            按下的点相对父控件的X坐标
	 */
	private void createDragImage() {
		int[] location = new int[2];
		mStartDragItemView.getLocationOnScreen(location);
		float drX = location[0];
		float drY = location[1] - mTopHeight;
		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(mDragBitmap);
		RootView.addView(mDragImageView);

		int drH = (int) (ItemWidth * 0.8);
		float w = mStartDragItemView.getWidth();
		final float scale = drH / w;
		createTranslationAnimations(mDragImageView, drX, mDownX - dragPointX + dragOffsetX, drY,
				mDownY - dragPointY + dragOffsetY - mTopHeight, scale, scale).setDuration(200).start();
	}

	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			RootView.removeView(mDragImageView);
			mDragImageView = null;
			if (mStartDragItemView != null)
				mStartDragItemView.setVisibility(View.VISIBLE);
		}
	}

	class resultSetListenerAdapter implements AnimatorListener {
		View mStartDragItemView, mDragImageView;
		boolean isStart;

		public resultSetListenerAdapter(View mStartDragItemView, View mDragImageView, boolean isStart) {
			// TODO Auto-generated constructor stub
			this.mStartDragItemView = mStartDragItemView;
			this.mDragImageView = mDragImageView;
			this.isStart = isStart;
		}

		@Override
		public void onAnimationStart(android.animation.Animator animation) {
			// TODO Auto-generated method stub
			if (isStart) {
				mStartDragItemView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onAnimationEnd(android.animation.Animator animation) {
			// TODO Auto-generated method stub
			if (!isStart) {
				mStartDragItemView.setVisibility(View.VISIBLE);
				RootView.removeView(mDragImageView);
				mDragImageView = null;
			}
		}

		@Override
		public void onAnimationCancel(android.animation.Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(android.animation.Animator animation) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * 
	 * @param x
	 * @param y
	 */
	private void onDragItem(int X, int Y) {
		if (mDragImageView != null) {
			ViewHelper.setTranslationX(mDragImageView, X);
			ViewHelper.setTranslationY(mDragImageView, Y);
		}
	}

	/**
	 * 交换item
	 * 
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		if (mDragImageView != null) {
			int tempPosition = pointToPosition(moveX, moveY);
			if (tempPosition > maxSize) {
				return;
			}
			if (tempPosition != mDragPosition && tempPosition != -1 && mAnimationEnd) {
				animateReorder(mDragPosition, tempPosition);
			}
		}
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 * 
	 * @param moveY
	 * @param moveX
	 */
	private void onStopDrag() {
		removeDragImage();
		hidePosition = -1;
	}

	/**
	 * 获取当前控件 距离屏幕 顶点 的高度
	 * 
	 * @param context
	 * @return
	 */
	private int getTopHeight(Context context) {
		int statusHeight = 0;
		Rect ViewRect = new Rect();
		getGlobalVisibleRect(ViewRect);
		statusHeight = ViewRect.top;
		if (0 == statusHeight) {
			Rect localRect = new Rect();
			getWindowVisibleDisplayFrame(localRect);
			statusHeight = localRect.top;
			if (0 == statusHeight) {
				Class<?> localClass;
				try {
					localClass = Class.forName("com.android.internal.R$dimen");
					Object localObject = localClass.newInstance();
					int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
					statusHeight = context.getResources().getDimensionPixelSize(i5);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 注意 如果上边方法 未能 获取成功 那么 请根据个人 应用情况 加上相应的值
			// 比如 +45 我加的是一个 大概Title 的值
			// 如果当前控件 上边 有其他控件 请加上其他控件的高度
			statusHeight += dp2px(45, context);
		}

		return statusHeight;
	}

	public int getItemWidth() {
		return ItemWidth;
	}

	public void setImages(List<PhotoItem> images) {
		this.images = images;
		initUI();
	}

	public List<PhotoItem> getImages() {
		List<PhotoItem> temimages = new ArrayList<PhotoItem>();
		for (PhotoItem Item : images) {
			if (!StringUtils.isEmpty(Item.hyperlink)) {
				temimages.add(Item);
			}
		}
		return temimages;
	}

	public void initUI() {
		views.clear();
		removeAllViews();
		for (int i = 0; i < images.size(); i++) {
			ImageView view = new ImageView(getContext());
			view.setScaleType(ScaleType.FIT_XY);
			if (!StringUtils.isEmpty(images.get(i).hyperlink)) {
				maxSize = i;
			}
			mImageLoader.displayImage(images.get(i).hyperlink, view, mImageOptions);
			views.add(view);
			addView(view);
		}
		initListener();
	}

	private void initListener() {
		for (int i = 0; i < views.size(); i++) {
			View view = views.get(i);
			view.setTag(i);
			view.setOnTouchListener(this);
		}
	}

	/**
	 * 创建移动动画
	 * 
	 * @param view
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(
				ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("translationX", startX, endX),
						PropertyValuesHolder.ofFloat("translationY", startY, endY)));
		return animSetXY;
	}

	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY,
			float scaleX, float scaleY) {
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(
				ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("translationX", startX, endX),
						PropertyValuesHolder.ofFloat("translationY", startY, endY),
						PropertyValuesHolder.ofFloat("scaleX", 1.0f, scaleX),
						PropertyValuesHolder.ofFloat("scaleY", 1.0f, scaleY)));
		return animSetXY;
	}

	public boolean IsOneTwo(int Position) {
		return Position == 1 || Position == 0;
	}

	@SuppressWarnings("unchecked")
	public void swap(List<?> List, int index1, int index2) {
		List<Object> rawList = (java.util.List<Object>) List;
		rawList.set(index2, rawList.set(index1, rawList.get(index2)));
	}

	/**
	 * item的交换动画效果
	 * 
	 * @param oldPosition
	 * @param newPosition
	 */
	public void animateReorder(int oldPosition, int newPosition) {
		boolean isForward = newPosition > oldPosition;
		final List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition + 1; pos <= newPosition; pos++) {
				View view = getChildAt(pos);
				if (pos == 1) {
					float h = view.getWidth() / 2;
					float mSpacing = padding / 2;
					float w = getChildAt(0).getWidth();
					float scale = w / view.getWidth();
					resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding + mSpacing + h), 0,
							h + mSpacing, scale, scale));
					swap(images, pos, pos - 1);
				}
				if (pos == 2) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0, -(view.getWidth() + padding)));
					swap(images, pos, pos - 1);
				}
				if (pos == 3) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0, -(view.getWidth() + padding)));
					swap(images, pos, pos - 1);
				}
				if (pos == 4) {
					resultList.add(createTranslationAnimations(view, 0, view.getWidth() + padding, 0, 0));
					swap(images, pos, pos - 1);
				}
				if (pos == 5) {
					resultList.add(createTranslationAnimations(view, 0, view.getWidth() + padding, 0, 0));
					swap(images, pos, pos - 1);
				}
			}
		} else {
			for (int pos = newPosition; pos < oldPosition; pos++) {
				View view = getChildAt(pos);
				if (pos == 0) {
					float h = getChildAt(1).getWidth() / 2;
					float mSpacing = padding / 2;
					float w = getChildAt(0).getWidth();
					float scale = getChildAt(1).getWidth() / w;
					resultList.add(createTranslationAnimations(view, 0,
							getChildAt(1).getWidth() + padding + mSpacing + h, 0, -(h + mSpacing), scale, scale));
				}
				if (pos == 1) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0, view.getWidth() + padding));
				}
				if (pos == 2) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0, view.getWidth() + padding));
				}
				if (pos == 3) {
					resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding), 0, 0));
				}
				if (pos == 4) {
					resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding), 0, 0));
				}
			}
			for (int i = oldPosition; i > newPosition; i--) {
				swap(images, i, i - 1);
			}
		}

		hidePosition = newPosition;
		resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(150);
		resultSet.setInterpolator(new OvershootInterpolator(1.6f));
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if (!mAnimationEnd) {
					initUI();
					resultSet.removeAllListeners();
					resultSet.clone();
					resultSet = null;
					mDragPosition = hidePosition;
				}
				mAnimationEnd = true;
			}
		});
		resultSet.start();
		resultList.clear();
	}

	AnimatorSet resultSet = null;
	OnItemClickListener clickListener;

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		// TODO Auto-generated method stub
		clickListener = onItemClickListener;
	}

	public interface OnItemClickListener {
		public void ItemClick(View view, int position, boolean Photo);

	}

	int ItemDownX;
	int ItemDownY;
	long strTime;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			ItemDownX = (int) event.getX();
			ItemDownY = (int) event.getY();
			strTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			int mDragPosition = (Integer) v.getTag();
			if (mDragPosition <= maxSize) {
				int moveX = (int) event.getX();
				int moveY = (int) event.getY();
				float abslMoveDistanceX = Math.abs(moveX - ItemDownX);
				float abslMoveDistanceY = Math.abs(moveY - ItemDownY);
				if (abslMoveDistanceX < 2.0 && abslMoveDistanceY < 2.0 && (System.currentTimeMillis() - strTime) < 50) {
					if (clickListener != null) {
						isOnItemClick = true;
						clickListener.ItemClick(getChildAt(mDragPosition), mDragPosition, true);
					} else {
						isOnItemClick = false;
					}
				} else {
					isOnItemClick = false;
				}
			} else {
				if (clickListener != null) {
					isOnItemClick = true;
					clickListener.ItemClick(getChildAt(mDragPosition), mDragPosition, false);
				} else {
					isOnItemClick = false;
				}
			}
			break;
		}
		return true;
	}

	public void onResume() {
		initUI();
	}

	public void onPause() {
		mImageLoader.clearMemoryCache();
	}

	public void onDestroy() {

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int Width = getMeasuredWidth();
		ItemWidth = Width / 3 - padding - (padding / 3);
		for (int i = 0, size = getChildCount(); i < size; i++) {
			View view = getChildAt(i);
			if (i == 0) {
				mItmeOne = ItemWidth * 2 + padding;
				l += padding;
				t += padding;
				view.layout(l, t, l + mItmeOne, t + mItmeOne);
				l += mItmeOne + padding;
			}
			if (i == 1) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				t += ItemWidth + padding;
			}
			if (i == 2) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				t += ItemWidth + padding;
			}
			if (i >= 3) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				l -= ItemWidth + padding;
			}

			if (i == hidePosition) {
				view.setVisibility(View.GONE);
				mStartDragItemView = view;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int resWidth = 0;
		int resHeight = 0;
		/**
		 * 根据传入的参数，分别获取测量模式和测量值
		 */
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int height = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		/**
		 * 如果宽或者高的测量模式非精确值
		 */
		if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
			// 主要设置为背景图的高度
			resWidth = getSuggestedMinimumWidth();
			// 如果未设置背景图片，则设置为屏幕宽高的默认值
			resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

			resHeight = getSuggestedMinimumHeight();
			// 如果未设置背景图片，则设置为屏幕宽高的默认值
			resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
		} else {
			// 如果都设置为精确值，则直接取小值；
			resWidth = resHeight = Math.min(width, height);
		}

		setMeasuredDimension(resWidth, resHeight);
	}

	/**
	 * 获得默认该layout的尺寸
	 * 
	 * @return
	 */
	private int getDefaultWidth() {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
	}

	public int dp2px(int dp, Context context) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (mTopHeight <= 0) {
				mTopHeight = getTopHeight(getContext());
			}
		}
	}

}