package org.dromara.easyai.naturalLanguage.word;

import java.util.List;

public class Trust {
    private List<Integer> keys;//结果id
    private float trust;//信任度

    public List<Integer> getKeys() {
        return keys;
    }

    public void setKeys(List<Integer> keys) {
        this.keys = keys;
    }

    public float getTrust() {
        return trust;
    }

    public void setTrust(float trust) {
        this.trust = trust;
    }
}
