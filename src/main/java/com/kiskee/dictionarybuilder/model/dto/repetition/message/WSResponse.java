package com.kiskee.dictionarybuilder.model.dto.repetition.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WSResponse {

    private String word;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> translationOptions;

    private String wordHint;
    private Long correctTranslationsCount;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;
}
