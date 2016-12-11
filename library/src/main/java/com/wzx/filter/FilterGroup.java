package com.wzx.filter;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wang_zx on 2015/1/6.
 * 筛选集合节点
 */
public class FilterGroup extends FilterNode implements FilterParent {

    private static final String TAG = FilterGroup.class.getSimpleName();

    protected List<FilterNode> mChildren = new ArrayList<FilterNode>();

    protected String mType;

    //TODO 单选节点下子节点不支持数据关联
    private boolean mSingleChoice = false;

    protected boolean mHasOpened = false;

    protected List<FilterNode> mHistorySelectList;

    /**
     * 添加节点
     *
     * @param node 新增节点
     */
    public synchronized void addNode(FilterNode node) {
        node.setParent(this);
        mChildren.add(node);
    }

    /**
     * 删除节点
     *
     * @param node 被删除节点
     */
    @Override
    public synchronized void remove(FilterNode node) {
        if (mChildren.remove(node)) {
                node.setParent(null);
        }
    }

    /**
     * 获取所有子节点
     *
     * @param containUnlimited 是否包含不限节点
     * @return 所有子节点
     */
    public synchronized List<FilterNode> getChildren(boolean containUnlimited) {
        List<FilterNode> children = new ArrayList<FilterNode>(mChildren);
        int childrenCount = children.size();
        for (int i = childrenCount - 1; i >= 0; i--) {
            FilterNode child = children.get(i);
            if (child instanceof InvisibleFilterNode || (!containUnlimited && child instanceof UnlimitedFilterNode)) {
                children.remove(i);
            }
        }
        return children;
    }

    /**
     * 是否该节点为空
     *
     * @param containUnlimited 是否包含不限节点
     * @return 是否该节点为空
     */
    public synchronized boolean isEmpty(boolean containUnlimited) {
        List<FilterNode> children = getChildren(containUnlimited);
        return children.isEmpty();
    }

    /**
     * 设置FilterGroup类型
     *
     * @param type FilterGroup类型
     */
    public void setType(String type) {
        mType = type;
    }

