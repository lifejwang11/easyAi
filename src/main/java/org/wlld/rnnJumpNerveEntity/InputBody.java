package org.wlld.rnnJumpNerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.Map;

public class InputBody implements Runnable {
    private final long eventId;
    private final double parameter;
    private final boolean isStudy;
    private final Map<Integer, Double> E;
    private final OutBack imageBack;
    private final boolean isEmbedding;
    private final Matrix rnnMatrix;
    private final int[] storeys;
    private final int index;
    private final int fromID;
    private final Nerve nerve;

    public InputBody(long eventId, double parameter, boolean isStudy
            , Map<Integer, Double> E, OutBack imageBack, boolean isEmbedding, Matrix rnnMatrix
            , int[] storeys, int index, int fromID, Nerve nerve) {
        this.eventId = eventId;
        this.parameter = parameter;
        this.isStudy = isStudy;
        this.E = E;
        this.imageBack = imageBack;
        this.isEmbedding = isEmbedding;
        this.rnnMatrix = rnnMatrix;
        this.storeys = storeys;
        this.index = index;
        this.fromID = fromID;
        this.nerve = nerve;
    }

    @Override
    public void run() {
        try {
            nerve.input(eventId, parameter, isStudy, E, imageBack, isEmbedding, rnnMatrix, storeys, index, fromID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
