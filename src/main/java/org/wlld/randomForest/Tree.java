package org.wlld.randomForest;

import org.wlld.tools.ArithUtil;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author lidapeng
 * @description
 * @date 3:12 下午 2020/2/17
 */
public class Tree {//决策树
    private DataTable dataTable;
    private Map<String, List<Integer>> table;//总样本
    private Node rootNode = new Node();//根节点
    private List<Integer> endList;//最终结果分类
    private List<Node> lastNodes = new ArrayList<>();//最后一层节点集合
    private Random random = new Random();

    private class Node {
        private boolean isEnd = false;//是否是最底层
        private List<Integer> fatherList;//父级样本
        private Set<String> attribute;//当前可用属性
        private String key;//该节点分类属性
        private int typeId;//该节点该属性分类的Id值
        private List<Node> nodeList;//下属节点
        private int type;//最底层的类别
        private Node fatherNode;//父级节点
    }

    private class Gain {
        private double gain;
        private double gainRatio;
    }

    public Tree(DataTable dataTable) throws Exception {
        if (dataTable.getKey() != null && dataTable.getLength() > 0) {
            table = dataTable.getTable();
            this.dataTable = dataTable;
        } else {
            throw new Exception("dataTable is empty");
        }
    }

    private double log2(double p) {
        return ArithUtil.div(Math.log(p), Math.log(2));
    }

    private double getEnt(List<Integer> list) {
        //记录了每个类别有几个
        Map<Integer, Integer> myType = new HashMap<>();
        for (int index : list) {
            int type = endList.get(index);//最终结果的类别
            if (myType.containsKey(type)) {
                myType.put(type, myType.get(type) + 1);
            } else {
                myType.put(type, 1);
            }
        }
        double ent = 0;
        //求信息熵
        for (Map.Entry<Integer, Integer> entry1 : myType.entrySet()) {
            double g = ArithUtil.div(entry1.getValue(), list.size());
            ent = ArithUtil.add(ent, ArithUtil.mul(g, log2(g)));
        }
        return -ent;
    }

    private double getGain(double ent, double dNub, double gain) {
        return ArithUtil.add(gain, ArithUtil.mul(ent, dNub));
    }

