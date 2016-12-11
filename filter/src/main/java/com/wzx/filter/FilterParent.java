package com.wzx.filter;

/**
 * Created by wang_zx on 2015/1/6.
 * 筛选节点parent接口
 */
public interface FilterParent {

    /**
     * 删除节点
     *
     * @param node 被删除节点
     */
    void remove(FilterNode node);

    /**
     * 请求root根据触发节点刷新整个筛选树状态
     * @param trigger 触发状态更新源节点
     * @param selected 是否选中
     */
    void requestSelect(FilterNode trigger, boolean selected);

}
