package com.kiskee.dictionarybuilder.model.dto.repetition;

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
}
