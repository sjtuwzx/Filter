package com.wzx.filter;

/**
 * Created by wang_zx on 2015/1/12.
 * “不可见”节点
 */
public class InvisibleFilterNode extends FilterNode {

    public InvisibleFilterNode(FilterNode node) {
        setDisplayName(node.getDisplayName());
        setCharacterCode(node.getCharacterCode());
        setData(node.getData());
    }

}
