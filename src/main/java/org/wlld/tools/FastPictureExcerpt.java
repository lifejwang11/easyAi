package org.wlld.tools;

import org.wlld.MatrixTools.Matrix;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.regressionForest.LinearRegression;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class FastPictureExcerpt {//图片摘要id生成

    public static void main(String[] args) throws Exception {
        Picture picture = new Picture();
        ThreeChannelMatrix t = picture.getThreeMatrix("E:\\pname\\a.jpg");
        long a = System.currentTimeMillis();
        cut(t, 100, "E:\\pname\\f.png");
        long b = System.currentTimeMillis() - a;
        System.out.println("b==" + b);
//        File file = new File("E:\\pname\\t.png");
//        InputStream inputStream = new FileInputStream(file);
//        byte[] a = new byte[inputStream.available()];
//        inputStream.read(a);
//        compare(a, b);
    }

    public static void compare(byte[] a, byte[] b) throws Exception {
        if (a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    throw new Exception("对比不一致");
                }
            }
        } else {
            System.out.println("长度不一致");
        }
    }

    public static ByteArrayOutputStream cut(ThreeChannelMatrix imageMatrix, int boxSize, String url) throws Exception {
        int xSize = imageMatrix.getMatrixR().getX();
        int ySize = imageMatrix.getMatrixR().getY();
        int size;
        if (xSize > ySize) {
            size = ySize;
        } else {
            size = xSize;
        }
        int x = xSize / 2 - size / 2;
        int y = ySize / 2 - size / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return narrow(imageMatrix.cutChannel(x, y, size, size), boxSize, url);
    }

    private static ByteArrayOutputStream narrow(ThreeChannelMatrix imageMatrix, int size, String url) throws Exception {//图片缩小
        Matrix matrix = imageMatrix.getMatrixB();
        int maxXSize = matrix.getX();
        int maxYSize = matrix.getY();
        int xKern = maxXSize / size;
        int yKern = maxYSize / size;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        int xIndex = 0;
        int yIndex = 0;
        for (int i = 0; i < maxXSize - xKern; i += xKern) {
            for (int j = 0; j < maxYSize - yKern; j += yKern) {
                ThreeChannelMatrix threeChannelMatrix = imageMatrix.cutChannel(i, j, xKern, yKern);
                Matrix matrixR = threeChannelMatrix.getMatrixR();
                Matrix matrixG = threeChannelMatrix.getMatrixG();
                Matrix matrixB = threeChannelMatrix.getMatrixB();
                int r = (int) (matrixR.getAVG() * 255);
                int g = (int) (matrixG.getAVG() * 255);
                int b = (int) (matrixB.getAVG() * 255);
                g2.setColor(new Color(r, g, b));
                g2.drawRect(yIndex, xIndex, 1, 1);
                yIndex++;
            }
            yIndex = 0;
            xIndex++;
        }
        ByteArrayOutputStream ar = new ByteArrayOutputStream();
        //File file = new File(url);
        ImageIO.write(bi, "PNG", ar);
        return ar;
    }

    //String name = creatImageName(threeChannelMatrix, 5, 10);
    // 图像矩阵，横纵各分多少个区域，余弦区域分几份
    public static String creatImageName(ThreeChannelMatrix threeChannelMatrix, int boxSize, int regionSize) throws Exception {//生成文件名
        int iSize = 5;
        Matrix vector = new Matrix(1, 3);
        vector.setNub(0, 0, 1);
        vector.setNub(0, 1, 0);
        vector.setNub(0, 2, 0);
        Matrix h = threeChannelMatrix.getH();
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
