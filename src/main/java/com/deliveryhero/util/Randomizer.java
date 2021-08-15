package com.deliveryhero.util;

import lombok.Data;

import java.util.*;

@Data
public class Randomizer {
    public static Random random = initRandom();

    private static Random initRandom() {
        random = new Random(1);
        for (int i = 0; i < 10; i++) {
            random.nextDouble();
        }
        return random;
    }

    public static <T> T nextElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Iterator<T> shuffledIterator(List<T> list) {
        List<T> listShuffled = new ArrayList<T>(list);
        Collections.shuffle(listShuffled, random);
        return listShuffled.iterator();
    }
}
