package org.wlld.rnnJumpNerveCenter;

import org.wlld.rnnJumpNerveEntity.BackBody;
import org.wlld.rnnJumpNerveEntity.InputBody;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferStation {//神经链接中转站
    private static final TransferStation transferStation = new TransferStation();
    private static ExecutorService POOL;
    private CountDownLatch latch;//线程计数器
    private int allNumber;

    public CountDownLatch getLatch() {
        return latch;
    }

    public void next() {
        latch = new CountDownLatch(allNumber);
    }

    private TransferStation() {
    }

    public void message(InputBody inputBody, BackBody backBody) {
        if (inputBody != null) {
            POOL.execute(inputBody);//从线程池里获取启动业务线程
        } else {
            POOL.execute(backBody);//从线程池里获取启动业务线程
        }
    }

    public void initPooL(int threadNumber, int allNumber) {
        POOL = Executors.newFixedThreadPool(threadNumber);
        latch = new CountDownLatch(allNumber);
        this.allNumber = allNumber;
    }

    public static TransferStation getTransferStation() {
        return transferStation;
    }
}
