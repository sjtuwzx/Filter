package com.wzx.filter.sample;

import com.wzx.filter.model.FilterGroup;
import com.wzx.filter.model.FilterNode;

public class LazyOpenFilterGroup extends FilterGroup {

    public LazyOpenFilterGroup() {
        setDisplayName("lazy open");
        setCharacterCode("lazy");
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
            node.setDisplayName(String.format("lazy[%d]", i + 1));
            node.setCharacterCode(String.format("lazy[%d]", i + 1));
            addNode(node);
        }
        return true;
    }

}
