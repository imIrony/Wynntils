/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.core.utils.objects;

public class CombatLevel {

    private final int min, max;

    public CombatLevel(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public CombatLevel(int level) {
        this(level, level);
    }

    public int getAverage() {
        return (min + max) / 2;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public String toString() {
        return min == max ? Integer.toString(max) : String.format("%d-%d", min, max);
    }

}
