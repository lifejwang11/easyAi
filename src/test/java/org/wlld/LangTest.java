package org.wlld;

import org.wlld.naturalLanguage.IOConst;
import org.wlld.naturalLanguage.Talk;
import org.wlld.naturalLanguage.TemplateReader;

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
        templateReader.read("/Users/lidapeng/Desktop/myDocment/a2.txt", "UTF-8", IOConst.NOT_WIN);
        Talk talk = new Talk();
        talk.talk("我要吃面包");
        talk.talk("我渴了");
        talk.talk("我要去看望你");
        talk.talk("我买两盒烟");
    }
}
