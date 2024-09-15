package com.kiskee.dictionarybuilder.mapper.user.preference;

import com.kiskee.dictionarybuilder.config.properties.user.DefaultUserPreferenceProperties;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.mapper.user.UserBaseMapper;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceOptionsDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceOptionDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import org.apache.commons.lang3.EnumUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(
        config = UserBaseMapper.class,
        imports = {EnumUtils.class})
public interface UserPreferenceMapper extends UserBaseMapper {

    UserPreference toEntity(
            DefaultUserPreferenceProperties defaultPreference,
            UserVocabularyApplication user,
            PageFilter pageFilter,
            ProfileVisibility profileVisibility);

    UserPreference toEntity(UserPreferenceDto userPreferenceDto, @MappingTarget UserPreference userPreference);

    UserPreferenceDto toDto(UserPreference userPreference);

    @Mapping(
            target = "pageFilterOptions",
            expression = "java(EnumUtils.getEnumMap(PageFilter.class, PageFilter::getFilter))")
    DictionaryPreferenceOptionDto toDto(DictionaryPreferenceDto dictionaryPreferenceDto);

    @Mappings({
        @Mapping(
                target = "profileVisibilityOptions",
                expression = "java(EnumUtils.getEnumMap(ProfileVisibility.class, ProfileVisibility::getVisibility))"),
        @Mapping(
                target = "pageFilterOptions",
                expression = "java(EnumUtils.getEnumMap(PageFilter.class, PageFilter::getFilter))")
    })
    UserPreferenceOptionsDto toDto(UserPreferenceDto userPreferenceDto);
}
