package org.dromara.randomForest;

import java.util.List;
import java.util.Set;

/**
 * @author lidapeng
 * @description
 * @date 3:09 下午 2020/2/22
 */
public class Node {
    public boolean isEnd = false;//是否是最底层
    public List<Integer> fatherList;//父级样本
    public Set<String> attribute;//当前可用属性
    public String key;//该节点分类属性
    public int typeId;//该节点该属性分类的Id值
    public List<Node> nodeList;//下属节点
    public int type;//最底层的类别
    public Node fatherNode;//父级节点
}
