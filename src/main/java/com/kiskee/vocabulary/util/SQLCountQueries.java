package com.kiskee.vocabulary.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SQLCountQueries {

    public final String WORD_COUNT_QUERY = "(SELECT COUNT(w.id) FROM word w WHERE w.dictionary_id = id)";

}
