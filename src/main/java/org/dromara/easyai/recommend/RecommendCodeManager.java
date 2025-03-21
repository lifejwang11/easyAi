package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.rnnNerveCenter.NerveManager;
import org.dromara.easyai.rnnNerveEntity.SensoryNerve;
import org.dromara.easyai.tools.IdCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RecommendCodeManager {//推荐id管理
    private final NerveManager nerveManager;
    private final float studyTh;
    private final int dim;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RecommendCodeManager(boolean initPower, RecommendConfig recommendConfig) throws Exception {
        studyTh = recommendConfig.getStudyTh();
        dim = recommendConfig.getDimension();
        nerveManager = new NerveManager(31, dim, 31, 1, new Tanh(),
                0.01f, RZ.L1, 0.0001f);
        nerveManager.setSoftMax(false);
        nerveManager.initRnn(initPower, false);
    }

    public float[] getFeatures(int feature) {
        float[] features = new float[31];
        for (int i = 0; i < 31; i++) {
            int t = 1 << i;
            if ((feature & t) != 0) {//存在
                features[i] = 1;
            }
        }
        return features;
    }

    public Map<Integer, Float> getMap(float[] feature) {
        Map<Integer, Float> map = new HashMap<>();
        for (int i = 0; i < feature.length; i++) {
            int t = i + 1;
            map.put(t, feature[i]);
        }
        return map;
    }

    private Matrix getSubMatrix(float sub) throws Exception {
        Matrix subMatrix = new Matrix(2, dim);
        for (int i = 0; i < dim; i++) {
            subMatrix.setNub(1, i, sub);
        }
        return subMatrix;
    }

    public void study(List<CodeBody> codeBodyList) throws Exception {
        for (int i = 0; i < codeBodyList.size(); i++) {
            CodeBody myCodeBody = codeBodyList.get(i);
            float myPower = myCodeBody.getPower();
            if (myPower >= studyTh) {
                float[] myId = getFeatures(myCodeBody.getId());
                Map<Integer, Float> myMapId = getMap(myId);
                for (int j = 0; j < codeBodyList.size(); j++) {
                    if (i != j) {
                        CodeBody codeBody = codeBodyList.get(j);
                        Matrix sub = getSubMatrix((myPower - codeBody.getPower()) / dim);
                        float[] id = getFeatures(codeBody.getId());
                        Map<Integer, Float> mapId = getMap(id);
                        lock.writeLock().lock();
                        studyNerve(1, myId, sub, mapId, true, null);
                        studyNerve(1, id, sub, myMapId, true, null);
                        lock.writeLock().unlock();
                    }
                }
            }
        }
    }

    public float[] getMappingId(int id) throws Exception {//获取映射id
        lock.readLock().lock();
        CodeBack codeBack = new CodeBack();
        codeBack.setMyFeature(new float[31]);
        Matrix subMatrix = new Matrix(2, dim);
        long nextId = IdCreator.get().nextId();
        float[] feature = getFeatures(id);
        studyNerve(nextId, feature, subMatrix, null, false, codeBack);
        lock.readLock().unlock();
        return codeBack.getMyFeature();
    }

    private void studyNerve(long eventId, float[] features, Matrix rnnMatrix, Map<Integer, Float> E, boolean isStudy, OutBack convBack) throws Exception {
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
