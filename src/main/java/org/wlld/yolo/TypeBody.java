package org.wlld.yolo;

import org.wlld.config.RZ;
import org.wlld.function.ReLu;
import org.wlld.function.Tanh;
import org.wlld.nerveCenter.NerveManager;

import java.util.ArrayList;
import java.util.List;

public class TypeBody {
    private int typeID;//该类型的类别id
    private int mappingID;//该类型的映射id
    private int maxWidth = 0;//该类别选择框最大宽度
    private int maxHeight = 0;//该类别映射的最大高度
    private int minWidth = -1;//该类别最小宽度
    private int minHeight = -1;//该类别最小高度
    private final NerveManager positonNerveManager;//定位网络
    private List<YoloBody> yoloBodies = new ArrayList<>();//该类别下所有的数据集合


    public NerveManager getPositonNerveManager() {
        return positonNerveManager;
    }

    public TypeBody(YoloConfig yoloConfig, int minWinWidth, int minWinHeight) throws Exception {
        positonNerveManager = new NerveManager(3, yoloConfig.getHiddenNerveNub(), 5, 1,
                new Tanh(), yoloConfig.getLineStudy(), RZ.L1, yoloConfig.getLineStudy() * yoloConfig.getRegular());
        positonNerveManager.initImageNet(2, yoloConfig.getKernelSize(), minWinHeight, minWinWidth,
                false, yoloConfig.isShowLog(), yoloConfig.getConvStudy(), new ReLu());
    }

    public double getRealWidth(double width) {
        if (maxWidth == minWidth) {
            return maxWidth;
        }
        return (maxWidth - minWidth) * width + minWidth;
    }

    public double getRealHeight(double height) {
        if (maxHeight == minHeight) {
            return maxHeight;
        }
        return (maxHeight - minHeight) * height + minHeight;
    }

    public double getOneWidth(int width) {
        if (maxWidth == minWidth) {
            return 1;
        }
        return (double) (width - minWidth) / (maxWidth - minWidth);
    }

    public double getOneHeight(int height) {
        if (maxHeight == minHeight) {
            return 1;
        }
        return (double) (height - minHeight) / (maxHeight - minHeight);
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