    /**
     * 获取FilterGroup类型
     *
     * @return FilterGroup类型
     */
    public String getType() {
        return mType;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public synchronized void addSelectNode(FilterNode node) {
        if (!contain(node)) {
            InvisibleFilterNode invisibleNode = new InvisibleFilterNode(node);
            dispatchUnknownNode(invisibleNode);
        }
        requestSelect(node, true);
    }

    protected void dispatchUnknownNode(FilterNode node) {
        addNode(node);
    }

    public synchronized void removeUnselectedInvisibleNode() {
        int childrenCount = mChildren.size();
        for (int i = childrenCount - 1; i >= 0; i--) {
            FilterNode child = mChildren.get(i);
            if (child instanceof FilterGroup) {
                FilterGroup group = (FilterGroup) child;
                group.removeUnselectedInvisibleNode();
            } else if (child instanceof InvisibleFilterNode && !child.isSelected()) {
                remove(child);
            }
        }
    }

    @Override
    public synchronized boolean forceSelect(boolean selected) {
        if (mIsSelected && !selected) {
            for (FilterNode child : mChildren) {
                if (child instanceof UnlimitedFilterNode) {
                    child.setSelected(true);
                } else {
                    child.forceSelect(false);
                }
            }
        }
        return super.setSelected(selected);
    }

    @Override
    protected synchronized boolean setSelected(boolean selected) {
        if (mIsSelected && !selected) {
            for (FilterNode child : mChildren) {
                if (child instanceof UnlimitedFilterNode) {
                    child.setSelected(true);
                }
            }
        }
        return super.setSelected(selected);

    }

    private synchronized List<FilterNode> getTriggerFirstChildren(FilterNode trigger) {
        if (contain(trigger, true)) {
            List<FilterNode> children = new ArrayList<FilterNode>(mChildren);
            int childrenCount = children.size();
            for (int i = 0; i < childrenCount; i++) {
                FilterNode child = children.get(i);
                if (child.contain(trigger, true)) {
                    children.remove(child);
                    children.add(0, child);
                    break;
                }
            }
            return children;
        } else {
            return mChildren;
        }
    }

    /**
     * 设置为单选节点
     */
    public void setSingleChoice() {
        mSingleChoice = true;
    }

    /**
     * 是否单选
     *
     * @return 是否单选
     */
    public boolean isSingleChoice() {
        return mSingleChoice;
    }

    @Override
    public synchronized void requestSelect(FilterNode trigger, boolean selected) {
        if (trigger instanceof UnlimitedFilterNode) {
            if (selected) {
                List<FilterNode> selectedLeafNodes = getSelectedLeafNodes();
                for (FilterNode child : selectedLeafNodes) {
                    child.requestSelect(false);
                }
                trigger.setSelected(true);
            }
            return;
        } else if (trigger instanceof AllFilterNode && selected) {
            FilterParent triggerParent = trigger.getParent();
            if (triggerParent == this) {
                List<FilterNode> selectedLeafNodes = getSelectedLeafNodes();
                for (FilterNode node : selectedLeafNodes) {
                    node.requestSelect(false);
                }
            }
        }

        FilterParent parent = getParent();
        if (parent != null) {
            parent.requestSelect(trigger, selected);
        }

    }

    @Override
    protected synchronized boolean refreshSelectState(FilterNode trigger, boolean selected) {
        if (mSingleChoice) {
            List<FilterNode> selectedChildrenList = getSelectedChildren();
            //单选节点中可能包含关联节点，这时以触发节点为主，优先选中
            List<FilterNode> children = getTriggerFirstChildren(trigger);
            for (FilterNode child : children) {
                boolean newSelected = child.refreshSelectState(trigger, selected);
                //children已排序，正常点击情况下第一个必为reference；关键字联想则取第一个关联节点
                if (newSelected || child.contain(trigger, false)) {
                    if (newSelected && !selectedChildrenList.isEmpty()) {
                        FilterNode node = selectedChildrenList.get(0);
                        //TODO 单选节点下子节点不支持数据关联
                        if (node instanceof FilterGroup) {
                            FilterGroup group = (FilterGroup)node;
                            List<FilterNode> selectLeafNodes = group.getSelectedLeafNodes();
                            for (FilterNode selectNode : selectLeafNodes) {
                                selectNode.requestSelect(false);
                            }
                        } else {
                            node.requestSelect(false);
                        }
                    }
                    break;
                }
            }
        } else {
            FilterNode allNode = findAllNode();
            //需支持筛选历史中全部节点恢复
            boolean shouldUnSelectAllNode = !(trigger instanceof AllFilterNode) && contain(trigger) && !trigger.isEquals(allNode);
            for (FilterNode child : mChildren) {
                if (shouldUnSelectAllNode && child instanceof AllFilterNode) {
                    child.setSelected(false);
                } else {
                    child.refreshSelectState(trigger, selected);
                }
            }
        }
        boolean isSelected = mIsSelected;
        int selectedChildrenCount = getSelectedChildrenCount();
        if (selectedChildrenCount > 0) {
            FilterNode unlimitedNode = findUnlimitedNode();
            if (unlimitedNode != null) {
                unlimitedNode.setSelected(false);
            }
            //不必再刷新children状态
            setSelected(true);
        } else {
            setSelected(false);
        }

        return !isSelected && mIsSelected;
    }

    /**
     * 查找“不限”节点
     *
     * @return “不限”节点 or null
     */
    public synchronized FilterNode findUnlimitedNode() {
        for (FilterNode child : mChildren) {
            if (child instanceof UnlimitedFilterNode) {
                return child;
            }
        }
        return null;
    }

    /**
     * 查找“全部”节点
     * @return “全部”节点 or null
     */
    private synchronized FilterNode findAllNode() {
        for (FilterNode child : mChildren) {
            if (child instanceof AllFilterNode) {
                return child;
            }
        }
        return null;
    }

    /**
     * 获取所有选中子节点
     *
     * @return 所有选中子节点列表
     */
    public synchronized List<FilterNode> getSelectedChildren() {
        List<FilterNode> selectedChildrenList = new ArrayList<FilterNode>();
        for (FilterNode child : mChildren) {
            if (!(child instanceof UnlimitedFilterNode) && child.isSelected()) {
                selectedChildrenList.add(child);
            }
        }
        return selectedChildrenList;
    }

    /**
     * 获取选中子节点个数
     *
     * @return 选中子节点个数
     */
    public synchronized int getSelectedChildrenCount() {
        List<FilterNode> selectedChildrenList = getSelectedChildren();
        return selectedChildrenList.size();
    }

    /**
     * 获取第一个选择的child position
     * @param containUnlimited 是否包含不限节点
     * @return 第一个选择的child position
     */
    public synchronized int getFirstSelectChildPosition(boolean containUnlimited) {
        List<FilterNode> children = getChildren(containUnlimited);
        int childrenCount = children.size();
        for (int i = 0; i < childrenCount; i++) {
            FilterNode child = children.get(i);
            if (child.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取下层所有选中叶子节点
     *
     * @return 下层所有选中叶子节点列表
     */
    public synchronized List<FilterNode> getSelectedLeafNodes() {
        List<FilterNode> selectLeafNodeList = new ArrayList<FilterNode>();
        for (FilterNode child : mChildren) {
            if (child.isSelected()) {
                if (child instanceof FilterGroup) {
                    FilterGroup group = (FilterGroup) child;
                    List<FilterNode> childSelectLeafNodeList = group.getSelectedLeafNodes();
                    selectLeafNodeList.addAll(childSelectLeafNodeList);
                } else if (!(child instanceof UnlimitedFilterNode)) {
                    selectLeafNodeList.add(child);
                }
            }
        }

        //去除重复节点，从头开始遍历，保证前位节点优先级最高
        Set<String> selectedCharacterCodes = new HashSet<String>();
        for (int i = 0; i < selectLeafNodeList.size(); i++) {
            FilterNode node = selectLeafNodeList.get(i);
            String characterCode = node.getCharacterCode();
            if (!TextUtils.isEmpty(characterCode)) {
                if (selectedCharacterCodes.contains(characterCode)) {
                    selectLeafNodeList.remove(i);
                    --i;
                } else {
                    selectedCharacterCodes.add(characterCode);
                }
            }
        }
        return selectLeafNodeList;
    }

    protected synchronized void resetFilterGroup() {
        for (FilterNode child : mChildren) {
            if (child instanceof FilterGroup) {
                FilterGroup group = (FilterGroup)child;
                group.resetFilterGroup();
            } else if (child instanceof UnlimitedFilterNode) {
                child.setSelected(true);
            } else {
                child.setSelected(false);
            }
        }
        super.setSelected(false);
    }

    /**
     * 保存当前筛选状态
     */
    public synchronized void save() {
        mHistorySelectList = getSelectedLeafNodes();
    }

    /**
     * 恢复先前筛选状态
     */
    public synchronized void restore() {
        restore(mHistorySelectList);
        if (mHistorySelectList != null) {
            discardHistory();
        }
    }

    /**
     * 使用selectLeafNodes恢复先前筛选状态
     * @param selectLeafNodes 选中叶子节点列表
     */
    private synchronized void restore(List<FilterNode> selectLeafNodes) {
        if (selectLeafNodes != null) {
            forceSelect(false);

            for (FilterNode node : selectLeafNodes) {
                addSelectNode(node);
            }
        }
    }

    /**
     * 丢弃先前保存的筛选状态
     */
    public synchronized void discardHistory() {
        mHistorySelectList = null;
    }

    /**
     * 是否当前筛选状态与上次save时状态发生改变
     *
     * @return 是否改变
     */
    public synchronized boolean hasFilterChanged() {
        if (mHistorySelectList == null) {
            return true;
        }
        Set<String> selectLeafCharacterCodes = new HashSet<String>();
        Set<FilterNode> selectUnknownLeafNodes = new HashSet<FilterNode>();
        for (FilterNode node : mHistorySelectList) {
            String characterCode = node.getCharacterCode();
            if (!TextUtils.isEmpty(characterCode)) {
                selectLeafCharacterCodes.add(characterCode);
            } else {
                selectUnknownLeafNodes.add(node);
            }
        }
        List<FilterNode> selectLeafNodeList = getSelectedLeafNodes();
        for (FilterNode node : selectLeafNodeList) {
            String characterCode = node.getCharacterCode();
            if (!TextUtils.isEmpty(characterCode)) {
                if (!selectLeafCharacterCodes.remove(characterCode)) {
                    return true;
                }
            } else if (!selectUnknownLeafNodes.remove(node)) {
                return true;
            }
        }
        return !selectLeafCharacterCodes.isEmpty() || !selectUnknownLeafNodes.isEmpty();
    }

    /**
     * 根据characterCode判断是否包含指定节点
     *
     * @param node 指定节点
     * @return 是否包含指定节点
     */
    public synchronized boolean contain(FilterNode node) {
        return contain(node, false);
    }

    /**
     * 根据characterCode或引用（==）判断是否包含指定节点
     *
     * @param node               指定节点(仅支持叶子节点)
     * @param accordingReference 是否使用引用（==）判断
     * @return 是否包含指定节点
     */
    @Override
    public synchronized boolean contain(FilterNode node, boolean accordingReference) {
        String characterCode = node.getCharacterCode();
        if (!accordingReference && TextUtils.isEmpty(characterCode)) {
            return false;
        }
        for (FilterNode child : mChildren) {
            if (child.contain(node, accordingReference)) {
                return true;
            } else if (child == node) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized FilterNode findNode(FilterNode node, boolean accordingReference) {
        String characterCode = node.getCharacterCode();
        if (!accordingReference && TextUtils.isEmpty(characterCode)) {
            return null;
        }
        for (FilterNode child : mChildren) {
            FilterNode result = child.findNode(node, accordingReference);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public boolean canOpen() {
        return mFilterGroupOpenPerformer != null;
    }

    public boolean hasOpened() {
        return mHasOpened;
    }

    private Object mOpenLock = new Object();

    public boolean open(FilterGroupOpenListener listener) {
        synchronized (mOpenLock) {
            if (!mHasOpened) {
                if (listener != null) {
                    listener.onOpenStart(this);
                }
                mHasOpened = performOpen(listener);

                dispatchUnknownNodeToChildren();

                if (listener != null) {
                    if (mHasOpened) {
                        listener.onOpenSuccess(this);
                    } else {
                        listener.onOpenFail(this, "");
                    }
                }
            }
            return mHasOpened;
        }
    }

    protected boolean performOpen(FilterGroupOpenListener listener) {
        if (mFilterGroupOpenPerformer != null) {
            return mFilterGroupOpenPerformer.performOpen(this);
        }
        return false;
    }

    protected void dispatchUnknownNodeToChildren() {
        List<FilterNode> selectLeafNodes = getSelectedLeafNodes();
        resetFilterGroup();
        removeUnselectedInvisibleNode();
        restore(selectLeafNodes);
    }

    private FilterGroupOpenPerformer mFilterGroupOpenPerformer;

    public void setFilterGroupOpenPerformer(FilterGroupOpenPerformer performer) {
        mFilterGroupOpenPerformer = performer;
    }

    public interface FilterGroupOpenPerformer {
        boolean performOpen(FilterGroup group);
    }

    public interface FilterGroupOpenListener {

        void onOpenStart(FilterGroup group);

        void onOpenSuccess(FilterGroup group);

        void onOpenFail(FilterGroup group, String errorMessage);

    }

}
