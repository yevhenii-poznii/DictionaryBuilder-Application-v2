package com.kiskee.dictionarybuilder.util;

import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenByteSerializationUtil {

    public byte[] toBytes(TokenData tokenData) {
        ByteBuffer byteBuffer;
        switch (tokenData) {
            case SharingTokenData sharingTokenData:
                byteBuffer = ByteBuffer.allocate(SharingTokenData.BYTE_ARRAY_SIZE);
                byteBuffer.putShort((short) SharingTokenData.TOKEN_TYPE.ordinal());
                byteBuffer.putLong(sharingTokenData.getDictionaryId());
                break;
            case VerificationTokenData verificationTokenData:
                byte[] emailBytes = verificationTokenData.getEmail().getBytes(StandardCharsets.UTF_8);
                int emailBytesSize = emailBytes.length;
                byteBuffer = ByteBuffer.allocate(VerificationTokenData.BYTE_ARRAY_SIZE + emailBytesSize);
                byteBuffer.putShort((short) VerificationTokenData.TOKEN_TYPE.ordinal());
                byteBuffer.put(emailBytes);
                break;
            default:
                throw new IllegalArgumentException("Unsupported token type");
        }
        byteBuffer.putLong(tokenData.getUserId().getMostSignificantBits());
        byteBuffer.putLong(tokenData.getUserId().getLeastSignificantBits());
        byteBuffer.putLong(tokenData.getExpiresAt().getEpochSecond());
        byteBuffer.putInt(tokenData.getExpiresAt().getNano());
        return byteBuffer.array();
    }

    public TokenData fromBytes(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int tokenTypeOrdinal = byteBuffer.getShort();
        TokenType tokenType = TokenType.values()[tokenTypeOrdinal];
        UUID userId;
        Instant expiresAt;
        switch (tokenType) {
            case SHARING_TOKEN -> {
                Long dictionaryId = byteBuffer.getLong();
                userId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
                expiresAt = Instant.ofEpochSecond(byteBuffer.getLong(), byteBuffer.getInt());
                return new SharingTokenData(userId, dictionaryId, expiresAt);
            }
            case VERIFICATION_TOKEN -> {
                int lastIndexOfEmail = byteBuffer.capacity() - Long.BYTES * 3 - Integer.BYTES;
                int firstIndexOfEmail = byteBuffer.position();
                byte[] emailBytes = new byte[lastIndexOfEmail - firstIndexOfEmail];
                byteBuffer.get(emailBytes);
                String email = new String(emailBytes, StandardCharsets.UTF_8);
                userId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
                expiresAt = Instant.ofEpochSecond(byteBuffer.getLong(), byteBuffer.getInt());
                return new VerificationTokenData(userId, email, expiresAt);
            }
            default -> throw new InvalidTokenException("Invalid token type");
        }
    }
}
