package com.kiskee.dictionarybuilder.mapper.user.preference;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.mapper.user.UserBaseMapper;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = UserBaseMapper.class)
public interface UserPreferenceMapper extends UserBaseMapper {

    UserPreference toEntity(
            DefaultUserPreferenceProperties defaultPreference,
            UserVocabularyApplication user,
            PageFilter pageFilter,
            ProfileVisibility profileVisibility);

    UserPreference toEntity(UserPreferenceDto userPreferenceDto, @MappingTarget UserPreference userPreference);

    UserPreferenceDto toDto(UserPreference userPreference);
}
