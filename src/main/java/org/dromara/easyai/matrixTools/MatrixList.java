package org.dromara.easyai.matrixTools;

/**
 * @author lidapeng 矩阵预扩容容器
 * @time 2025/2/21 10:18
 */
public class MatrixList {
    private int pointerX;
    private int pointerY;
    private int capacity = 256;
    private boolean addRow;
    private Matrix matrix;

    public int getX() {
        return pointerX;
    }

    public int getY() {
        return pointerY;
    }

    private void copy(Matrix myMatrix) throws Exception {
        for (int i = 0; i < this.pointerX; i++) {
            for (int j = 0; j < this.pointerY; j++) {
                myMatrix.setNub(i, j, matrix.getNumber(i, j));
            }
        }
        matrix = myMatrix;
    }

    public Matrix getMatrix() throws Exception {
        Matrix myMatrix = new Matrix(pointerX, pointerY);
        for (int i = 0; i < this.pointerX; i++) {
            for (int j = 0; j < this.pointerY; j++) {
                myMatrix.setNub(i, j, matrix.getNumber(i, j));
            }
        }
        return myMatrix;
    }

    private void insert(Matrix addMatrix) throws Exception {
        int xSize, ySize, startX, startY;
        if (addRow) {
            xSize = pointerX + addMatrix.getX();
            ySize = pointerY;
            startX = pointerX;
            startY = 0;
        } else {
            xSize = pointerX;
            ySize = pointerY + addMatrix.getY();
            startX = 0;
            startY = pointerY;
        }
        for (int i = startX; i < xSize; i++) {
            for (int j = startY; j < ySize; j++) {
                matrix.setNub(i, j, addMatrix.getNumber(i - startX, j - startY));
            }
        }
    }

    public void add(Matrix addMatrix) throws Exception {
        if (addMatrix != null) {
            if (addRow) {//向行添加
                if (pointerY == addMatrix.getY()) {
                    int x = addMatrix.getX();
                    if (pointerX + x > matrix.getX()) {//超出容量了 进行扩容后复制数值
                        copy(new Matrix(pointerX + x + capacity, pointerY));
                    }
                    insert(addMatrix);
                    pointerX = pointerX + x;
                } else {
                    throw new Exception("向行添加，列数不一致");
                }
            } else {//向列添加
                if (pointerX == addMatrix.getX()) {
                    int y = addMatrix.getY();
                    if (pointerY + y > matrix.getY()) {//超出容量，进行扩容后复制数值
                        copy(new Matrix(pointerX, pointerY + y + capacity));
                    }
                    insert(addMatrix);
                    pointerY = pointerY + y;
                } else {
                    throw new Exception("向列添加，行数不一致");
                }
            }

        }
    }

    public MatrixList(Matrix firstMatrix, boolean addRow, int capacity) throws Exception {
        this.capacity = capacity;
        init(firstMatrix, addRow);
    }

    public MatrixList(Matrix firstMatrix, boolean addRow) throws Exception {
        init(firstMatrix, addRow);
    }

    private void init(Matrix firstMatrix, boolean addRow) throws Exception {
        this.addRow = addRow;
        pointerX = firstMatrix.getX();
        pointerY = firstMatrix.getY();
        if (addRow) {
            matrix = new Matrix(pointerX + capacity, pointerY);
        } else {
            matrix = new Matrix(pointerX, pointerY + capacity);
        }
        for (int i = 0; i < pointerX; i++) {
            for (int j = 0; j < pointerY; j++) {
                matrix.setNub(i, j, firstMatrix.getNumber(i, j));
            }
        }
    }
}
