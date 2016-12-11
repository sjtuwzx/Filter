package com.wzx.filter;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wang_zx on 2015/1/6.
 * 筛选树root
 */
public class FilterRoot extends FilterGroup {

    private Map<String, FilterNode> mChildrenMap = new HashMap<String, FilterNode>();

    @Override
    public synchronized void addNode(FilterNode node) {
        super.addNode(node);
        if (node instanceof FilterGroup) {
            FilterGroup group = (FilterGroup)node;
            String type = group.getType();
            if (!TextUtils.isEmpty(type)) {
                mChildrenMap.put(type, node);
            }
        }
    }

    /**
     * 根据type获取节点
     * @param type 节点类型
     * @return 符合type的节点 or null
     */
    public synchronized <T extends FilterNode> T getChild(String type) {
        return (T)mChildrenMap.get(type);
    }

    @Override
    public synchronized void requestSelect(FilterNode trigger, boolean selected) {
        if (!(trigger instanceof UnlimitedFilterNode)) {
            refreshSelectState(trigger, selected);
        }
    }

    /**
     * 清空所有选中状态
     */
    public synchronized void resetFilterTree(boolean force) {
        if (force) {
            resetFilterGroup();
        } else {
            forceSelect(false);
        }
    }

    /**
     * 保存当前筛选状态
     */
    @Override
    public synchronized void save() {
        for (FilterNode child : mChildren) {
            FilterGroup group = (FilterGroup)child;
            group.save();
        }
    }

    /**
     * 恢复先前筛选状态
     */
    @Override
    public synchronized void restore() {
        for (FilterNode child : mChildren) {
            FilterGroup group = (FilterGroup)child;
            group.restore();
        }
    }

    /**
     * 丢弃先前保存的筛选状态
     */
    @Override
    public synchronized void discardHistory() {
        for (FilterNode child : mChildren) {
            FilterGroup group = (FilterGroup)child;
            group.discardHistory();
        }
    }

    public synchronized void submit() {
        removeUnselectedInvisibleNode();
    }

    /**
     * 是否当前筛选状态与上次save时状态发生改变
     * @return 是否改变
     */
    @Override
    public synchronized boolean hasFilterChanged() {
        for (FilterNode child : mChildren) {
            FilterGroup group = (FilterGroup)child;
            if (group.hasFilterChanged()) {
                return true;
            }
        }
        return false;
    }

    private Object mOpenLock = new Object();

    @Override
    public boolean open(FilterGroupOpenListener listener) {
        synchronized (mOpenLock) {
            if (listener != null) {
                listener.onOpenStart(this);
            }
            if (!mHasOpened) {
                boolean hasOpened = true;
                for (FilterNode child : mChildren) {
                    if (child instanceof FilterGroup) {
                        FilterGroup group = (FilterGroup) child;
                        if (group.canOpen() && !group.hasOpened()) {
                            hasOpened &= group.open(listener);
                        }
                    }
                }
                mHasOpened = hasOpened;
            }
            if (listener != null) {
                if (mHasOpened) {
                    listener.onOpenSuccess(this);
                } else {
                    listener.onOpenFail(this, "");
                }
            }
            return mHasOpened;
        }
    }
}
