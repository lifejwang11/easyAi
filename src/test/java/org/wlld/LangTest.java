package org.wlld;

import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.naturalLanguage.Talk;
import org.wlld.naturalLanguage.TemplateReader;
import org.wlld.naturalLanguage.Tokenizer;
import org.wlld.naturalLanguage.WordTemple;
import org.wlld.naturalLanguage.languageCreator.CatchKeyWord;
import org.wlld.naturalLanguage.languageCreator.KeyWordModel;
import org.wlld.randomForest.DataTable;
import org.wlld.randomForest.RandomForest;

import java.io.*;
import java.util.*;

/**
 * @author lidapeng
 * @description
 * @date 2:07 下午 2020/2/23
 */
public class LangTest {
    public static void main(String[] args) {
        CatchKeyWord catchKeyWord = new CatchKeyWord();
        catchKeyWord.insertModel(readModel("E:\\model\\keyWord.json"));
        List<String> a = catchKeyWord.getKeyWord("热水器不出热水");
        System.out.println("关键词:" + a);
    }

    public static KeyWordModel readModel(String fileName) {
        KeyWordModel model = JSONObject.parseObject(readPaper(fileName), KeyWordModel.class);
        return model;
    }

    private static String readPaper(String fileName) {
        File file = new File(fileName); //创建文件
        Reader read = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //一次读一个字符
            read = new InputStreamReader(new FileInputStream(file));
            int tempchar;
            while ((tempchar = read.read()) != -1) {
                stringBuilder.append((char) tempchar);
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close(); //确保关闭
                } catch (IOException el) {
                }
            }
        }
        return stringBuilder.toString();
    }

    public static void reTest() throws Exception {
        Matrix parameter = new Matrix(29, 3, "[173,198,1]#" +
                "[174,199,1]#" +
                "[170,195,1]#" +
                "[169,194,1]#" +
                "[159,181,1]#" +
                "[154,175,1]#" +
                "[151,175,1]#" +
                "[152,177,1]#" +
                "[175,200,1]#" +
                "[169,194,1]#" +
                "[172,200,1]#" +
                "[170,195,1]#" +
                "[172,197,1]#" +
                "[173,198,1]#" +
                "[157,177,1]#" +
                "[158,178,1]#" +
                "[156,176,1]#" +
                "[156,177,1]#" +
                "[157,177,1]#" +
                "[162,187,1]#" +
                "[165,188,1]#" +
                "[166,189,1]#" +
                "[163,188,1]#" +
                "[162,187,1]#" +
                "[163,186,1]#" +
                "[164,184,1]#" +
                "[163,186,1]#" +
                "[179,202,1]#" +
                "[182,207,1]#");
        Matrix out = new Matrix(29, 1, "[203]#" +
                "[204]#" +
                "[200]#" +
                "[199]#" +
                "[185]#" +
                "[180]#" +
                "[179]#" +
                "[181]#" +
                "[205]#" +
                "[198]#" +
                "[204]#" +
                "[199]#" +
                "[202]#" +
                "[203]#" +
                "[184]#" +
                "[185]#" +
                "[183]#" +
                "[182]#" +
                "[184]#" +
                "[194]#" +
                "[194]#" +
                "[195]#" +
                "[195]#" +
                "[194]#" +
                "[192]#" +
                "[191]#" +
                "[192]#" +
                "[210]#" +
                "[214]#");
        Matrix w = MatrixOperation.getLinearRegression(parameter, out);
        System.out.println(w.getString());
        double r = 162;
        double g = 187;
        double b = r * w.getNumber(0, 0) + g * w.getNumber(1, 0) + w.getNumber(2, 0);
        System.out.println("r:" + r + ",g:" + g + ",b:" + b);
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

    public static void test2() throws Exception {//自定义输入训练语句
        WordTemple wordTemple = new WordTemple();//初始化语言模版，该语言模板训练结束后一定要static出来,在内存中长期持有，Talk识别构造参数进行复用
        Tokenizer tokenizer = new Tokenizer(wordTemple);//学习类
        //训练模板 主键为类别id,值为该类别id的语句集合
        //注意
        //1，若训练量不足，建议训练语句通过标点符号拆分为若干句，且不要将标点符号带入训练语句
        //2，包含数字的语句用统一的占位符代替 例如 35,3,36% 变为 #,#,#%
        Map<Integer, List<String>> model = new HashMap<>();
        //开始训练
        //tokenizer.start(model,null);
    }

    public static void test() throws Exception {
        //创建模板读取累
        TemplateReader templateReader = new TemplateReader();
        WordTemple wordTemple = new WordTemple();//初始化语言模版，该语言模板训练结束后一定要static出来,在内存中长期持有，Talk识别构造参数进行复用
        //wordTemple.setTreeNub(9);
        //wordTemple.setTrustPunishment(0.5);
        //wordTemple.setSplitWord(true);
        //读取语言模版，第一个参数是模版地址，第二个参数是编码方式 (教程里的第三个参数已经省略)
        //同时也是学习过程
        templateReader.read("/Users/lidapeng/Desktop/myDocument/model.txt", "UTF-8", wordTemple);
        Talk talk = new Talk(wordTemple);
        //单纯对输入语句进行切词结果返回，不进行识别
        List<List<String>> lists = talk.getSplitWord("空调坏了，帮我修一修");
        for (List<String> list : lists) {
            System.out.println(list);
        }
        //输入语句进行识别，若有标点符号会形成LIST中的每个元素
        //返回的集合中每个值代表了输入语句，每个标点符号前语句的分类
        List<Integer> list = talk.talk("空调坏了，帮我修一修");
        System.out.println(list);
    }
}
