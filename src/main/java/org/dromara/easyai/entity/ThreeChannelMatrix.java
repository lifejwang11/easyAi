package org.dromara.easyai.entity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

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

    public float getDist(ThreeChannelMatrix th) throws Exception {
        if (th.getX() == x && th.getY() == y) {
            float subR = matrixOperation.getEDistByMatrix(matrixR, th.getMatrixR());
            float subG = matrixOperation.getEDistByMatrix(matrixG, th.getMatrixG());
            float subB = matrixOperation.getEDistByMatrix(matrixB, th.getMatrixB());
            return (subR + subB + subG) / 3;
        } else {
            throw new Exception("图像尺寸大小不匹配，本图像尺寸x是：" + x + ",y:" + y + "。待匹配尺寸图像 x:" + th.getX() +
                    ",y:" + th.getY());
        }
    }

    public void grayscaleScale(float toR, float toG, float toB) throws Exception {//灰度缩放
        MatrixOperation matrixOperation = new MatrixOperation();
        float avgR = matrixR.getAVG();
        float avgG = matrixG.getAVG();
        float avgB = matrixB.getAVG();
        float rk = toR / avgR;
        float gk = toG / avgG;
        float bk = toB / avgB;
        matrixOperation.mathMul(matrixR, rk);
        matrixOperation.mathMul(matrixG, gk);
        matrixOperation.mathMul(matrixB, bk);
    }

    //生成一个高斯核
    private Matrix createGaussianKern(float sd, int kerSize) throws Exception {
        float a = (float) (1 / (2 * (float) Math.PI * (float) Math.pow(sd, 2)));
        Matrix matrix = new Matrix(kerSize, kerSize);
        int half = kerSize / 2;
        for (int i = 0; i < kerSize; i++) {
            for (int j = 0; j < kerSize; j++) {
                int xIndex = i - half;
                int yIndex = j - half;
                float b = (float) Math.exp(-((float) Math.pow(xIndex, 2) + (float) Math.pow(yIndex, 2)) / (2 * (float) Math.pow(sd, 2)));
                matrix.setNub(i, j, a * b);
            }
        }
        float sigma = matrix.getSigma();
        matrixOperation.mathDiv(matrix, sigma);
        return matrix;
    }

    public ThreeChannelMatrix rotate(double rotateSize) throws Exception {//旋转
        ThreeChannelMatrix outPic = new ThreeChannelMatrix();
        Matrix matrixRR = new Matrix(x, y);
        Matrix matrixGG = new Matrix(x, y);
        Matrix matrixBB = new Matrix(x, y);
        outPic.setX(x);
        outPic.setY(y);
        outPic.setMatrixR(matrixRR);
        outPic.setMatrixG(matrixGG);
        outPic.setMatrixB(matrixBB);
        double centerX = x / 2d;//中心点坐标
        double centerY = y / 2d;//中心点坐标
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nj = (j - centerY) * Math.cos(rotateSize) - (i - centerX) * Math.sin(rotateSize) + centerY;
                double ni = (j - centerY) * Math.sin(rotateSize) + (i - centerX) * Math.cos(rotateSize) + centerX;
                int tj = (int) nj;
                int ti = (int) ni;
                float r = matrixR.getNumber(i, j);
                float g = matrixG.getNumber(i, j);
                float b = matrixB.getNumber(i, j);
                if (ti > 0 && ti < x && tj > 0 && tj < y) {
                    matrixRR.setNub(ti, tj, r);
                    matrixGG.setNub(ti, tj, g);
                    matrixBB.setNub(ti, tj, b);
                }
            }
        }
        return outPic;
    }

    public Matrix calculateAvgGrayscale() throws Exception {//计算均值灰度
        Matrix matrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = (matrixR.getNumber(i, j) + matrixG.getNumber(i, j) + matrixB.getNumber(i, j)) / 3F;
                matrix.setNub(i, j, value);
            }
        }
        return matrix;
    }

    public ThreeChannelMatrix gaussianVague(float sd, int kerSize, boolean scaleWidth, float scaleSize) throws Exception {//高斯模糊
        if (kerSize % 2 != 1) {
            throw new Exception("高斯核大小必须为奇数");
        }
        ThreeChannelMatrix threeChannelMatrix = scale(scaleWidth, scaleSize);
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        Matrix gaussianMatrix = createGaussianKern(sd, kerSize);
        Matrix gr = conv(matrixR, gaussianMatrix, kerSize);
        Matrix gg = conv(matrixG, gaussianMatrix, kerSize);
        Matrix gb = conv(matrixB, gaussianMatrix, kerSize);
        ThreeChannelMatrix vague = new ThreeChannelMatrix();
        vague.setX(gr.getX());
        vague.setY(gr.getY());
        vague.setMatrixR(gr);
        vague.setMatrixG(gg);
        vague.setMatrixB(gb);
        return vague;
    }

    private float getConv(Matrix conv, Matrix gaussianMatrix) throws Exception {
        int x = gaussianMatrix.getX();
        int y = gaussianMatrix.getY();
        float sigma = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = conv.getNumber(i, j) * gaussianMatrix.getNumber(i, j);
                sigma = sigma + value;
            }
        }
        return sigma;
    }

    private Matrix conv(Matrix matrix, Matrix gaussianMatrix, int kerSize) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        int half = kerSize / 2;
        Matrix convMatrix = new Matrix(x - half * 2, y - half * 2);
        for (int i = half; i < x - half; i++) {
            for (int j = half; j < y - half; j++) {
                Matrix conv = matrix.getSonOfMatrix(i - half, j - half, kerSize, kerSize);
                float value = getConv(conv, gaussianMatrix);
                convMatrix.setNub(i - half, j - half, value);
            }
        }
        return convMatrix;
    }

    public Matrix getLBPMatrix() throws Exception {
        Matrix addMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = matrixR.getNumber(i, j) + matrixG.getNumber(i, j) + matrixB.getNumber(i, j);
                addMatrix.setNub(i, j, value);
            }
        }
        return matrixOperation.lbpMatrix(addMatrix);
    }

    public void standardization() throws Exception {//标准化
        standardizationMatrix(matrixR);
        standardizationMatrix(matrixG);
        standardizationMatrix(matrixB);
    }

    private void standardizationMatrix(Matrix matrix) throws Exception {
        float avg = matrix.getAVG();
        float sigma = 0;
        float size = x * y;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                sigma = sigma + (float) Math.pow(matrix.getNumber(i, j) - avg, 2);
            }
        }
        sigma = sigma / size;//方差
        float b = (float) Math.sqrt(sigma);//标准差
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = (matrix.getNumber(i, j) - avg) / b;
                matrix.setNub(i, j, value);
            }
        }
    }

    public ThreeChannelMatrix scale(boolean scaleWidth, float size) throws Exception {//缩放图像
        float value;
        if (scaleWidth) {//将宽度等比缩放至指定尺寸
            value = y / size;
        } else {//将高度等比缩放至指定尺寸
            value = x / size;
        }
        int narrowX = (int) (x / value);
        int narrowY = (int) (y / value);
        if (scaleWidth) {
            narrowY = (int) size;
        } else {
            narrowX = (int) size;
        }
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

    public void add(float nub, boolean add) throws Exception {//对rgb矩阵曝光进行处理
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
        float avg = matrix.getAVG();
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
        Matrix matrixTR = new Matrix(XSize, YSize);
        Matrix matrixTG = new Matrix(XSize, YSize);
        Matrix matrixTB = new Matrix(XSize, YSize);
        int xr;
        int yr;
        for (int i = 0; i < XSize; i++) {
            xr = i + x;
            for (int j = 0; j < YSize; j++) {
                yr = j + y;
                matrixTR.setNub(i, j, matrixR.getNumber(xr, yr));
                matrixTG.setNub(i, j, matrixG.getNumber(xr, yr));
                matrixTB.setNub(i, j, matrixB.getNumber(xr, yr));
            }
        }
        threeChannelMatrix.setX(XSize);
        threeChannelMatrix.setY(YSize);
        threeChannelMatrix.setMatrixR(matrixTR);
        threeChannelMatrix.setMatrixG(matrixTG);
        threeChannelMatrix.setMatrixB(matrixTB);
        return threeChannelMatrix;
    }
}
