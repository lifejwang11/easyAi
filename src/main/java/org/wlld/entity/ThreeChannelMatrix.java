package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;

public class ThreeChannelMatrix {
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Matrix H;
    private Matrix matrixRGB;
    private int x;
    private int y;

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

    public ThreeChannelMatrix() {
    }

    public ThreeChannelMatrix(int x, int y) {
        matrixRGB = new Matrix(x, y);
    }

    public double getDist(ThreeChannelMatrix th) throws Exception {
        double subR = Math.abs(MatrixOperation.sub(matrixR, th.getMatrixR()).getAVG());
        double subG = Math.abs(MatrixOperation.sub(matrixG, th.getMatrixG()).getAVG());
        double subB = Math.abs(MatrixOperation.sub(matrixB, th.getMatrixB()).getAVG());
        return (subR + subB + subG) / 3;
    }

    public void add(double nub, boolean add) throws Exception {//对rgb矩阵曝光进行处理
        if (add) {//加数值
            MatrixOperation.mathAdd(matrixR, nub);
            MatrixOperation.mathAdd(matrixG, nub);
            MatrixOperation.mathAdd(matrixB, nub);
        } else {//减数值
            MatrixOperation.mathSub(matrixR, nub);
            MatrixOperation.mathSub(matrixG, nub);
            MatrixOperation.mathSub(matrixB, nub);
        }
    }

    //对颜色进行填充
    public void fill(int x, int y, int xSize, int ySize, double color) throws Exception {
        int xIndex = x + xSize;
        int yIndex = y + ySize;
        for (int i = x; i < xIndex; i++) {
            for (int j = y; j < yIndex; j++) {
                matrixRGB.setNub(i, j, color);
            }
        }
    }

    public Matrix late(int size) throws Exception {//池化处理
        int xn = matrixRGB.getX();
        int yn = matrixRGB.getY();
        int x = xn / size;//求导后矩阵的行数
        int y = yn / size;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//迟化后的矩阵
        for (int i = 0; i <= xn - size; i += size) {
            for (int j = 0; j <= yn - size; j += size) {
                Matrix matrix1 = matrixRGB.getSonOfMatrix(i, j, size, size);
                double maxNub;
                double n = size * size;
                double sigma = 0;
                for (int t = 0; t < matrix1.getX(); t++) {
                    for (int k = 0; k < matrix1.getY(); k++) {
                        double nub = matrix1.getNumber(t, k);
                        sigma = sigma + nub;
                    }
                }
                maxNub = sigma / n;
                //迟化的最大值是 MAXNUB
                myMatrix.setNub(i / size, j / size, maxNub);
            }
        }
        return myMatrix;
    }

    public ThreeChannelMatrix cutChannel(int x, int y, int XSize, int YSize) throws Exception {
        ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
        threeChannelMatrix.setX(XSize);
        threeChannelMatrix.setY(YSize);
        int xLen = this.matrixR.getX();
        int yLen = this.matrixR.getY();
        if (x < 0 || y < 0 || x + XSize > xLen || y + YSize > yLen) {
            throw new Exception("size out,xLen:" + xLen + ",yLen:" + yLen + "," +
                    "x:" + x + ",y:" + y + ",xSize:" + (x + XSize) + ",ySize:" + (y + YSize));
        }
        Matrix matrixR = this.matrixR.getSonOfMatrix(x, y, XSize, YSize);
        Matrix matrixG = this.matrixG.getSonOfMatrix(x, y, XSize, YSize);
        Matrix matrixB = this.matrixB.getSonOfMatrix(x, y, XSize, YSize);
        Matrix matrixH = H.getSonOfMatrix(x, y, XSize, YSize);
        threeChannelMatrix.setX(XSize);
        threeChannelMatrix.setY(YSize);
        threeChannelMatrix.setMatrixR(matrixR);
        threeChannelMatrix.setMatrixG(matrixG);
        threeChannelMatrix.setMatrixB(matrixB);
        threeChannelMatrix.setH(matrixH);
        return threeChannelMatrix;
    }

    public Matrix getMatrixRGB() {
        return matrixRGB;
    }

    public void setMatrixRGB(Matrix matrixRGB) {
        this.matrixRGB = matrixRGB;
    }

    public Matrix getH() {
        return H;
    }

    public void setH(Matrix h) {
        H = h;
    }

    public Matrix getMatrixR() {
        return matrixR;
    }

    public void setMatrixR(Matrix matrixR) {
        this.matrixR = matrixR;
    }

    public Matrix getMatrixG() {
        return matrixG;
    }

    public void setMatrixG(Matrix matrixG) {
        this.matrixG = matrixG;
    }

    public Matrix getMatrixB() {
        return matrixB;
    }

    public void setMatrixB(Matrix matrixB) {
        this.matrixB = matrixB;
    }
}
