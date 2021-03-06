package com.andview.refreshview;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.andview.refreshview.listener.OnTopRefreshTime;
import com.andview.refreshview.utils.LogUtils;

public class XRefreshContentView implements OnScrollListener, OnTopRefreshTime,
		OnBottomLoadMoreTime {
	private View child;
	@SuppressWarnings("unused")
	private XRefreshViewType childType = XRefreshViewType.NONE;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	private OnTopRefreshTime mTopRefreshTime;
	private OnBottomLoadMoreTime mBottomLoadMoreTime;
	private XRefreshView mContainer;
	private OnScrollListener mScrollListener;
	private XRefreshViewListener mRefreshViewListener;
	private RecyclerView.OnScrollListener mOnScrollListener;
	protected LAYOUT_MANAGER_TYPE layoutManagerType;

	private int mVisibleItemCount = 0;
	private int previousTotal = 0;
	private int mFirstVisibleItem;
	private int lastVisibleItemPosition;
	private boolean mIsLoadingMore;

	public void setContentViewLayoutParams(boolean isHeightMatchParent,
			boolean isWidthMatchParent) {
		LinearLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if (isHeightMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		if (isWidthMatchParent) {
			lp.height = LayoutParams.MATCH_PARENT;
		}
		// 默认设置宽高为match_parent
		child.setLayoutParams(lp);
	}

	public void setContentView(View child) {
		this.child = child;
	}

	public View getContentView() {
		return child;
	}

	public void setContainer(XRefreshView container) {
		mContainer = container;
	}

	public void scrollToTop() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			absListView.setSelection(0);
		} else {
			child.scrollTo(0, 0);
		}
	}

	public void setScrollListener() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			absListView.setOnScrollListener(this);
		} else if (isRecyclerView()) {
			final RecyclerView recyclerView = (RecyclerView) child;
			recyclerView.removeOnScrollListener(mOnScrollListener);

			mOnScrollListener = new RecyclerView.OnScrollListener() {
				private int[] lastPositions;

				@Override
				public void onScrollStateChanged(RecyclerView recyclerView,
						int newState) {
					super.onScrollStateChanged(recyclerView, newState);
				}

				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					RecyclerView.LayoutManager layoutManager = null;
					if (layoutManager == null) {
						layoutManager = recyclerView.getLayoutManager();
					}
					if (layoutManagerType == null) {
						if (layoutManager instanceof GridLayoutManager) {
							layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
						} else if (layoutManager instanceof LinearLayoutManager) {
							layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
						} else if (layoutManager instanceof StaggeredGridLayoutManager) {
							layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
						} else {
							throw new RuntimeException(
									"Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
						}
					}
					switch (layoutManagerType) {
					case LINEAR:
						mVisibleItemCount = layoutManager.getChildCount();
						mTotalItemCount = layoutManager.getItemCount();
					case GRID:
						lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
								.findLastVisibleItemPosition();
						mFirstVisibleItem = ((LinearLayoutManager) layoutManager)
								.findFirstVisibleItemPosition();
						break;
					case STAGGERED_GRID:
						StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
						if (lastPositions == null)
							lastPositions = new int[staggeredGridLayoutManager
									.getSpanCount()];

						staggeredGridLayoutManager
								.findLastVisibleItemPositions(lastPositions);
						lastVisibleItemPosition = findMax(lastPositions);

						staggeredGridLayoutManager
								.findFirstVisibleItemPositions(lastPositions);
						mFirstVisibleItem = findMin(lastPositions);
						break;
					}
					if (mIsLoadingMore) {
						// todo: there are some bugs needs to be adjusted for
						// admob adapter
						if (mTotalItemCount > previousTotal) {
							mIsLoadingMore = false;
							previousTotal = mTotalItemCount;
						}
					}
					if (!mIsLoadingMore
							&& (mTotalItemCount - mVisibleItemCount) <= mFirstVisibleItem) {
						// todo: there are some bugs needs to be adjusted for
						// admob adapter
						if (mRefreshViewListener != null) {
							mRefreshViewListener.onRecyclerViewLoadMore(
									recyclerView.getAdapter().getItemCount(),
									lastVisibleItemPosition);
						}
						mIsLoadingMore = true;
						previousTotal = mTotalItemCount;
					}
				}
			};

			recyclerView.addOnScrollListener(mOnScrollListener);
			// UltimateViewAdapter adapter = (UltimateViewAdapter)
			// recyclerView.getAdapter();
			// if (adapter != null && adapter.getCustomLoadMoreView() == null)
			// adapter.setCustomLoadMoreView(LayoutInflater.from(getContext())
			// .inflate(R.layout.bottom_progressbar, null));
		}
	}

	public void setOnScrollListener(OnScrollListener listener) {
		mScrollListener = listener;
	}

	public void setXRefreshViewListener(XRefreshViewListener refreshViewListener) {
		mRefreshViewListener = refreshViewListener;
	}

	public boolean isTop() {
		if (mTopRefreshTime != null) {
			return mTopRefreshTime.isTop();
		}
		return hasChildOnTop();
	}

	public boolean isBottom() {
		if (mBottomLoadMoreTime != null) {
			return mBottomLoadMoreTime.isBottom();
		}
		return hasChildOnBottom();
	}

	/**
	 * 设置顶部监听
	 * 
	 * @param topListener
	 */
	public void setOnTopRefreshTime(OnTopRefreshTime topRefreshTime) {
		this.mTopRefreshTime = topRefreshTime;
	}

	/**
	 * 设置底部监听
	 * 
	 * @param mRefreshBase
	 */
	public void setOnBottomLoadMoreTime(OnBottomLoadMoreTime bottomLoadMoreTime) {
		this.mBottomLoadMoreTime = bottomLoadMoreTime;
	}

	public void setRefreshViewType(XRefreshViewType type) {
		this.childType = type;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mContainer != null
				&& scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& mTotalItemCount - 1 == view.getLastVisiblePosition()) {
			mContainer.invoketLoadMore();
		}
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public int getTotalItemCount() {
		return mTotalItemCount;
	}

	public boolean hasChildOnTop() {
		return !canChildPullDown();
	}

	public boolean hasChildOnBottom() {
		return !canChildPullUp();
	}

	/**
	 * @return Whether it is possible for the child view of this layout to
	 *         scroll up. Override this if the child view is a custom view.
	 */
	public boolean canChildPullDown() {
		if (child instanceof AbsListView) {
			final AbsListView absListView = (AbsListView) child;
			return canScrollVertically(child, -1)
					|| absListView.getChildCount() > 0
					&& (absListView.getFirstVisiblePosition() > 0 || absListView
							.getChildAt(0).getTop() < absListView
							.getPaddingTop());
		} else {
			return canScrollVertically(child, -1) || child.getScrollY() > 0;
		}
	}

	public boolean canChildPullUp() {
		if (child instanceof AbsListView) {
			AbsListView absListView = (AbsListView) child;
			return canScrollVertically(child, 1)
					|| absListView.getLastVisiblePosition() != mTotalItemCount - 1;
		} else if (child instanceof WebView) {
			WebView webview = (WebView) child;
			return canScrollVertically(child, 1)
					|| webview.getContentHeight() * webview.getScale() != webview
							.getHeight() + webview.getScrollY();
		} else if (child instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) child;
			View childView = scrollView.getChildAt(0);
			if (childView != null) {
				return canScrollVertically(child, 1)
						|| scrollView.getScrollY() != childView.getHeight()
								- scrollView.getHeight();
			}
		} else {
			return canScrollVertically(child, 1);
		}
		return true;
	}

	/**
	 * 用来判断view在竖直方向上能不能向上或者向下滑动
	 * 
	 * @param view
	 *            v
	 * @param direction
	 *            方向 负数代表向上滑动 ，正数则反之
	 * @return
	 */
	public boolean canScrollVertically(View view, int direction) {
		return ViewCompat.canScrollVertically(view, direction);
	}

	public void offsetTopAndBottom(int offset) {
		child.offsetTopAndBottom(offset);
	}

	public boolean isRecyclerView() {
		if (null != child && child instanceof RecyclerView) {
			return true;
		}
		return false;
	}

	private int findMax(int[] lastPositions) {
		int max = Integer.MIN_VALUE;
		for (int value : lastPositions) {
			if (value > max)
				max = value;
		}
		return max;
	}

	private int findMin(int[] lastPositions) {
		int min = Integer.MAX_VALUE;
		for (int value : lastPositions) {
			if (value != RecyclerView.NO_POSITION && value < min)
				min = value;
		}
		return min;
	}

	public static enum LAYOUT_MANAGER_TYPE {
		LINEAR, GRID, STAGGERED_GRID
	}

}
