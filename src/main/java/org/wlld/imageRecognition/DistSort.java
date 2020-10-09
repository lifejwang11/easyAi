package org.wlld.imageRecognition;

import org.wlld.imageRecognition.border.DistBody;

import java.util.Comparator;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class DistSort implements Comparator<DistBody> {
    @Override
    public int compare(DistBody o1, DistBody o2) {
        if (o1.getDist() < o2.getDist()) {
            return -1;
        } else if (o1.getDist() > o2.getDist()) {
            return 1;
        } else {
            return 0;
        }
    }
}
