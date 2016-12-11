package com.wzx.filter;

/**
 * Created by wang_zx on 2015/1/6.
 * “不限”节点
 */
public class UnlimitedFilterNode extends FilterNode {

    @Override
    public void requestSelect(boolean selected) {
        if (selected) {
            super.requestSelect(selected);
        }
    }
}
