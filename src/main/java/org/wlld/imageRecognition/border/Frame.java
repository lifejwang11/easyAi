package org.wlld.imageRecognition.border;

/**
 * @author lidapeng
 * @description 先验检测边框
 * @date 11:16 上午 2020/1/26
 */
public class Frame {
    private int width;//检测边框的宽
    private int height;//检测边框的高
    private int lengthWidth;//宽一次走多长
    private int lengthHeight;//高一次走多长

    public boolean isReady() {
        if (width > 0 && height > 0 && lengthWidth > 0 && lengthHeight > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLengthWidth() {
        return lengthWidth;
    }

    public void setLengthWidth(int lengthWidth) {
        this.lengthWidth = lengthWidth;
    }

    public int getLengthHeight() {
        return lengthHeight;
    }

    public void setLengthHeight(int lengthHeight) {
        this.lengthHeight = lengthHeight;
    }
}
