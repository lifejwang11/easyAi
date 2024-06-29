package org.wlld.tools;

import org.wlld.entity.Box;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class NMS {
    private final double iouTh;//iou阈值

    public NMS(double iouTh) {
        this.iouTh = iouTh;
    }

    public List<Box> start(List<Box> pixelPositions) {
        //先进行排序
        if (pixelPositions.isEmpty()) {
            return null;
        }
        List<Box> pixels = new ArrayList<>();
        ConfidenceSort2 confidenceSort = new ConfidenceSort2();
        pixelPositions.sort(confidenceSort);
        screen(pixelPositions, pixels);
        return pixels;
    }

    public double getSRatio(Box box1, Box box2, boolean first) {
        IouMessage iouMessage = getMyIou(box1, box2);
        if (first) {
            return iouMessage.intersectS / iouMessage.s1;
        }
        return iouMessage.intersectS / iouMessage.s2;
    }

    private IouMessage getMyIou(Box box1, Box box2) {
        int minX1 = box1.getX();
        int minY1 = box1.getY();
        int maxX1 = minX1 + box1.getxSize();
        int maxY1 = minY1 + box1.getySize();
        double s1 = box1.getxSize() * box1.getySize();
        int minX2 = box2.getX();
        int minY2 = box2.getY();
        int maxX2 = minX2 + box2.getxSize();
        int maxY2 = minY2 + box2.getySize();
        double s2 = box2.getxSize() * box2.getySize();
        double[] row = new double[]{minX1, maxX1, minX2, maxX2};
        double[] col = new double[]{minY1, maxY1, minY2, maxY2};
        Arrays.sort(row);
        Arrays.sort(col);
        double rowSub = row[3] - row[0];
        double colSub = col[3] - col[0];
        double width = box1.getySize() + box2.getySize();
        double height = box1.getxSize() + box2.getxSize();
        double widthSub = width - colSub;
        double heightSub = height - rowSub;
        if (widthSub < 0) {
            widthSub = 0;
        }
        if (heightSub < 0) {
            heightSub = 0;
        }
        IouMessage iouMessage = new IouMessage();
        iouMessage.intersectS = widthSub * heightSub;
        iouMessage.s1 = s1;
        iouMessage.s2 = s2;
        return iouMessage;
    }

    private boolean isOne(Box box1, Box box2, double iouTh) {
        boolean isOne = false;
        IouMessage iouMessage = getMyIou(box1, box2);
        double mergeS = iouMessage.s1 + iouMessage.s2 - iouMessage.intersectS;
        double iou = iouMessage.intersectS / mergeS;
        if (iou > iouTh) {
            isOne = true;
        }
        return isOne;
    }

    private void screen(List<Box> pixelPositions, List<Box> boxes) {
        do {
            Box maxPixelPosition = pixelPositions.get(0);
            boxes.add(maxPixelPosition);
            pixelPositions.remove(0);
            for (int i = 0; i < pixelPositions.size(); i++) {
                Box box = pixelPositions.get(i);
                if (isOne(maxPixelPosition, box, iouTh)) {//要移除
                    pixelPositions.remove(i);
                    i--;
                }
            }
        } while (pixelPositions.size() > 0);
    }

    static class ConfidenceSort2 implements Comparator<Box> {

        @Override
        public int compare(Box o1, Box o2) {
            if (o1.getConfidence() > o2.getConfidence()) {
                return -1;
            } else if (o1.getConfidence() < o2.getConfidence()) {
                return 1;
            }
            return 0;
        }
    }

    static class IouMessage {
        double intersectS;
        double s1;
        double s2;
    }
}
