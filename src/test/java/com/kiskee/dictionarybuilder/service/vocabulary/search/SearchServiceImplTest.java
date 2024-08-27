package com.kiskee.dictionarybuilder.service.vocabulary.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.vocabulary.SearchWordRepository;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryFetcher;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class SearchServiceImplTest {

    @InjectMocks
    private SearchServiceImpl searchService;

    @Mock
    private DictionaryFetcher dictionaryFetcher;

    @Mock
    private SearchWordRepository repository;

    @Mock
    private SecurityContext securityContext;

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @Test
    void testSearch_WhenGivenValidSearchWord_ThenReturnWordProjectionList() {
        setAuth();

        String searchWord = "test";

        List<DictionaryDto> dictionaries =
                List.of(new DictionaryDto(1L, "Default Dictionary"), new DictionaryDto(2L, "Custom Dictionary"));
        when(dictionaryFetcher.getDictionaries()).thenReturn(dictionaries);

        WordProjection word = mock(WordProjection.class);
        when(word.getWord()).thenReturn(searchWord);
        when(repository.findByWordContainsIgnoreCaseAndDictionaryIdIn(searchWord, Set.of(1L, 2L)))
                .thenReturn(List.of(word));

        List<WordProjection> result = searchService.search(searchWord);

        assertThat(result.getFirst().getWord()).isEqualTo(searchWord);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidSearchWords")
    void testSearch_WhenGivenNullSearchWord_ThenThrowValidationException(String searchWord) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> searchService.search(searchWord))
                .withMessage("Search word cannot be null or empty");
    }

    private static Stream<String> provideInvalidSearchWords() {
        return Stream.of(null, "", " ");
    }

    private void setAuth() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
