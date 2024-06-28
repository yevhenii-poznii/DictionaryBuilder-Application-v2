package com.kiskee.vocabulary.model.dto.redis;

import lombok.Getter;

import java.time.Instant;

@Getter
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
