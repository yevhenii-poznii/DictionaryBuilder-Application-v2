package com.kiskee.vocabulary.model.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class UpdateReportResult {

    private boolean updated;

    @Nullable
    private String causedBy;
}
