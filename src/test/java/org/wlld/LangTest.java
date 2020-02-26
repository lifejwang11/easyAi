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
        TemplateReader templateReader = new TemplateReader();
        templateReader.read("/Users/lidapeng/Desktop/myDocment/a1.txt", "UTF-8", IOConst.NOT_WIN);
        Talk talk = new Talk();
        List<Integer> list = talk.talk("被锁外面了");
        System.out.println(list);
    }
}
