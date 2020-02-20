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
        private Map<String, List<Integer>> fatherTable;//父级样本
        private Set<String> attribute;//当前可用属性
        private double Ent;//信息熵
        private List<Node> nodeList;//下属节点
    }

    private class Gain {
        private double gain;
        private double IV;
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

    private Gain getGainNode(List<Integer> dataBodyList, double fatherEnt) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        int fatherNub = dataBodyList.size();//总样本数
        double gain = 0;//信息增益
        double IV = 0;//增益率
        //该属性每个离散数据分类的集合
        for (int i = 0; i < dataBodyList.size(); i++) {
            int classification = dataBodyList.get(i);//当前属性
            if (map.containsKey(classification)) {
                List<Integer> list = map.get(classification);
                list.add(i);
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(i);
                map.put(classification, list);
            }
        }
        //求信息增益
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            List<Integer> list = entry.getValue();
            int myNub = list.size();
            double ent = getEnt(list);//每一个信息熵都是一个子集
            double dNub = ArithUtil.div(myNub, fatherNub);
            IV = ArithUtil.add(ArithUtil.mul(dNub, log2(dNub)), IV);
            gain = getGain(ent, dNub, gain);
        }
        Gain gain1 = new Gain();
        gain1.gain = ArithUtil.sub(fatherEnt, gain);//信息增益
        gain1.IV = -IV;
        return gain1;
    }

    private Node createNode(Node node) {
        Map<String, List<Integer>> fatherTable = node.fatherTable;
        Set<String> attributes = node.attribute;
        double fatherEnt = node.Ent;
        for (String name : attributes) {
            List<Integer> dataBodyList = fatherTable.get(name);
            Gain gain = getGainNode(dataBodyList, fatherEnt);//信息增益

        }
        return null;
    }

    public void study() throws Exception {
        if (dataTable != null) {
            endList = dataTable.getTable().get(dataTable.getKey());
            Node node = new Node();
            node.attribute = dataTable.getKeyType();//当前可用属性
            node.fatherTable = table;//当前父级样本
            node.Ent = getEnt(endList);
            createNode(node);
        } else {
            throw new Exception("dataTable is null");
        }
    }
}
