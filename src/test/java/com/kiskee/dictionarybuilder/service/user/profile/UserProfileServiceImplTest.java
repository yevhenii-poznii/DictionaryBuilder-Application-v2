package com.kiskee.dictionarybuilder.service.user.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserProfileProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.exception.user.DuplicateUserException;
import com.kiskee.dictionarybuilder.mapper.user.profile.UserProfileMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserCreatedAt;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserFullProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.user.profile.UserProfileRepository;
import com.kiskee.dictionarybuilder.service.user.preference.ProfilePreferenceService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryCreationService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    private ProfilePreferenceService profilePreference;

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
    void testGetFullProfile_WhenProfileExists_ThenReturnUserProfileDto() {
        setAuth();

        ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;
        when(profilePreference.getProfileVisibility()).thenReturn(profileVisibility);

        UserProfileDto userProfile = new UserProfileDto(
                "username", "public name", "someEncodedPicture", Instant.parse("2024-08-20T12:45:33Z"));
        when(userProfileRepository.findUserProfileByUserId(USER_ID)).thenReturn(Optional.of(userProfile));

        UserFullProfileDto userFullProfileDto = new UserFullProfileDto(
                userProfile.publicUsername(),
                userProfile.publicName(),
                userProfile.profilePicture(),
                userProfile.createdAt(),
                profileVisibility);
        when(userProfileMapper.toDto(userProfile, profileVisibility)).thenReturn(userFullProfileDto);

        UserFullProfileDto actual = userProfileService.getFullProfile();

        assertThat(actual.publicUsername()).isEqualTo(userProfile.publicUsername());
        assertThat(actual.publicName()).isEqualTo(userProfile.publicName());
        assertThat(actual.profilePicture()).isEqualTo(userProfile.profilePicture());
        assertThat(actual.createdAt()).isEqualTo(userProfile.createdAt());
    }

    @Test
    void testUpdateProfile_WhenProfileExists_ThenReturnUserProfileDto() {
        setAuth();
        UpdateUserProfileDto updateUserProfileDto =
                new UpdateUserProfileDto("newPublicUsername", "new name", "new avatar");

        when(userProfileRepository.existsByPublicUsernameIgnoreCase(updateUserProfileDto.publicUsername()))
                .thenReturn(false);
        UserProfile userProfile = mock(UserProfile.class);
        when(userProfileRepository.findById(USER_ID)).thenReturn(Optional.of(userProfile));

        UserProfile updatedUserProfile = UserProfile.builder()
                .publicUsername(updateUserProfileDto.publicUsername())
                .publicName(updateUserProfileDto.publicName())
                .profilePicture(updateUserProfileDto.profilePicture())
                .build();
        when(userProfileMapper.toEntity(updateUserProfileDto, userProfile)).thenReturn(updatedUserProfile);
        when(userProfileRepository.save(userPreferenceArgumentCaptor.capture())).thenReturn(updatedUserProfile);
        when(userProfileMapper.toDto(updatedUserProfile)).thenReturn(mock(UserProfileDto.class));

        userProfileService.updateProfile(updateUserProfileDto);

        UserProfile actual = userPreferenceArgumentCaptor.getValue();
        assertThat(actual.getPublicUsername()).isEqualTo(updateUserProfileDto.publicUsername());
        assertThat(actual.getPublicName()).isEqualTo(updateUserProfileDto.publicName());
        assertThat(actual.getProfilePicture()).isEqualTo(updateUserProfileDto.profilePicture());
    }

    @Test
    void testUpdateProfile_WhenNewUsernameAlreadyExists_ThenThrowDuplicateUserException() {
        setAuth();
        UpdateUserProfileDto updateUserProfileDto =
                new UpdateUserProfileDto("newPublicUsername", "new name", "new avatar");

        when(userProfileRepository.existsByPublicUsernameIgnoreCase(updateUserProfileDto.publicUsername()))
                .thenReturn(true);

        assertThatThrownBy(() -> userProfileService.updateProfile(updateUserProfileDto))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("Username \"newPublicUsername\" already exists");
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
