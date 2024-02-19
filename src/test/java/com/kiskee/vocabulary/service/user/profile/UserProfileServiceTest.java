package com.kiskee.vocabulary.service.user.profile;

import com.kiskee.vocabulary.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.vocabulary.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.profile.UserProfile;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.user.profile.UserProfileRepository;
import com.kiskee.vocabulary.service.vocabulary.VocabularyService;
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
public class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private VocabularyService vocabularyService;
    @Mock
    private ProfilePictureEncoder profilePictureEncoder;
    @Mock
    private DefaultUserProfileProperties defaultUserProfileProperties;
    @Captor
    private ArgumentCaptor<UserProfile> userPreferenceArgumentCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testInitDefault_WhenRegistrationRequestIsGivenAndPictureIsNull_ThenBuildAndSaveDefaultUserProfile() {
        String username = "UsErNaMe";
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getUser()).thenReturn(givenUserEntity);

        when(givenUserEntity.getId()).thenReturn(USER_ID);
        when(registrationRequest.getUsername()).thenReturn(username);

        Dictionary dictionaryMock = mock(Dictionary.class);
        String dictionaryName = "Default Dictionary";
        when(vocabularyService.createEmptyDictionary(dictionaryName)).thenReturn(dictionaryMock);
        when(dictionaryMock.getDictionaryName()).thenReturn(dictionaryName);

        when(defaultUserProfileProperties.getDefaultAvatar()).thenReturn("defaultAvatar");

        userProfileService.initDefault(registrationRequest);

        verify(userProfileRepository).save((UserProfilePreferenceType) userPreferenceArgumentCaptor.capture());

        UserProfile actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(USER_ID);
        assertThat(actual.getPublicUsername()).isEqualTo(username);
        assertThat(actual.getDictionaries())
                .extracting(Dictionary::getDictionaryName)
                .containsExactly(dictionaryName);
    }

    @Test
    void testInitDefault_WhenPictureInRegistrationRequestIsNotNull_ThenBuildAndSaveDefaultUserProfile() {
        String username = "UsErNaMe";
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);
        when(registrationRequest.getUser()).thenReturn(givenUserEntity);

        when(givenUserEntity.getId()).thenReturn(USER_ID);
        when(registrationRequest.getUsername()).thenReturn(username);
        when(registrationRequest.getPicture()).thenReturn("pictureUrl");

        Dictionary dictionaryMock = mock(Dictionary.class);
        String dictionaryName = "Default Dictionary";
        when(vocabularyService.createEmptyDictionary(dictionaryName)).thenReturn(dictionaryMock);
        when(dictionaryMock.getDictionaryName()).thenReturn(dictionaryName);

        String encodedPicture = "encodedPicture";
        when(profilePictureEncoder.encodeWithBase64(registrationRequest.getPicture())).thenReturn(encodedPicture);

        userProfileService.initDefault(registrationRequest);

        verify(userProfileRepository).save((UserProfilePreferenceType) userPreferenceArgumentCaptor.capture());

        UserProfile actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(USER_ID);
        assertThat(actual.getPublicUsername()).isEqualTo(username);
        assertThat(actual.getProfilePicture()).isEqualTo(encodedPicture);
        assertThat(actual.getDictionaries())
                .extracting(Dictionary::getDictionaryName)
                .containsExactly(dictionaryName);
    }

}
