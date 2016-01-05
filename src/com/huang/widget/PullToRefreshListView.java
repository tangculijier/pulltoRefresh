package com.huang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huang.pulltorefresh.R;
public class PullToRefreshListView extends ListView implements OnScrollListener
{
	
	private RefreshStatus mCurrentRefreshStatus;
	public enum RefreshStatus
	{
		TAP_TO_REFRESH,PULL_TO_REFRESH,REFRESHING,RELEASE_TO_REFRESH
	}

	private RelativeLayout mheader;
	private ProgressBar mheaderProgress;
	private ImageView mheaderImageView;
	private TextView mheaderTextView;

	private int lastTouchDown = 0;
	
	private OnRefreshListener onRefreshListener;
	
	public void setOnRefreshListener(OnRefreshListener onRefreshListener)
	{
		this.onRefreshListener = onRefreshListener;
	}
	
	private int mheaderOriginalTopPadding;
	private int mheaderHeight;
	
	private RotateAnimation mUpReverseAnimation;
	private RotateAnimation mDownReverseAnimation;
	
	public PullToRefreshListView(Context context)
	{
		this(context,null);
	}
	public PullToRefreshListView(Context context, AttributeSet attrs)
	{
		this(context, attrs,-1);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs,
			int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	

	private void init(Context context)
	{
	  	 Log.d("huang", "init");
	  	mCurrentRefreshStatus = RefreshStatus.TAP_TO_REFRESH;
		setOnScrollListener(this);
		
		LayoutInflater minflater =  LayoutInflater.from(context);  
		mheader = (RelativeLayout)(minflater.inflate(R.layout.pull_to_refresh_header,this,false));
		addHeaderView(mheader);
		
		mheaderOriginalTopPadding = mheader.getPaddingTop();
		mheaderProgress = (ProgressBar) mheader.findViewById(R.id.header_progressBar);
		mheaderImageView = (ImageView) mheader.findViewById(R.id.header_image);
		mheaderTextView = (TextView) mheader.findViewById(R.id.header_text);
		
		measureHeader(mheader);
		mheaderHeight = mheader.getMeasuredHeight();
		Log.d("huang", "mRefreshViewHeight=" + mheaderHeight);

		
		//init animation
		mUpReverseAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mUpReverseAnimation.setInterpolator(new LinearInterpolator());
		mUpReverseAnimation.setDuration(250);
		mUpReverseAnimation.setFillAfter(true);
		
		mDownReverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mDownReverseAnimation.setInterpolator(new LinearInterpolator());
		mDownReverseAnimation.setDuration(250);
		mDownReverseAnimation.setFillAfter(true);
		
	}
	
	private void measureHeader(View child)
	{
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null)
		{
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0)
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
	@Override
	protected void onAttachedToWindow()
	{
	  	 Log.d("huang", "onAttachedToWindow");
		super.onAttachedToWindow();
		setSelection(1);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		int touchY = (int) ev.getY();
	 	// Log.d("huang", "ACTION_UP"+mheader.getTop());
		switch(ev.getAction())
		{
		case MotionEvent.ACTION_UP:
			//header回滚
			
		//	resetHeaderPadding();
		//	Log.d("huang", "mheader.getBottom()"+(mheader.getBottom() ) +"");
		//	Log.d("huang", "mheader.gettop"+(mheader.getTop() ) +"");
		//	Log.d("huang", getFirstVisiblePosition()+"");
			if (getFirstVisiblePosition() == 0 && mCurrentRefreshStatus != RefreshStatus.REFRESHING)
			{
				if(mheader.getBottom() >= mheaderHeight)
				{
					Log.d("huang", "refesh");
					//refesh
					mCurrentRefreshStatus = RefreshStatus.REFRESHING;
					resetHeaderPadding();
					prepareForRefresh();
					onRefresh();
					
				}
				else if(mheader.getBottom() < mheaderHeight || mheader.getTop() <= 0)   //如果拖的距离不够  或者 取消拖动
				{
					Log.d("huang", "cancel refesh");
					//cancel
					setSelection(1);
				}
			}
		
		 
			break;
		case MotionEvent.ACTION_DOWN:
			lastTouchDown = touchY;
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.d("huang", "ACTION_MOVE"+touchY+"lastTouchDown="+lastTouchDown);
			
			 if(touchY > lastTouchDown) 
			 {
				// getHistorySize has been available since API 1
					int pointerCount = ev.getHistorySize();

					for (int p = 0; p < pointerCount; p++)
					{
//						if (mRefreshState == RELEASE_TO_REFRESH)
//						{
							if (isVerticalFadingEdgeEnabled())
							{
								setVerticalScrollBarEnabled(false);
							}

							int historicalY = (int) ev.getHistoricalY(p);

							// Calculate the padding to apply, we divide by 1.7 to
							// simulate a more resistant effect during pull.
							int topPadding = (int) (((historicalY - lastTouchDown) - mheaderHeight) / 1.7);

							mheader.setPadding(mheader.getPaddingLeft(),
									topPadding, mheader.getPaddingRight(),
									mheader.getPaddingBottom());
					}
			 }
		
			break;
			default:
				break;
		
		}
		return super.onTouchEvent(ev);
	}
	
	
	

	

	private void prepareForRefresh()
	{
		Log.d("huang", "prepareForRefresh");
		mheaderImageView.setVisibility(View.GONE);
		mheaderProgress.setVisibility(View.VISIBLE);
		mheaderTextView.setText("loading...");
	
		
	}
	private void onRefresh()
	{
		if(onRefreshListener != null)
		{
			onRefreshListener.onRefresh();
		}
		
	}
	
	public void onRefreshComplete()
	{
		
		// reset header
		resetHeader();
		//invalidateViews();
		setSelection(1);
		
	}
	private void resetHeader()
	{
		if (mCurrentRefreshStatus != RefreshStatus.TAP_TO_REFRESH)
		{
			mCurrentRefreshStatus = RefreshStatus.TAP_TO_REFRESH;
			mheaderImageView.setVisibility(View.VISIBLE);
			mheaderImageView.clearAnimation();
			mheaderProgress.setVisibility(View.GONE);
			mheaderTextView.setText("Tap to refresh...");
			resetHeaderPadding();
		}
		
	}
	
	private void resetHeaderPadding()
	{
		mheader.setPadding(mheader.getPaddingLeft(),
				mheaderOriginalTopPadding, mheader.getPaddingRight(),
					mheader.getPaddingBottom()); 
		
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		 switch (scrollState) {
         case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        	 //Log.d("huang", "SCROLL_STATE_TOUCH_SCROLL");
             // 手指触屏拉动准备滚动，只触发一次        顺序: 1
             break;
         case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
        	 //Log.d("huang", "SCROLL_STATE_FLING");
             // 持续滚动开始，只触发一次                顺序: 2
             break;
         case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
        	// Log.d("huang", "SCROLL_STATE_IDLE");
             // 整个滚动事件结束，只触发一次            顺序: 4
             break;
         default:
             break;
     }
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount)
	{
		
		if(mCurrentRefreshStatus != RefreshStatus.REFRESHING)
		{
			if(firstVisibleItem == 0)
			{	
				if(mheader != null)
				{
					if((mheader.getBottom() < mheaderHeight + 20)
						&& mCurrentRefreshStatus != RefreshStatus.PULL_TO_REFRESH)
					{
						Log.d("huang", "Pull to refresh...");
						mheaderTextView.setText("Pull to refresh...");
						mheaderImageView.clearAnimation();
						mheaderImageView.startAnimation(mDownReverseAnimation);
						mCurrentRefreshStatus = RefreshStatus.PULL_TO_REFRESH;
					}
					else if ((mheader.getBottom() >= mheaderHeight + 20 )
							&& mCurrentRefreshStatus != RefreshStatus.RELEASE_TO_REFRESH)
					{
						Log.d("huang", "Release to refresh...");
						mheaderTextView.setText("Release to refresh...");
						mheaderImageView.clearAnimation();
						mheaderImageView.startAnimation(mUpReverseAnimation);
						mCurrentRefreshStatus = RefreshStatus.RELEASE_TO_REFRESH;
					}
				}
			
			}
		}
	}
	
	public interface OnRefreshListener
	{
		public void onRefresh();
	}

}
