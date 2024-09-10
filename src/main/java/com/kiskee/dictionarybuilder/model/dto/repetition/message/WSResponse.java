package com.kiskee.dictionarybuilder.model.dto.repetition.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WSResponse {

    private String word;
    private String wordHint;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;
    private Long correctTranslationsCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> translationOptions;
}
