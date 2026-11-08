package com.abyss.abyss_outpost.factory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class Picker {
    private Picker() {
    }

    static <T> T pick(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
