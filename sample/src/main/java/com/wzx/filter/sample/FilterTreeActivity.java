package com.wzx.filter.sample;

import java.util.List;

import com.wzx.filter.AllFilterNode;
import com.wzx.filter.FilterGroup;
import com.wzx.filter.FilterNode;
import com.wzx.filter.FilterRoot;
import com.wzx.filter.UnlimitedFilterNode;
import com.wzx.filter.sample.tools.ViewUtils;
import com.wzx.filter.sample.view.FilterTreeView;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class FilterTreeActivity extends AppCompatActivity implements FilterTreeView.OnItemClickListener {

    private FilterTreeView mFilterTreeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_tree);
        mFilterTreeView = (FilterTreeView) findViewById(R.id.filter_tree);
        mFilterTreeView.setLazyLoader(mLazyLoader);
        mFilterTreeView.setOnItemClickListener(this);
        TestFilterRoot root = new TestFilterRoot();
        bindViewConfig(root);

        mFilterTreeView.setFilterGroup(root);
    }

    private FilterTreeView.LazyLoader mLazyLoader = new FilterTreeView.LazyLoader() {

        @Override
        public void lazyLoad(FilterTreeView treeView, FilterGroup group, int position, FilterTreeView.SubTreeLoaderListener listener) {
            new Thread(new OpenTreeTask(group, position, listener)).start();
        }
    };

    private class OpenTreeTask implements Runnable {
        private FilterGroup mFilterGroup;
        private int mPosition;
        FilterTreeView.SubTreeLoaderListener mSubTreeLoaderListener;

        public OpenTreeTask(FilterGroup group,
                            int position, FilterTreeView.SubTreeLoaderListener listener) {
            mFilterGroup = group;
            mPosition = position;
            mSubTreeLoaderListener = listener;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            final boolean result = mFilterGroup.open(null);
            if (result) {
                bindViewConfig(mFilterGroup);
            }
            if (!isFinishing()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (result) {
                            mSubTreeLoaderListener.onLoadSuccess(mFilterGroup, mPosition);
                        } else {
                            mSubTreeLoaderListener.onLoadFail(mFilterGroup, mPosition);
                        }

                    }
                });
            }

        }
    }

    protected void bindViewConfig(FilterGroup group) {
        FilterTreeView.TreeViewConfig config = new FilterTreeView.TreeViewConfig();
        group.setTag(config);

        List<FilterNode> children = group.getChildren(true);
        int childrenCount = children.size();
        boolean hasSetTreeNode = false;
        for (int i = 0; i < childrenCount; i++) {
            FilterNode child = children.get(i);
            if (!hasSetTreeNode && (child instanceof FilterGroup || i == childrenCount - 1)) {
                if (child.isLeaf()) {
                    config.mDividerColor = Color.parseColor("#dddddd");
                    config.mPadding = ViewUtils.dip2px(this, 5);
                    FilterGroup groupParent = (FilterGroup) group.getParent();
                    if (groupParent instanceof FilterRoot) {
                        config.mItemMinHeight = 80;
                    } else {
                        config.mItemMinHeight = 120;
                    }
                } else if (group instanceof FilterRoot) {
                    config.mIsRoot = true;
                    config.mDividerColor = Color.parseColor("#dddddd");
                    config.mWidthWeight = 0.28f;
                    config.mItemMinHeight = 120;
                } else {
                    config.mWidthWeight = 0.4f;
                    config.mItemMinHeight = 120;
                    config.mPadding = ViewUtils.dip2px(this, 5);
                }
                hasSetTreeNode = true;
            }
            if (child instanceof FilterGroup) {
                FilterGroup childGroup = (FilterGroup) child;
                bindViewConfig(childGroup);
            }
        }
    }

    @Override
    public void onLeafItemClick(FilterTreeView treeView, View view, FilterGroup parent, FilterNode node, int position) {
        if (node.isSelected()
                && (node instanceof UnlimitedFilterNode || node instanceof AllFilterNode)) {
            return;
        }
        if (!node.isSelected() || !parent.isSingleChoice()) {
            node.requestSelect(!node.isSelected());
            mFilterTreeView.refresh();
        }
    }

    @Override
    public void onGroupItemClick(FilterTreeView treeView, View view, FilterGroup parent, FilterGroup group, int position) {
        Object tag = group.getTag();
        if (!(tag instanceof FilterTreeView.TreeViewConfig)) {
            bindViewConfig(group);
        }

        treeView.openSubTree(position);
    }
}
