package com.kiskee.dictionarybuilder.model.entity.report;

import java.util.Set;

public interface Report<RR extends ReportRow> {

    Set<RR> getReportRows();

    Report<RR> buildFrom(Set<RR> updatedReportRows);
}
