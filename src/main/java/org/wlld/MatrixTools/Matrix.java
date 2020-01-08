package org.wlld.MatrixTools;

import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;

public class Matrix {
    private double[][] matrix;//矩阵本体
    private int x;//矩阵的行数
    private int y;//矩阵的列数

    public int getX() {//获取行数
        return x;
    }

    public int getY() {//获取列数
        return y;
    }

    public Matrix(int x, int y) {//初始化矩阵
        matrix = new double[x][y];
        this.x = x;
        this.y = y;
    }

    public Matrix(int x, int y, String matr) throws Exception {//初始化矩阵
        matrix = new double[x][y];
        this.x = x;
        this.y = y;
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
            if (coordinate.coordinateList.size() > 0) {//继续向丛林深处进发
                defCalculation(coordinate.coordinateList);
            } else {//到道路的尽头了,进行核算
                mulFather(coordinate, 1, new ArrayList<>());
            }
        }
    }

    private double mulFather(Coordinate coordinate, double element, List<Coordinate> div) {
//        System.out.println("x=====" + coordinate.x + ",y===" + coordinate.y +
//                ",element==" + matrix[coordinate.x][coordinate.y]);
        div.add(coordinate);
        element = ArithUtil.mul(matrix[coordinate.x][coordinate.y], element);
        if (coordinate.father != null) {
            element = mulFather(coordinate.father, element, div);
        } else {//道路尽头
            //System.out.println("尽头============" + element);
            if (invinverse(div)) {//偶排列
                defNub = defNub + element;
            } else {//奇排列
                defNub = defNub - element;
            }
            div.clear();
            element = 1;
        }
        return element;
    }

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

    private boolean invinverse(List<Coordinate> list) {//获取排列奇偶性
        boolean parity = true;//默认是偶排列
        double[] row = new double[list.size()];
        double[] clo = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            row[i] = list.get(i).x + 1;
            clo[i] = list.get(i).y + 1;
        }
        int rowInv = MatrixOperation.inverseNumber(row);
        int cloInv = MatrixOperation.inverseNumber(clo);
        int inverserNumber = rowInv + cloInv;
        if (inverserNumber % 2 != 0) {//奇排列
            parity = false;
        }
        return parity;
    }

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

    public Matrix getSonOfMatrix(int x, int y, int xSize, int ySize) {//将矩阵分块
        Matrix myMatrix = new Matrix(xSize, ySize);
        int xr = 0;
        int yr = 0;
        try {
            for (int i = 0; i < xSize; i++) {
                xr = i + x;
                for (int j = 0; j < ySize; j++) {
                    yr = j + y;
                    myMatrix.setNub(i, j, matrix[xr][yr]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myMatrix;
    }

    public Matrix getRow(int x) throws Exception {//获取行向量
        Matrix myMatrix = new Matrix(1, y);
        for (int i = 0; i < y; i++) {
            myMatrix.setNub(0, i, matrix[x][i]);
        }
        return myMatrix;
    }

    public Matrix getColumn(int y) throws Exception {//获取列向量
        Matrix myMatrix = new Matrix(x, 1);
        for (int i = 0; i < x; i++) {
            myMatrix.setNub(i, 0, matrix[i][y]);
        }
        return myMatrix;
    }

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

    public String getPositionString() {//矩阵输出字符串
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < x; i++) {
            builder.append(i + ":[");
            for (int j = 0; j < y; j++) {
                double number = matrix[i][j];
                if (j == 0) {
                    builder.append(number);
                } else {
                    builder.append(",(j:" + j + ",i:" + i + ")" + number);
                }
            }
            builder.append("]\r\n");
        }
        return builder.toString();
    }

    public void setNub(int x, int y, double number) throws Exception {//给矩阵设置值
        if (this.x >= x && this.y >= y) {
            matrix[x][y] = number;
        } else {
            System.out.println("myX==" + this.x + ",myY==" + this.y + ",x==" + x + ",y==" + y);
            throw new Exception("matrix length too little");
        }
    }

    public double getNumber(int x, int y) throws Exception {//从矩阵中拿值
        if (this.x >= x && this.y >= y) {
            return matrix[x][y];
        } else {
            throw new Exception("matrix length too little");
        }
    }
}
