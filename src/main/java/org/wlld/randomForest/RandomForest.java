package org.wlld.randomForest;

import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * @description 随机森林
 * @date 3:50 下午 2020/2/22
 */
public class RandomForest {
    private Random random = new Random();
    private Tree[] forest;

    public RandomForest(int treeNub) throws Exception {
        if (treeNub > 0) {
            forest = new Tree[treeNub];
        } else {
            throw new Exception("Number of trees must be greater than 0");
        }
    }

    public void insertModel(RfModel rfModel) throws Exception {//注入模型
        if (rfModel != null) {
            Map<Integer, Node> nodeMap = rfModel.getNodeMap();
            for (int i = 0; i < forest.length; i++) {
                forest[i].setRootNode(nodeMap.get(i));
            }
        } else {
            throw new Exception("model is null");
        }
    }

    public RfModel getModel() {//获取模型
        RfModel rfModel = new RfModel();
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (int i = 0; i < forest.length; i++) {
            Node node = forest[i].getRootNode();
            nodeMap.put(i, node);
        }
        rfModel.setNodeMap(nodeMap);
        return rfModel;
    }

    public int forest(Object object) throws Exception {//随机森林识别
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < forest.length; i++) {
            Tree tree = forest[i];
            int type = tree.judge(object);
            if (map.containsKey(type)) {
                map.put(type, map.get(type) + 1);
            } else {
                map.put(type, 1);
            }
        }
        int type = 0;
        int nub = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int myNub = entry.getValue();
            if (myNub > nub) {
                type = entry.getKey();
                nub = myNub;
            }
        }
        return type;
    }

    public void init(DataTable dataTable) throws Exception {
        //一棵树属性的数量
        if (dataTable.getSize() > 4) {
            int kNub = (int) ArithUtil.div(Math.log(dataTable.getSize()), Math.log(2));
            for (int i = 0; i < forest.length; i++) {
                Tree tree = new Tree(getRandomData(dataTable, kNub));
                forest[i] = tree;
            }
        } else {
            throw new Exception("Number of feature categories must be greater than 3");
        }
    }

    public void study() throws Exception {//学习
        for (int i = 0; i < forest.length; i++) {
            Tree tree = forest[i];
            tree.study();
        }
    }

    public void insert(Object object) {//添加学习参数
        for (int i = 0; i < forest.length; i++) {
            Tree tree = forest[i];
            tree.getDataTable().insert(object);
        }
    }

    private DataTable getRandomData(DataTable dataTable, int kNub) throws Exception {
        Set<String> attr = dataTable.getKeyType();
        Set<String> myName = new HashSet<>();
        String key = dataTable.getKey();//结果
        List<String> list = new ArrayList<>();
        for (String name : attr) {//加载主键
            if (!name.equals(key)) {
                list.add(name);
            }
        }
        for (int i = 0; i < kNub; i++) {
            int index = random.nextInt(list.size());
            myName.add(list.get(index));
            list.remove(index);
        }
        myName.add(key);
        DataTable data = new DataTable(myName);
        data.setKey(key);
        return data;
    }
}
