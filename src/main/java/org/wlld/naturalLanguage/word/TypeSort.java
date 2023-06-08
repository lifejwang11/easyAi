package org.wlld.naturalLanguage.word;

import java.util.Comparator;

public class TypeSort implements Comparator<TypeBody> {
    @Override
    public int compare(TypeBody o1, TypeBody o2) {
        if (o1.getPower() > o2.getPower()) {
            return -1;
        } else if (o1.getPower() < o2.getPower()) {
            return 1;
        }
        return 0;
    }
}
