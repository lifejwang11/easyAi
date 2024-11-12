package org.wlld.entity;


import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;

public class ThreeChannelMatrix {
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Matrix H;
    private int x;
    private int y;
    private final MatrixOperation matrixOperation = new MatrixOperation();

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

    public double getDist(ThreeChannelMatrix th) throws Exception {
        if (th.getX() == x && th.getY() == y) {
            double subR = matrixOperation.getEDistByMatrix(matrixR, th.getMatrixR());
            double subG = matrixOperation.getEDistByMatrix(matrixG, th.getMatrixG());
            double subB = matrixOperation.getEDistByMatrix(matrixB, th.getMatrixB());
            return (subR + subB + subG) / 3;
        } else {
            throw new Exception("图像尺寸大小不匹配，本图像尺寸x是：" + x + ",y:" + y + "。待匹配尺寸图像 x:" + th.getX() +
                    ",y:" + th.getY());
        }
    }

    public ThreeChannelMatrix scale(boolean scaleWidth, double size) throws Exception {//缩放图像
        double value;
        if (scaleWidth) {//将宽度等比缩放至指定尺寸
            value = y / size;
        } else {//将高度等比缩放至指定尺寸
            value = x / size;
        }
        int narrowX = (int) (x / value);
        int narrowY = (int) (y / value);
        ThreeChannelMatrix scaleMatrix = new ThreeChannelMatrix();
        scaleMatrix.setX(narrowX);
        scaleMatrix.setY(narrowY);
        Matrix matrixCR = new Matrix(narrowX, narrowY);
        Matrix matrixCG = new Matrix(narrowX, narrowY);
        Matrix matrixCB = new Matrix(narrowX, narrowY);
        Matrix matrixCH = new Matrix(narrowX, narrowY);
        scaleMatrix.setMatrixR(matrixCR);
        scaleMatrix.setMatrixG(matrixCG);
        scaleMatrix.setMatrixB(matrixCB);
        scaleMatrix.setH(matrixCH);
        for (int i = 0; i < narrowX; i++) {
            for (int j = 0; j < narrowY; j++) {
                int indexX = (int) (i * value);
                int indexY = (int) (j * value);
                matrixCR.setNub(i, j, matrixR.getNumber(indexX, indexY));
                matrixCG.setNub(i, j, matrixG.getNumber(indexX, indexY));
                matrixCB.setNub(i, j, matrixB.getNumber(indexX, indexY));
            }
        }
        return scaleMatrix;
    }

    public void add(double nub, boolean add) throws Exception {//对rgb矩阵曝光进行处理
        if (add) {//加数值
            matrixOperation.mathAdd(matrixR, nub);
            matrixOperation.mathAdd(matrixG, nub);
            matrixOperation.mathAdd(matrixB, nub);
        } else {//减数值
            matrixOperation.mathSub(matrixR, nub);
            matrixOperation.mathSub(matrixG, nub);
            matrixOperation.mathSub(matrixB, nub);
        }
    }

    public void center() throws Exception {
        center(matrixR);
        center(matrixG);
        center(matrixB);
    }

    private void center(Matrix matrix) throws Exception {
        double avg = matrix.getAVG();
        matrixOperation.mathSub(matrix, avg);
    }

    public ThreeChannelMatrix copy() throws Exception {//复制当前的三通道矩阵并返回
        ThreeChannelMatrix copyThreeChannelMatrix = new ThreeChannelMatrix();
        copyThreeChannelMatrix.setX(this.x);
        copyThreeChannelMatrix.setY(this.y);
        Matrix matrixCR = new Matrix(this.x, this.y);
        Matrix matrixCG = new Matrix(this.x, this.y);
        Matrix matrixCB = new Matrix(this.x, this.y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrixCR.setNub(i, j, matrixR.getNumber(i, j));
                matrixCG.setNub(i, j, matrixG.getNumber(i, j));
                matrixCB.setNub(i, j, matrixB.getNumber(i, j));
            }
        }
        copyThreeChannelMatrix.setMatrixR(matrixCR);
        copyThreeChannelMatrix.setMatrixG(matrixCG);
        copyThreeChannelMatrix.setMatrixB(matrixCB);
        return copyThreeChannelMatrix;
    }

    //将一个图像填充到本图像的指定位置
    public void fill(int x, int y, ThreeChannelMatrix fillThreeChannelMatrix) throws Exception {
        int xIndex = x + fillThreeChannelMatrix.getX();
        int yIndex = y + fillThreeChannelMatrix.getY();
        Matrix matrixFR = fillThreeChannelMatrix.getMatrixR();
        Matrix matrixFG = fillThreeChannelMatrix.getMatrixG();
        Matrix matrixFB = fillThreeChannelMatrix.getMatrixB();
        if (xIndex <= this.x && yIndex <= this.y) {
            for (int i = x; i < xIndex; i++) {
                for (int j = y; j < yIndex; j++) {
                    matrixR.setNub(i, j, matrixFR.getNumber(i - x, j - y));
                    matrixG.setNub(i, j, matrixFG.getNumber(i - x, j - y));
                    matrixB.setNub(i, j, matrixFB.getNumber(i - x, j - y));
                }
            }
        } else {
            throw new Exception("The filled image goes beyond the boundary !");
        }
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
