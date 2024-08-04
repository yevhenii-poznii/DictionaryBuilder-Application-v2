package com.kiskee.dictionarybuilder.service.user.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.mapper.user.profile.UserProfileMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserCreatedAt;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.user.profile.UserProfileRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryCreationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
public class UserProfileServiceImplTest {

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private DictionaryCreationService dictionaryCreationService;

    @Mock
    private ProfilePictureEncoder profilePictureEncoder;

    @Mock
    private DefaultUserProfileProperties defaultUserProfileProperties;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<UserProfile> userPreferenceArgumentCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testInitDefault_WhenRegistrationRequestIsGivenAndPictureIsNull_ThenBuildAndSaveDefaultUserProfile() {
        String username = "UsErNaMe";
        UserVocabularyApplication givenUserEntity = mock(UserVocabularyApplication.class);
        InternalRegistrationRequest registrationRequest = mock(InternalRegistrationRequest.class);

        when(givenUserEntity.getId()).thenReturn(USER_ID);

        Dictionary dictionaryMock = mock(Dictionary.class);
        String dictionaryName = "Default Dictionary";
        when(dictionaryCreationService.addDictionary(dictionaryName)).thenReturn(dictionaryMock);
        when(dictionaryMock.getDictionaryName()).thenReturn(dictionaryName);

        String defaultAvatar = "defaultAvatar";
        when(defaultUserProfileProperties.getDefaultAvatar()).thenReturn(defaultAvatar);

        UserProfile userProfile = UserProfile.builder()
                .user(givenUserEntity)
                .publicUsername(username)
                .dictionaries(List.of(dictionaryMock))
                .profilePicture(defaultAvatar)
                .build();
        when(userProfileMapper.toEntity(registrationRequest, List.of(dictionaryMock), defaultAvatar))
                .thenReturn(userProfile);

        userProfileService.initUser(registrationRequest);

        verify(userProfileRepository).save(userPreferenceArgumentCaptor.capture());

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

        when(givenUserEntity.getId()).thenReturn(USER_ID);
        when(registrationRequest.getPicture()).thenReturn("pictureUrl");

        Dictionary dictionaryMock = mock(Dictionary.class);
        String dictionaryName = "Default Dictionary";
        when(dictionaryCreationService.addDictionary(dictionaryName)).thenReturn(dictionaryMock);
        when(dictionaryMock.getDictionaryName()).thenReturn(dictionaryName);

        String encodedPicture = "encodedPicture";
        when(profilePictureEncoder.encodeWithBase64(registrationRequest.getPicture()))
                .thenReturn(encodedPicture);

        UserProfile userProfile = UserProfile.builder()
                .user(givenUserEntity)
                .publicUsername(username)
                .dictionaries(List.of(dictionaryMock))
                .profilePicture(encodedPicture)
                .build();
        when(userProfileMapper.toEntity(registrationRequest, List.of(dictionaryMock), encodedPicture))
                .thenReturn(userProfile);

        userProfileService.initUser(registrationRequest);

        verify(userProfileRepository).save(userPreferenceArgumentCaptor.capture());

        UserProfile actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getUser().getId()).isEqualTo(USER_ID);
        assertThat(actual.getPublicUsername()).isEqualTo(username);
        assertThat(actual.getProfilePicture()).isEqualTo(encodedPicture);
        assertThat(actual.getDictionaries())
                .extracting(Dictionary::getDictionaryName)
                .containsExactly(dictionaryName);
    }

    @Test
    void testGetMiniProfile_WhenUserIdIsGiven_ThenReturnMiniProfile() {
        setAuth();

        String publicUsername = "public username";
        String avatar = "someEncodedAvatar";
        UserMiniProfileDto userMiniProfileDto = new UserMiniProfileDto(publicUsername, avatar);
        when(userProfileRepository.findUserMiniProfileByUserId(USER_ID)).thenReturn(userMiniProfileDto);

        UserMiniProfileDto actual = userProfileService.getMiniProfile();

        assertThat(actual.publicUsername()).isEqualTo(publicUsername);
        assertThat(actual.profilePicture()).isEqualTo(avatar);
    }

    @Test
    void testGetCreatedAtField_WhenUserIdIsGiven_ThenReturnCreatedAtField() {
        Instant createdAtInstant = Instant.parse("2021-01-01T00:00:00Z");
        UserCreatedAt userCreatedAt = new UserCreatedAt(createdAtInstant);
        when(userProfileRepository.findCreatedAtByUserId(USER_ID)).thenReturn(userCreatedAt);

        Instant actual = userProfileService.getCreatedAtField(USER_ID);

        assertThat(actual).isEqualTo(createdAtInstant);
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
