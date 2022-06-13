package org.wlld.entity;

public class PicturePosition {
    private String url;//照片本地地址
    private int x;
    private int y;
    private int XSize;
    private int YSize;
    private boolean isNeedCut = false;//是否需要切割

    public PicturePosition() {

    }

    public PicturePosition(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getXSize() {
        return XSize;
    }

    public void setXSize(int XSize) {
        this.XSize = XSize;
    }

    public int getYSize() {
        return YSize;
    }

    public void setYSize(int YSize) {
        this.YSize = YSize;
    }

    public boolean isNeedCut() {
        return isNeedCut;
    }

    public void setNeedCut(boolean needCut) {
        isNeedCut = needCut;
    }
}
