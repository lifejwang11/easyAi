package org.dromara.matrixTools;

import java.util.ArrayList;
import java.util.List;

/**
 * 矩阵
 **/
public class Matrix extends MatrixOperation {
    private double[][] matrix;//矩阵本体
    private int x;//矩阵的行数
    private int y;//矩阵的列数
    private boolean isRowVector = false;//是否是单行矩阵
    private boolean isVector = false;//是否是向量
    private boolean isZero = false;//是否是单元素矩阵

    /**
     * 获取行数
     *
     * @return 获取行数
     */
    public int getX() {//获取行数
        return x;
    }

    /**
     * 获取列数
     *
     * @return 获取列数
     */
    public int getY() {//获取列数
        return y;
    }

    /**
     * 初始化矩阵
     *
     * @param x 行数
     * @param y 列数
     */
    public Matrix(int x, int y) {
        matrix = new double[x][y];
        this.x = x;
        this.y = y;
        setState(x, y);
    }

    /**
     * 设置矩阵属性
     *
     * @param x 行数
     * @param y 列数
     */
    private void setState(int x, int y) {
        if (x == 1 && y == 1) {
            isZero = true;
            isVector = true;
        } else if (x == 1 || y == 1) {
            isVector = true;
            if (x == 1) {
                isRowVector = true;
            } else {
                isRowVector = false;
            }
        }
    }

