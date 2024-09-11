package com.kiskee.dictionarybuilder.mapper.user.profile;

import com.kiskee.dictionarybuilder.mapper.user.UserBaseMapper;
import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = UserBaseMapper.class)
public interface UserProfileMapper extends UserBaseMapper {

    @Mapping(source = "registrationRequest.username", target = "publicUsername")
    @Mapping(source = "registrationRequest.name", target = "publicName")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    UserProfile toEntity(RegistrationRequest registrationRequest, List<Dictionary> dictionaries, String profilePicture);

    UserProfile toEntity(UpdateUserProfileDto updateUserProfileDto, @MappingTarget UserProfile userProfile);

    UserProfileDto toDto(UserProfile userProfile);
}
