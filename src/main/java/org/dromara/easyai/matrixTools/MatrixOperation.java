package org.dromara.easyai.matrixTools;


import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixOperation {
    private final int coreNumber;
    private ExecutorService POOL;//线程池

    private static final CudaMatrix cudaMatrix = getCudaMatrix();
    private static final AscendMatrix ascendMatrix = getAscendMatrix();

    private static AscendMatrix getAscendMatrix() {
        ServiceLoader<AscendMatrix> loader = ServiceLoader.load(AscendMatrix.class);
        Iterator<AscendMatrix> iterator = loader.iterator();
        if (iterator.hasNext()) {
            AscendMatrix cm = iterator.next();
            try {
                cm.init();
                return cm;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static CudaMatrix getCudaMatrix() {
        ServiceLoader<CudaMatrix> loader = ServiceLoader.load(CudaMatrix.class);
        Iterator<CudaMatrix> iterator = loader.iterator();
        if (iterator.hasNext()) {
            CudaMatrix cm = iterator.next();
            try {
                cm.init();
                return cm;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public MatrixOperation() {
        coreNumber = 1;
    }

    public MatrixOperation(int coreNumber) {
        this.coreNumber = coreNumber;
        if (coreNumber > 1) {
            POOL = Executors.newFixedThreadPool(coreNumber);
        }
    }

    public List<Float> rowVectorToList(Matrix matrix) throws Exception {
        List<Float> list = new ArrayList<>();
        for (int j = 0; j < matrix.getY(); j++) {
            list.add(matrix.getNumber(0, j));
        }
        return list;
    }

    public List<Matrix> addMatrixList(List<Matrix> matrixList1, List<Matrix> matrixList2) throws Exception {//两个矩阵集合相加
        if (matrixList1.size() == matrixList2.size()) {
            List<Matrix> result = new ArrayList<>();
            int size = matrixList1.size();
            for (int i = 0; i < size; i++) {
                result.add(add(matrixList1.get(i), matrixList2.get(i)));
            }
            return result;
        } else {
            throw new Exception("两个矩阵集合相加大小不相等");
        }
    }

    //重点
    public Matrix add(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相加
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix1.getY());
            int x = matrix1.getX();
            int y = matrix1.getY();
            for (int i = 0; i < x; i++) {//遍历行
                for (int j = 0; j < y; j++) {//遍历列
                    matrix.setNub(i, j, matrix1.getNumber(i, j) + matrix2.getNumber(i, j));
                }
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    //重点
    public Matrix addThreeMatrix(Matrix matrix1, Matrix matrix2, Matrix matrix3) throws Exception {//三矩阵相加
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY() && matrix1.getX() == matrix3.getX()
                && matrix1.getY() == matrix3.getY()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix1.getY());
            int x = matrix1.getX();
            int y = matrix1.getY();
            for (int i = 0; i < x; i++) {//遍历行
                for (int j = 0; j < y; j++) {//遍历列
                    matrix.setNub(i, j, matrix1.getNumber(i, j) + matrix2.getNumber(i, j) + matrix3.getNumber(i, j));
                }
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    public void center(Matrix matrix) throws Exception {//将矩阵中心化
        float avgValue = matrix.getAVG();
        mathSub(matrix, avgValue);
    }

    public float getCrossEntropy(Matrix matrix1, Matrix matrix2) throws Exception {//两个概率矩阵的交叉熵
        float sigMod = 0;
        int x = matrix1.getX();
        int y = matrix1.getY();
        if (x == matrix2.getX() && y == matrix2.getY()) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    sigMod = sigMod + matrix2.getNumber(i, j) * (float) Math.log(matrix1.getNumber(i, j));
                }
            }
            return -sigMod;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    public Matrix softMaxByMatrix(Matrix feature) throws Exception {
        float sigma = 0;
        int x = feature.getX();
        int y = feature.getY();
        Matrix softMaxMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = feature.getNumber(i, j);
                sigma = (float) Math.exp(value) + sigma;
            }
        }
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float eSelf = (float) Math.exp(feature.getNumber(i, j));
                float value = (float) (eSelf / sigma);
                softMaxMatrix.setNub(i, j, value);
            }
        }
        return softMaxMatrix;
    }

    //矩阵相减 重点
    public Matrix sub(Matrix matrix1, Matrix matrix2) throws Exception {//
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix1.getY());
            int x = matrix1.getX();
            int y = matrix1.getY();
            for (int i = 0; i < x; i++) {//遍历行
                for (int j = 0; j < y; j++) {//遍历列
                    matrix.setNub(i, j, matrix1.getNumber(i, j) - matrix2.getNumber(i, j));
                }
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    private int getLBPValue(Matrix matrix) throws Exception {
        int value = 0;
        float avg = matrix.getAVG();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != 1 || j != 1) {
                    value = value << 1;
                    if (matrix.getNumber(i, j) > avg) {
                        value = value | 1;
                    }
                }
            }
        }
        return value;
    }

    public Matrix lbpMatrix(Matrix addMatrix) throws Exception {
        int x = addMatrix.getX();
        int y = addMatrix.getY();
        Matrix matrix = new Matrix(x / 3, y / 3);
        for (int i = 0; i <= x - 3; i += 3) {
            for (int j = 0; j <= y - 3; j += 3) {
                Matrix aMatrix = addMatrix.getSonOfMatrix(i, j, 3, 3);
                int lbp = getLBPValue(aMatrix);
                matrix.setNub(i / 3, j / 3, lbp);
            }
        }
        return matrix;
    }

    //多元线性回归
    public Matrix getLinearRegression(Matrix parameter, Matrix out) throws Exception {
        if (parameter.getX() == out.getX() && out.isVector()) {
            //将参数矩阵转置
            Matrix matrix1 = transPosition(parameter);
            //转置的参数矩阵乘以参数矩阵
            Matrix matrix2 = mulMatrix(matrix1, parameter);
            //求上一步的逆矩阵 这一步需要矩阵非奇异,若出现奇异矩阵，则返回0矩阵，意味失败
            Matrix matrix3 = getInverseMatrix(matrix2);
            if (matrix3.getX() == 1 && matrix3.getY() == 1) {
                return matrix3;
            } else {
                //逆矩阵乘以转置矩阵
                Matrix matrix4 = mulMatrix(matrix3, matrix1);
                //最后乘以输出矩阵,生成权重矩阵并返回
                return mulMatrix(matrix4, out);
            }
        } else {
            throw new Exception("invalid regression matrix");
        }
    }

    public float getEDistByMatrix(Matrix matrix1, Matrix matrix2) throws Exception {
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            int x = matrix1.getX();
            int y = matrix1.getY();
            float sigma = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float sub = matrix1.getNumber(i, j) - matrix2.getNumber(i, j);
                    sigma = sigma + (float) Math.pow(sub, 2);
                }
            }
            return sigma / (x * y);
        } else {
            throw new Exception("two matrixes is not equals");
        }
    }

    //返回两个向量之间的欧氏距离的平方
    public float getEDist(Matrix matrix1, Matrix matrix2) throws Exception {
        if (matrix1.isRowVector() && matrix2.isRowVector() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = sub(matrix1, matrix2);
            return getNorm(matrix);
        } else {
            throw new Exception("this matrix is not  rowVector or length different");
        }
    }

    public float errorNub(Matrix matrix, Matrix avgMatrix) throws Exception {//求均方误差
        int y = matrix.getY();
        if (matrix.isRowVector() && avgMatrix.isRowVector() && y == avgMatrix.getY()) {
            float[] subAll = new float[y];
            for (int j = 0; j < y; j++) {
                float mySelf = matrix.getNumber(0, j);
                float avg = avgMatrix.getNumber(0, j);
                float sub = (float) Math.pow(avg - mySelf, 2);
                subAll[j] = sub;
            }
            float sigma = 0;
            for (int i = 0; i < y; i++) {
                sigma = sigma + subAll[i];
            }
            return sigma / y;
        } else {
            throw new Exception("this matrix is not  rowVector or length different");
        }
    }

    public Matrix pushVector(Matrix myMatrix, Matrix matrix, boolean addRow) throws Exception {
        //向一个矩阵里合并一个行向量或者列向量到矩阵行或者列的末尾
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix addMatrix;
            if (addRow) {//增加一行
                if (matrix.getY() != myMatrix.getY()) {
                    throw new Exception("this matrix column is not equals");
                }
                addMatrix = new Matrix(myMatrix.getX() + 1, myMatrix.getY());
            } else {//增加一列
                if (matrix.getX() != myMatrix.getX()) {
                    throw new Exception("this matrix row is not equals");
                }
                addMatrix = new Matrix(myMatrix.getX(), myMatrix.getY() + 1);
            }
            for (int i = 0; i < addMatrix.getX(); i++) {
                for (int j = 0; j < addMatrix.getY(); j++) {
                    if (addRow) {
                        if (i == addMatrix.getX() - 1) {//最后一行
                            addMatrix.setNub(i, j, matrix.getNumber(0, j));
                        } else {
                            addMatrix.setNub(i, j, myMatrix.getNumber(i, j));
                        }
                    } else {
                        if (j == addMatrix.getY() - 1) {//最后一列
                            addMatrix.setNub(i, j, matrix.getNumber(i, 0));
                        } else {
                            addMatrix.setNub(i, j, myMatrix.getNumber(i, j));
                        }
                    }
                }
            }
            return addMatrix;
        } else {
            throw new Exception("this matrix is not a vector");
        }
    }

    public Matrix push(Matrix matrix, float nub, boolean isRow) throws Exception {//向一个向量里PUSH一个值
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix myMatrix;
            int nubs;
            if (isRow) {//行向量
                nubs = matrix.getY() + 1;
                myMatrix = new Matrix(1, nubs);
            } else {//列向量
                nubs = matrix.getX() + 1;
                myMatrix = new Matrix(nubs, 1);
            }
            for (int i = 0; i < nubs; i++) {
                if (i == nubs - 1) {
                    if (isRow) {
                        myMatrix.setNub(0, i, nub);
                    } else {
                        myMatrix.setNub(i, 0, nub);
                    }
                } else {
                    if (isRow) {//行向量
                        myMatrix.setNub(0, i, matrix.getNumber(0, i));
                    } else {//列向量
                        myMatrix.setNub(i, 0, matrix.getNumber(i, 0));
                    }
                }
            }
            return myMatrix;
        } else {
            throw new Exception("this matrix is not a vector");
        }
    }

    public Matrix getPoolVector(Matrix matrix) throws Exception {
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix vector;
            int nub;
            boolean isRow = false;
            if (matrix.getX() == 1) {//行向量
                isRow = true;
                nub = matrix.getY() / 4;
                vector = new Matrix(1, nub);
            } else {//列向量
                nub = matrix.getX() / 4;
                vector = new Matrix(nub, 1);
            }
            int k = 0;
            for (int i = 0; i < nub * 4 - 3; i += 4) {
                float max = 0;
                if (isRow) {
                    max = matrix.getNumber(0, i);
                    max = getMax(max, matrix.getNumber(0, i + 1));
                    max = getMax(max, matrix.getNumber(0, i + 2));
                    max = getMax(max, matrix.getNumber(0, i + 3));
                    vector.setNub(0, k, max);
                } else {
                    max = matrix.getNumber(i, 0);
                    max = getMax(max, matrix.getNumber(i + 1, 0));
                    max = getMax(max, matrix.getNumber(i + 2, 0));
                    max = getMax(max, matrix.getNumber(i + 3, 0));
                    vector.setNub(k, 0, max);
                }
                k++;
            }
            return vector;
        } else {
            throw new Exception("this matrix is not a vector");
        }

    }

    private float getMax(float o1, float o2) {
        return (float) Math.max(o1, o2);
    }

    public Matrix matrixToVector(Matrix matrix, boolean isRow) throws Exception {//将一个矩阵转成向量
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix;
        if (isRow) {
            myMatrix = new Matrix(1, x * y);
        } else {
            myMatrix = new Matrix(x * y, 1);
        }
        int t = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (isRow) {
                    myMatrix.setNub(0, t, matrix.getNumber(i, j));
                } else {
                    myMatrix.setNub(t, 0, matrix.getNumber(i, j));
                }
                t++;
            }
        }
        return myMatrix;
    }

    public float innerProduct(Matrix matrix1, Matrix matrix2) throws Exception {//两个向量内积
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            float sigma = 0;
            for (int i = 0; i < matrix1.getX(); i++) {
                for (int j = 0; j < matrix1.getY(); j++) {
                    sigma = sigma + matrix1.getNumber(i, j) * matrix2.getNumber(i, j);
                }
            }
            return sigma;
        } else {
            throw new Exception("两个向量的长宽必须相同");
        }
    }

    public float getNorm(Matrix matrix) throws Exception {//求向量范数
        if (matrix.getY() == 1 || matrix.getX() == 1) {
            float nub = 0;
            for (int i = 0; i < matrix.getX(); i++) {
                for (int j = 0; j < matrix.getY(); j++) {
                    nub = (float) Math.pow(matrix.getNumber(i, j), 2) + nub;
                }
            }
            return (float) Math.sqrt(nub);
        } else {
            throw new Exception("this matrix is not vector");
        }
    }

    public float getNormCos(Matrix matrix1, Matrix matrix2) throws Exception {//求两个向量之间的余弦
        float inner = innerProduct(matrix1, matrix2);
        float mulNorm = getNorm(matrix1) * getNorm(matrix2);
        return inner / mulNorm;
    }

    public Matrix transPosition(Matrix matrix) throws Exception {//矩阵转置
        Matrix myMatrix = new Matrix(matrix.getY(), matrix.getX());
        for (int i = 0; i < matrix.getY(); i++) {
            Matrix matrixColumn = matrix.getColumn(i);
            for (int j = 0; j < matrixColumn.getX(); j++) {
                float myNode = matrixColumn.getNumber(j, 0);
                myMatrix.setNub(i, j, myNode);
            }
        }
        return myMatrix;
    }

    public float convolution(Matrix matrix, Matrix kernel, int x, int y) throws Exception {//计算卷积
        float allNub = 0;
        int xr = 0;
        int yr = 0;
        int kxMax = kernel.getX();
        int kyMax = kernel.getY();
        for (int i = 0; i < kxMax; i++) {
            xr = i + x;
            for (int j = 0; j < kyMax; j++) {
                yr = j + y;
                allNub = matrix.getNumber(xr, yr) * kernel.getNumber(i, j) + allNub;
            }
        }

        return allNub;
    }

    public float getKernelNub(Matrix matrix, Matrix kernel) throws Exception {
        float allNub = 0;
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                allNub = matrix.getNumber(i, j) * kernel.getNumber(i, j) + allNub;
            }
        }
        return allNub;
    }

    public int inverseNumber(float[] myInverse) {//逆序数奇偶性判定
        int size = myInverse.length;
        int inverserNumber = 0;
        for (int i = 0; i < size; i++) {
            float element = myInverse[i];
            for (int j = (i + 1); j < size; j++) {
                if (myInverse[j] < element) {
                    inverserNumber++;
                }
            }
        }
        return inverserNumber;
    }

    public Matrix getInverseMatrix(Matrix matrix) throws Exception {//矩阵求逆
        float def = matrix.getDet();
        if (def != 0) {
            def = 1 / def;
            Matrix myMatrix = adjointMatrix(matrix);//伴随矩阵
            mathMul(myMatrix, def);
            return myMatrix;
        } else {
            //System.out.println("matrix def is zero error:");
            //System.out.println(matrix.getString());
            return new Matrix(1, 1);
        }
    }

    public Matrix adjointMatrix(Matrix matrix) throws Exception {//求伴随矩阵
        Matrix myMatrix = new Matrix(matrix.getX(), matrix.getY());
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                myMatrix.setNub(i, j, algebraicCofactor(matrix, i, j));
            }
        }

        return transPosition(myMatrix);
    }

    public float algebraicCofactor(Matrix matrix, int row, int column) throws Exception {//获取代数余子式
        if (row >= 0 && column >= 0 && row < matrix.getX() && column < matrix.getY()) {
            int x = matrix.getX() - 1;
            int y = matrix.getY() - 1;
            int ij = row + column + 2;
            int oldX = 0;//从目标矩阵中取值的X
            int oldY = 0;//从目标矩阵中取值的Y
            boolean isXNext = false;//行是否跳过一次
            Matrix myMatrix = new Matrix(x, y);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    if (i == row && !isXNext) {
                        isXNext = true;
                        oldX++;
                    }
                    if (j == column) {
                        oldY++;
                    }
                    myMatrix.setNub(i, j, matrix.getNumber(oldX, oldY));
                    oldY++;
                }
                oldX++;
                oldY = 0;
            }
            float dm = myMatrix.getDet();
            if ((ij % 2) != 0) {//ij是奇数
                dm = -dm;
            }
            return dm;
        } else {
            throw new Exception("row or column index Beyond the limit");
        }
    }

    public Matrix matrixSoftMaxPd(Matrix qkt, Matrix errorMatrix, float wordVectorDimension) throws Exception {//重重点
        if (null != cudaMatrix) {
            return cudaMatrix.matrixSoftMaxPd(qkt, errorMatrix, wordVectorDimension);
        }

        float param = (float) Math.sqrt(wordVectorDimension);
        int x = qkt.getX();
        int y = qkt.getY();
        Matrix grMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            Matrix qr = qkt.getRow(i);//该行的行向量
            for (int j = 0; j < y; j++) {
                float jValue = qr.getNumber(0, j);//遍历qr每一个元素，分别对他们求偏导
                int z = qr.getY();
                float sigma = 0;
                for (int k = 0; k < z; k++) {
                    float kValue = qr.getNumber(0, k);//遍历qr每一个元素，分别对他们求偏导
                    float error = errorMatrix.getNumber(i, k);
                    float er;
                    if (k != j) {
                        er = -error * kValue * jValue;
                    } else {
                        er = jValue * (1 - jValue) * error;
                    }
                    sigma = sigma + er;
                }
                float gr = (float) (sigma / param);//该位置梯度
                grMatrix.setNub(i, j, gr);
            }
        }
        return grMatrix;
    }

    public void softMax(Matrix matrix) throws Exception {//重重点
        for (int i = 0; i < matrix.getX(); i++) {
            Matrix row = matrix.getRow(i);
            float sigma = getRowSoftMaxSigma(row);
            for (int j = 0; j < matrix.getY(); j++) {
                float self = row.getNumber(0, j);
                float eSelf = (float) Math.exp(self);
                matrix.setNub(i, j, eSelf / sigma);
            }
        }
    }

    private float getRowSoftMaxSigma(Matrix row) throws Exception {
        float sigma = 0;
        for (int i = 0; i < row.getY(); i++) {
            float value = row.getNumber(0, i);
            sigma = (float) Math.exp(value) + sigma;
        }
        return sigma;
    }

    public Matrix matrixPointDiv(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵点除
        int x = matrix1.getX();
        int y = matrix1.getY();
        Matrix matrix = new Matrix(x, y);
        if (matrix2.getX() == x && matrix2.getY() == y) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    matrix.setNub(i, j, matrix1.getNumber(i, j) / matrix2.getNumber(i, j));
                }
            }
        } else {
            throw new Exception("two matrix is not equals");
        }
        return matrix;
    }

    public Matrix matrixMulPd(Matrix errorMatrix, Matrix first, Matrix second, boolean isFirstPd) throws Exception {//对两个相乘的矩阵求偏导
        Matrix matrix;
        if (isFirstPd) {//对相乘的前矩阵进行求导
            matrix = mulMatrix(errorMatrix, transPosition(second));
        } else {
            matrix = mulMatrix(transPosition(first), errorMatrix);
        }
        return matrix;
    }

    //重点
    public float getSdByMatrix(Matrix m, float avg, float e) throws Exception {//计算矩阵元素的标准差
        float var = 0;
        float size = m.getX() * m.getY();
        int x = m.getX();
        int y = m.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                var = var + (float) Math.pow(m.getNumber(i, j) - avg, 2);
            }
        }
        return (float) Math.sqrt(var / size + e);
    }

    private Matrix mulMatrixOne(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相乘单进程
        if (matrix1.getY() == matrix2.getX()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix2.getY());
            for (int i = 0; i < matrix1.getX(); i++) {
                Matrix matrixRow = matrix1.getRow(i);//行向量
                for (int j = 0; j < matrix2.getY(); j++) {
                    Matrix matrixColumn = matrix2.getColumn(j);
                    float columnAllNumber = 0;//对每一项的乘积求和
                    for (int h = 0; h < matrixColumn.getX(); h++) {
                        float columnNumber = matrixColumn.getNumber(h, 0);
                        float rowNumber = matrixRow.getNumber(0, h);
                        float nowNumber = columnNumber * rowNumber;
                        columnAllNumber = columnAllNumber + nowNumber;
                    }
                    if (!Float.isNaN(columnAllNumber)) {
                        matrix.setNub(i, j, columnAllNumber);
                    } else {
                        System.out.println(matrix1);
                        System.out.println(matrix2);
                        throw new Exception("计算时出现数值溢出，梯度爆炸!");
                    }
                }
            }
            return matrix;
        } else {
            throw new Exception("row is not equals column");
        }
    }

    private Matrix mulMatrixMany(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相乘多进程
        if (matrix1.getY() == matrix2.getX()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix2.getY());
            CountDownLatch countDownLatch = new CountDownLatch(matrix1.getX() * matrix2.getY());
            int x = matrix1.getX();
            int y = matrix2.getY();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    MatrixMulAccelerate matrixMulAccelerate = new MatrixMulAccelerate(matrix1, matrix2
                            , matrix, i, j, countDownLatch);
                    POOL.execute(matrixMulAccelerate);
                }
            }
            countDownLatch.await();
            return matrix;
        } else {
            throw new Exception("row is not equals column");
        }
    }

    public List<Matrix> mulMatrixList(List<Matrix> matrixList1, List<Matrix> matrixList2) throws Exception {//两个矩阵集合相乘
        if (matrixList1.size() == matrixList2.size()) {
            List<Matrix> mulMatrixList = new ArrayList<>();
            int size = matrixList1.size();
            for (int i = 0; i < size; i++) {
                Matrix mulMatrix = mulMatrix(matrixList1.get(i), matrixList2.get(i));
                mulMatrixList.add(mulMatrix);
            }
            return mulMatrixList;
        } else {
            throw new IllegalArgumentException("两个矩阵集合的大小不相等");
        }
    }

    //重重点
    public Matrix mulMatrix(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相乘
        if (null != cudaMatrix) {
            return cudaMatrix.mulMatrix(matrix1, matrix2);
        } else if (null != ascendMatrix) {
            return ascendMatrix.mulMatrix(matrix1, matrix2);
        }

        if (coreNumber > 1) {
            return mulMatrixMany(matrix1, matrix2);
        } else {
            return mulMatrixOne(matrix1, matrix2);
        }
    }

    //重重点
    public Matrix mathMulBySelf(Matrix matrix, float nub) throws Exception {//矩阵数乘并返回新矩阵
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                myMatrix.setNub(i, j, matrix.getNumber(i, j) * nub);
            }
        }
        return myMatrix;
    }

    public void mathMulByList(List<Matrix> matrixList, float value) throws Exception {
        for (Matrix matrix : matrixList) {
            mathMul(matrix, value);
        }
    }

    //重点
    public void mathMul(Matrix matrix, float nub) throws Exception {//矩阵数乘
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) * nub);
            }
        }
    }

    //重点
    public void mathAdd(Matrix matrix, float nub) throws Exception {//矩阵数加
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) + nub);
            }
        }
    }

    //重点
    public void mathSub(Matrix matrix, float nub) throws Exception {//矩阵数减
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) - nub);
            }
        }
    }

    //重点
    public void mathDiv(Matrix matrix, float nub) throws Exception {//矩阵数除
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) / nub);
            }
        }
    }

    //矩阵转LIST
    public List<Float> matrixToList(Matrix matrix) throws Exception {
        List<Float> list = new ArrayList<>();
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                list.add(matrix.getNumber(i, j));
            }
        }
        return list;
    }

    public Matrix vectorToMatrix(Matrix vector, int xSize, int ySize) throws Exception {//向量转矩阵
        Matrix matrix = new Matrix(xSize, ySize);
        if (vector.isRowVector()) {//行向量
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    int index = i * ySize + j;
                    matrix.setNub(i, j, vector.getNumber(0, index));
                }
            }
        } else {//列向量
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    int index = i * ySize + j;
                    matrix.setNub(i, j, vector.getNumber(index, 0));
                }
            }
        }
        return matrix;
    }

    //list转矩阵
    public Matrix ListToMatrix(List<Float> list, int matrixX, int matrixY) throws Exception {
        Matrix matrix = new Matrix(matrixX, matrixY);
        for (int i = 0; i < matrixX; i++) {
            for (int j = 0; j < matrixY; j++) {
                int index = i * matrixY + j;
                matrix.setNub(i, j, list.get(index));
            }
        }
        return matrix;
    }

    //list转行向量
    public Matrix listToRowVector(List<Float> list) throws Exception {
        Matrix matrix = new Matrix(1, list.size());
        for (int i = 0; i < list.size(); i++) {
            matrix.setNub(0, i, list.get(i));
        }
        return matrix;
    }

    //list转行向量定长
    public Matrix listToRowVector(List<Float> list, int nub) throws Exception {
        Matrix matrix = new Matrix(1, nub);
        for (int i = 0; i < nub; i++) {
            float n = 0;
            if (list.size() > i) {
                n = list.get(i);
            }
            matrix.setNub(0, i, n);
        }
        return matrix;
    }

    private void inputVector(Matrix matrix, Matrix feature, int index, int kenLen) throws Exception {
        int y = matrix.getY();
        for (int i = 0; i < y; i++) {
            matrix.setNub(index, i, feature.getNumber(i / kenLen, i % kenLen));
        }
    }

    //重重点
    public Matrix im2col(Matrix matrix, int kernLen, int step) throws Exception {//卷积拉平
        int ySize = kernLen * kernLen;
        int x = matrix.getX();
        int y = matrix.getY();
        int xSize = ((x - (kernLen - step)) / step) * ((y - (kernLen - step)) / step);
        Matrix myMatrix = new Matrix(xSize, ySize);
        int index = 0;
        for (int i = 0; i <= x - kernLen; i += step) {
            for (int j = 0; j <= y - kernLen; j += step) {
                inputVector(myMatrix, matrix.getSonOfMatrix(i, j, kernLen, kernLen), index, kernLen);
                index++;
            }
        }
        return myMatrix;
    }

    //重重点
    public Matrix reverseIm2col(Matrix matrix, int kernLen, int step, int xSize, int ySize) throws Exception {//逆向im2col
        int col = matrix.getY();//核长
        int row = matrix.getX();
        int sub = kernLen - step;
        int y = (ySize - sub) / step;//线性变换后矩阵的列数,一行该有几列
        Matrix myMatrix = new Matrix(xSize, ySize);
        for (int i = 0; i < row; i++) {//某一行全部的值,首先要看这一行的值应该在逆运算的第几行
            int xz = (i / y) * step;//左上角行数
            int yz = (i % y) * step;//左上角列数
            for (int j = 0; j < col; j++) {
                int xr = j / kernLen + xz;
                int yr = j % kernLen + yz;
                float value = myMatrix.getNumber(xr, yr) + matrix.getNumber(i, j);
                myMatrix.setNub(xr, yr, value);
            }
        }
        return myMatrix;
    }

    private void insertVectorValue(Matrix matrix, Matrix vector, int col) throws Exception {//向一个矩阵指定行注入一个向量
        int x = matrix.getX();
        int y = matrix.getY();
        if (x == vector.getX() && vector.isVector() && y > col) {//矩阵为行向量
            for (int i = 0; i < x; i++) {
                matrix.setNub(i, col, vector.getNumber(i, 0));
            }
        } else {
            throw new Exception("注入向量异常，注入的必须为向量，且矩阵的列数必须与向量的列数相等，且行数没有溢出");
        }
    }

    private void matrixSqrt(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (i == j) {
                    float value = (float) Math.sqrt(matrix.getNumber(i, j));
                    matrix.setNub(i, j, (float) value);
                }
            }
        }
    }

    public Matrix pca(Matrix feature, Matrix avgMatrix, int time, int featureSize) throws Exception {
        if (feature.getX() == avgMatrix.getX() && feature.getY() == avgMatrix.getY()) {
            Matrix myMatrix = sub(feature, avgMatrix);
            return svd(myMatrix, time, featureSize).getMatrixU();
        } else {
            throw new Exception("特征矩阵与平均值矩阵大小不相等");
        }
    }

    public SVDBody svd(Matrix matrix, int time, int featureSize) throws Exception {//svd分解
        Matrix tMatrix = transPosition(matrix);//转制矩阵
        Matrix UA = mulMatrix(matrix, tMatrix);
        Matrix VA = mulMatrix(tMatrix, matrix);
        FeatureDe featureU = qrIteration(UA, time);
        FeatureDe featureV = qrIteration(VA, time);
        Matrix uValue = featureU.getFeatureValue();//特征值
        Matrix U = featureU.getFeatureMatrix();
        Matrix VT = transPosition(featureV.getFeatureMatrix());
        if (featureSize <= U.getY() && featureSize <= VT.getX()) {
            SVDBody svdBody = new SVDBody();
            Matrix rU = U.getSonOfMatrix(0, 0, U.getX(), featureSize);
            Matrix rVT = VT.getSonOfMatrix(0, 0, featureSize, VT.getY());
            Matrix feature = uValue.getSonOfMatrix(0, 0, featureSize, featureSize);
            matrixSqrt(feature);
            svdBody.setMatrixU(rU);
            svdBody.setMatrixVT(rVT);
            svdBody.setFeature(feature);
            return svdBody;
        } else {
            throw new Exception("指定尺寸过大");
        }
    }

    public FeatureDe qrIteration(Matrix matrix, int time) throws Exception {//QR迭代求特征值
        Matrix V = null;
        Matrix featureValue = matrix.copy();
        for (int i = 0; i < time; i++) {
            QRMatrix qrMatrix = qrd(featureValue);
            Matrix matrixQ = qrMatrix.getQ();
            Matrix matrixR = qrMatrix.getR();
            if (V == null) {
                V = matrixQ;
            } else {
                V = mulMatrix(V, matrixQ);
            }
            featureValue = mulMatrix(matrixR, matrixQ);
        }
        int x = featureValue.getX();
        int y = featureValue.getY();
        Matrix featureMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (i == j) {
                    featureMatrix.setNub(i, j, featureValue.getNumber(i, j));
                }
            }
        }
        FeatureDe featureDe = new FeatureDe();
        featureDe.setFeatureValue(featureMatrix);
        featureDe.setFeatureMatrix(V);
        return featureDe;
    }

    public QRMatrix qrd(Matrix matrix) throws Exception {//返回qr分解
        int x = matrix.getX();
        int y = matrix.getY();
        if (x == y) {
            Matrix schMatrix = new Matrix(x, y);
            Matrix R = new Matrix(x, x);//R矩阵
            Matrix normMatrix = new Matrix(x, x);
            for (int i = 0; i < y; i++) {
                Matrix xn = matrix.getColumn(i);
                R.setNub(i, i, 1F);
                if (i > 0) {
                    for (int k = 0; k < i; k++) {
                        Matrix vn = schMatrix.getColumn(k);
                        float value = innerProduct(xn, vn) / innerProduct(vn, vn);
                        R.setNub(k, i, value);
                        mathMul(vn, value);
                        xn = sub(xn, vn);
                    }
                }
                float norm = getNorm(xn);//范数
                normMatrix.setNub(i, i, norm);
                insertVectorValue(schMatrix, xn, i);
            }
            R = mulMatrix(normMatrix, R);
            for (int i = 0; i < x; i++) {
                float norm = normMatrix.getNumber(i, i);
                for (int j = 0; j < y; j++) {
                    float value = schMatrix.getNumber(j, i) / norm;
                    schMatrix.setNub(j, i, value);
                }
            }
            QRMatrix qrMatrix = new QRMatrix();
            qrMatrix.setR(R);
            qrMatrix.setQ(schMatrix);
            return qrMatrix;
        } else {
            throw new Exception("请将矩阵先处理为方阵才可以进行QR分解");
        }
    }

}
