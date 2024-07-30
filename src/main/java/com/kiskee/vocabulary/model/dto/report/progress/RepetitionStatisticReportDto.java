package com.kiskee.vocabulary.model.dto.report.progress;

import com.kiskee.vocabulary.model.dto.report.ReportDto;
import com.kiskee.vocabulary.model.dto.report.ReportRowDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepetitionStatisticReportDto implements ReportDto {

    private List<ReportRowDto> reportRows;
}
