package com.kiskee.vocabulary.service.report;

import com.kiskee.vocabulary.model.dto.report.goal.ReportData;
import com.kiskee.vocabulary.model.entity.report.ReportRow;

public interface ReportRowService<RR extends ReportRow, RD extends ReportData> {

    RR buildRowFromScratch(RD wordAdditionData);

    RR updateRow(RR row, RD wordAdditionData);

    String getRowPeriod();
}
