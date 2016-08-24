package com.wzx.filter.view;

import com.wzx.filter.R;
import com.wzx.filter.model.FilterGroup;
import com.wzx.filter.model.FilterNode;
import com.wzx.filter.tools.ViewUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FilterTreeView extends ViewGroup implements OnItemClickListener {

    private FilterGroup mFilterGroup;
    private FilterTreeView mSubTreeView;
    private InternalListView mListView;
    private ProgressBar mProgressBar;
    private FilterListAdapter mFilterListAdapter;

    private int mBorderWidth;
    private Paint mPaint = new Paint();

    private boolean mContainUnlimitedNode = true;
    private boolean mIndicateSelectState = true;

    private TreeViewConfig mTreeViewConfig;

    public FilterTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFilterListAdapter = new FilterListAdapter(context);

        mListView = new InternalListView(context);
        mListView.setSelector(R.drawable.tree_list_selector);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView paramAbsListView,
                                             int paramInt) {
            }

            @Override
            public void onScroll(AbsListView paramAbsListView, int paramInt1,
                                 int paramInt2, int paramInt3) {
                invalidate();
            }
        });
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mFilterListAdapter);

        addView(mListView);
        initInfoPanel();

        mBorderWidth = ViewUtils.dip2px(getContext(), 0.5f);
        mPaint.setColor(0xFFDDDDDD);
        mPaint.setStrokeWidth(mBorderWidth);
    }

    private void initInfoPanel() {
        mProgressBar = new ProgressBar(getContext());
        mProgressBar.setVisibility(View.GONE);

        addView(mProgressBar);
    }

    private void makeAndAddSubTree() {
        mSubTreeView = new FilterTreeView(getContext(), null);
        mSubTreeView.setBackgroundColor(Color.WHITE);

        mSubTreeView.setContainUnlimitedNode(mContainUnlimitedNode);
        mSubTreeView.setIndicateSelectState(mIndicateSelectState);
        mSubTreeView.setLazyLoader(mLazyLoader);
        mSubTreeView.setOnItemClickListener(mOnItemClickListener);

        addView(mSubTreeView);
    }

    public void setContainUnlimitedNode(boolean containUnlimitedNode) {
        mContainUnlimitedNode = containUnlimitedNode;
        if (mSubTreeView != null) {
            mSubTreeView.setContainUnlimitedNode(containUnlimitedNode);
        }
    }

    public void setIndicateSelectState(boolean indicateSelectState) {
        mIndicateSelectState = indicateSelectState;
        if (mSubTreeView != null) {
            mSubTreeView.setIndicateSelectState(indicateSelectState);
        }
    }

    public void refresh() {
        mFilterListAdapter.notifyDataSetChanged();
        if (mSubTreeView != null) {
            mSubTreeView.refresh();
        }
    }

    public void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }

    public void setFilterGroup(FilterGroup group) {
        mFilterGroup = group;
        mTreeViewConfig = mFilterGroup.getTag();

        int dividerColor = mTreeViewConfig.dividerColor;
        if (dividerColor == -1) {
            mListView.setDivider(null);
            mListView.setDividerHeight(0);
        } else {
            ColorDrawable divider = new ColorDrawable(dividerColor);
            mListView.setDivider(divider);
            mListView.setDividerHeight(ViewUtils.dip2px(getContext(), 0.5f));
        }
        int padding = mTreeViewConfig.padding;
        mListView.setPadding(padding, 0, padding, 0);
        mListView.setVisibility(VISIBLE);

        mFilterListAdapter.setFilterGroup(mFilterGroup, mContainUnlimitedNode, mIndicateSelectState);

        int openChildIndex = 0;
        if (mIndicateSelectState) {
            int selectChildPosition = mFilterGroup
                    .getFirstSelectChildPosition(mContainUnlimitedNode);
            openChildIndex = Math.max(0, selectChildPosition);
        }
        openSubTree(openChildIndex);

        int selectionPosition = Math.max(0, openChildIndex - 3);
        mListView.setSelectionPosition(selectionPosition, 0);

        requestLayout();
        invalidate();
    }


    public boolean openSubTree(int position) {
        if (position >= mFilterListAdapter.getCount()) {
            return false;
        }
        FilterNode child = (FilterNode)mFilterListAdapter.getItem(position);
        TreeViewConfig config = mFilterGroup.getTag();
        if (child instanceof FilterGroup && config != null) {
            mFilterListAdapter.setActivePosition(position);

            FilterGroup childGroup = (FilterGroup) child;
            if (mSubTreeView == null) {
                makeAndAddSubTree();
            }
            mSubTreeView.setVisibility(VISIBLE);
            if (childGroup.canOpen() && !childGroup.hasOpened()) {
                mSubTreeView.mListView.setVisibility(GONE);
                if (mSubTreeView.mSubTreeView != null) {
                    mSubTreeView.mSubTreeView.setVisibility(GONE);
                }
                mSubTreeView.mProgressBar.setVisibility(VISIBLE);
                if (mLazyLoader != null) {
                    mLazyLoader.lazyLoad(this, childGroup, position, mSubTreeLoaderListener);
                }
            } else {
                mSubTreeView.setFilterGroup(childGroup);
                mSubTreeView.mProgressBar.setVisibility(View.GONE);
                return true;
            }
        } else if (mSubTreeView != null) {
            mSubTreeView.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int navWidth = width;
        if (mTreeViewConfig != null) {
            navWidth = (int) (width * mTreeViewConfig.widthWeight);
        }

        int childWidth = width - navWidth;
        if (mListView != null) {
            int widthSpec = MeasureSpec.makeMeasureSpec(navWidth,
                    MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(height,
                    MeasureSpec.EXACTLY);
            mListView.measure(widthSpec, heightSpec);
        }

        int pgWidthSpec = MeasureSpec.makeMeasureSpec(
                0, MeasureSpec.UNSPECIFIED);
        int pgHeightSpec = MeasureSpec.makeMeasureSpec(
                0, MeasureSpec.UNSPECIFIED);
        mProgressBar.measure(pgWidthSpec, pgHeightSpec);
        if (mSubTreeView != null) {
            int widthSpec = MeasureSpec.makeMeasureSpec(childWidth,
                    MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(height,
                    MeasureSpec.EXACTLY);
            mSubTreeView.measure(widthSpec, heightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = b - t;
        int navWidth = width;
        if (mTreeViewConfig != null) {
            navWidth = (int) (width * mTreeViewConfig.widthWeight);
        }

        if (mListView != null) {
            mListView.layout(0, 0, navWidth, height);

        }
        if (mSubTreeView != null) {
            mSubTreeView.layout(navWidth, 0, r, height);
        }
        int mProgressBarWidth = mProgressBar.getMeasuredWidth();
        int mProgressBarHeight = mProgressBar.getMeasuredHeight();
        int mStartX = (width - mProgressBarWidth) / 2;
        int startY = (height - mProgressBarHeight) / 2;
        mProgressBar.layout(mStartX, startY, mStartX + mProgressBarWidth,
                startY + mProgressBarHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (mListView.getVisibility() != View.VISIBLE
                || mFilterListAdapter.getCount() <= 0) {
            return;
        }
        int activePosition = mFilterListAdapter.getActivePosition();
        int activeChildIndex = activePosition
                - mListView.getFirstVisiblePosition();
        View child = mListView.getChildAt(activeChildIndex);

        int height = getHeight();
        int width = mListView.getWidth();
        TreeViewConfig config = mFilterGroup.getTag();
        if (child == null) {
            canvas.drawLine(width, 0, width, height, mPaint);
        } else if (config != null && config.isRoot) {
            canvas.drawLine(width, 0, width, child.getTop(), mPaint);
            canvas.drawLine(width, child.getBottom(), width, height, mPaint);
        } else if (config != null){
            int childHeight = child.getBottom() - child.getTop();
            int shapeHeight = ViewUtils.dip2px(getContext(), 10);
            int shapeWidth = ViewUtils.dip2px(getContext(), 7);
            int compensation = (childHeight - shapeHeight) / 2;
            canvas.drawLine(width, 0, width, child.getTop() + compensation,
                    mPaint);
            canvas.drawLine(width, child.getTop() + compensation, width
                    - shapeWidth, child.getTop() + childHeight / 2, mPaint);
            canvas.drawLine(width - shapeWidth, child.getTop() + childHeight
                    / 2, width, child.getBottom() - compensation, mPaint);
            canvas.drawLine(width, child.getBottom() - compensation, width,
                    height, mPaint);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        FilterNode node = (FilterNode)mFilterListAdapter.getItem(position);

        if (node.isLeaf()) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onLeafItemClick(this, view, mFilterGroup, node, position);
            }
        } else if (mFilterListAdapter.getActivePosition() != position) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onGroupItemClick(this, view, mFilterGroup, (FilterGroup) node, position);
            }
        }
    }

    public static class TreeViewConfig {
        public boolean isRoot = false;
        public int dividerColor = -1;
        public float widthWeight = 1.0f;
        public int itemMinHeight = 0;
        public int padding = 0;
    }

    private LazyLoader mLazyLoader;

    public void setLazyLoader(LazyLoader loader) {
        mLazyLoader = loader;
        if (mSubTreeView != null) {
            mSubTreeView.setLazyLoader(mLazyLoader);
        }
    }

    private SubTreeLoaderListener mSubTreeLoaderListener = new SubTreeLoaderListener() {
        @Override
        public void onLoadSuccess(FilterGroup childGroup, int position) {
            if (mFilterGroup.contain(childGroup, true)
                    && mFilterListAdapter != null
                    && mFilterListAdapter.getActivePosition() == position) {
                openSubTree(position);
            }
        }

        @Override
        public void onLoadFail(FilterGroup childGroup, int position) {
            if (mFilterGroup.contain(childGroup, true)
                    && mFilterListAdapter != null
                    && mFilterListAdapter.getActivePosition() == position
                    && mSubTreeView != null) {
                mSubTreeView.mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    public interface LazyLoader {
        void lazyLoad(FilterTreeView treeView, FilterGroup group,
                             int position, SubTreeLoaderListener listener);
    }

    public interface SubTreeLoaderListener {
        void onLoadSuccess(FilterGroup childGroup, int position);

        void onLoadFail(FilterGroup childGroup, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        if (mSubTreeView != null) {
            mSubTreeView.setOnItemClickListener(mOnItemClickListener);
        }
    }

    public static interface OnItemClickListener {
        void onLeafItemClick(FilterTreeView treeView, View view,
                             FilterGroup parent, FilterNode node, int position);

        void onGroupItemClick(FilterTreeView treeView, View view,
                              FilterGroup parent, FilterGroup group, int position);
    }

    private static class InternalListView extends ListView {

        private int mSelectionPosition = -1;
        private int mTop = 0;

        public InternalListView(Context context) {
            super(context);
        }

        public void setSelectionPosition(int position, int top) {
            mSelectionPosition = position;
            mTop = top;
        }

        @Override
        protected void layoutChildren() {
            if (mSelectionPosition >= 0) {
                setSelectionFromTop(mSelectionPosition, mTop);
                mSelectionPosition = -1;
                mTop = 0;
            }
            super.layoutChildren();
        }
    }

}
