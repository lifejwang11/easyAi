package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class Picture {
    private int pictureWidth;
    private int pictureHeight;

    //从本地文件拿出图像矩阵
    public Matrix getImageMatrixByLocal(String fileURL) throws Exception {
        File file = new File(fileURL);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(bi);
    }

    public ThreeChannelMatrix getThreeMatrix(InputStream file) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.getThreeChannel(bi);
    }

    public ThreeChannelMatrix getThreeMatrix(String fileURL) throws Exception {
        File file = new File(fileURL);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getThreeChannel(bi);
    }

    //
    public Matrix getImageMatrixByIo(InputStream inputStream) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(bi);
    }

    private Matrix getImage(BufferedImage bi) throws Exception {
        int width = bi.getWidth();//最大宽度
        int height = bi.getHeight();//最大高度
        pictureWidth = width;
        pictureHeight = height;
        Matrix matrix = new Matrix(height, width);//行，列
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = bi.getRGB(j, i);// 下面三行代码将一个数字转换为RGB数字
                double grab = dimensionReduction(pixel);//抽取灰度
                matrix.setNub(i, j, grab);
            }
        }
        return matrix;
    }

    private ThreeChannelMatrix getThreeChannel(BufferedImage bi) throws Exception {
        int width = bi.getWidth();//最大宽度
        int height = bi.getHeight();//最大高度
        ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
        Matrix matrixR = new Matrix(height, width);//行，列
        Matrix matrixG = new Matrix(height, width);//行，列
        Matrix matrixB = new Matrix(height, width);//行，列
        threeChannelMatrix.setMatrixR(matrixR);
        threeChannelMatrix.setMatrixG(matrixG);
        threeChannelMatrix.setMatrixB(matrixB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = bi.getRGB(j, i);// 下面三行代码将一个数字转换为RGB数字
                matrixR.setNub(i, j, (pixel & 0xff0000) >> 16);
                matrixG.setNub(i, j, (pixel & 0xff00) >> 8);
                matrixB.setNub(i, j, (pixel & 0xff));
            }
        }
        return threeChannelMatrix;
    }

    public double dimensionReduction(int pixel) {//提取灰度进行降维
        int r = (pixel & 0xff0000) >> 16;//R
        int g = (pixel & 0xff00) >> 8;//G
        int b = (pixel & 0xff);//B
        double gray = (r * 38 + g * 75 + b * 15) >> 7;
        return gray;
    }

    public int getPictureWidth() {
        return pictureWidth;
    }

    public void setPictureWidth(int pictureWidth) {
        this.pictureWidth = pictureWidth;
    }

    public int getPictureHeight() {
        return pictureHeight;
    }

    public void setPictureHeight(int pictureHeight) {
        this.pictureHeight = pictureHeight;
    }
}
