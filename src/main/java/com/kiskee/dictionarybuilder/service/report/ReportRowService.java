package com.kiskee.dictionarybuilder.service.report;

import com.kiskee.dictionarybuilder.model.dto.report.update.ReportData;
import com.kiskee.dictionarybuilder.model.entity.report.ReportRow;

public interface ReportRowService<RR extends ReportRow, RD extends ReportData> {

    RR buildRowFromScratch(RD reportData);

    RR updateRow(RR row, RD reportData);

    String getRowPeriod();
}
