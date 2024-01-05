package com.kiskee.vocabulary.repository.token;

import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByVerificationToken(String verificationToken);

}
