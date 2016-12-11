package com.wzx.filter;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wang_zx on 2015/1/29.
 */
public class VirtualFilterRoot extends FilterRoot {

    private Map<String, FilterNode> mChildrenMap = new HashMap<String, FilterNode>();

    @Override
    public void addNode(FilterNode node) {
        mChildren.add(node);
        String type = ((FilterGroup)node).getType();
        if (!TextUtils.isEmpty(type)) {
            mChildrenMap.put(type, node);
        }
    }

    @Override
    public <T extends FilterNode> T getChild(String type) {
        return (T)mChildrenMap.get(type);
    }

    @Override
    public boolean isSelected() {
        for (FilterNode child : mChildren) {
            if (child.isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized List<FilterNode> getChildren(boolean containUnlimited) {
        return getChildren(containUnlimited, true);
    }

    protected List<FilterNode> getChildren(boolean needUnlimited, boolean needEmptyGroup) {
        List<FilterNode> children = new ArrayList<FilterNode>(super.getChildren(needUnlimited));
        int childrenCount = children.size();
        for (int i = childrenCount - 1; i >= 0; i--) {
            FilterNode child = children.get(i);
            if (child instanceof FilterGroup) {
                FilterGroup group = (FilterGroup)child;
                if (!needEmptyGroup && group.isEmpty(false)) {
                    children.remove(i);
                }
            }
        }
        return children;
    }

    @Override
    public void resetFilterTree(boolean force) {
        super.resetFilterTree(true);
    }
}
