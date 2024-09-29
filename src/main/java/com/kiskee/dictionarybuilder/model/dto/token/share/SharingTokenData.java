package com.kiskee.dictionarybuilder.model.dto.token.share;

import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import jakarta.validation.constraints.FutureOrPresent;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharingTokenData implements TokenData {

    private UUID userId;
    private Long dictionaryId;

    @FutureOrPresent
    private Instant expiresAt;

    public SharingTokenData(byte... data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        verifyTokenTypeOrdinal(byteBuffer.getInt());
        this.userId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        this.dictionaryId = byteBuffer.getLong();
        this.expiresAt = Instant.ofEpochSecond(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + Long.BYTES * 5);
        bb.putInt(TokenType.SHARING_TOKEN.ordinal());
        bb.putLong(userId.getMostSignificantBits());
        bb.putLong(userId.getLeastSignificantBits());
        bb.putLong(dictionaryId);
        bb.putLong(expiresAt.getEpochSecond());
        bb.putLong(expiresAt.getNano());
        return bb.array();
    }

    private void verifyTokenTypeOrdinal(int tokenTypeOrdinal) {
        if (tokenTypeOrdinal != TokenType.SHARING_TOKEN.ordinal()) {
            throw new InvalidTokenException("Invalid token type");
        }
    }
}
