package com.kiskee.dictionarybuilder.service.security.cipher;

import com.kiskee.dictionarybuilder.config.properties.token.cipher.CipherProperties;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.springframework.stereotype.Component;

@Component
public class CipherPool {

    private final CipherProperties cipherProperties;
    private final BlockingQueue<Cipher> cipherPool;

    public CipherPool(CipherProperties cipherProperties) {
        this.cipherProperties = cipherProperties;
        this.cipherPool = new ArrayBlockingQueue<>(cipherProperties.getPoolSize());
        initializePool();
    }

    private void initializePool() {
        IntStream.range(0, cipherProperties.getPoolSize())
                .mapToObj(this::getCipher)
                .forEach(cipherPool::add);
    }

    private Cipher getCipher(int i) {
        try {
            return Cipher.getInstance(cipherProperties.getAlgorithm());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public Cipher acquireCipher() throws InterruptedException {
        return cipherPool.take();
    }

    public void releaseCipher(Cipher cipher) {
        cipherPool.offer(cipher);
    }
}
