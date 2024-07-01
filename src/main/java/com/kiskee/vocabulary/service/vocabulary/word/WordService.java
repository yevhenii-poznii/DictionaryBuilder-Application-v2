package com.kiskee.vocabulary.service.vocabulary.word;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import java.util.Set;

public interface WordService {

    WordSaveResponse addWord(Long dictionaryId, WordSaveRequest wordSaveRequest);

    WordSaveResponse updateWord(Long dictionaryId, Long wordId, WordUpdateRequest updateRequest);

    ResponseMessage updateRepetition(Long dictionaryId, Long wordId, Boolean useInRepetition);

    ResponseMessage deleteWord(Long dictionaryId, Long wordId);

    ResponseMessage deleteWords(Long dictionaryId, Set<Long> wordIds);
}
