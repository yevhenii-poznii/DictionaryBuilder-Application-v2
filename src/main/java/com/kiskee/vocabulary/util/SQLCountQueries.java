package com.kiskee.vocabulary.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SQLCountQueries {

    public final String WORD_COUNT_QUERY = "(SELECT COUNT(w.id) FROM word w WHERE w.dictionary_id = id)";
    public final String USE_IN_REPETITION_TRUE_COUNT =
            "(SELECT COUNT(w.id) FROM word w WHERE w.dictionary_id = id AND w.use_in_repetition = true)";
    public final String USE_IN_REPETITION_FALSE_COUNT =
            "(SELECT COUNT(w.id) FROM word w WHERE w.dictionary_id = id AND w.use_in_repetition = false)";
}
