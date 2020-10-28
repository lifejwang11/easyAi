package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.Knn;
import org.wlld.imageRecognition.modelEntity.KeyMapping;
import org.wlld.pso.PSO;

import java.util.*;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 维度映射
 */
public class DimensionMappingStudy {
    private TempleConfig templeConfig;
    private Knn myKnn = new Knn(1);
    private double[] mappingSigma;//映射层

    public DimensionMappingStudy(TempleConfig templeConfig, boolean isClone) throws Exception {
        this.templeConfig = templeConfig;
        //深度克隆
        if (isClone) {
            myKnn.setFeatureMap(cloneFeature(templeConfig.getKnn().getFeatureMap()));
        }
    }

    public Knn getMyKnn() {
        return myKnn;
    }

    private Map<Integer, List<Matrix>> cloneFeature(Map<Integer, List<Matrix>> feature) throws Exception {
        Map<Integer, List<Matrix>> realFeatures = new HashMap<>();
        for (Map.Entry<Integer, List<Matrix>> entry : feature.entrySet()) {
            List<Matrix> matrixList = new ArrayList<>();
            List<Matrix> matrixAll = entry.getValue();
            for (Matrix matrix : matrixAll) {
                Matrix myMatrix = new Matrix(1, matrix.getY());
                for (int i = 0; i < matrix.getY(); i++) {
                    myMatrix.setNub(0, i, matrix.getNumber(0, i));
                }
                // System.out.println(myMatrix.getString());
                matrixList.add(myMatrix);
            }
            realFeatures.put(entry.getKey(), matrixList);
        }
        return realFeatures;
    }

