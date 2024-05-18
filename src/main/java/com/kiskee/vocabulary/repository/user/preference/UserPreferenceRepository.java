package com.kiskee.vocabulary.repository.user.preference;

import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {}
