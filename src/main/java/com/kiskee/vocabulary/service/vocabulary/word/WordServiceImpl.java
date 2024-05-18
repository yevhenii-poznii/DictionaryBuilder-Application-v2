package com.kiskee.vocabulary.service.vocabulary.word;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.LogMessageEnum;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.ForbiddenAccessException;
import com.kiskee.vocabulary.mapper.dictionary.WordMapper;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.word.translation.WordTranslationService;
import com.kiskee.vocabulary.util.IdentityUtil;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class WordServiceImpl implements WordService {

    private final WordRepository repository;
    private final WordMapper mapper;

    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordTranslationService wordTranslationService;

    @Override
    @Transactional
    public WordSaveResponse addWord(Long dictionaryId, WordSaveRequest wordSaveRequest) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        Word wordToSave = mapper.toEntity(wordSaveRequest, dictionaryId);

        Word saved = repository.save(wordToSave);

        return mapToResponse(saved, VocabularyResponseMessageEnum.WORD_ADDED, LogMessageEnum.WORD_ADDED);
    }

    @Override
    @Transactional
    public WordSaveResponse updateWord(Long dictionaryId, Long wordId, WordUpdateRequest updateRequest) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        Word wordToUpdate = repository.getWord(wordId);

        verifyWordBelongsToSpecifiedDictionary(wordToUpdate, dictionaryId);

        List<WordTranslation> wordTranslations = wordTranslationService.updateTranslations(
                updateRequest.getWordTranslations(), wordToUpdate.getWordTranslations());

        wordToUpdate = mapper.toEntity(wordToUpdate, updateRequest, wordTranslations);

        Word updated = repository.save(wordToUpdate);

        return mapToResponse(updated, VocabularyResponseMessageEnum.WORD_UPDATED, LogMessageEnum.WORD_UPDATED);
    }

    @Override
    @Transactional
    public ResponseMessage deleteWord(Long dictionaryId, Long wordId) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        Word wordToDelete = repository.getWord(wordId);

        verifyWordBelongsToSpecifiedDictionary(wordToDelete, dictionaryId);

        repository.delete(wordToDelete);

        displayLog(LogMessageEnum.WORD_DELETED, wordToDelete);

        return new ResponseMessage(
                String.format(VocabularyResponseMessageEnum.WORD_DELETED.getResponseMessage(), wordToDelete));
    }

    @Override
    @Transactional
    public ResponseMessage deleteWords(Long dictionaryId, Set<Long> wordIds) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        List<Word> wordsToDelete = repository.findByIdIn(wordIds);

        verifyWordBelongsToSpecifiedDictionary(wordsToDelete, dictionaryId);

        repository.deleteAll(wordsToDelete);

        return new ResponseMessage(String.format("Words %s have been deleted", wordsToDelete));
    }

    private void verifyWordBelongsToSpecifiedDictionary(List<Word> words, Long dictionaryId) {
        List<Long> forbiddenWords = words.stream()
                .filter(word -> !word.getDictionaryId().equals(dictionaryId))
                .map(Word::getId)
                .toList();

        if (CollectionUtils.isNotEmpty(forbiddenWords)) {
            throw new ForbiddenAccessException(String.format(
                    ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), forbiddenWords));
        }
    }

    private void verifyWordBelongsToSpecifiedDictionary(Word word, Long dictionaryId) {
        if (!word.getDictionaryId().equals(dictionaryId)) {
            throw new ForbiddenAccessException(String.format(
                    ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(),
                    Word.class.getSimpleName(),
                    word.getId().toString()));
        }
    }

    private void displayLog(LogMessageEnum logMessage, Word word) {
        log.info(logMessage.getMessage(), word.getWord(), word.getDictionaryId(), IdentityUtil.getUserId());
    }

    private WordSaveResponse mapToResponse(
            Word word, VocabularyResponseMessageEnum messageEnum, LogMessageEnum logMessageEnum) {
        displayLog(logMessageEnum, word);

        WordDto wordDto = mapper.toDto(word);

        return new WordSaveResponse(String.format(messageEnum.getResponseMessage(), wordDto.getWord()), wordDto);
    }
}
