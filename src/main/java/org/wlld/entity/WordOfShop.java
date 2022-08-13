package org.wlld.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordOfShop {
    private String word;//词
    private int id;//词id
    private Set<Integer> shops = new HashSet<>();//拥有的店id

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Integer> getShops() {
        return shops;
    }
}