    public double getSigma() {
        double sigma = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                sigma = sigma + matrix[i][j];
            }
        }
        return sigma;
    }

    /**
     * 计算全矩阵元素平均值
     *
     * @return 返回当前矩阵全部元素的平均值
     */
    public double getAVG() {
        double sigma = 0;
        int s = x * y;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                sigma = sigma + matrix[i][j];
            }
        }
        sigma = sigma / s;
        return sigma;
    }

    public double[][] getMatrix() {
        return matrix;
    }

    /**
     * 是否为单行
     *
     * @return true表示此矩阵为一个单行矩阵
     */
    public boolean isRowVector() {
        return isRowVector;
    }

    /**
     * 是否是一个向量矩阵
     * 单行和单列矩阵都是向量矩阵
     *
     * @return true表示此矩阵为一个向量矩阵
     */
    public boolean isVector() {
        return isVector;
    }

    /**
     * 是否是一个单元素矩阵
     *
     * @return true表示是里面只有一个元素
     */
    public boolean isZero() {
        return isZero;
    }

    /**
     * 清除矩阵数据
     **/
    public void clear() {
        matrix = new double[x][y];
    }

    /**
     * 初始化矩阵
     *
     * @param x    行数
     * @param y    列数
     * @param matr 数据
     * @throws Exception
     */
    public Matrix(int x, int y, String matr) throws Exception {
        matrix = new double[x][y];
        this.x = x;
        this.y = y;
        setState(x, y);
        setAll(matr);
    }

    class Coordinate {//保存行数列数的实体类

        Coordinate(Coordinate father, int x, int y) {
            this.x = x;
            this.y = y;
            this.father = father;
            coordinateList = new ArrayList<>();
        }

        Coordinate father;
        List<Coordinate> coordinateList;
        int x;//路径
        int y;//深度
    }

    private List<Coordinate> coordinateRoot;
    private double defNub = 0;//行列式计算结果

    private boolean isDo(Coordinate coordinates, int i, int j) {
        boolean isOk = false;
        if (coordinates != null) {
            for (Coordinate coordinate : coordinates.coordinateList) {
                if (coordinate.x == i && coordinate.y == j) {
                    isOk = true;
                    break;
                }
            }
        }
        return isOk;
    }

    private boolean findRout(Coordinate coordinate, int j, int initi, boolean isDown) {
        for (int i = 0; i < x; i++) {//层数
            if (!isDown) {
                break;
            }
            int row = i;
            if (coordinate == null) {
                row = initi;
            }
            boolean isOk = isNext(coordinate, row, true) && !isDo(coordinate, row, j);
            if (isOk) {
                Coordinate coordinateNext = new Coordinate(coordinate, row, j);
                if (coordinate != null) {
                    coordinate.coordinateList.add(coordinateNext);
                } else {
                    coordinateRoot.add(coordinateNext);
                }

                if (coordinateNext.y < (y - 1)) {//深入
                    j++;
                    isDown = findRout(coordinateNext, j, initi, isDown);
                } else if (coordinate != null && coordinateNext.y > 1 && coordinateNext.x == (x - 1)) {
                    //缩头
                    j--;
                    isDown = findRout(coordinate.father, j, initi, isDown);
                } else if (coordinateNext.y == 1) {
                    isDown = false;
                    break;
                }
            } else {
                if (i == (x - 1) && j > 1) {//行已经到极限了 缩头
                    j--;
                    isDown = findRout(coordinate.father, j, initi, isDown);
                } else if (j == 1 && i == (x - 1)) {//跳出
                    isDown = false;
                    break;
                }
            }
        }

        return isDown;
    }

    private boolean isNext(Coordinate coordinate, int i, boolean isOk) {
        if (coordinate == null) {//此路可走
            return true;
        }
        if (isOk) {
            if (coordinate.x != i) {
                isOk = isNext(coordinate.father, i, true);
            } else {//此路不通
                return false;
            }
        }
        return isOk;
    }

    private void defCalculation(List<Coordinate> coordinates) {
        for (Coordinate coordinate : coordinates) {
            if (!coordinate.coordinateList.isEmpty()) {//继续向丛林深处进发
                defCalculation(coordinate.coordinateList);
            } else {//到道路的尽头了,进行核算
                mulFather(coordinate, 1, new ArrayList<>());
            }
        }
    }

    private double mulFather(Coordinate coordinate, double element, List<Coordinate> div) {
        div.add(coordinate);
        element = matrix[coordinate.x][coordinate.y] * element;
        if (coordinate.father != null) {
            element = mulFather(coordinate.father, element, div);
        } else {//道路尽头
            if (parity(div)) {//偶排列
                defNub = defNub + element;
            } else {//奇排列
                defNub = defNub - element;
            }
            div.clear();
            element = 1;
        }
        return element;
    }

    /**
     * 求矩阵的行列式 递归算法
     *
     * @return 计算后的值
     * @throws Exception 如果矩阵不是一个方阵抛出异常
     */
    public double getDet() throws Exception {//求矩阵的行列式
        if (x == y) {
            coordinateRoot = new ArrayList<>();
            for (int i = 0; i < x; i++) {
                findRout(null, 0, i, true);
            }
            defCalculation(coordinateRoot);
        } else {
            throw new Exception("Matrix is not Square");
        }
        return defNub;
    }

    private boolean parity(List<Coordinate> list) {//获取排列奇偶性
        boolean parity = true;//默认是偶排列
        double[] row = new double[list.size()];
        double[] clo = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            row[i] = list.get(i).x + 1;
            clo[i] = list.get(i).y + 1;
        }
        int rowInv = inverseNumber(row);
        int cloInv = inverseNumber(clo);
        int inverserNumber = rowInv + cloInv;
        if (inverserNumber % 2 != 0) {//奇排列
            parity = false;
        }
        return parity;
    }

    /**
     * 给矩阵设置数据
     *
     * @param messages 数据
     * @throws Exception 给出的数据不正确时候会抛出异常
     */
    public void setAll(String messages) throws Exception {//全设置矩阵
        String[] message = messages.split("#");
        if (x == message.length) {
            for (int i = 0; i < message.length; i++) {
                String mes = message[i];
                String[] me = mes.substring(1, mes.length() - 1).split(",");
                if (y == me.length) {
                    y = me.length;
                    for (int j = 0; j < y; j++) {
                        matrix[i][j] = Double.parseDouble(me[j]);
                    }
                } else {
                    matrix = null;
                    throw new Exception("matrix column is not equals");
                }
            }
        } else {
            throw new Exception("matrix row is not equals");
        }
    }

    /**
     * 将矩阵分块
     *
     * @param x     要分块的x坐标
     * @param y     要分块的y坐标
     * @param xSize 分块矩阵的宽度
     * @param ySize 分块矩阵的长度
     * @return 返回分块后的矩阵
     */
    public Matrix getSonOfMatrix(int x, int y, int xSize, int ySize) {
        Matrix myMatrix = new Matrix(xSize, ySize);
        int xr = 0;
        int yr = 0;
        try {
            for (int i = 0; i < xSize; i++) {
                xr = i + x;
                for (int j = 0; j < ySize; j++) {
                    yr = j + y;
                    if (this.x > xr && this.y > yr) {
                        myMatrix.setNub(i, j, matrix[xr][yr]);
                    } else {
                        throw new Exception("xr:" + xr + ",yr:" + yr + ",x:" + this.x + ",y:" + this.y + ",xSize:" + xSize + ",ySize:" + ySize + ",x:" + x + ",y:" + y);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("xr:" + xr + ",yr:" + yr);
            e.printStackTrace();
        }
        return myMatrix;
    }

    /**
     * 获取行向量
     *
     * @param x 你要指定的行数
     * @return 返回一个一行的矩阵
     * @throws Exception 超出矩阵范围抛出异常
     */
    public Matrix getRow(int x) throws Exception {
        Matrix myMatrix = new Matrix(1, y);
        for (int i = 0; i < y; i++) {
            myMatrix.setNub(0, i, matrix[x][i]);
        }
        return myMatrix;
    }


    /**
     * 获取列向量
     *
     * @param y 要制定的列数
     * @return 返回一个一列的矩阵
     * @throws Exception 超出矩阵范围抛出异常
     */
    public Matrix getColumn(int y) throws Exception {//获取列向量
        Matrix myMatrix = new Matrix(x, 1);
        for (int i = 0; i < x; i++) {
            myMatrix.setNub(i, 0, matrix[i][y]);
        }
        return myMatrix;
    }

    /**
     * 返回一个矩阵字符串
     *
     * @return 返回一个矩阵字符串
     */
    public String getString() {//矩阵输出字符串
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < x; i++) {
            builder.append(i + ":[");
            for (int j = 0; j < y; j++) {
                double number = matrix[i][j];
                if (j == 0) {
                    builder.append(number);
                } else {
                    builder.append("," + number);
                }
            }
            builder.append("]\r\n");
        }
        return builder.toString();
    }

    /**
     * 返回一个带坐标的矩阵字符串
     *
     * @return 返回一个带坐标的矩阵字符串
     */
    public String getPositionString() {//矩阵输出字符串
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < x; i++) {
            builder.append(i + ":[");
            for (int j = 0; j < y; j++) {
                double number = matrix[i][j];
                if (j == 0) {
                    builder.append(number);
                } else {
                    builder.append("," + j + ":" + number);
                }
            }
            builder.append("]\r\n");
        }
        return builder.toString();
    }

    /**
     * 给矩阵设置值
     *
     * @param x      x坐标
     * @param y      y坐标
     * @param number 要设置的值
     * @throws Exception 超出矩阵范围抛出
     */
    public void setNub(int x, int y, double number) throws Exception {
        if (this.x > x && this.y > y && x >= 0 && y >= 0) {
            matrix[x][y] = number;
        } else {
            throw new Exception("setNub matrix length too little x:" + x + ",y:" + y);
        }
    }

    public Matrix copy() throws Exception {//复制一个矩阵
        Matrix myMatrix = new Matrix(this.x, this.y);
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                myMatrix.setNub(i, j, matrix[i][j]);
            }
        }
        return myMatrix;
    }

    /**
     * 取矩阵的数值
     *
     * @param x x坐标
     * @param y y坐标
     * @return 返回指定坐标的数值
     * @throws Exception 超出矩阵范围抛出
     */
    public double getNumber(int x, int y) throws Exception {//从矩阵中拿值
        if (this.x > x && this.y > y && x >= 0 && y >= 0) {
            return matrix[x][y];
        } else {
            System.out.println("x==" + x + ",y==" + y + ",maxX:" + this.x + ",maxY:" + this.y);
            throw new Exception("getNumber matrix length too little x:" + x + ",y:" + y);
        }
    }

    /**
     * 计算矩阵中某一行向量或者列向量所有元素的和
     *
     * @param isRow 是否取行向量
     * @param index 索取向量在矩阵当中的下标
     * @return 返回指定向量所有元素的和
     * @throws Exception 超出矩阵范围抛出
     */
    public double getSigmaByVector(boolean isRow, int index) throws Exception {
        double sigma = 0;
        if (index >= 0 && ((isRow && x > index) || (!isRow && y > index))) {
            if (isRow) {//取行向量
                for (int i = 0; i < y; i++) {
                    sigma = matrix[index][i] + sigma;
                }
            } else {
                for (int i = 0; i < x; i++) {
                    sigma = matrix[i][index] + sigma;
                }
            }
        } else {
            throw new Exception("index 数值下标溢出:" + index);
        }
        return sigma;
    }
}
