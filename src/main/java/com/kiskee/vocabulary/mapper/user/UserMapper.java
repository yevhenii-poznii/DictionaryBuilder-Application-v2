package com.kiskee.vocabulary.mapper.user;

import com.kiskee.vocabulary.enums.user.UserRole;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = UserBaseMapper.class)
public interface UserMapper extends UserBaseMapper {

    @Mapping(source = "registrationRequest.active", target = "isActive")
    @Mapping(source = "registrationRequest.hashedPassword", target = "password")
    UserVocabularyApplication toEntity(RegistrationRequest registrationRequest, UserRole role);
}
