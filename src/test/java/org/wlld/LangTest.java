package org.wlld;

import org.wlld.naturalLanguage.Talk;
import org.wlld.naturalLanguage.TemplateReader;
import org.wlld.randomForest.DataTable;
import org.wlld.randomForest.RandomForest;

import java.util.*;

/**
 * @author lidapeng
 * @description
 * @date 2:07 下午 2020/2/23
 */
public class LangTest {
    public static void main(String[] args) throws Exception {
        List<Double> listAll = new ArrayList<>();
        List<Double> list = new ArrayList<>();
        List<Double> list2 = new ArrayList<>();
        List<Double> list3 = new ArrayList<>();
        list.add(1.0);
        list.add(2.0);
        list2.add(3.0);
        list2.add(4.0);
        list3.add(5.0);
        list3.add(6.0);
        listAll.addAll(list);
        listAll.addAll(list2);
        listAll.addAll(list3);
        System.out.println(listAll);
        //test1();
    }

    public static void test1() throws Exception {
        Set<String> column = new HashSet<>();
        column.add("math");
        column.add("eng");
        column.add("word");
        column.add("history");
        column.add("h1");
        column.add("h2");
        column.add("h3");
        column.add("h4");
        column.add("point");
        DataTable dataTable = new DataTable(column);
        dataTable.setKey("point");
        RandomForest randomForest = new RandomForest(7);
        randomForest.init(dataTable);//唤醒随机森林里的树木
        //创建实体类输入数据
        for (int i = 0; i < 100; i++) {
            Student student = new Student();
            student.setEng(getPoint());
            student.setH1(getPoint());
            student.setH2(getPoint());
            student.setH3(getPoint());
            student.setH4(getPoint());
            student.setMath(getPoint());
            student.setWord(getPoint());
            student.setHistory(getPoint());
            student.setPoint(getPoint());
            randomForest.insert(student);
        }
        randomForest.study();
        //以上都是学习过程
        Student student = new Student();
        student.setEng(getPoint());
        student.setH1(getPoint());
        student.setH2(getPoint());
        student.setH3(getPoint());
        student.setH4(getPoint());
        student.setMath(getPoint());
        student.setWord(getPoint());
        student.setHistory(getPoint());
        int point = randomForest.forest(student);
        System.out.println("当前学生成绩的综合评定是：" + point);
    }

    private static int getPoint() {
        return new Random().nextInt(3) + 1;
    }

    public static void test() throws Exception {
        //创建模板读取累
        TemplateReader templateReader = new TemplateReader();
        //读取语言模版，第一个参数是模版地址，第二个参数是编码方式 (教程里的第三个参数已经省略)
        //同时也是学习过程
        templateReader.read("/Users/lidapeng/Desktop/myDocment/a1.txt", "UTF-8");
        //学习结束获取模型参数
        //WordModel wordModel = WordTemple.get().getModel();
        //不用学习注入模型参数
        //WordTemple.get().insertModel(wordModel);
        Talk talk = new Talk();
        //输入语句进行识别，若有标点符号会形成LIST中的每个元素
        //返回的集合中每个值代表了输入语句，每个标点符号前语句的分类
        List<Integer> list = talk.talk("帮我配把锁");
        System.out.println(list);
    }
}
