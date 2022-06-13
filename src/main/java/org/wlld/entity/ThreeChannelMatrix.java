package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;

public class ThreeChannelMatrix {
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Matrix H;
    private Matrix matrixRGB;

    public ThreeChannelMatrix() {
    }

    public ThreeChannelMatrix(int x, int y) {
        matrixRGB = new Matrix(x, y);
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
