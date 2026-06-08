package org.dromara.easyai.resnet;

import org.dromara.easyai.batchNerve.BatchNerveModel;
import org.dromara.easyai.conv.dcn.DConv;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.resnet.entity.BackParameter;
import org.dromara.easyai.resnet.entity.NormModel;
import org.dromara.easyai.resnet.entity.ResConvModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 17:18
 * @des 单个卷积层
 */
public class ConvLay {
    private List<Matrix> convPower;//第一层卷积权重 需要作为模型取出
    private List<Matrix> dymStudyRateList;//一阶动态学习率
    private List<Matrix> dymStudyRate2List;//二阶动态学习率
    private List<MatrixNorm> matrixNormList;//归一化层// 需要作为模型取出
    private DConv DConv;
    private final List<BackParameter> backParameterList = new ArrayList<>();

    public DConv getDConv() {
        return DConv;
    }

    public void setDConv(DConv DConv) {
        this.DConv = DConv;
    }

    public ConvLay(int batchSize) {
        for (int i = 0; i < batchSize; i++) {
            backParameterList.add(new BackParameter());
        }
    }

    public List<Matrix> getDymStudyRate2List() {
        return dymStudyRate2List;
    }

    public void setDymStudyRate2List(List<Matrix> dymStudyRate2List) {
        this.dymStudyRate2List = dymStudyRate2List;
    }

    public List<BackParameter> getBackParameterList() {
        return backParameterList;
    }

    public List<Matrix> getDymStudyRateList() {
        return dymStudyRateList;
    }

    public void setDymStudyRateList(List<Matrix> dymStudyRateList) {
        this.dymStudyRateList = dymStudyRateList;
    }

    private void insertDConvModel(BatchNerveModel model) {
        if (DConv != null) {
            if (model != null) {
                DConv.insertModel(model);
            } else {
                throw new IllegalArgumentException("可变形卷积模型为空");
            }
        }
    }

    private BatchNerveModel getDConvModel() {
        if (DConv != null) {
            return DConv.getModel();
        }
        return null;
    }

    public ResConvModel getModel() {
        ResConvModel resConvModel = new ResConvModel();
        resConvModel.setDcnModel(getDConvModel());
        List<NormModel> normModelList = new ArrayList<>();
        List<Float[]> convPowerList = new ArrayList<>();
        resConvModel.setNormModelList(normModelList);
        resConvModel.setConvPowerModelList(convPowerList);
        for (MatrixNorm matrixNorm : matrixNormList) {
            normModelList.add(matrixNorm.getModel());
        }
        for (Matrix matrix : convPower) {
            convPowerList.add(matrix.getMatrixModel());
        }
        return resConvModel;
    }

    public void insertModel(ResConvModel resConvModel) {
        insertDConvModel(resConvModel.getDcnModel());
        List<NormModel> normModelList = resConvModel.getNormModelList();
        List<Float[]> convPowerList = resConvModel.getConvPowerModelList();
        int normSize = matrixNormList.size();
        for (int i = 0; i < normSize; i++) {
            matrixNormList.get(i).insertModel(normModelList.get(i));
        }
        int nerveSize = convPower.size();
        for (int i = 0; i < nerveSize; i++) {
            convPower.get(i).insertMatrixModel(convPowerList.get(i));
        }
    }

//    public BackParameter getBackParameter() {
//        return backParameter;
//    }

    public List<Matrix> getConvPower() {
        return convPower;
    }

    public void setConvPower(List<Matrix> convPower) {
        this.convPower = convPower;
    }

    public List<MatrixNorm> getMatrixNormList() {
        return matrixNormList;
    }

    public void setMatrixNormList(List<MatrixNorm> matrixNormList) {
        this.matrixNormList = matrixNormList;
    }
}
