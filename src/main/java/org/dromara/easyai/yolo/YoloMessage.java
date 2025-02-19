package org.dromara.easyai.yolo;


import org.dromara.easyai.entity.ThreeChannelMatrix;

public class YoloMessage {
    private float distX;
    private float distY;
    private float width;
    private float height;
    private float trust;
    private int mappingID;
    private ThreeChannelMatrix pic;
    private TypeBody typeBody;
    private boolean backGround = false;//背景

    public boolean isBackGround() {
        return backGround;
    }

    public void setBackGround(boolean backGround) {
        this.backGround = backGround;
    }

    public TypeBody getTypeBody() {
        return typeBody;
    }

    public void setTypeBody(TypeBody typeBody) {
        this.typeBody = typeBody;
    }

    public int getMappingID() {
        return mappingID;
    }

    public void setMappingID(int mappingID) {
        this.mappingID = mappingID;
    }

    public ThreeChannelMatrix getPic() {
        return pic;
    }

    public void setPic(ThreeChannelMatrix pic) {
        this.pic = pic;
    }

    public float getTrust() {
        return trust;
    }

    public void setTrust(float trust) {
        this.trust = trust;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getDistX() {
        return distX;
    }

    public void setDistX(float distX) {
        this.distX = distX;
    }

    public float getDistY() {
        return distY;
    }

    public void setDistY(float distY) {
        this.distY = distY;
    }
}
