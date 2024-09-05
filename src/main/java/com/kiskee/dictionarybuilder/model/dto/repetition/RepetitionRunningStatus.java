package com.kiskee.dictionarybuilder.model.dto.repetition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RepetitionRunningStatus {

    private boolean isRunning;
    private boolean paused;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RepetitionType repetitionType;

    public RepetitionRunningStatus(boolean isRunning, boolean paused) {
        this.isRunning = isRunning;
        this.paused = paused;
    }
}
