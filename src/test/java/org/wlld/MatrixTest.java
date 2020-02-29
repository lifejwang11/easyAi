package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.randomForest.DataTable;
import org.wlld.randomForest.Node;
import org.wlld.randomForest.Tree;

import java.awt.*;
import java.util.*;

/**
 * @author lidapeng
 * @description
 * @date 3:35 下午 2020/1/23
 */
public class MatrixTest {
    public static void main(String[] args) throws Exception {
        test4();
    }

    public static void test4() throws Exception {
        Set<String> column = new HashSet<>();
        column.add("height");
        column.add("weight");
        column.add("sex");
        column.add("h1");
        column.add("h2");
        DataTable dataTable = new DataTable(column);
        dataTable.setKey("sex");
        Random random = new Random();
        int cla = 3;
        for (int i = 0; i < 50; i++) {
            Food food = new Food();
            food.setHeight(random.nextInt(cla));
            food.setWeight(random.nextInt(cla));
            food.setSex(random.nextInt(cla));
            food.setH1(random.nextInt(cla));
            food.setH2(random.nextInt(cla));
//            System.out.println("index==" + i + ",height==" + food.getHeight() +
//                    ",weight==" + food.getWeight() + ",sex==" + food.getSex());
            dataTable.insert(food);
        }
        Tree tree = new Tree(dataTable);
        tree.study();
        Node node = tree.getRootNode();
        String a = JSON.toJSONString(node);
        Node node1 = JSONObject.parseObject(a, Node.class);
        ////
        Tree tree2 = new Tree(dataTable);
        tree2.setRootNode(node1);
        for (int i = 0; i < 10; i++) {
            Food food = new Food();
            food.setHeight(random.nextInt(cla));
            food.setWeight(random.nextInt(cla));
            food.setSex(random.nextInt(cla));
            food.setH1(random.nextInt(cla));
            food.setH2(random.nextInt(cla));
            int type = tree.judge(food).getType();
            int type2 = tree2.judge(food).getType();
            if (type != type2) {
                System.out.println("出错,type1==" + type + ",type2==" + type2);
            } else {
                System.out.println(type);
            }
        }
        System.out.println("结束");

    }


    public static void test3() throws Exception {
        Matrix matrix = new Matrix(4, 3);
        Matrix matrixY = new Matrix(4, 1);
        String b = "[7]#" +
                "[8]#" +
                "[9]#" +
                "[19]#";
        matrixY.setAll(b);
        String a = "[1,2,17]#" +
                "[3,4,18]#" +
                "[5,6,10]#" +
                "[15,16,13]#";
        matrix.setAll(a);
        //将参数矩阵转置
        Matrix matrix1 = MatrixOperation.transPosition(matrix);
        //转置的参数矩阵乘以参数矩阵
        Matrix matrix2 = MatrixOperation.mulMatrix(matrix1, matrix);
        //求上一步的逆矩阵
        Matrix matrix3 = MatrixOperation.getInverseMatrixs(matrix2);
        //逆矩阵乘以转置矩阵
        Matrix matrix4 = MatrixOperation.mulMatrix(matrix3, matrix1);
        //最后乘以输出矩阵,生成权重矩阵
        Matrix matrix5 = MatrixOperation.mulMatrix(matrix4, matrixY);
        System.out.println(matrix5.getString());
    }

    public static void test1() throws Exception {
        Matrix matrix = new Matrix(2, 2);
        Matrix matrix2 = new Matrix(1, 5);
        String b = "[6,7,8,9,10]#";
        String a = "[1,2]#" +
                "[3,4]#";
        matrix.setAll(a);
        matrix2.setAll(b);
        Matrix matrix1 = MatrixOperation.matrixToVector(matrix, true);
        matrix1 = MatrixOperation.push(matrix1, 5, true);
        matrix1 = MatrixOperation.pushVector(matrix1, matrix2, true);
        System.out.println(matrix1.getString());
    }

    public static void test2() throws Exception {
        Matrix matrix = new Matrix(2, 2);
        Matrix matrix1 = new Matrix(1, 5);
        String a = "[1,2]#" +
                "[3,4]#";
        String b = "[6,7,8,9,10]#";
        matrix.setAll(a);
        matrix1.setAll(b);
        matrix1 = MatrixOperation.matrixToVector(matrix1, false);
        matrix = MatrixOperation.matrixToVector(matrix, false);
        matrix = MatrixOperation.push(matrix, 5, true);
        matrix = MatrixOperation.pushVector(matrix, matrix1, false);
        System.out.println(matrix.getString());
    }
}
