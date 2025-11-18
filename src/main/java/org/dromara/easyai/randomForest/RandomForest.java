package org.dromara.easyai.randomForest;

import org.dromara.easyai.tools.ArithUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @description 随机森林
 * @date 3:50 下午 2020/2/22
 */
public class RandomForest {
    private final Random random = new Random();
    private Tree[] forest;
    private float trustTh = 0.1F;//信任阈值
    private float trustPunishment = 0.1F;//信任惩罚

    public float getTrustPunishment() {
        return trustPunishment;
    }

    public void setTrustPunishment(float trustPunishment) {
        this.trustPunishment = trustPunishment;
    }

    public float getTrustTh() {
        return trustTh;
    }

    public void setTrustTh(float trustTh) {
        this.trustTh = trustTh;
    }

    public RandomForest() {
    }

    public RandomForest(int treeNub) throws Exception {
        if (treeNub > 0) {
            forest = new Tree[treeNub];
        } else {
            throw new Exception("Number of trees must be greater than 0");
        }
    }

    public RfModel getModel() {//获取模型
        RfModel rfModel = new RfModel();
        Map<Integer, Node> nodeMap = new ConcurrentHashMap<>();
        for (int i = 0; i < forest.length; i++) {
            Node node = forest[i].getRootNode();
            nodeMap.put(i, node);
        }
        rfModel.setNodeMap(nodeMap);
        return rfModel;
    }

    public int forest(Object object) throws Exception {//随机森林识别
        Map<Integer, Float> map = new ConcurrentHashMap<>();
        for (int i = 0; i < forest.length; i++) {
            Tree tree = forest[i];
            TreeWithTrust treeWithTrust = tree.judge(object);
            int type = treeWithTrust.getType();
            //System.out.println(type);
            float trust = treeWithTrust.getTrust();
            if (map.containsKey(type)) {
                map.put(type, map.get(type) + trust);
            } else {
                map.put(type, trust);
            }
        }
        int type = 0;
        float nub = 0;
        for (Map.Entry<Integer, Float> entry : map.entrySet()) {
            float myNub = entry.getValue();
            //System.out.println("type==" + entry.getKey() + ",nub==" + myNub);
            if (myNub > nub) {
                type = entry.getKey();
                nub = myNub;
            }
        }
        if (nub < ArithUtil.mul(forest.length, trustTh)) {
            type = 0;
        }
        return type;
    }

    //rf初始化
    public void init(DataTable dataTable) throws Exception {
        //一棵树属性的数量
        if (dataTable.getSize() > 4) {
            int kNub = (int) ((int) (float)Math.log(dataTable.getSize()) / (float)Math.log(2));
            //int kNub = dataTable.getSize() / 2;
            // System.out.println("knNub==" + kNub);
            for (int i = 0; i < forest.length; i++) {
                Tree tree = new Tree(getRandomData(dataTable, kNub), trustPunishment);
                forest[i] = tree;
            }
        } else {
            throw new Exception("Number of feature categories must be greater than 3");
        }
    }

    public void study() throws Exception {//学习
        for (int i = 0; i < forest.length; i++) {
            //System.out.println("开始学习==" + i + ",treeNub==" + forest.length);
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

    //从总属性列表中随机挑选属性kNub个属性数量
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
        //System.out.println(myName);
        DataTable data = new DataTable(myName);
        data.setKey(key);
        return data;
    }
}
