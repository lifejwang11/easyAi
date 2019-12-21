package org.wlld;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.Nerve;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

import java.util.List;
import java.util.Random;

/**
 * 测试入口类!
 */
public class App {
    public static void main(String[] args) {
        Random random = new Random();
        double n = random.nextDouble();
        System.out.println(n);
    }

    public static void createNerveTest() {
        NerveManager nerveManager =
                new NerveManager(1, 2, 1, 2);
        nerveManager.init();
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        SensoryNerve sensoryNerve = sensoryNerves.get(0);
        sensoryNerve.postMessage(20);
    }
}
