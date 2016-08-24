# Filter

##简介
一款通用的筛选组件，支持单选，复选，多级筛选，筛选联动，筛选数据懒加载

##设计思路

我们可以把所有筛选项抽象成一棵树，每个筛选节点抽象成一个FilterNode，他的父节点为一个FilterGroup，最后的根节点为FilterRoot，构建一棵筛选树。每个Node仅对外暴露displayName、isSelected两个属性。选中或反选节点后，将改事件从parent抛到root节点，然后向下遍历所有节点，设置所有节点的isSelected，刷新整棵筛选树。

优点：

1. 逻辑层与UI层解耦，UI不关系筛选逻辑，仅需根据筛选树的状态刷新UI
2. 可通过配置的方式构建筛选树，支持任意层级的筛选结构
	

##预览
 ![image](https://github.com/sjtuwzx/Filter/blob/master/filter.gif)
