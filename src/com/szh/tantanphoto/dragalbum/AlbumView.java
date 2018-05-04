package com.szh.tantanphoto.dragalbum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.utils.L;
import com.szh.tantanphoto.dragalbum.entity.PhotoItem;
import com.szh.tantanphoto.dragalbum.util.ImageLoad;
import com.szh.tantanphoto.dragalbum.util.StringUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 模仿探探 相册View
 */
public class AlbumView extends ViewGroup implements OnTouchListener {

	private List<ImageView> views = new ArrayList<ImageView>();
	private List<PhotoItem> images = new ArrayList<PhotoItem>();
	// 动画处于停止状态
	private boolean mAnimationEnd = true;
	// 第一个最大的view的宽高
	private int mItmeOne;
	// item 其余宽高
	private int ItemWidth;
	// 当前隐藏控件的位置
	private int hidePosition = -1;
	// 根据数据 获取的 最大可拖拽的控件
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

	int moveX;
	int moveY;

	int ItemDownX;
	int ItemDownY;
	long strTime;

	/**
	 * 为了兼容小米等系统 就不用WindowManager了 如果本类生成view就不能拖到全屏 所以我们在最外层生成一个view传递过来
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
		padding = dp2px(4, context);
		initUI();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isOnItemClick = false;
			removeCallbacks(mDragRunnable);
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
			postDelayed(mDragRunnable, 50);
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
			removeCallbacks(mDragRunnable);
			break;
		case MotionEvent.ACTION_CANCEL:
			onStopDrag();
			removeCallbacks(mDragRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

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
				int absMoveDistanceX = Math.abs(moveX - ItemDownX);
				int absMoveDistanceY = Math.abs(moveY - ItemDownY);
				if (absMoveDistanceX < 20 && absMoveDistanceY < 20 && (System.currentTimeMillis() - strTime) < 200) {
					if (clickListener != null) {
						isOnItemClick = true;
						clickListener.onItemClick(getChildAt(mDragPosition), mDragPosition, true);
					} else {
						isOnItemClick = false;
					}
				} else {
					isOnItemClick = false;
				}
			} else {
				if (clickListener != null) {
					isOnItemClick = true;
					clickListener.onItemClick(getChildAt(mDragPosition), mDragPosition, false);
				} else {
					isOnItemClick = false;
				}
			}
			break;
		}
		return true;
	}

	// 用来处理是否为长按的Runnable
	private Runnable mDragRunnable = new Runnable() {
		@Override
		public void run() {
			// 根据我们按下的点显示item镜像
			if (isOnItemClick)
				return;
			if (mStartDragItemView.isShown()) {
				createDragImage();
				mStartDragItemView.setVisibility(View.GONE);
			}
		}

	};

	Rect frame = new Rect();

	/**
	 * 判断按下的位置是否在Item上 并返回Item的位置 {@link AbsListView #pointToPosition(int, int)}
	 */
	public int pointToPosition(int x, int y) {
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

	ObjectAnimator translationAnimator;

	/**
	 * 创建拖动的镜像
	 */
	private void createDragImage() {
		int[] location = new int[2];
		mStartDragItemView.getLocationOnScreen(location);
		float drX = location[0];
		float drY = location[1] - mTopHeight;
		if (mDragImageView == null) {
			mDragImageView = new ImageView(getContext());
		} else {
			ViewGroup parent = (ViewGroup) mDragImageView.getParent();
			if (parent != null) {
				parent.removeView(mDragImageView);// 先移除
			}
		}
		mDragImageView.setImageBitmap(mDragBitmap);
		RootView.addView(mDragImageView);
		float scale = (float) (ItemWidth * 0.8 / mStartDragItemView.getWidth());
		float endX = mDownX - dragPointX + dragOffsetX;
		float endY = mDownY - dragPointY + dragOffsetY - mTopHeight;
		ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mDragImageView,
				PropertyValuesHolder.ofFloat("scaleX", 1.0f, scale),
				PropertyValuesHolder.ofFloat("scaleY", 1.0f, scale));
		scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		scaleAnimator.setDuration(320).start();
		translationAnimator = ObjectAnimator.ofPropertyValuesHolder(mDragImageView,
				PropertyValuesHolder.ofFloat("translationX", drX, endX),
				PropertyValuesHolder.ofFloat("translationY", drY, endY));
		translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		translationAnimator.setDuration(200).start();
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

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 */
	private void onDragItem(int X, int Y) {
		if (mDragImageView != null) {
			if (translationAnimator != null && translationAnimator.isRunning()) {
				translationAnimator.end();
			}
			ViewCompat.setTranslationX(mDragImageView, X);
			ViewCompat.setTranslationY(mDragImageView, Y);
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
		int[] location = new int[2];
		getLocationOnScreen(location);
		int statusHeight = 0;
		statusHeight = location[1];
		if (0 == statusHeight) {
			getLocationInWindow(location);
			statusHeight = location[1];
		}
		return statusHeight;
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
			ImageLoad.getInstance().loadPhotoImage(images.get(i).hyperlink, view);
			views.add(view);
			addView(view);
		}
		initListener();
	}

	private void onRefresh() {
		initUI();
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
	boolean isAniReverse1 = true;
	boolean isAniReverse2 = true;

	public void animateReorder(int oldPosition, int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition + 1; pos <= newPosition; pos++) {
				View view = getChildAt(pos);
				if (pos < 6) {
					if (pos == 1) {
						float h = view.getWidth() / 2;
						float mSpacing = padding / 2;
						float w = getChildAt(0).getWidth();
						float scale = w / view.getWidth();
						resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding + mSpacing + h),
								0, h + mSpacing, scale, scale));
					}
					if (pos == 2 || pos == 3) {
						resultList.add(createTranslationAnimations(view, 0, 0, 0, -(view.getWidth() + padding)));
					}
					if (pos == 4 || pos == 5) {
						resultList.add(createTranslationAnimations(view, 0, view.getWidth() + padding, 0, 0));
					}
				} else {
					if (pos % 3 == 0) {
						if (pos % 6 == 0) {
							isAniReverse1 = true;
						} else {
							isAniReverse1 = false;
						}
						resultList.add(createTranslationAnimations(view, 0, 0, 0, -(view.getWidth() + padding)));
					} else {
						if (isAniReverse1) {
							resultList.add(createTranslationAnimations(view, 0, -view.getWidth() + padding, 0, 0));
						} else {
							resultList.add(createTranslationAnimations(view, 0, view.getWidth() + padding, 0, 0));
						}
					}

				}
				swap(images, pos, pos - 1);
			}
		} else {
			for (int pos = newPosition; pos < oldPosition; pos++) {
				View view = getChildAt(pos);
				if (pos < 5) {
					if (pos == 0) {
						float h = getChildAt(1).getWidth() / 2;
						float mSpacing = padding / 2;
						float w = getChildAt(0).getWidth();
						float scale = getChildAt(1).getWidth() / w;
						resultList.add(createTranslationAnimations(view, 0,
								getChildAt(1).getWidth() + padding + mSpacing + h, 0, -(h + mSpacing), scale, scale));
					}
					if (pos == 1 || pos == 2) {
						resultList.add(createTranslationAnimations(view, 0, 0, 0, view.getWidth() + padding));
					}
					if (pos == 3 || pos == 4) {
						resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding), 0, 0));
					}
				} else {
					if ((pos + 1) % 3 == 0) {
						if ((pos + 1) % 6 == 0) {
							isAniReverse2 = true;
						} else {
							isAniReverse2 = false;
						}
						resultList.add(createTranslationAnimations(view, 0, 0, 0, view.getWidth() + padding));
					} else {
						if (isAniReverse2) {
							resultList.add(createTranslationAnimations(view, 0, (view.getWidth() + padding), 0, 0));
						} else {
							resultList.add(createTranslationAnimations(view, 0, -(view.getWidth() + padding), 0, 0));
						}
					}
				}
			}
			for (int i = oldPosition; i > newPosition; i--) {
				swap(images, i, i - 1);
			}
		}

		hidePosition = newPosition;
		if (resultSet == null) {
			resultSet = new AnimatorSet();
		}
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
					onRefresh();
					resultSet.removeAllListeners();
					resultSet.clone();
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
		public void onItemClick(View view, int position, boolean Photo);
	}

	public void onResume() {
		initUI();
	}

	public void onPause() {
		ImageLoad.getInstance().clearMemoryCache();
	}

	public void onDestroy() {

	}

	int mItemCount = 1;
	boolean isReverse = false;
	int mViewHeight = 0;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//
	}

	private void setViewsLayout(int resWidth, int l, int t) {
		ItemWidth = resWidth / 3 - padding - (padding / 3);
        mItemCount = 1;
        isReverse = false;
		for (int i = 0, size = getChildCount(); i < size; i++) {
			View view = getChildAt(i);
			if (i == 0) {
				mItmeOne = ItemWidth * 2 + padding;
				l += padding;
				t += padding;
				view.layout(l, t, l + mItmeOne, t + mItmeOne);
				l += mItmeOne + padding;
			}
			if (i == 1 || i == 2) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				t += ItemWidth + padding;
			}
			if (i >= 3) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				if (mItemCount % 3 == 0) {
					isReverse = !isReverse;
					t += ItemWidth + padding;
				} else {
					if (isReverse) {
						l += ItemWidth + padding;
					} else {
						l -= ItemWidth + padding;
					}
				}
				mItemCount++;
			}
			if (i == hidePosition) {
				view.setVisibility(View.GONE);
				mStartDragItemView = view;
			}
		}
		if (mViewHeight != t) {
			mViewHeight = t;
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
			resWidth = getSuggestedMinimumWidth();
			resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;
			setViewsLayout(resWidth, 0, 0);
			resHeight = getSuggestedMinimumHeight();
			resHeight = resHeight == 0 ? mViewHeight : resHeight;
		} else {
			resWidth = width;
			setViewsLayout(resWidth, 0, 0);
			resHeight = height;
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
		if (mTopHeight <= 0) {
			mTopHeight = getTopHeight(getContext());
		}
	}

}