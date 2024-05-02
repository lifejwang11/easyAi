package org.wlld.yolo;


import org.wlld.entity.ThreeChannelMatrix;

public class YoloMessage {
    private double distX;
    private double distY;
    private double width;
    private double height;
    private double trust;
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

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getDistX() {
        return distX;
    }

    public void setDistX(double distX) {
        this.distX = distX;
    }

    public double getDistY() {
        return distY;
    }

    public void setDistY(double distY) {
        this.distY = distY;
    }
}
