package com.kiskee.dictionarybuilder.repository.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    boolean existsByTokenAndIsInvalidatedFalse(String token);

    @Modifying
    @Query("update Token t set t.isInvalidated = true, t.expiresAt = current_timestamp where t.token = ?1")
    int invalidateToken(String token);
}
