package com.wzx.filter.sample;

import com.wzx.filter.model.FilterGroup;
import com.wzx.filter.model.FilterNode;
import com.wzx.filter.model.FilterRoot;
import com.wzx.filter.model.UnlimitedFilterNode;

/**
 * Created by wang_zx on 2015/1/7.
 */
public class TestFilterRoot extends FilterRoot {

    private static String[] sDescriptors = {"含不限", "关联tab1", "单选", "单选|不限", "互斥tab6",  "互斥tab5", "三级|单", "三级|复",
            "三级|单独选中|单"};

    public TestFilterRoot() {
        setDisplayName("TestFilterRoot");
        //setSingleChoice();
        for (int i = 0; i < 9; i++) {
            FilterGroup group = new FilterGroup();
            group.setDisplayName(String.format("%d(%s)", i+1, sDescriptors[i]));
            group.setCharacterCode(String.format("%d", i));
            if (i == 2 || i == 3) {
                group.setSingleChoice();
            }
            if (i == 0 || i == 3) {
                UnlimitedFilterNode node = new UnlimitedFilterNode();
                node.setDisplayName("不限");
                group.addNode(node);
            }
            for (int j = 0; j < 10; j++) {
                FilterNode node = (i == 6 || i == 7 || i == 8) ? new FilterGroup() : new FilterNode();
                node.setDisplayName(String.format("%d-%d", i + 1, j + 1));
                if (i == 1) {
                    node.setCharacterCode(String.format("0-%d", j + 1));
                } else {
                    node.setCharacterCode(String.format("%d-%d", i, j + 1));
                }
                if (i == 4 || i == 5) {
                    node.addMutexCode(String.format("%d-%d", i == 4 ? 5 : 4, j + 1));
                }
                if (i == 6) {
                    group.setSingleChoice();
                }
                if (i == 6 || i == 7 || i == 8) {
                    UnlimitedFilterNode unlimitedNode = new UnlimitedFilterNode();
                    unlimitedNode.setDisplayName("不限");
                    ((FilterGroup)node).addNode(unlimitedNode);
                    for(int k = 0; k < 10; k++) {
                        FilterNode node1 = new FilterNode();
                        node1.setDisplayName(String.format("%d-%d-%d", i + 1, j + 1, k + 1));
                        node1.setCharacterCode(String.format("%d-%d-%d", i, j + 1, k + 1));
                        ((FilterGroup)node).addNode(node1);
                    }
                    if (i == 8) {
                        ((FilterGroup)node).setSingleChoice();
                       /* InvisibleFilterNode invisibleNode = new InvisibleFilterNode();
                        invisibleNode.setDisplayName(String.format("%d-%d", i + 1, j + 1));
                        ((FilterGroup)node).addNode(invisibleNode);*/
                    }
                }
                group.addNode(node);
            }
            addNode(group);
        }
        addNode(new LazyOpenFilterGroup());
        addNode(new LazyOpenFilterGroup());
        addNode(new LazyOpenFilterGroup());
        addNode(new LazyOpenFilterGroup());
        addNode(new LazyOpenFilterGroup());
        addNode(new LazyOpenFilterGroup());
        resetFilterTree(true);
    }
}
