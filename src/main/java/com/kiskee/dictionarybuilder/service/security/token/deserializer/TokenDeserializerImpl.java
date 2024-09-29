package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.token.share.TokenData;
import com.kiskee.dictionarybuilder.service.security.cipher.CipherPool;
import java.security.InvalidKeyException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenDeserializerImpl<D extends TokenData> implements TokenDeserializer<String, D> {

    private final CipherPool cipherPool;
    private final SecretKey cipherSecretKey;

    @Override
    @SneakyThrows
    public D deserialize(String string, Class<D> cls) {
        Cipher cipher = cipherPool.acquireCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, cipherSecretKey);
            byte[] decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(string));
            return cls.getDeclaredConstructor(byte[].class).newInstance(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new InvalidTokenException("Invalid token");
        } finally {
            cipherPool.releaseCipher(cipher);
        }
    }
}
