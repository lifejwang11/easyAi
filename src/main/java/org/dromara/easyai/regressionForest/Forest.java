package org.dromara.easyai.regressionForest;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.tools.Frequency;

import java.util.*;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 分段切割容器
 */
public class Forest extends Frequency {
    private Matrix conditionMatrix;//条件矩阵
    private Matrix resultMatrix;//结果矩阵
    private Forest forestLeft;//左森林
    private Forest forestRight;//右森林
    private int featureSize;
    private float resultVariance;//结果矩阵方差
    private float median;//结果矩阵中位数
    private float shrinkParameter;//方差收缩参数
    private Matrix pc;//需要映射的基的集合
    private Matrix pc1;//需要映射的基
    private float[] w;
    private boolean isOldG = true;//是否使用老基
    private int oldGId = 0;//老基的id
    private Matrix matrixAll;//全矩阵
    private float gNorm;//新维度的摸
    private Forest father;//父级
    private final Map<Integer, Forest> forestMap;//尽头列表
    private int id;//本节点的id
    private boolean isRemove = false;//是否已经被移除了
    private boolean notRemovable = false;//不可移除
    private final int minGrain;//最小粒度
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public Forest(int featureSize, float shrinkParameter, Matrix pc, Map<Integer, Forest> forestMap
            , int id, int minGrain) {
        this.featureSize = featureSize;
        this.shrinkParameter = shrinkParameter;
        this.pc = pc;
        w = new float[featureSize];
        this.forestMap = forestMap;
        this.id = id;
        this.minGrain = minGrain;
    }

    public float getMedian() {
        return median;
    }

    public float getResultVariance() {
        return resultVariance;
    }

    public void setResultVariance(float resultVariance) {
        this.resultVariance = resultVariance;
    }

    public float getMappingFeature(Matrix feature) throws Exception {//获取映射后的特征
        float nub;
        if (feature.isRowVector()) {
            if (isOldG) {//使用原有基
                nub = feature.getNumber(0, oldGId);
            } else {
                nub = transG(pc1, feature, gNorm);
            }
        } else {
            throw new Exception("feature is not a rowVector");
        }
        return nub;
    }

