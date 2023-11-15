package org.wlld.recommend;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.rnnNerveCenter.NerveManager;
import org.wlld.rnnNerveEntity.SensoryNerve;
import org.wlld.tools.IdCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RecommendCodeManager {//推荐id管理
    private NerveManager nerveManager;
    private double studyTh;
    private int dim;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RecommendCodeManager(boolean initPower, RecommendConfig recommendConfig) throws Exception {
        studyTh = recommendConfig.getStudyTh();
        dim = recommendConfig.getDimension();
        nerveManager = new NerveManager(31, dim, 31, 1, new Tanh(), false,
                0.01, RZ.L1, 0.01 * 0.2);
        nerveManager.setSoftMax(false);
        nerveManager.initRnn(initPower, false);
    }

    public double[] getFeatures(int feature) {
        double[] features = new double[31];
        for (int i = 0; i < 31; i++) {
            int t = 1 << i;
            if ((feature & t) != 0) {//存在
                features[i] = 1;
            }
        }
        return features;
    }

    public Map<Integer, Double> getMap(double[] feature) {
        Map<Integer, Double> map = new HashMap<>();
        for (int i = 0; i < feature.length; i++) {
            int t = i + 1;
            map.put(t, feature[i]);
        }
        return map;
    }

    private Matrix getSubMatrix(double sub) throws Exception {
        Matrix subMatrix = new Matrix(2, dim);
        for (int i = 0; i < dim; i++) {
            subMatrix.setNub(1, i, sub);
        }
        return subMatrix;
    }

    public void study(List<CodeBody> codeBodyList) throws Exception {
        for (int i = 0; i < codeBodyList.size(); i++) {
            CodeBody myCodeBody = codeBodyList.get(i);
            double myPower = myCodeBody.getPower();
            if (myPower >= studyTh) {
                double[] myId = getFeatures(myCodeBody.getId());
                Map<Integer, Double> myMapId = getMap(myId);
                for (int j = 0; j < codeBodyList.size(); j++) {
                    if (i != j) {
                        CodeBody codeBody = codeBodyList.get(j);
                        Matrix sub = getSubMatrix((myPower - codeBody.getPower()) / dim);
                        double[] id = getFeatures(codeBody.getId());
                        Map<Integer, Double> mapId = getMap(id);
                        lock.writeLock().lock();
                        studyNerve(1, myId, sub, mapId, true, null);
                        studyNerve(1, id, sub, myMapId, true, null);
                        lock.writeLock().unlock();
                    }
                }
            }
        }
    }

    public double[] getMappingId(int id) throws Exception {//获取映射id
        lock.readLock().lock();
        CodeBack codeBack = new CodeBack();
        codeBack.setMyFeature(new double[31]);
        Matrix subMatrix = new Matrix(2, dim);
        long nextId = IdCreator.get().nextId();
        double[] feature = getFeatures(id);
        studyNerve(nextId, feature, subMatrix, null, false, codeBack);
        lock.readLock().unlock();
        return codeBack.getMyFeature();
    }

    private void studyNerve(long eventId, double[] features, Matrix rnnMatrix, Map<Integer, Double> E, boolean isStudy, OutBack convBack) throws Exception {
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        if (sensoryNerves.size() == features.length) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, features[i], isStudy, E, convBack, false, rnnMatrix);
            }
        } else {
            throw new Exception("size not equals,feature size:" + features.length + ",sensorySize:" + sensoryNerves.size());
        }
    }
}
