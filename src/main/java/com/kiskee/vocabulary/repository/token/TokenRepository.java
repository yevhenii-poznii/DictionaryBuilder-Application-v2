package com.kiskee.vocabulary.repository.token;

import com.kiskee.vocabulary.model.entity.token.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);
}
