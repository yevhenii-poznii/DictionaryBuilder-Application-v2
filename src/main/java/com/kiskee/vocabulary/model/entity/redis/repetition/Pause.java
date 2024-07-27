package com.kiskee.vocabulary.model.entity.redis.repetition;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pause {

    private Instant startTime;
    private Instant endTime;

    private Pause() {
        this.startTime = Instant.now();
    }

    public static Pause start() {
        return new Pause();
    }

    public Pause end() {
        this.endTime = Instant.now();
        return this;
    }
}
