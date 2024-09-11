package com.kiskee.dictionarybuilder.service.user.profile;

import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserFullProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;

public interface UserProfileService {

    UserMiniProfileDto getMiniProfile();

    UserFullProfileDto getFullProfile();

    UserProfileDto updateProfile(UpdateUserProfileDto updateUserProfileDto);
}
