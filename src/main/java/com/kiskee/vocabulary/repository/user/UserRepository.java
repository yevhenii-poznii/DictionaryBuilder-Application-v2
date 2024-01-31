package com.kiskee.vocabulary.repository.user;

import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserVocabularyApplication, UUID> {

    boolean existsByUsernameOrEmail(String username, String email);

    Optional<UserVocabularyApplication> findByUsernameOrEmail(String username, String email);

}
