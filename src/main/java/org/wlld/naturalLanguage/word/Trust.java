package org.wlld.naturalLanguage.word;

import java.util.List;

public class Trust {
    private List<Integer> keys;//结果id
    private double trust;//信任度

    public List<Integer> getKeys() {
        return keys;
    }

    public void setKeys(List<Integer> keys) {
        this.keys = keys;
    }

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }
}
