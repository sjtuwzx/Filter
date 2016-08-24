package com.wzx.filter.sample;

import java.util.List;

import com.wzx.filter.R;
import com.wzx.filter.model.AllFilterNode;
import com.wzx.filter.model.FilterGroup;
import com.wzx.filter.model.FilterNode;
import com.wzx.filter.model.FilterRoot;
import com.wzx.filter.model.UnlimitedFilterNode;
import com.wzx.filter.tools.ViewUtils;
import com.wzx.filter.view.FilterTreeView;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class FilterTreeActivity extends Activity implements FilterTreeView.OnItemClickListener {

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
        int N = children.size();
        boolean hasSetTreeNode = false;
        for (int i = 0; i < N; i++) {
            FilterNode child = children.get(i);
            if (!hasSetTreeNode && (child instanceof FilterGroup || i == N - 1)) {
                if (child.isLeaf()) {
                    config.dividerColor = Color.parseColor("#dddddd");
                    config.padding = ViewUtils.dip2px(this, 5);
                    FilterGroup groupParent = (FilterGroup)group.getParent();
                    if (groupParent instanceof FilterRoot) {
                        config.itemMinHeight = 80;
                    } else {
                        config.itemMinHeight = 120;
                    }
                } else if (group instanceof FilterRoot) {
                    config.isRoot = true;
                    config.dividerColor = Color.parseColor("#dddddd");
                    config.widthWeight = 0.28f;
                    config.itemMinHeight = 120;
                } else {
                    config.widthWeight = 0.4f;
                    config.itemMinHeight = 120;
                    config.padding = ViewUtils.dip2px(this, 5);
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
