package com.kiskee.dictionarybuilder.repository.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);

    boolean existsByTokenAndIsInvalidatedFalse(String token);

    @Query("SELECT t.token FROM Token t WHERE t.userId = :userId AND t.isInvalidated = false AND TYPE(t) = :tokenType")
    List<String> findValidTokensByUserIdAndTokenType(UUID userId, Class<? extends Token> tokenType);

    @Modifying
    @Query("UPDATE Token t set t.isInvalidated = true, t.expiresAt = current_timestamp WHERE t.token = :token")
    int invalidateToken(String token);

    @Modifying
    @Query("UPDATE Token t set t.isInvalidated = true, t.expiresAt = current_timestamp "
            + "WHERE t.userId = :userId AND t.token = :token  AND t.isInvalidated = false")
    int invalidateTokenByUserIdAndToken(UUID userId, String token);

    @Modifying
    @Query("UPDATE Token t set t.isInvalidated = true, t.expiresAt = current_timestamp "
            + "WHERE t.userId = :userId AND t.isInvalidated = false AND TYPE(t) = :tokenType")
    int invalidateTokensByUserIdAndTokenType(UUID userId, Class<? extends Token> tokenType);
}
