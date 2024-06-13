package org.wlld.tools;

import org.wlld.MatrixTools.Matrix;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.regressionForest.LinearRegression;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class FastPictureExcerpt {//图片摘要id生成

    public String creatImageName(ThreeChannelMatrix threeChannelMatrix, int boxSize, int regionSize) throws Exception {
        String R = creatImageName(threeChannelMatrix.getMatrixR(), boxSize, regionSize);
        String G = creatImageName(threeChannelMatrix.getMatrixG(), boxSize, regionSize);
        String B = creatImageName(threeChannelMatrix.getMatrixB(), boxSize, regionSize);
        return R + G + B;
    }

    //String name = creatImageName(threeChannelMatrix, 5, 10);
    // 图像矩阵，横纵各分多少个区域，余弦区域分几份
    private String creatImageName(Matrix h, int boxSize, int regionSize) throws Exception {//生成文件名
        int iSize = 5;
        Matrix vector = new Matrix(1, 3);
        vector.setNub(0, 0, 1);
        vector.setNub(0, 1, 0);
        vector.setNub(0, 2, 0);
        int xf = h.getX();
        int yf = h.getY();
        int xMO = (xf % boxSize) / 2;
        int yMO = (yf % boxSize) / 2;
        int xSize = xf / boxSize;
        int ySize = yf / boxSize;
        Matrix hr = h.getSonOfMatrix(xMO, yMO, xSize * boxSize, ySize * boxSize);
        int x = hr.getX();
        int y = hr.getY();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= x - xSize; i += xSize) {
            for (int j = 0; j <= y - ySize; j += ySize) {
                Matrix sonH = hr.getSonOfMatrix(i, j, xSize, ySize);
                String name = getName(sonH, iSize, vector, regionSize);
                if (regionSize > 10 && regionSize <= 100) {
                    if (name.length() == 1) {
                        name = "0" + name;
                    }
                } else if (regionSize > 100 && regionSize <= 1000) {
                    if (name.length() == 1) {
                        name = "00" + name;
                    } else if (name.length() == 2) {
                        name = "0" + name;
                    }
                }
                stringBuilder.append(name);
            }
        }
        return stringBuilder.toString();
    }

    private static String getName(Matrix h, int iSize, Matrix vector, int regionSize) throws Exception {
        int x = h.getX();
        int y = h.getY();
        int size = (x / iSize) * (y / iSize);
        LinearRegression linearRegression = new LinearRegression(size);
        int cPoint = iSize / 2 + 1;
        double maxXSize = (double) x / iSize;
        double maxYSize = (double) y / iSize;
        double[] xy = new double[2];
        for (int i = 0; i <= x - iSize; i += iSize) {
            for (int j = 0; j <= y - iSize; j += iSize) {
                double value = h.getSonOfMatrix(i, j, iSize, iSize).getNumber(cPoint, cPoint);//灰度值
                double px = i / (double) iSize / maxXSize;
                double py = j / (double) iSize / maxYSize;
                xy[0] = px;
                xy[1] = py;
                linearRegression.insertXY(xy, value);
            }
        }
        linearRegression.regression();
        double myCos = linearRegression.getCos(vector);//余弦
        double oneSize = 1 / (double) regionSize;//分几个区间
        int index = 0;
        double minSub = -1;
        for (int i = 0; i < regionSize; i++) {
            double cos = Math.cos(Math.PI * oneSize * i);
            double sub = Math.abs(cos - myCos);
            if (minSub == -1 || sub < minSub) {
                minSub = sub;
                index = i;
            }
        }
        return String.valueOf(index);
    }
}
