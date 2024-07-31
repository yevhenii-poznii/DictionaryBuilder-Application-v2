package com.kiskee.dictionarybuilder.model.dto.report.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class UpdateReportResult {

    private boolean updated;

    @Nullable
    private String causedBy;

    public UpdateReportResult(Boolean updated) {
        this.updated = updated;
    }
}
