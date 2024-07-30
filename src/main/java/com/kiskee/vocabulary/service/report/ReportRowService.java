package com.kiskee.vocabulary.service.report;

import com.kiskee.vocabulary.model.dto.report.update.ReportData;
import com.kiskee.vocabulary.model.entity.report.ReportRow;

public interface ReportRowService<RR extends ReportRow, RD extends ReportData> {

    RR buildRowFromScratch(RD reportData);

    RR updateRow(RR row, RD reportData);

    String getRowPeriod();
}