    private List<Node> createNode(Node node) {
        Set<String> attributes = node.attribute;
        List<Integer> fatherList = node.fatherList;
        if (attributes.size() > 0) {
            Map<String, Map<Integer, List<Integer>>> mapAll = new HashMap<>();
            double fatherEnt = getEnt(fatherList);
            int fatherNub = fatherList.size();//总样本数
            //该属性每个离散数据分类的集合
            for (int i = 0; i < fatherList.size(); i++) {
                int index = fatherList.get(i);//编号
                for (String attr : attributes) {
                    if (!mapAll.containsKey(attr)) {
                        mapAll.put(attr, new HashMap<>());
                    }
                    Map<Integer, List<Integer>> map = mapAll.get(attr);
                    int attrValue = table.get(attr).get(index);
                    if (!map.containsKey(attrValue)) {
                        map.put(attrValue, new ArrayList<>());
                    }
                    List<Integer> list = map.get(attrValue);
                    list.add(index);
                }
            }
            Map<String, List<Node>> nodeMap = new HashMap<>();
            int i = 0;
            double sigmaG = 0;
            Map<String, Gain> gainMap = new HashMap<>();
            for (Map.Entry<String, Map<Integer, List<Integer>>> mapEntry : mapAll.entrySet()) {
                Map<Integer, List<Integer>> map = mapEntry.getValue();
                //求信息增益
                double gain = 0;//信息增益
                double IV = 0;//增益率
                List<Node> nodeList = new ArrayList<>();
                String name = mapEntry.getKey();
                nodeMap.put(name, nodeList);
                for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
                    Set<String> nowAttribute = removeAttribute(attributes, name);
                    Node sonNode = new Node();
                    nodeList.add(sonNode);
                    sonNode.attribute = nowAttribute;
                    List<Integer> list = entry.getValue();
                    sonNode.fatherList = list;
                    sonNode.typeId = entry.getKey();
                    int myNub = list.size();
                    double ent = getEnt(list);
                    double dNub = ArithUtil.div(myNub, fatherNub);
                    IV = ArithUtil.add(ArithUtil.mul(dNub, log2(dNub)), IV);
                    gain = getGain(ent, dNub, gain);
                }
                Gain gain1 = new Gain();
                gainMap.put(name, gain1);
                gain1.gain = ArithUtil.sub(fatherEnt, gain);//信息增益
                if (IV != 0) {
                    gain1.gainRatio = ArithUtil.div(gain1.gain, -IV);//增益率
                } else {
                    gain1.gainRatio = -1;
                }
                sigmaG = ArithUtil.add(gain1.gain, sigmaG);
                i++;
            }
            double avgGain = sigmaG / i;
            double gainRatio = 0;//最大增益率
            String key = null;//可选属性
            for (Map.Entry<String, Gain> entry : gainMap.entrySet()) {
                Gain gain = entry.getValue();
                if (gainRatio == -1) {
                    break;
                }
                if (gain.gain >= avgGain && (gain.gainRatio >= gainRatio || gain.gainRatio == -1)) {
                    gainRatio = gain.gainRatio;
                    key = entry.getKey();
                }
            }
            node.key = key;
            List<Node> nodeList = nodeMap.get(key);
            for (int j = 0; j < nodeList.size(); j++) {//儿子绑定父亲关系
                nodeList.get(j).fatherNode = node;
            }
            for (int j = 0; j < nodeList.size(); j++) {
                Node node1 = nodeList.get(j);
                node1.nodeList = createNode(node1);
            }
            return nodeList;
        } else {
            //判断类别
            node.isEnd = true;//叶子节点
            node.type = getType(fatherList);
            lastNodes.add(node);//将全部最后一层节点集合
            return null;
        }
    }

    private int getType(List<Integer> list) {
        Map<Integer, Integer> myType = new HashMap<>();
        for (int index : list) {
            int type = endList.get(index);//最终结果的类别
            if (myType.containsKey(type)) {
                myType.put(type, myType.get(type) + 1);
            } else {
                myType.put(type, 1);
            }
        }
        int type = 0;
        int nub = 0;
        for (Map.Entry<Integer, Integer> entry : myType.entrySet()) {
            int nowNub = entry.getValue();
            if (nowNub > nub) {
                type = entry.getKey();
                nub = nowNub;
            }
        }
        return type;
    }

    private Set<String> removeAttribute(Set<String> attributes, String name) {
        Set<String> attriBute = new HashSet<>();
        for (String myName : attributes) {
            if (!myName.equals(name)) {
                attriBute.add(myName);
            }
        }
        return attriBute;
    }

    private int getTypeId(Object ob, String name) throws Exception {
        Class<?> body = ob.getClass();
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method method = body.getMethod(methodName);
        return (int) method.invoke(ob);
    }

    public int judge(Object ob) throws Exception {//进行类别判断
        return goTree(ob, rootNode);
    }

    private int goTree(Object ob, Node node) throws Exception {//从树顶向下攀爬
        if (!node.isEnd) {
            int myType = getTypeId(ob, node.key);//当前类别的ID
            List<Node> nodeList = node.nodeList;
            boolean isOk = false;
            for (Node testNode : nodeList) {
                if (testNode.typeId == myType) {
                    isOk = true;
                    node = testNode;
                    break;
                }
            }
            if (!isOk) {//当前类别缺失，未知的属性值
                int index = random.nextInt(nodeList.size());
                node = nodeList.get(index);
            }
            return goTree(ob, node);
        } else {
            return node.type;
        }
    }

    public void study() throws Exception {
        if (dataTable != null) {
            endList = dataTable.getTable().get(dataTable.getKey());
            Set<String> set = dataTable.getKeyType();
            set.remove(dataTable.getKey());
            rootNode.attribute = set;//当前可用属性
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < endList.size(); i++) {
                list.add(i);
            }
            rootNode.fatherList = list;//当前父级样本
            List<Node> nodeList = createNode(rootNode);
            rootNode.nodeList = nodeList;
            //进行后剪枝
            for (Node lastNode : lastNodes) {
                prune(lastNode.fatherNode);
            }
            lastNodes.clear();
        } else {
            throw new Exception("dataTable is null");
        }
    }

    private void prune(Node node) {//执行剪枝
        if (node != null && !node.isEnd) {
            List<Node> listNode = node.nodeList;//子节点
            if (isPrune(node, listNode)) {//剪枝
                deduction(node);
                prune(node.fatherNode);
            }
        }
    }

    private void deduction(Node node) {
        node.isEnd = true;
        node.nodeList = null;
        node.type = getType(node.fatherList);
    }

    private boolean isPrune(Node father, List<Node> sonNodes) {
        boolean isRemove = false;
        List<Integer> typeList = new ArrayList<>();
        for (int i = 0; i < sonNodes.size(); i++) {
            Node node = sonNodes.get(i);
            List<Integer> list = node.fatherList;
            typeList.add(getType(list));
        }
        int fatherType = getType(father.fatherList);
        int nub = getRightPoint(father.fatherList, fatherType);
        //父级该样本正确率
        double rightFather = ArithUtil.div(nub, father.fatherList.size());
        int rightNub = 0;
        int rightAllNub = 0;
        for (int i = 0; i < sonNodes.size(); i++) {
            Node node = sonNodes.get(i);
            List<Integer> list = node.fatherList;
            int right = getRightPoint(list, typeList.get(i));
            rightNub = rightNub + right;
            rightAllNub = rightAllNub + list.size();
        }
        double rightPoint = ArithUtil.div(rightNub, rightAllNub);//子节点正确率
        if (rightPoint <= rightFather) {
            isRemove = true;
        }
        return isRemove;
    }

    private int getRightPoint(List<Integer> types, int type) {
        int nub = 0;
        for (int index : types) {
            int end = endList.get(index);
            if (end == type) {
                nub++;
            }
        }
        return nub;
    }
}
