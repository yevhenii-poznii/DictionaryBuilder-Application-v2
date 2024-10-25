package com.kiskee.dictionarybuilder.service.security.cipher;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.kiskee.dictionarybuilder.config.properties.token.cipher.CipherProperties;
import javax.crypto.Cipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CipherPoolTest {

    private CipherPool cipherPool;

    @Mock
    private CipherProperties cipherProperties;

    @BeforeEach
    public void setUp() {
        cipherProperties = new CipherProperties();
        cipherProperties.setAlgorithm("AES");
        cipherProperties.setPoolSize(10);
        cipherPool = new CipherPool(cipherProperties);
    }

    @Test
    void testAcquireCipher() throws InterruptedException {
        Cipher acquiredCipher = cipherPool.acquireCipher();
        assertNotNull(acquiredCipher);
    }

    @Test
    void testReleaseCipher() throws InterruptedException {
        Cipher cipher = cipherPool.acquireCipher();
        cipherPool.releaseCipher(cipher);

        Cipher acquiredAgain = cipherPool.acquireCipher();
        assertNotNull(acquiredAgain);
    }
}
