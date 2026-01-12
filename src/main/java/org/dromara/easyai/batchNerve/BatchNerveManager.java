package org.dromara.easyai.batchNerve;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.CustomEncoding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2026/1/7 14:43
 * @des 批量线性分类器管理类
 */
public class BatchNerveManager {
    private final List<QBlock> qBlockList = new ArrayList<>();
    private BatchInputBlock inputBlock;

    public BatchInputBlock getInputBlock() {
        return inputBlock;
    }

    public BatchNerveModel getModel() {
        BatchNerveModel batchNerveModel = new BatchNerveModel();
        List<QBlockModel> blockModelList = new ArrayList<>();
        for (QBlock qBlock : qBlockList) {
            blockModelList.add(qBlock.getModel());
        }
        batchNerveModel.setBlockModelList(blockModelList);
        return batchNerveModel;
    }

    public void insertModel(BatchNerveModel batchNerveModel) {
        List<QBlockModel> blockModelList = batchNerveModel.getBlockModelList();
        for (int i = 0; i < qBlockList.size(); i++) {
            qBlockList.get(i).insertModel(blockModelList.get(i));
        }
    }

    public BatchNerveManager(BatchNerveConfig batchNerveConfig, ActiveFunction activeFunction, CustomEncoding customEncoding) throws Exception {
        int deep = batchNerveConfig.getDeep();//深度
        float studyRate = batchNerveConfig.getStudyRate();
        int inputSize = batchNerveConfig.getInputSize();//输入参数数量
        int hiddenSize = batchNerveConfig.getHiddenSize();//隐层神经元数量
        int outSize = batchNerveConfig.getOutSize();//输出神经元数量
        boolean softMax = batchNerveConfig.isSoftMax();
        DymStudy dymStudy = new DymStudy(batchNerveConfig.getGaMa(), batchNerveConfig.getGMaxTh(), batchNerveConfig.isAuto());
        for (int i = 0; i < deep; i++) {
            QBlock qBlock;
            if (i == 0) {
                qBlock = new QBlock(dymStudy, inputSize, hiddenSize, activeFunction, studyRate, customEncoding, batchNerveConfig.isShowLog()
                        , batchNerveConfig.getRegularModel(), batchNerveConfig.getRegular());
            } else {
                qBlock = new QBlock(dymStudy, hiddenSize, hiddenSize, activeFunction, studyRate, null, batchNerveConfig.isShowLog()
                        , batchNerveConfig.getRegularModel(), batchNerveConfig.getRegular());
            }
            qBlockList.add(qBlock);
        }
        QBlock qBlock = new QBlock(dymStudy, hiddenSize, outSize, activeFunction, studyRate, null, batchNerveConfig.isShowLog(),
                batchNerveConfig.getRegularModel(), batchNerveConfig.getRegular());
        if (softMax) {
            SoftMaxByQBlock softMaxByQBlock = new SoftMaxByQBlock(qBlock, batchNerveConfig.isShowLog());
            qBlock.setSoftMaxByQBlock(softMaxByQBlock);
        }
        qBlockList.add(qBlock);
        connection(inputSize);
    }

    private void connection(int inputSize) {
        int size = qBlockList.size();
        for (int i = 0; i < size - 1; i++) {
            QBlock qBlock = qBlockList.get(i);
            QBlock nextQBlock = qBlockList.get(i + 1);
            qBlock.setSonBlock(nextQBlock);
            nextQBlock.setFatherBlock(qBlock);
        }
        inputBlock = new BatchInputBlock(qBlockList.get(0), inputSize);
    }

}
