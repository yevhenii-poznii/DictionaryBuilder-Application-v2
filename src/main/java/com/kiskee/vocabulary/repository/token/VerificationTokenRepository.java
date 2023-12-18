package com.kiskee.vocabulary.repository.token;

import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
}
