package com.kiskee.vocabulary.service.user.preference;

import com.kiskee.vocabulary.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.preference.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceServiceTest {

    @InjectMocks
    private UserPreferenceService userPreferenceService;
    @Mock
    private UserPreferenceRepository userPreferenceRepository;
    @Mock
    private DefaultUserPreferenceProperties defaultUserPreferenceProperties;
    @Captor
    private ArgumentCaptor<UserPreference> userPreferenceArgumentCaptor;

    @Test
    void testInitDefault_WhenUserVocabularyApplicationIsGiven_ThenBuildAndSaveDefaultUserPreference() {
        UUID useId = UUID.fromString("75ab44f4-40a3-4094-a885-51ade9e6df4a");
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getUser()).thenReturn(givenUserEntity);

        when(givenUserEntity.getId()).thenReturn(useId);
        when(defaultUserPreferenceProperties.getRightAnswersToDisableInRepetition()).thenReturn(10);

        userPreferenceService.initDefault(registrationRequest);

        verify(userPreferenceRepository).save((UserProfilePreferenceType) userPreferenceArgumentCaptor.capture());

        UserPreference actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(useId);
        assertThat(actual.getProfileVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
        assertThat(actual.getRightAnswersToDisableInRepetition()).isEqualTo(10);
    }

}
