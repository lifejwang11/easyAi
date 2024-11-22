package org.dromara.easyai.transFormer.seflAttention;

import org.dromara.easyai.matrixTools.Matrix;

public class EventBody {
    private long eventID;//事件id
    private Matrix featureMatrix;//特征矩阵
    private int selfID;//该多头注意力层id
    private int number;//次数

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSelfID() {
        return selfID;
    }

    public void setSelfID(int selfID) {
        this.selfID = selfID;
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public Matrix getFeatureMatrix() {
        return featureMatrix;
    }

    public void setFeatureMatrix(Matrix featureMatrix) {
        this.featureMatrix = featureMatrix;
    }
}
