package org.wlld.imageRecognition;

import org.wlld.imageRecognition.modelEntity.MappingBody;

import java.util.Comparator;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class MappingSort implements Comparator<MappingBody> {
    @Override
    public int compare(MappingBody o1, MappingBody o2) {
        if (o1.getMappingNub() > o2.getMappingNub()) {
            return -1;
        } else if (o1.getMappingNub() < o2.getMappingNub()) {
            return 1;
        }
        return 0;
    }
}
