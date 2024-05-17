package com.kiskee.vocabulary.mapper.user.preference;

import com.kiskee.vocabulary.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.vocabulary.enums.user.ProfileVisibility;
import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.user.UserBaseMapper;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import org.mapstruct.Mapper;

@Mapper(config = UserBaseMapper.class)
public interface UserPreferenceMapper extends UserBaseMapper {

    UserPreference toEntity(DefaultUserPreferenceProperties defaultPreference,
                            UserVocabularyApplication user,
                            PageFilter pageFilter,
                            ProfileVisibility profileVisibility);

}
