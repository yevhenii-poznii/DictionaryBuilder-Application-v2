package com.kiskee.dictionarybuilder.service.user.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.mapper.user.preference.UserPreferenceMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceOptionsDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceOptionDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.profile.ProfilePreferenceDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import com.kiskee.dictionarybuilder.repository.user.preference.UserPreferenceRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceServiceImplTest {

    @InjectMocks
    private UserPreferenceServiceImpl userPreferenceService;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserPreferenceMapper userPreferenceMapper;

    @Mock
    private DefaultUserPreferenceProperties defaultUserPreferenceProperties;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<UserPreference> userPreferenceArgumentCaptor;

    private static final UUID USER_ID = UUID.fromString("75ab44f4-40a3-4094-a885-51ade9e6df4a");

    @Test
    void testInitDefault_WhenUserVocabularyApplicationIsGiven_ThenBuildAndSaveDefaultUserPreference() {
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getUser()).thenReturn(givenUserEntity);

        when(givenUserEntity.getId()).thenReturn(USER_ID);

        UserPreference userPreference = new UserPreference(
                USER_ID,
                ProfileVisibility.PRIVATE,
                100,
                true,
                PageFilter.BY_ADDED_AT_ASC,
                10,
                10,
                Duration.ofHours(1),
                givenUserEntity);
        when(userPreferenceMapper.toEntity(
                        defaultUserPreferenceProperties,
                        givenUserEntity,
                        PageFilter.BY_ADDED_AT_ASC,
                        ProfileVisibility.PRIVATE))
                .thenReturn(userPreference);

        userPreferenceService.initUser(registrationRequest);

        verify(userPreferenceRepository).save(userPreferenceArgumentCaptor.capture());

        UserPreference actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(USER_ID);
        assertThat(actual.getProfileVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
        assertThat(actual.getRightAnswersToDisableInRepetition()).isEqualTo(10);
        assertThat(actual.getWordsPerPage()).isEqualTo(100);
        assertThat(actual.isBlurTranslation()).isTrue();
    }

    @Test
    void testGetWordPreference_WhenUserIdIsGiven_ThenReturnWordPreference() {
        UUID userId = UUID.fromString("75ab44f4-40a3-4094-a885-51ade9e6df4a");
        int rightAnswersToDisableInRepetition = 10;
        int newWordsPerDayGoal = 10;
        WordPreference expectedWordPreference =
                new WordPreference(rightAnswersToDisableInRepetition, newWordsPerDayGoal, Duration.ofHours(1));
        when(userPreferenceRepository.findWordPreferenceByUserId(userId)).thenReturn(expectedWordPreference);

        WordPreference actualWordPreference = userPreferenceService.getWordPreference(userId);

        assertThat(actualWordPreference.rightAnswersToDisableInRepetition())
                .isEqualTo(rightAnswersToDisableInRepetition);
        assertThat(actualWordPreference.newWordsPerDayGoal()).isEqualTo(newWordsPerDayGoal);
    }

    @Test
    void testGetUserPreference_WhenUserPreferenceExists_ThenReturnUserPreferenceOptionsDto() {
        setAuth();

        UserPreferenceDto userPreferenceDto = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));
        when(userPreferenceRepository.findUserPreferenceByUserId(USER_ID)).thenReturn(userPreferenceDto);
        UserPreferenceOptionsDto userPreferenceOptionsDto = new UserPreferenceOptionsDto(
                userPreferenceDto.profileVisibility(),
                userPreferenceDto.wordsPerPage(),
                userPreferenceDto.blurTranslation(),
                userPreferenceDto.pageFilter(),
                userPreferenceDto.rightAnswersToDisableInRepetition(),
                userPreferenceDto.newWordsPerDayGoal(),
                userPreferenceDto.dailyRepetitionDurationGoal(),
                EnumUtils.getEnumMap(ProfileVisibility.class, ProfileVisibility::getVisibility),
                EnumUtils.getEnumMap(PageFilter.class, PageFilter::getFilter));
        when(userPreferenceMapper.toDto(userPreferenceDto)).thenReturn(userPreferenceOptionsDto);

        UserPreferenceOptionsDto actualUserPreference = userPreferenceService.getUserPreference();

        assertThat(actualUserPreference.profileVisibility()).isEqualTo(ProfileVisibility.PUBLIC);
        assertThat(actualUserPreference.wordsPerPage()).isEqualTo(userPreferenceDto.wordsPerPage());
        assertThat(actualUserPreference.blurTranslation()).isTrue();
        assertThat(actualUserPreference.pageFilter()).isEqualTo(PageFilter.BY_ADDED_AT_ASC);
        assertThat(actualUserPreference.rightAnswersToDisableInRepetition())
                .isEqualTo(userPreferenceDto.rightAnswersToDisableInRepetition());
        assertThat(actualUserPreference.newWordsPerDayGoal()).isEqualTo(userPreferenceDto.newWordsPerDayGoal());
        assertThat(actualUserPreference.dailyRepetitionDurationGoal())
                .isEqualTo(userPreferenceDto.dailyRepetitionDurationGoal());
    }

    @Test
    void testUpdateUserPreference_WhenUserPreferenceExists_ThenUpdateAndReturnUserPreferenceDto() {
        setAuth();
        UserPreferenceDto userPreferenceDto = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));

        UserPreference userPreference = mock(UserPreference.class);
        when(userPreferenceRepository.findById(USER_ID)).thenReturn(Optional.of(userPreference));
        UserPreference updated = new UserPreference(
                USER_ID,
                userPreferenceDto.profileVisibility(),
                userPreferenceDto.wordsPerPage(),
                userPreferenceDto.blurTranslation(),
                userPreferenceDto.pageFilter(),
                userPreferenceDto.rightAnswersToDisableInRepetition(),
                userPreferenceDto.newWordsPerDayGoal(),
                userPreferenceDto.dailyRepetitionDurationGoal(),
                mock(UserVocabularyApplication.class));
        when(userPreferenceMapper.toEntity(userPreferenceDto, userPreference)).thenReturn(updated);
        when(userPreferenceRepository.save(userPreferenceArgumentCaptor.capture()))
                .thenReturn(userPreference);
        when(userPreferenceMapper.toDto(userPreference)).thenReturn(userPreferenceDto);

        UserPreferenceDto result = userPreferenceService.updateUserPreference(userPreferenceDto);

        UserPreference actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUserId()).isEqualTo(USER_ID);
        assertThat(actual.getProfileVisibility()).isEqualTo(result.profileVisibility());
        assertThat(actual.getWordsPerPage()).isEqualTo(result.wordsPerPage());
        assertThat(actual.isBlurTranslation()).isEqualTo(result.blurTranslation());
        assertThat(actual.getPageFilter()).isEqualTo(result.pageFilter());
        assertThat(actual.getRightAnswersToDisableInRepetition()).isEqualTo(result.rightAnswersToDisableInRepetition());
        assertThat(actual.getNewWordsPerDayGoal()).isEqualTo(result.newWordsPerDayGoal());
        assertThat(actual.getDailyRepetitionDurationGoal()).isEqualTo(result.dailyRepetitionDurationGoal());
    }

    @Test
    void testUpdateUserPreference_WhenUserPreferenceDoesNotExist_ThenThrowResourceNotFoundException() {
        setAuth();
        UserPreferenceDto userPreferenceDto = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));

        when(userPreferenceRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userPreferenceService.updateUserPreference(userPreferenceDto))
                .withMessage(String.format("%s [%s] hasn't been found", UserPreference.class.getSimpleName(), USER_ID));
    }

    @Test
    void testGetDictionaryPreference_WhenDictionaryPreferenceExists_ThenReturnDictionaryPreferenceDto() {
        setAuth();

        int wordsPerPage = 100;
        boolean blurTranslation = true;
        PageFilter pageFilter = PageFilter.BY_ADDED_AT_ASC;
        DictionaryPreferenceDto expectedDictionaryPreference =
                new DictionaryPreferenceDto(wordsPerPage, blurTranslation, pageFilter);
        when(userPreferenceRepository.findDictionaryPreferenceByUserId(USER_ID))
                .thenReturn(expectedDictionaryPreference);
        DictionaryPreferenceOptionDto dictionaryPreferenceOptionDto = new DictionaryPreferenceOptionDto(
                wordsPerPage,
                blurTranslation,
                pageFilter,
                EnumUtils.getEnumMap(PageFilter.class, PageFilter::getFilter));
        when(userPreferenceMapper.toDto(expectedDictionaryPreference)).thenReturn(dictionaryPreferenceOptionDto);

        DictionaryPreferenceOptionDto actualDictionaryPreference = userPreferenceService.getDictionaryPreference();

        assertThat(actualDictionaryPreference.wordsPerPage()).isEqualTo(wordsPerPage);
        assertThat(actualDictionaryPreference.blurTranslation()).isEqualTo(blurTranslation);
        assertThat(actualDictionaryPreference.pageFilter()).isEqualTo(pageFilter);
    }

    @Test
    void testGetProfileVisibility_WhenProfilePreferenceExists_ThenReturnProfileVisibility() {
        setAuth();

        ProfilePreferenceDto profilePreferenceDto = new ProfilePreferenceDto(ProfileVisibility.PUBLIC);
        when(userPreferenceRepository.findProfileVisibilityByUserId(USER_ID)).thenReturn(profilePreferenceDto);

        ProfileVisibility actualProfileVisibility = userPreferenceService.getProfileVisibility();

        assertThat(actualProfileVisibility).isEqualTo(profilePreferenceDto.profileVisibility());
    }

    private void setAuth() {
        UserVocabularyApplication user = UserVocabularyApplication.builder()
                .setId(USER_ID)
                .setUsername("username")
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
