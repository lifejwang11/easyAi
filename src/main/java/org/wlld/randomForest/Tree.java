package org.wlld.randomForest;


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
    private Node rootNode;//根节点
    private List<Integer> endList;//最终结果分类
    private final List<Node> lastNodes = new ArrayList<>();//最后一层节点集合
    private final Random random = new Random();
    private final double trustPunishment;//信任惩罚

    public Node getRootNode() {
        return rootNode;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    private static class Gain {
        private double gain;//信息增益
        private double gainRatio;//信息增益率
    }

    public Tree(double trustPunishment) {
        this.trustPunishment = trustPunishment;
    }

    public Tree(DataTable dataTable, double trustPunishment) throws Exception {
        if (dataTable != null && dataTable.getKey() != null) {
            this.trustPunishment = trustPunishment;
            this.dataTable = dataTable;
        } else {
            throw new Exception("dataTable is empty");
        }
    }

    private double log2(double p) {
        return Math.log(p) / Math.log(2);
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
            double g = (double) entry1.getValue() / (double) list.size();//每个类别的概率
            ent = ent + g * log2(g);
        }
        return -ent;
    }

    private double getGain(double ent, double dNub, double gain) {
        return gain + ent * dNub;
    }

    private List<Node> createNode(Node node) {
        Set<String> attributes = node.attribute;
        List<Integer> fatherList = node.fatherList;
        if (!attributes.isEmpty()) {
            Map<String, Map<Integer, List<Integer>>> mapAll = new HashMap<>();//主键：可用属性 次主键该属性的属性值，集合该值对应的id
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
                    int attrValue = table.get(attr).get(index);//获取当前属性值
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
                Map<Integer, List<Integer>> map = mapEntry.getValue();//当前属性的 属性值及id
                //求信息增益
                double gain = 0;//信息增益
                double IV = 0;//增益率
                List<Node> nodeList = new ArrayList<>();
                String name = mapEntry.getKey();//可用属性名称
                nodeMap.put(name, nodeList);
                for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {//遍历当前属性下的所有属性值的集合
                    Set<String> nowAttribute = removeAttribute(attributes, name);
                    Node sonNode = new Node();
                    nodeList.add(sonNode);
                    sonNode.attribute = nowAttribute;
                    List<Integer> list = entry.getValue();//该属性值下的数据id集合
                    sonNode.fatherList = list;
                    sonNode.typeId = entry.getKey();//该属性值
                    int myNub = list.size();//该属性值下数据的数量
                    double ent = getEnt(list);//该属性值 的信息熵
                    double dNub = (double) myNub / (double) fatherNub;//该属性值在 父级样本中出现的概率
                    IV = dNub * log2(dNub) + IV;
                    gain = getGain(ent, dNub, gain);
                }
                Gain gain1 = new Gain();
                gainMap.put(name, gain1);
                gain1.gain = fatherEnt - gain;//信息增益
                if (IV != 0) {
                    gain1.gainRatio = gain1.gain / -IV;//增益率
                } else {
                    gain1.gainRatio = 1000000;
                }
                sigmaG = gain1.gain + sigmaG;
                i++;
            }
            double avgGain = sigmaG / i;
            double gainRatio = -2;//最大增益率
            String key = null;//可选属性
            System.out.println("平均信息增益==============================" + avgGain);
            for (Map.Entry<String, Gain> entry : gainMap.entrySet()) {
                Gain gain = entry.getValue();
//                System.out.println("主键:" + entry.getKey() + ",平均信息增益:" + avgGain + ",可用属性数量:" + gainMap.size()
//                        + "该属性信息增益:" + gain.gain + ",该属性增益率：" + gain.gainRatio + ",当前最高增益率:" + gainRatio);
                if (gainMap.size() == 1 || ((gain.gain >= avgGain || Math.abs(gain.gain - avgGain) < 0.000001) && (gain.gainRatio >= gainRatio || gainRatio == -2))) {
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
        return Integer.parseInt(method.invoke(ob).toString());
    }

    public TreeWithTrust judge(Object ob) throws Exception {//进行类别判断
        if (rootNode != null) {
            TreeWithTrust treeWithTrust = new TreeWithTrust();
            treeWithTrust.setTrust(1.0);
            goTree(ob, rootNode, treeWithTrust, 0);
            return treeWithTrust;
        } else {
            throw new Exception("rootNode is null");
        }
    }

    private void punishment(TreeWithTrust treeWithTrust) {//信任惩罚
        //System.out.println("惩罚");
        double trust = treeWithTrust.getTrust();//获取当前信任值
        trust = trust * trustPunishment;
        treeWithTrust.setTrust(trust);
    }

    private void goTree(Object ob, Node node, TreeWithTrust treeWithTrust, int times) throws Exception {//从树顶向下攀爬
        if (!node.isEnd) {
            int myType = getTypeId(ob, node.key);//当前类别的ID
            if (myType == 0) {//做信任惩罚
                punishment(treeWithTrust);
            }
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
                punishment(treeWithTrust);
                int index = random.nextInt(nodeList.size());
                node = nodeList.get(index);
            }
            times++;
            goTree(ob, node, treeWithTrust, times);
        } else {
            //当以0作为结束的时候要做严厉的信任惩罚
            if (node.typeId == 0) {
                int nub = rootNode.attribute.size() - times;
                //System.out.println("惩罚次数" + nub);
                for (int i = 0; i < nub; i++) {
                    punishment(treeWithTrust);
                }
            }
            treeWithTrust.setType(node.type);
        }
    }

    public void study() throws Exception {
        if (dataTable != null && dataTable.getLength() > 0) {
            rootNode = new Node();
            table = dataTable.getTable();
            endList = dataTable.getTable().get(dataTable.getKey());
            Set<String> set = dataTable.getKeyType();
            set.remove(dataTable.getKey());
            rootNode.attribute = set;//当前可用属性
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < endList.size(); i++) {
                list.add(i);
            }
            rootNode.fatherList = list;//当前父级样本
            rootNode.nodeList = createNode(rootNode);
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
        double rightFather = (double) nub / (double) father.fatherList.size();
        int rightNub = 0;
        int rightAllNub = 0;
        for (int i = 0; i < sonNodes.size(); i++) {
            Node node = sonNodes.get(i);
            List<Integer> list = node.fatherList;
            int right = getRightPoint(list, typeList.get(i));
            rightNub = rightNub + right;
            rightAllNub = rightAllNub + list.size();
        }
        double rightPoint = (double) rightNub / (double) rightAllNub;//子节点正确率
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
