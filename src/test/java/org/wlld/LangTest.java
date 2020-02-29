package org.wlld;

import org.wlld.naturalLanguage.IOConst;
import org.wlld.naturalLanguage.Talk;
import org.wlld.naturalLanguage.TemplateReader;

import java.util.List;

/**
 * @author lidapeng
 * @description
 * @date 2:07 下午 2020/2/23
 */
public class LangTest {
    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {
        //创建模板读取累
        TemplateReader templateReader = new TemplateReader();
        //读取语言模版，第一个参数是模版地址，第二个参数是编码方式，第三个参数是是否是WIN系统
        //同时也是学习过程
        templateReader.read("/Users/lidapeng/Desktop/myDocment/a1.txt", "UTF-8", IOConst.NOT_WIN);
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
