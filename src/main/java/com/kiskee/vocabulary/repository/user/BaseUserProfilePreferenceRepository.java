package com.kiskee.vocabulary.repository.user;

import com.kiskee.vocabulary.model.entity.user.UserProfilePreferenceType;

public interface BaseUserProfilePreferenceRepository {

    void save(UserProfilePreferenceType entity);

}
