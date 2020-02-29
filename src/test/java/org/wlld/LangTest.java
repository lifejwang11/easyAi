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
        //学习过程 过程（长期内存持有）
        //模版类
        TemplateReader templateReader = new TemplateReader();
        //读取模版
        templateReader.read("/Users/lidapeng/Desktop/myDocment/a1.txt", "UTF-8", IOConst.NOT_WIN);
        //识别过程
        Talk talk = new Talk();
        //我饿了，我想吃个饭
        List<Integer> list = talk.talk("语速");
        System.out.println(list);
    }
}
