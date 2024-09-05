package com.kiskee.dictionarybuilder.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomElementsPicker {

    private static final Random RANDOM = new Random();
    private static final int DEFAULT_NUMBER_OF_ELEMENTS = 4;

    public <T> Set<Integer> getRandomElements(List<T> list, int currentWordIndex) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Set<Integer> indices = new HashSet<>();
        indices.add(currentWordIndex);
        indices.addAll(IntStream.generate(() -> RANDOM.nextInt(list.size()))
                .distinct()
                .limit(Math.min(DEFAULT_NUMBER_OF_ELEMENTS - 1, list.size()))
                .boxed()
                .collect(Collectors.toSet()));
        return indices;
    }

    public <T> T getRandomElement(List<T> list) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return null;
        }
        return list.get(RANDOM.nextInt(list.size()));
    }
}
