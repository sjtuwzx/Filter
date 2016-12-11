package com.wzx.filter;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wang_zx on 2015/1/6.
 * 筛选节点
 */
public class FilterNode {

    //展示名称
    private String mDisplayName;

    //特征code，用于数据关联
    private String mCharacterCode = "";

    private Set<String> mMutexCodes = new HashSet<String>();

    private FilterParent mParent;
    boolean mIsSelected = false;

    private Object mData;

    private Object mTag;

    /**
     * 设置展示名称
     * @param displayName 展示名称
     */
    public final void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    /**
     * 获取展示名称
     * @return 展示名称
     */
    public final String getDisplayName() {
        return mDisplayName;
    }

    /**
     * 设置特征code，用于节点选中及关联
     * @param characterCode 特征code
     */
    public final void setCharacterCode(String characterCode) {
        mCharacterCode = characterCode;
    }

    /**
     * 获取特征code
     * @return 特征code
     */
    public final String getCharacterCode() {
        return mCharacterCode;
    }

    /**
     * 设置节点关联的数据
     * @param data 与节点关联的数据
     */
    public void setData(Object data) {
        mData = data;
    }

    /**
     * 获取节点关联的数据
     * @return 与节点关联的数据
     */
    public <T> T getData() {
        return (T)mData;
    }

    /**
     * 设置与节点关联的tag
     * @param tag 与节点关联的tag
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * 获取与节点关联的tag
     * @return 与节点关联的tag
     */
    public <T> T getTag() {
        return (T)mTag;
    }

    /**
     * 设置节点的parent
     * @param parent 节点的父节点
     */
    public final void setParent(FilterParent parent) {
        mParent = parent;
    }

    /**
     * 获取节点的parent
     * @return 节点的父节点
     */
    public final FilterParent getParent() {
        return mParent;
    }

    /**
     * 是否叶子节点
     * @return 是否叶子节点
     */
    public boolean isLeaf() {
        return true;
    }

    /**
     * 请求root刷新整个筛选树状态
     * @param selected 是否选中
     */
    public void requestSelect(boolean selected) {
        if (mIsSelected != selected && mParent != null) {
            mParent.requestSelect(this, selected);
        }
    }

    /**
     * 刷新该节点及其下层节点状态
     * @param selected 是否选中
     * @return 是否新选中
     */
    public boolean forceSelect(boolean selected) {
        return setSelected(selected);
    }

    /**
     * 刷新该节点状态
     * @param selected 是否选中
     * @return 是否新选中
     */
    protected boolean setSelected(boolean selected) {
        if (mIsSelected != selected) {
            mIsSelected = selected;
            if (mOnSelectChangeListener != null) {
                mOnSelectChangeListener.onSelectChange(this, selected);
            }
            return selected;
        }
        return false;
    }

    /**
     * 是否选中
     * @return 是否选中
     */
    public boolean isSelected() {
        return mIsSelected;
    }

    /**
     * 根据触发节点刷新该节点及下层节点状态
     * @param trigger 触发刷新节点
     * @param selected 是否选中
     * @return 是否新选中
     */
    protected boolean refreshSelectState(FilterNode trigger, boolean selected) {
        if (trigger.isEquals(this)) {
            return forceSelect(selected);
        } else if (selected && isExclusive(trigger)) {
            forceSelect(false);
        }
        return false;
    }

    /**
     * 添加互斥code，用于节点间互斥
     * @param mutexCode 互斥code
     */
    public void addMutexCode(String mutexCode) {
        mMutexCodes.add(mutexCode);
    }

    private boolean isExclusive(FilterNode node) {
        return !TextUtils.isEmpty(node.mCharacterCode) && mMutexCodes.contains(node.mCharacterCode);
    }

    public boolean isEquals(Object o) {
        if (o instanceof  FilterNode) {
            FilterNode right = (FilterNode)o;
            if (TextUtils.isEmpty(mCharacterCode) || TextUtils.isEmpty(right.mCharacterCode)) {
                return this == right;
            }
            return mCharacterCode.equals(right.mCharacterCode);
        }
        return false;
    }

    public boolean contain(FilterNode node, boolean accordingReference) {
        return (accordingReference && this == node) || (!accordingReference && isEquals(node));
    }

    public FilterNode findNode(FilterNode node, boolean accordingReference) {
        if (accordingReference && this == node || !accordingReference && isEquals(node)) {
            return this;
        }
        return null;
    }

    private OnSelectChangeListener mOnSelectChangeListener;

    /**
     * 设置节点选中状态变化监听器
     * @param listener
     */
    public void setOnSelectChangeListener(OnSelectChangeListener listener) {
        mOnSelectChangeListener = listener;
    }

    public interface OnSelectChangeListener {
        /**
         * 回调当节点选中状态变化时
         * @param node 被监听节点
         * @param selected 是否选中
         */
        void onSelectChange(FilterNode node, boolean selected);
    }
}
