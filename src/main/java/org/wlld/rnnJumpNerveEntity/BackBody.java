package org.wlld.rnnJumpNerveEntity;

public class BackBody implements Runnable {
    private final double parameter;
    private final long eventId;
    private final boolean fromOutNerve;
    private final int[] storeys;
    private final int index;
    private final Nerve nerve;

    public BackBody(double parameter, long eventId, boolean fromOutNerve, int[] storeys, int index, Nerve nerve) {
        this.parameter = parameter;
        this.eventId = eventId;
        this.fromOutNerve = fromOutNerve;
        this.storeys = storeys;
        this.index = index;
        this.nerve = nerve;
    }

    @Override
    public void run() {
        try {
            nerve.backGetMessage(parameter, eventId, fromOutNerve, storeys, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
