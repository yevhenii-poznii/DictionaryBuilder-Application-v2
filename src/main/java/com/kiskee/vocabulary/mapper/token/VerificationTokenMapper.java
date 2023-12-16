package com.kiskee.vocabulary.mapper.token;

import com.kiskee.vocabulary.model.dto.token.VerificationTokenDto;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerificationTokenMapper {

    VerificationTokenDto toDto(VerificationToken token);

}