    private double getDist(Matrix data1, Matrix data2) throws Exception {
        int size = data1.getY();
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            sigma = sigma + Math.pow(data1.getNumber(0, i) - data2.getNumber(0, i), 2);
        }
        return Math.sqrt(sigma);
    }

    class MinTypeSort implements Comparator<MinType> {

        @Override
        public int compare(MinType o1, MinType o2) {
            if (o1.minDist < o2.minDist) {
                return -1;
            } else if (o1.minDist > o2.minDist) {
                return 1;
            }
            return 0;
        }
    }

    class MinType {
        private double minDist;
        private int type;
    }

    private double getSub(List<Matrix> featureSame, List<Matrix> featureDifferent) throws Exception {
        double minSub = 0;
        for (int i = 0; i < featureSame.size(); i++) {
            Matrix sameFeature = featureSame.get(i);
            double differentMin = -1;//与异类相比的最小值
            double sameMin = -1;//与同类相比的最小值
            for (int j = 0; j < featureDifferent.size(); j++) {
                Matrix differentFeature = featureDifferent.get(j);
                double dist = getDist(sameFeature, differentFeature);
                if (differentMin < 0 || dist < differentMin) {
                    differentMin = dist;
                }
            }
            for (int k = 0; k < featureSame.size(); k++) {
                if (k != i) {
                    Matrix feature = featureSame.get(k);
                    double dist = getDist(sameFeature, feature);
                    if (sameMin < 0 || dist < sameMin) {
                        sameMin = dist;
                    }
                }
            }
            double sub = differentMin - sameMin;//异类距离与同类距离的差距,选出其中最小的
            if (i == 0) {
                minSub = sub;
            } else if (sub < minSub) {
                minSub = sub;
            }
        }
        return minSub;
    }

    public List<KeyMapping> selfTest(int nub) throws Exception {//对模型数据进行自检测
        Map<Integer, List<Matrix>> myFeatureMap = templeConfig.getKnn().getFeatureMap();//未映射的克隆的
        Map<Integer, List<Matrix>> featureMap = myKnn.getFeatureMap();
        MinTypeSort minTypeSort = new MinTypeSort();
        Map<Integer, List<MinType>> minMap = new HashMap<>();//保存与该类别最相似的类别,及距离值
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<MinType> minTypes = new ArrayList<>();
            List<Matrix> myFeatures = entry.getValue();
            for (Map.Entry<Integer, List<Matrix>> entry2 : featureMap.entrySet()) {
                int testKey = entry2.getKey();
                if (testKey != key) {//找出最小距离
                    List<Matrix> features = entry2.getValue();
                    MinType minType = new MinType();
                    double minDist = getSub(myFeatures, features);
                    minType.minDist = minDist;
                    minType.type = testKey;
                    minTypes.add(minType);
                }
            }
            Collections.sort(minTypes, minTypeSort);
            minTypes = minTypes.subList(0, nub);
            minMap.put(key, minTypes);
        }
        List<int[]> cups = new ArrayList<>();
        for (Map.Entry<Integer, List<MinType>> entry : minMap.entrySet()) {
            System.out.println("类别===============================" + entry.getKey());
            int[] cup = new int[nub + 1];
            cup[0] = entry.getKey();
            List<MinType> minTypeList = entry.getValue();
            for (int i = 0; i < minTypeList.size(); i++) {
                cup[i + 1] = minTypeList.get(i).type;
            }
            System.out.println("type:" + Arrays.toString(cup));
            cups.add(cup);
        }
        List<Set<Integer>> setList = getTypeSet(cups);
        List<KeyMapping> mappingList = new ArrayList<>();
        for (Set<Integer> set : setList) {
            KeyMapping keyMapping = new KeyMapping();
            mappingList.add(keyMapping);
            keyMapping.setKeys(set);
            Map<Integer, List<Matrix>> map = new HashMap<>();
            for (int type : set) {
                List<Matrix> features = myFeatureMap.get(type);
                map.put(type, features);
            }
            DimensionMappingStudy dimension = new DimensionMappingStudy(templeConfig, false);
            keyMapping.setDimensionMapping(dimension);
            Knn knn = dimension.getMyKnn();
            knn.setFeatureMap(cloneFeature(map));
            dimension.mappingStart();
        }
        return mappingList;
    }

    private void insertSet(Set<Integer> team, int[] features) {
        for (int feature : features) {
            team.add(feature);
        }
    }

    private void getSet(List<int[]> cups, Map<Integer, Set<Integer>> teams) {
        for (int[] types : cups) {
            for (int type : types) {
                insertSet(teams.get(type), types);
            }
        }
    }

    private List<Set<Integer>> getTypeSet(List<int[]> t) {
        //最终输入结果
        List<Set<Integer>> t1 = new ArrayList<>();
        //储存临时值
        Set<Integer> s = new HashSet<>();
        //用来校验重复游标组
        List<List<Integer>> k = new ArrayList<>();
        Boolean x = true;//对循环进行判断
        int num = 0;//循环游标初始值
        while (x) {
            //校验重复游标
            List<Integer> l = new ArrayList<>();
            //清楚临时存储值
            s.clear();
            //单个最终值
            Set<Integer> s1 = new HashSet<>();
            for (int i = num; i < t.size(); i++) {
                Boolean check = false;
                //如果存在已经验证的数组，则跳过
                for (int j = num; j < k.size(); j++) {
                    if (k.get(j).contains(i)) {
                        check = true;
                        break;
                    }
                }
                if (check) {
                    continue;
                }

                //循环判断数组是否存在重复（当i=num时，给初始的s赋值）
                //当S中包含重复值的时候，插入
                for (int info : t.get(i)) {
                    if (i == num) {
                        for (int info1 : t.get(i)) {
                            l.add(i);
                            k.add(l);
                            s.add(info1);
                        }
                    } else if (s.contains(info)) {
                        for (int info1 : t.get(i)) {
                            l.add(i);
                            k.add(l);
                            s.add(info1);
                        }
                        break;
                    }
                }

            }
            //如果临时s不为空，给s1赋值，并存入到最后输出的t1中
            if (!s.isEmpty()) {
                for (Integer info : s) {
                    s1.add(info);
                }
                t1.add(s1);
            }
            num++;
            //跳出循环
            if (num >= t.size()) {
                x = false;
            }
        }
        return t1;
    }

    public int getType(Matrix feature) throws Exception {
        Matrix myFeature = mapping(feature, mappingSigma);
        return myKnn.getType(myFeature);
    }

    public void mappingStart() throws Exception {
        Map<Integer, List<Matrix>> featureMap = myKnn.getFeatureMap();
        //创建粒子群适应函数
        FeatureMapping featureMapping = new FeatureMapping(featureMap);
        int dimensionNub = templeConfig.getFeatureNub() * 3 * 2;//PSO维度
        //创建粒子群
        PSO pso = new PSO(dimensionNub, null, null, 400, 50,
                featureMapping, 0.8, 2, 2, true, 0.2, 0.01);
        List<double[]> mappings = pso.start();
        int size = mappings.size();
        mappingSigma = new double[dimensionNub];
        for (int i = 0; i < size; i++) {
            double[] mapping = mappings.get(i);
            for (int j = 0; j < mapping.length; j++) {
                mappingSigma[j] = mappingSigma[j] + mapping[j];
            }
        }
        //生成第一层映射层
        for (int i = 0; i < mappingSigma.length; i++) {
            mappingSigma[i] = mappingSigma[i] / size;
        }
        //还要把所有已存在的knn与混合高斯特征完成线性映射
        myKnn.setFeatureMap(featureMapping(featureMap, mappingSigma));
    }

    public List<KeyMapping> start() throws Exception {
        mappingStart();
        return selfTest(2);
    }

    private Map<Integer, List<Matrix>> featureMapping(Map<Integer, List<Matrix>> featureMap, double[] mapping) throws Exception {
        Map<Integer, List<Matrix>> featureMap2 = new HashMap<>();
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<Matrix> features = entry.getValue();
            List<Matrix> mappingFeatures = new ArrayList<>();
            for (Matrix matrix : features) {
                mappingFeatures.add(mapping(matrix, mapping));
            }
            featureMap2.put(key, mappingFeatures);
        }
        return featureMap2;
    }

    private Matrix mapping(Matrix feature, double[] mapping) throws Exception {
        int size = feature.getY();
        Matrix matrix = new Matrix(1, size);
        for (int i = 0; i < size; i++) {
            double nub = feature.getNumber(0, i) * mapping[i] + mapping[size + i];
            matrix.setNub(0, i, nub);
        }
        return matrix;
    }
}
