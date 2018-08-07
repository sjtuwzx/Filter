package com.wzx.filter.sample;

import com.wzx.filter.FilterGroup;
import com.wzx.filter.FilterNode;

public class LazyOpenFilterGroup extends FilterGroup {

    private int mIndex;

    public LazyOpenFilterGroup(int index) {
        mIndex = index;
        setDisplayName(String.format("lazy open[%d]", index + 1));
    }

    @Override
    public boolean canOpen() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected boolean performOpen(FilterGroupOpenListener listener) {
        // TODO Auto-generated method stub
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            FilterNode node = new FilterNode();
            node.setDisplayName(String.format("lazy[%d-%d]", mIndex + 1, i + 1));
            node.setID(String.format("lazy[%d-%d]", mIndex + 1, i + 1));
            addNode(node);
        }
        return true;
    }

}
