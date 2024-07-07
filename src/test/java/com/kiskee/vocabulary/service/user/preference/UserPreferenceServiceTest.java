package com.kiskee.vocabulary.service.user.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.user.preference.UserPreferenceMapper;
import com.kiskee.vocabulary.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.vocabulary.model.dto.user.preference.WordPreference;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.preference.UserPreferenceRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceServiceTest {

    @InjectMocks
    private UserPreferenceService userPreferenceService;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserPreferenceMapper userPreferenceMapper;

    @Mock
    private DefaultUserPreferenceProperties defaultUserPreferenceProperties;

    @Captor
    private ArgumentCaptor<UserPreference> userPreferenceArgumentCaptor;

    @Test
    void testInitDefault_WhenUserVocabularyApplicationIsGiven_ThenBuildAndSaveDefaultUserPreference() {
        UUID userId = UUID.fromString("75ab44f4-40a3-4094-a885-51ade9e6df4a");
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getUser()).thenReturn(givenUserEntity);

        when(givenUserEntity.getId()).thenReturn(userId);

        UserPreference userPreference = new UserPreference(
                userId, ProfileVisibility.PRIVATE, 10, 100, true, PageFilter.BY_ADDED_AT_ASC, givenUserEntity);
        when(userPreferenceMapper.toEntity(
                        defaultUserPreferenceProperties,
                        givenUserEntity,
                        PageFilter.BY_ADDED_AT_ASC,
                        ProfileVisibility.PRIVATE))
                .thenReturn(userPreference);

        userPreferenceService.initUser(registrationRequest);

        verify(userPreferenceRepository).save(userPreferenceArgumentCaptor.capture());

        UserPreference actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(userId);
        assertThat(actual.getProfileVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
        assertThat(actual.getRightAnswersToDisableInRepetition()).isEqualTo(10);
        assertThat(actual.getWordsPerPage()).isEqualTo(100);
        assertThat(actual.isBlurTranslation()).isTrue();
    }

    @Test
    void testWordPreference_WhenUserIdIsGiven_ThenReturnWordPreference() {
        UUID userId = UUID.fromString("75ab44f4-40a3-4094-a885-51ade9e6df4a");
        int rightAnswersToDisableInRepetition = 10;
        WordPreference expectedWordPreference = new WordPreference(rightAnswersToDisableInRepetition);
        when(userPreferenceRepository.findWordPreferenceByUserId(userId)).thenReturn(expectedWordPreference);

        WordPreference actualWordPreference = userPreferenceService.getWordPreference(userId);

        assertThat(actualWordPreference.getRightAnswersToDisableInRepetition())
                .isEqualTo(rightAnswersToDisableInRepetition);
    }
}
