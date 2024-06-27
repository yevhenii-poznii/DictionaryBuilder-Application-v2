package com.kiskee.vocabulary.model.dto.repetition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RepetitionStatusResponse {

    private boolean isRunning;
}
