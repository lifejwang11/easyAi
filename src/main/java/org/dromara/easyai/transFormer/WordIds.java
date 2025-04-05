package org.dromara.easyai.transFormer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/3/29 18:43
 */
public class WordIds {
    private final List<Integer> encoder = new ArrayList<>();
    private final List<Integer> decoder = new ArrayList<>();

    public List<Integer> getEncoder() {
        return encoder;
    }

    public List<Integer> getDecoder() {
        return decoder;
    }
}
