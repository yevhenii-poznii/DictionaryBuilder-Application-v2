package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryMapper;
import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryRepository;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.DictionaryProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.AbstractDictionaryService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Service
@AllArgsConstructor
public class DictionaryServiceImpl extends AbstractDictionaryService
        implements DictionaryService, DictionaryAccessValidator {

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
    public DictionaryPageResponseDto getDictionaryPageByOwner(
            Long dictionaryId, DictionaryPageRequestDto dictionaryPageRequest) {
        ensureDictionaryBelongsToUser(dictionaryId);
        return load(dictionaryId, dictionaryPageRequest);
    }

    @Override
    public List<DictionaryDto> getDictionaries() {
        UUID userProfileId = IdentityUtil.getUserId();
        List<DictionaryDto> dictionaries = repository.findAllByUserProfileId(userProfileId);
        log.info("Retrieved [{}] dictionaries for user [{}]", dictionaries.size(), userProfileId);
        return dictionaries;
    }

    @Override
    public List<DictionaryDetailDto> getDetailedDictionaries() {
        UUID userProfileId = IdentityUtil.getUserId();
        List<DictionaryDetailDto> dictionaries = repository.findDetailedDictionariesByUserProfileId(userProfileId);
        log.info("Retrieved [{}] detailed dictionaries for user [{}]", dictionaries.size(), userProfileId);
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
    @Transactional
    public ResponseMessage deleteDictionaries(Set<Long> dictionaryIds) {
        UUID userId = IdentityUtil.getUserId();
        List<Long> userDictionaryIds = repository.findIdsByUserProfileIdAndIdIn(userId, dictionaryIds);

        if (userDictionaryIds.isEmpty()) {
            log.info("No dictionaries found for user [{}]", userId);
            throw new ResourceNotFoundException(String.format(
                    ExceptionStatusesEnum.RESOURCES_NOT_FOUND.getStatus(), "Dictionaries", dictionaryIds));
        }
        repository.deleteAllById(userDictionaryIds);
        log.info("Deleted {} dictionaries for user [{}]", userDictionaryIds, userId);
        return new ResponseMessage(String.format(
                VocabularyResponseMessageEnum.DICTIONARIES_DELETED.getResponseMessage(), userDictionaryIds.size()));
    }

    @Override
    @Transactional
    public ResponseMessage deleteDictionary(Long dictionaryId) {
        UUID userProfileId = IdentityUtil.getUserId();
        Dictionary dictionary = repository.getUserDictionary(dictionaryId, userProfileId);
        repository.delete(dictionary);

        log.info("Dictionary with id [{}] deleted for user [{}]", dictionaryId, userProfileId);
        return new ResponseMessage(String.format(
                VocabularyResponseMessageEnum.DICTIONARY_DELETED.getResponseMessage(), dictionary.getDictionaryName()));
    }

    @Override
    public void verifyUserHasDictionary(Long dictionaryId) {
        ensureDictionaryBelongsToUser(dictionaryId);
    }

    @Override
    public DictionaryDto getDictionaryByIdAndUserId(Long dictionaryId, UUID userId) {
        return repository
                .findDictionaryDtoByIdAndUserProfileId(dictionaryId, userId)
                .orElseThrow(
                        ThrowUtil.throwNotFoundException(Dictionary.class.getSimpleName(), dictionaryId.toString()));
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

    private void ensureDictionaryBelongsToUser(Long dictionaryId) {
        UUID userProfileId = IdentityUtil.getUserId();
        if (!repository.existsByIdAndUserProfileId(dictionaryId, userProfileId)) {
            log.info("Dictionary with id [{}] not found for user [{}]", dictionaryId, userProfileId);
            throw new ResourceNotFoundException(String.format(
                    ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                    Dictionary.class.getSimpleName(),
                    dictionaryId));
        }
    }

    private DictionarySaveResponse mapToResponse(Dictionary dictionary, VocabularyResponseMessageEnum responseMessage) {
        DictionaryProjection dictionaryProjection = mapper.toDto(dictionary);
        return new DictionarySaveResponse(
                String.format(responseMessage.getResponseMessage(), dictionary.getDictionaryName()),
                dictionaryProjection);
    }
}
