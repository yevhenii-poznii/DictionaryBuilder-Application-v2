package com.kiskee.dictionarybuilder.service.user.profile;

import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;

public interface UserProfileService {

    UserMiniProfileDto getMiniProfile();

    UserProfileDto getFullProfile();
}
