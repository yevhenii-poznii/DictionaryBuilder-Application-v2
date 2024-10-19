package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import com.kiskee.dictionarybuilder.exception.token.ExpiredTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.service.security.cipher.CipherPool;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.util.TokenByteSerializationUtil;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class EncryptedStringDeserializer<D extends TokenData> implements TokenDeserializer<String, D> {

    private final CipherPool cipherPool;
    private final SecretKey cipherSecretKey;
    private final CurrentDateTimeService currentDateTimeService;

    @Override
    @SuppressWarnings("unchecked")
    public D deserialize(String string) throws Exception {
        Cipher cipher = cipherPool.acquireCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, cipherSecretKey);
            byte[] decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(string));
            TokenData tokenData = TokenByteSerializationUtil.fromBytes(decrypted);
            if (currentDateTimeService.getCurrentInstant().isAfter(tokenData.getExpiresAt())) {
                throw new ExpiredTokenException("Token has expired", tokenData.getClass());
            }
            return (D) tokenData;
        } finally {
            cipherPool.releaseCipher(cipher);
        }
    }
}
