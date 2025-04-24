package org.dromara.easyai.yolo;

import org.dromara.easyai.config.ResnetConfig;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.nerveCenter.NerveManager;
import org.dromara.easyai.resnet.ResnetManager;

import java.util.ArrayList;
import java.util.List;

public class TypeBody {
    private int typeID;//该类型的类别id
    private int mappingID;//该类型的映射id
    private int maxWidth = 0;//该类别选择框最大宽度
    private int maxHeight = 0;//该类别映射的最大高度
    private int minWidth = -1;//该类别最小宽度
    private int minHeight = -1;//该类别最小高度
    private int times = 0;//训练出现次数
    private final int winWidth;
    private final int winHeight;
    private final NerveManager positionNerveManager;//定位网络

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    private List<YoloBody> yoloBodies = new ArrayList<>();//该类别下所有的数据集合

    public int getWinWidth() {
        return winWidth;
    }

    public int getWinHeight() {
        return winHeight;
    }

    public NerveManager getPositionNerveManager() {
        return positionNerveManager;
    }

    public TypeBody(YoloConfig yoloConfig, int minWinWidth, int minWinHeight) throws Exception {
        //是否为轻量模型
        winWidth = minWinWidth;
        winHeight = minWinHeight;
        positionNerveManager = new NerveManager(3, yoloConfig.getHiddenNerveNub(), 5, 1,
                new Tanh(), yoloConfig.getPositionStudyRate(), yoloConfig.getRegularModel(), yoloConfig.getRegular()
                , yoloConfig.getCoreNumber(), yoloConfig.getGaMa(), yoloConfig.getGMaxTh(), yoloConfig.isAuto());
        positionNerveManager.initImageNet(yoloConfig.getChannelNo(), yoloConfig.getKernelSize(), minWinHeight, minWinWidth,
                false, false, yoloConfig.getPositionStudyRate(), new ReLu(),
                yoloConfig.getMinFeatureValue(), yoloConfig.getOneConvStudy(), yoloConfig.isNorm());
    }

    public float getRealWidth(float width) {
        if (maxWidth == minWidth) {
            return maxWidth;
        }
        return (maxWidth - minWidth) * width + minWidth;
    }

    public float getRealHeight(float height) {
        if (maxHeight == minHeight) {
            return maxHeight;
        }
        return (maxHeight - minHeight) * height + minHeight;
    }

    public float getOneWidth(int width) {
        if (maxWidth == minWidth) {
            return 1;
        }
        return (float) (width - minWidth) / (maxWidth - minWidth);
    }

    public float getOneHeight(int height) {
        if (maxHeight == minHeight) {
            return 1;
        }
        return (float) (height - minHeight) / (maxHeight - minHeight);
    }

    public void insertYoloBody(YoloBody yoloBody) {
        if (yoloBody.getWidth() > maxWidth) {
            maxWidth = yoloBody.getWidth();
        }
        if (yoloBody.getHeight() > maxHeight) {
            maxHeight = yoloBody.getHeight();
        }
        if (minWidth < 0 || yoloBody.getWidth() < minWidth) {
            minWidth = yoloBody.getWidth();
        }
        if (minHeight < 0 || yoloBody.getHeight() < minHeight) {
            minHeight = yoloBody.getHeight();
        }
        yoloBodies.add(yoloBody);
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public List<YoloBody> getYoloBodies() {
        return yoloBodies;
    }

    public void setYoloBodies(List<YoloBody> yoloBodies) {
        this.yoloBodies = yoloBodies;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getMappingID() {
        return mappingID;
    }

    public void setMappingID(int mappingID) {
        this.mappingID = mappingID;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }
}
