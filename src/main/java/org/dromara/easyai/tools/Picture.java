package org.dromara.easyai.tools;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.entity.ThreeChannelMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * @author lidapeng
 * 图片工具类
 */
public class Picture {
    /**
     * 创建一个指定大小的纯白图像并返回
     *
     * @param x 图像像素的行数
     * @param y 图像的列数
     * @return 返回图像三通道矩阵
     */
    public static ThreeChannelMatrix getMyPicture(int x, int y) throws Exception {
        Matrix matrixR = new Matrix(x, y);
        Matrix matrixG = new Matrix(x, y);
        Matrix matrixB = new Matrix(x, y);
        ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
        threeChannelMatrix.setX(x);
        threeChannelMatrix.setY(y);
        threeChannelMatrix.setMatrixR(matrixR);
        threeChannelMatrix.setMatrixG(matrixG);
        threeChannelMatrix.setMatrixB(matrixB);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrixR.setNub(i, j, 1);
                matrixG.setNub(i, j, 1);
                matrixB.setNub(i, j, 1);
            }
        }
        return threeChannelMatrix;
    }

    /**
     * 从本地文件拿出图像矩阵
     *
     * @param fileURL 图片本地地址
     * @return Matrix
     * @throws Exception
     */
    public static Matrix getImageMatrixByLocal(String fileURL) throws Exception {
        File file = new File(fileURL);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(bi);
    }

    public static Matrix getImageMatrixByFile(File file) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(bi);
    }

    /**
     * 获取图片的RGB三通道矩阵
     *
     * @param file     文件
     * @param vertical 是否强制竖直
     * @return threeChannelMatrix
     * @throws Exception
     */
    public static ThreeChannelMatrix getThreeMatrix(File file, boolean vertical) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getThreeChannel(bi, vertical);
    }

    public static ThreeChannelMatrix getThreeMatrix(InputStream file, boolean vertical) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getThreeChannel(bi, vertical);
    }

    public static ThreeChannelMatrix getThreeMatrix(String fileURL, boolean vertical) throws Exception {
        File file = new File(fileURL);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getThreeChannel(bi, vertical);
    }

    //
    public static Matrix getImageMatrixByIo(InputStream inputStream) throws Exception {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getImage(bi);
    }

    private static Matrix getImage(BufferedImage bi) throws Exception {
        int width = bi.getWidth();//最大宽度
        int height = bi.getHeight();//最大高度
        Matrix matrix = new Matrix(height, width);//行，列
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = bi.getRGB(j, i);// 下面三行代码将一个数字转换为RGB数字
                float grab = dimensionReduction(pixel);//抽取灰度
                matrix.setNub(i, j, grab);
            }
        }
        return matrix;
    }

    public static ThreeChannelMatrix getThreeChannel(BufferedImage bi, boolean vertical) throws Exception {
        //最大宽度
        int width = bi.getWidth();
        //最大高度
        int height = bi.getHeight();
        boolean rotate = false;
        if (vertical && width > height) {
            rotate = true;
            //最大宽度
            width = bi.getHeight();
            //最大高度
            height = bi.getWidth();
        }
        ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
        threeChannelMatrix.setX(height);
        threeChannelMatrix.setY(width);
        //行，列
        Matrix matrixR = new Matrix(height, width);
        Matrix matrixG = new Matrix(height, width);
        Matrix matrixB = new Matrix(height, width);
        Matrix matrixH = new Matrix(height, width);
        threeChannelMatrix.setMatrixR(matrixR);
        threeChannelMatrix.setMatrixG(matrixG);
        threeChannelMatrix.setMatrixB(matrixB);
        threeChannelMatrix.setH(matrixH);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 下面三行代码将一个数字转换为RGB数字
                int pixel;
                if (rotate) {
                    // 下面三行代码将一个数字转换为RGB数字
                    pixel = bi.getRGB(i, j);
                } else {
                    pixel = bi.getRGB(j, i);
                }
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                matrixR.setNub(i, j, r / 255f);
                matrixG.setNub(i, j, g / 255f);
                matrixB.setNub(i, j, b / 255f);
                matrixH.setNub(i, j, ((r * 38 + g * 75 + b * 15) >> 7) / 255f);
            }
        }
        return threeChannelMatrix;
    }

    private static float dimensionReduction(int pixel) {//提取灰度进行降维
        int r = (pixel & 0xff0000) >> 16;//R
        int g = (pixel & 0xff00) >> 8;//G
        int b = (pixel & 0xff);//B
        float gray = (r * 38 + g * 75 + b * 15) >> 7;
        return gray;
    }


}
