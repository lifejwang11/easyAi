package org.wlld;

import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.List;

/**
 * 测试入口类!
 */
public class App {
    public static void main(String[] args) throws Exception {
        createNerveTest();
    }

    public static void createNerveTest() throws Exception {
        NerveManager nerveManager =
                new NerveManager(2, 3, 1, 2);
        nerveManager.init();
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        for (int i = 0; i < sensoryNerves.size(); i++) {
            sensoryNerves.get(i).postMessage(1, 2 + i, true);
        }
    }
}
