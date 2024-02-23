package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryRepository;
import com.kiskee.vocabulary.repository.vocabulary.projections.DictionaryProjection;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Getter
@AllArgsConstructor
public class DictionaryService implements DictionaryCreationService {

    private final DictionaryRepository repository;
    private final DictionaryMapper mapper;

    @Override
    public DictionarySaveResponse addDictionary(DictionarySaveRequest dictionarySaveRequest) {
        Dictionary dictionary = createEmptyDictionary(dictionarySaveRequest.getDictionaryName());

        DictionaryProjection dictionaryProjection = mapper.toDto(dictionary);

        return new DictionarySaveResponse(String.format(
                VocabularyResponseMessageEnum.DICTIONARY_CREATED.getResponseMessage(), dictionary.getDictionaryName()),
                dictionaryProjection);
    }

    @Override
    public Dictionary addDictionary(String dictionaryName) {
        return createEmptyDictionary(dictionaryName);
    }

    private Dictionary createEmptyDictionary(String dictionaryName) {
        UUID userProfileId = IdentityUtil.getUserId();

        if (repository.existsByDictionaryNameAndUserProfileId(dictionaryName, userProfileId)) {
            throw new DuplicateResourceException(String.format(
                    VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), dictionaryName));
        }

        return buildDictionaryAndSave(dictionaryName, userProfileId);
    }

    private Dictionary buildDictionaryAndSave(String dictionaryName, UUID userProfileId) {
        Dictionary dictionary = new Dictionary(null, dictionaryName, List.of(), userProfileId);

        return repository.save(dictionary);
    }

}
