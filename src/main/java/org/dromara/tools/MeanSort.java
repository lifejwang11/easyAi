package org.dromara.tools;

import org.dromara.entity.RGBNorm;

import java.util.Comparator;

public class MeanSort implements Comparator<RGBNorm> {
    @Override
    public int compare(RGBNorm o1, RGBNorm o2) {
        if (o1.getNorm() > o2.getNorm()) {
            return 1;
        } else if (o1.getNorm() < o2.getNorm()) {
            return -1;
        }
        return 0;
    }
}