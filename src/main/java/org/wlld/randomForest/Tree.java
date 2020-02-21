package org.wlld.randomForest;

import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * @description
 * @date 3:12 下午 2020/2/17
 */
public class Tree {//决策树
    private DataTable dataTable;
    private Map<String, List<Integer>> table;//总样本
    private Node rootNode;//根节点
    private List<Integer> endList;//最终结果分类

    private class Node {
        private boolean isEnd = false;
        private List<Integer> fatherList;//父级样本
        private Set<String> attribute;//当前可用属性
        private String key;//该节点分类属性
        private List<Node> nodeList;//下属节点
        private int type;
    }

    private class Gain {
        private double gain;
        private double gainRatio;
    }

    Tree(DataTable dataTable) throws Exception {
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
                    sonNode.key = mapEntry.getKey();
                    sonNode.attribute = nowAttribute;
                    List<Integer> list = entry.getValue();
                    sonNode.fatherList = list;
                    int myNub = list.size();
                    double ent = getEnt(list);//每一个信息熵都是一个子集
                    double dNub = ArithUtil.div(myNub, fatherNub);
                    IV = ArithUtil.add(ArithUtil.mul(dNub, log2(dNub)), IV);
                    gain = getGain(ent, dNub, gain);
                }
                Gain gain1 = new Gain();
                gainMap.put(name, gain1);
                gain1.gain = ArithUtil.sub(fatherEnt, gain);//信息增益
                gain1.gainRatio = ArithUtil.div(gain1.gain, -IV);//增益率
                sigmaG = ArithUtil.add(gain1.gain, sigmaG);
                i++;
            }
            double avgGain = ArithUtil.div(sigmaG, i);
            double gainRatio = 0;//最大增益率
            String key = null;//可选属性
            for (Map.Entry<String, Gain> entry : gainMap.entrySet()) {
                Gain gain = entry.getValue();
                if (gain.gain > avgGain && gain.gainRatio > gainRatio) {
                    gainRatio = gain.gainRatio;
                    key = entry.getKey();
                }
            }
            List<Node> nodeList = nodeMap.get(key);
            for (int j = 0; j < nodeList.size(); j++) {
                Node node1 = nodeList.get(j);
                node1.nodeList = createNode(node1);
            }
            return nodeList;
        } else {
            //判断类别
            node.isEnd = true;
            node.type = getType(fatherList);
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

    public void study() throws Exception {
        if (dataTable != null) {
            endList = dataTable.getTable().get(dataTable.getKey());
            Node node = new Node();
            node.attribute = dataTable.getKeyType();//当前可用属性
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < endList.size(); i++) {
                list.add(i);
            }
            node.fatherList = list;//当前父级样本
            createNode(node);
        } else {
            throw new Exception("dataTable is null");
        }
    }
}
