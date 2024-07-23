package com.kiskee.vocabulary.model.entity.redis;

import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@AllArgsConstructor
@RedisHash("temporaryWordAdditionData")
public class TemporaryWordAdditionData implements Serializable {

    @Id
    private String id;

    private UUID userId;
    private Long dictionaryId;
    private int addedWords;
    private LocalDate date;
    private ZoneId userTimeZone;

    public TemporaryWordAdditionData incrementAddedWords() {
        this.addedWords++;
        return this;
    }
}
