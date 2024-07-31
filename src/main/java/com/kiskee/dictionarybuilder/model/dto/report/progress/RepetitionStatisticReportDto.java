package com.kiskee.dictionarybuilder.model.dto.report.progress;

import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportRowDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepetitionStatisticReportDto implements ReportDto {

    private List<ReportRowDto> reportRows;
}
