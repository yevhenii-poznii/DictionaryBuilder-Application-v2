package com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DictionaryPageResponseDto {

    List<WordDto> words;
    int totalPages;
    int totalElements;
}
