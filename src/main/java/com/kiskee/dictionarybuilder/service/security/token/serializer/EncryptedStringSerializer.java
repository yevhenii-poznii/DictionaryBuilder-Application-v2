package com.kiskee.dictionarybuilder.service.security.token.serializer;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.service.security.cipher.CipherPool;
import com.kiskee.dictionarybuilder.util.TokenByteSerializationUtil;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EncryptedStringSerializer<D extends TokenData> implements TokenSerializer<D, String> {

    private final CipherPool cipherPool;
    private final SecretKey cipherSecretKey;

    @Override
    @SneakyThrows
    public String serialize(D data) {
        Cipher cipher = cipherPool.acquireCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherSecretKey);
            byte[] encrypted = cipher.doFinal(TokenByteSerializationUtil.toBytes(data));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } finally {
            cipherPool.releaseCipher(cipher);
        }
    }
}
