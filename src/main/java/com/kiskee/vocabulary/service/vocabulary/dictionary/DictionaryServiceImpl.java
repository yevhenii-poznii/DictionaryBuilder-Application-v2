package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryMapper;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryRepository;
import com.kiskee.vocabulary.repository.vocabulary.projections.DictionaryProjection;
import com.kiskee.vocabulary.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository repository;
    private final DictionaryMapper mapper;

    private final DictionaryPageLoaderFactory dictionaryPageLoaderFactory;

    @Override
    public DictionarySaveResponse addDictionary(DictionarySaveRequest dictionarySaveRequest) {
        Dictionary dictionary = createEmptyDictionary(dictionarySaveRequest.getDictionaryName());

        return mapToResponse(dictionary, VocabularyResponseMessageEnum.DICTIONARY_CREATED);
    }

    @Override
    public Dictionary addDictionary(String dictionaryName) {
        return createEmptyDictionary(dictionaryName);
    }

    @Override
    @Transactional
    public DictionaryPageResponseDto getDictionaryPageByOwner(Long dictionaryId,
                                                              DictionaryPageRequestDto dictionaryPageRequest) {
        UUID userProfileId = IdentityUtil.getUserId();

        if (!repository.existsByIdAndUserProfileId(dictionaryId, userProfileId)) {
            throw new ResourceNotFoundException(String.format(ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                    Dictionary.class.getSimpleName(), dictionaryId));
        }

        return getDictionaryPage(dictionaryId, dictionaryPageRequest);
    }

    @Override
    public List<DictionaryDto> getDictionaries() {
        UUID userProfileId = IdentityUtil.getUserId();

        List<DictionaryDto> dictionaries = repository.findAllByUserProfileId(userProfileId);

        log.info("Retrieved [{}] dictionaries for user [{}]", dictionaries.size(), userProfileId);

        return dictionaries;
    }

    @Override
    public DictionarySaveResponse updateDictionary(Long dictionaryId, DictionarySaveRequest dictionarySaveRequest) {
        UUID userProfileId = IdentityUtil.getUserId();

        validateDictionaryName(dictionarySaveRequest.getDictionaryName(), userProfileId);

        Dictionary dictionaryToUpdate = repository.getUserDictionary(dictionaryId, userProfileId);

        dictionaryToUpdate.setDictionaryName(dictionarySaveRequest.getDictionaryName());

        Dictionary dictionary = repository.save(dictionaryToUpdate);

        return mapToResponse(dictionary, VocabularyResponseMessageEnum.DICTIONARY_UPDATED);
    }

    @Override
    public ResponseMessage deleteDictionary(Long dictionaryId) {
        UUID userProfileId = IdentityUtil.getUserId();

        Dictionary dictionary = repository.getUserDictionary(dictionaryId, userProfileId);

        repository.delete(dictionary);

        log.info("Dictionary with id [{}] deleted for user [{}]", dictionaryId, userProfileId);

        return new ResponseMessage(String.format(
                VocabularyResponseMessageEnum.DICTIONARY_DELETED.getResponseMessage(), dictionary.getDictionaryName()));
    }

    private Dictionary createEmptyDictionary(String dictionaryName) {
        UUID userProfileId = IdentityUtil.getUserId();

        validateDictionaryName(dictionaryName, userProfileId);

        return buildDictionaryAndSave(dictionaryName, userProfileId);
    }

    private void validateDictionaryName(String dictionaryName, UUID userProfileId) {
        if (repository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionaryName, userProfileId)) {
            log.info("Dictionary with name [{}] already exists for user [{}]", dictionaryName, userProfileId);

            throw new DuplicateResourceException(String.format(
                    VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), dictionaryName));
        }
    }

    private Dictionary buildDictionaryAndSave(String dictionaryName, UUID userProfileId) {
        Dictionary dictionary = Dictionary.builder()
                .dictionaryName(dictionaryName)
                .words(List.of())
                .userProfileId(userProfileId)
                .build();

        Dictionary savedDictionary = repository.save(dictionary);

        log.info("New dictionary with name [{}] for user [{}] saved", dictionaryName, userProfileId);

        return savedDictionary;
    }

    private DictionaryPageResponseDto getDictionaryPage(Long dictionaryId,
                                                        DictionaryPageRequestDto dictionaryPageRequest) {
        int page = Optional.ofNullable(dictionaryPageRequest.getPage())
                .orElse(0);

        int size = Optional.ofNullable(dictionaryPageRequest.getSize())
                .orElse(100);

        PageFilter pageFilter = Optional.ofNullable(dictionaryPageRequest.getFilter())
                .orElse(PageFilter.BY_ADDED_AT_ASC);

        PageRequest pageRequest = PageRequest.of(page, size);

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(pageFilter);

        return dictionaryPageLoader.loadDictionaryPage(dictionaryId, pageRequest);
    }

    private DictionarySaveResponse mapToResponse(Dictionary dictionary, VocabularyResponseMessageEnum responseMessage) {
        DictionaryProjection dictionaryProjection = mapper.toDto(dictionary);

        return new DictionarySaveResponse(String.format(
                responseMessage.getResponseMessage(), dictionary.getDictionaryName()), dictionaryProjection);
    }

}