    private float[] findG() throws Exception {//寻找新的切入维度
        // 先尝试从原有维度切入
        int xSize = conditionMatrix.getX();
        int ySize = conditionMatrix.getY();
        matrixAll = new Matrix(xSize, ySize);
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (j < ySize - 1) {
                    matrixAll.setNub(i, j, conditionMatrix.getNumber(i, j));
                } else {
                    matrixAll.setNub(i, j, resultMatrix.getNumber(i, 0));
                }
            }
        }
        float maxOld = 0;
        int type = 0;
        for (int i = 0; i < featureSize; i++) {
            float[] g = new float[conditionMatrix.getX()];
            for (int j = 0; j < g.length; j++) {
                if (i < featureSize - 1) {
                    g[j] = conditionMatrix.getNumber(j, i);
                } else {
                    g[j] = resultMatrix.getNumber(j, 0);
                }
            }
            float var = dc(g);//计算方差
            if (var > maxOld) {
                maxOld = var;
                type = i;
            }
        }
        int x = pc.getX();
        float max = 0;
        for (int i = 0; i < x; i++) {
            Matrix g = pc.getRow(i);
            float gNorm = matrixOperation.getNorm(g);
            float[] var = new float[xSize];
            for (int j = 0; j < xSize; j++) {
                Matrix parameter = matrixAll.getRow(j);
                float dist = transG(g, parameter, gNorm);
                var[j] = dist;
            }
            float variance = dc(var);
            if (variance > max) {
                max = variance;
                pc1 = g;
            }
        }
        //找到非原始基最离散的新基:
        if (max > maxOld) {//使用新基
            isOldG = false;
        } else {//使用原有基
            isOldG = true;
            oldGId = type;
        }
        return findTwo(xSize);
    }

    private float transG(Matrix g, Matrix parameter, float gNorm) throws Exception {//将数据映射到新基
        //先求内积
        float innerProduct = matrixOperation.innerProduct(g, parameter);
        return innerProduct / gNorm;
    }

    private float[] findTwo(int dataSize) throws Exception {
        Matrix matrix;//创建一个列向量
        float[] data = new float[dataSize];
        if (isOldG) {//使用原有基
            if (oldGId == featureSize - 1) {//从结果矩阵提取数据
                matrix = resultMatrix;
            } else {//从条件矩阵中提取数据
                matrix = conditionMatrix.getColumn(oldGId);
            }
            //将数据塞入数组
            for (int i = 0; i < dataSize; i++) {
                data[i] = matrix.getNumber(i, 0);
            }
        } else {//使用转换基
            int x = matrixAll.getX();
            gNorm = matrixOperation.getNorm(pc1);
            for (int i = 0; i < x; i++) {
                Matrix parameter = matrixAll.getRow(i);
                float dist = transG(pc1, parameter, gNorm);
                data[i] = dist;
            }
        }
        Arrays.sort(data);//对数据进行排序
        return data;
    }

    private float getDist(float[] data, float[] w) {
        int len = data.length;
        float sigma = 0;
        for (int i = 0; i < len; i++) {
            float sub = data[i] - w[i];
            sigma = sigma + (float)Math.pow(sub, 2);
        }
        return sigma / len;
    }

    public void pruning() {//进行后剪枝，跟父级进行比较
        if (!notRemovable) {
            Forest fatherForest = this.getFather();
            float[] fatherW = fatherForest.getW();
            float sub = getDist(w, fatherW);
            if (sub < shrinkParameter) {//需要剪枝,通知父级
                fatherForest.getSonMessage(true, id);
                isRemove = true;
                //System.out.println("剪枝id==" + id + ",sub==" + sub + ",th==" + shrinkParameter);
            } else {//通知父级，不需要剪枝,并将父级改为不可移除
                fatherForest.getSonMessage(false, id);
            }
        }
    }

    public void getSonMessage(boolean isPruning, int myId) {//进行剪枝
        if (isPruning) {//剪枝
            if (myId == id * 2) {//左节点
                forestLeft = null;
            } else {//右节点
                forestRight = null;
            }
        } else {//不剪枝,将自己变为不可剪枝状态
            notRemovable = true;
        }
    }

    public void cut() throws Exception {
        int y = resultMatrix.getX();
        if (y > minGrain) {
            float[] dm = findG();
            int z = y / 2;
            median = dm[z];
            int rightNub = 0;
            int leftNub = 0;
            for (int i = 0; i < dm.length; i++) {
                if (dm[i] > median) {
                    rightNub++;
                } else {
                    leftNub++;
                }
            }
            int leftId = 2 * id;
            int rightId = leftId + 1;
            //System.out.println("id:" + id + ",size:" + dm.length);
            forestMap.put(id, this);
            forestLeft = new Forest(featureSize, shrinkParameter, pc, forestMap, leftId, minGrain);
            forestRight = new Forest(featureSize, shrinkParameter, pc, forestMap, rightId, minGrain);
            forestRight.setFather(this);
            forestLeft.setFather(this);
            Matrix conditionMatrixLeft = new Matrix(leftNub, featureSize);//条件矩阵左
            Matrix conditionMatrixRight = new Matrix(rightNub, featureSize);//条件矩阵右
            Matrix resultMatrixLeft = new Matrix(leftNub, 1);//结果矩阵左
            Matrix resultMatrixRight = new Matrix(rightNub, 1);//结果矩阵右
            forestLeft.setConditionMatrix(conditionMatrixLeft);
            forestLeft.setResultMatrix(resultMatrixLeft);
            forestRight.setConditionMatrix(conditionMatrixRight);
            forestRight.setResultMatrix(resultMatrixRight);
            int leftIndex = 0;//左矩阵添加行数
            int rightIndex = 0;//右矩阵添加行数
            for (int i = 0; i < y; i++) {
                float nub;
                if (isOldG) {//使用原有基
                    nub = matrixAll.getNumber(i, oldGId);
                } else {//使用新基
                    Matrix parameter = matrixAll.getRow(i);
                    nub = transG(pc1, parameter, gNorm);
                }
                if (nub > median) {//进入右森林并计算右森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixRight.setNub(rightIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultMatrixRight.setNub(rightIndex, 0, resultMatrix.getNumber(i, 0));
                    rightIndex++;
                } else {//进入左森林并计算左森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixLeft.setNub(leftIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultMatrixLeft.setNub(leftIndex, 0, resultMatrix.getNumber(i, 0));
                    leftIndex++;
                }
            }
            //分区完成
        }
    }

    public Matrix getConditionMatrix() {
        return conditionMatrix;
    }

    public void setConditionMatrix(Matrix conditionMatrix) {
        this.conditionMatrix = conditionMatrix;
    }

    public Matrix getResultMatrix() {
        return resultMatrix;
    }

    public void setResultMatrix(Matrix resultMatrix) {
        this.resultMatrix = resultMatrix;
    }

    public float[] getW() {
        return w;
    }

    public void setW(float[] w) {
        this.w = w;
    }

    public Forest getForestLeft() {
        return forestLeft;
    }

    public Forest getForestRight() {
        return forestRight;
    }

    public Forest getFather() {
        return father;
    }

    public void setFather(Forest father) {
        this.father = father;
    }

    public boolean isRemove() {
        return isRemove;
    }

    public void setRemove(boolean remove) {
        isRemove = remove;
    }

    public boolean isNotRemovable() {
        return notRemovable;
    }

    public void setNotRemovable(boolean notRemovable) {
        this.notRemovable = notRemovable;
    }
}
