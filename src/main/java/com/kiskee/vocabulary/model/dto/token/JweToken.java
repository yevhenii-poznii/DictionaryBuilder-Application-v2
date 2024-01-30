package com.kiskee.vocabulary.model.dto.token;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(setterPrefix = "set")
public class JweToken {

    private UUID id;
    private String subject;
    private List<String> authorities;
    private Instant createdAt;
    private Instant expiresAt;

}
